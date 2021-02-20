/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.lambdaurora.aurorasdeco.block.entity;

import com.google.common.collect.Maps;
import dev.lambdaurora.aurorasdeco.block.ExtensionType;
import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.util.MapUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a Lantern Block Entity for the wall lanterns.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanternBlockEntity extends SwayingBlockEntity implements BlockEntityClientSerializable {
    public static final Block DEFAULT_LANTERN = Blocks.LANTERN;

    private static final Map<Block, Map<Direction, Map<ExtensionType, VoxelShape>>> SHAPES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Block> ITEM_BLOCK_MAP = new Object2ObjectOpenHashMap<>();

    private Block lantern = DEFAULT_LANTERN;
    private VoxelShape outlineShape;

    public int swingTicks;
    private boolean swinging;
    public Direction lastSideHit;

    private boolean colliding = false;

    private Box lanternCollisionBoxX;
    private Box lanternCollisionBoxZ;

    private final Map<Entity, Direction.Axis> collisions = new Object2ObjectOpenHashMap<>();

    public LanternBlockEntity(BlockPos pos, BlockState state) {
        super(AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE, pos, state);

        this.updateShape();
    }

    public static Block getLanternFromItem(Item item) {
        return ITEM_BLOCK_MAP.getOrDefault(item, DEFAULT_LANTERN);
    }

    public static Stream<Item> streamLanternItems() {
        return ITEM_BLOCK_MAP.keySet().stream();
    }

    public static int getLuminanceFromItem(Item item) {
        return getLanternFromItem(item).getDefaultState().getLuminance();
    }

    public static void registerLantern(Item item, Block block) {
        ITEM_BLOCK_MAP.put(item, block);

        if (!block.hasDynamicBounds()) {
            VoxelShape lanternShape = block.getDefaultState().getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
                    .offset(0, 2.0 / 16.0, 0);

            SHAPES.put(block, WallLanternBlock.FACING.getValues().stream()
                    .collect(Maps.toImmutableEnumMap(Function.<Direction>identity(),
                            direction -> MapUtil.mapWithEnumKey(WallLanternBlock.ATTACHMENT_SHAPES.get(direction),
                                    (key, input) -> {
                                        VoxelShape shape = lanternShape;
                                        if (key != ExtensionType.NONE) {
                                            shape = shape.offset(
                                                    (-direction.getOffsetX() * key.getOffset()) / 16.0,
                                                    0,
                                                    (-direction.getOffsetZ() * key.getOffset()) / 16.0
                                            );
                                        }
                                        return VoxelShapes.union(shape, input);
                                    }))));
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.updateCollisionBoxes();
    }

    /**
     * Returns the lantern of this wall lantern.
     *
     * @return the lantern
     */
    public Block getLantern() {
        return this.lantern;
    }

    /**
     * Returns the lantern of this wall lantern as block state.
     *
     * @return the lantern as block state
     */
    public BlockState getLanternState() {
        return this.getLantern().getDefaultState();
    }

    /**
     * Sets the lantern of this wall lantern.
     *
     * @param lantern the lantern
     */
    public void setLantern(Block lantern) {
        if (lantern instanceof LanternBlock && this.lantern != lantern) {
            this.lantern = lantern;
            this.updateLantern();
            this.markDirty();
        }
    }

    private void updateLantern() {
        this.updateCollisionBoxes();
        this.updateShape();
    }

    private void updateShape() {
        if (this.lantern.hasDynamicBounds()) {
            this.outlineShape = null;
        } else {
            this.outlineShape = SHAPES.get(this.lantern).get(this.getCachedState().get(WallLanternBlock.FACING))
                    .get(this.getCachedState().get(WallLanternBlock.EXTENSION));
        }
    }

    /**
     * Returns the outline shape of this wall lantern.
     *
     * @return the outline shape
     */
    public VoxelShape getOutlineShape() {
        if (this.outlineShape == null) {
            Direction facing = this.getCachedState().get(WallLanternBlock.FACING);
            ExtensionType extension = this.getCachedState().get(WallLanternBlock.EXTENSION);
            return VoxelShapes.union(
                    this.lantern.getDefaultState().getOutlineShape(this.getWorld(), this.getPos())
                            .offset((-facing.getOffsetX() * extension.getOffset()) / 16.0,
                                    2.0 / 16.0,
                                    (-facing.getOffsetZ() * extension.getOffset()) / 16.0),
                    WallLanternBlock.ATTACHMENT_SHAPES.get(facing)
                            .get(extension)
            );
        }
        return this.outlineShape;
    }

    public static VoxelShape getOutlineShape(BlockView world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LanternBlockEntity) {
            return ((LanternBlockEntity) blockEntity).getOutlineShape();
        }
        return SHAPES.get(DEFAULT_LANTERN)
                .get(state.get(WallLanternBlock.FACING))
                .get(state.get(WallLanternBlock.EXTENSION));
    }

    /**
     * Sets the lantern from an item.
     *
     * @param item the item
     */
    public void setLanternFromItem(Item item) {
        this.setLantern(getLanternFromItem(item));
    }

    /**
     * Returns the lantern collision box of the given axis.
     *
     * @param axis the axis which allows colliding
     * @return the collision box
     */
    public Box getLanternCollisionBox(Direction.Axis axis) {
        return axis == Direction.Axis.X ? this.lanternCollisionBoxX : this.lanternCollisionBoxZ;
    }

    /**
     * Returns whether this lantern is swaying or not.
     *
     * @return {@code true} if this lantern is swaying, else {@code false}
     */
    public boolean isSwinging() {
        return this.swinging;
    }

    /**
     * Returns whether this lantern is colliding with an entity or not.
     *
     * @return {@code true} if this lantern is colliding with an entity, else {@code false}
     */
    public boolean isColliding() {
        return this.colliding;
    }

    /**
     * Swings the lantern in a given direction.
     *
     * @param direction the direction to swing to
     */
    public void activate(Direction direction) {
        BlockPos blockPos = this.getPos();
        this.lastSideHit = direction;
        if (this.swinging) {
            if (this.isColliding())
                this.swingTicks = 4;
            else
                this.swingTicks = 0;
        } else {
            this.swinging = true;
        }

        this.getWorld().addSyncedBlockEvent(blockPos, this.getCachedState().getBlock(), 1, direction.getId());
    }

    /**
     * Swings the lantern in a given direction. Caused by an entity collision.
     *
     * @param direction the direction to swing to
     * @param entity the entity who made the lantern swing
     * @param lanternCollisionAxis the axis on which the lantern and the entity collides
     */
    public void activate(Direction direction, Entity entity, Direction.Axis lanternCollisionAxis) {
        this.collisions.put(entity, lanternCollisionAxis);
        this.colliding = true;
        this.getWorld().addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 2, 1);

        this.activate(direction);
    }

    private void updateCollisionBoxes() {
        if (this.getWorld() == null)
            return;

        BlockPos pos = this.getPos();

        BlockState lanternState = this.getLanternState();
        Box box = lanternState.getOutlineShape(this.getWorld(), pos, ShapeContext.absent())
                .offset(0, 2.0 / 16.0, 0).getBoundingBox();

        this.lanternCollisionBoxX = box.expand(0.1, 0, 0).offset(pos);
        this.lanternCollisionBoxZ = box.expand(0, 0, 0.1).offset(pos);
    }

    /* Serialization */

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
        this.fromClientTag(tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag compound) {
        return this.toClientTag(super.toTag(compound));
    }

    @Override
    public void fromClientTag(CompoundTag compound) {
        Block lantern = DEFAULT_LANTERN;
        if (compound.contains("lantern", NbtType.STRING)) {
            Block block = Registry.BLOCK.get(new Identifier(compound.getString("lantern")));
            if (block instanceof LanternBlock) {
                lantern = block;
            }
        }

        this.lantern = lantern;
        this.updateLantern();
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compound) {
        compound.putString("lantern", Registry.BLOCK.getId(this.lantern).toString());
        return compound;
    }

    /* Syncing */

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.lastSideHit = Direction.byId(data);
            if (!this.swinging || !this.isColliding()) {
                this.swingTicks = 0;
            }
            this.swinging = true;
            return true;
        } else if (type == 2) {
            this.colliding = data != 0;
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    /* Ticking */

    private static void tick(LanternBlockEntity lantern) {
        if (lantern.swinging) {
            ++lantern.swingTicks;
        }

        if (lantern.swingTicks >= 4 && lantern.isColliding()) {
            lantern.swingTicks = 4;
        }

        float maxTicks = lantern.getCachedState().getFluidState().isEmpty() ? 60 : 100;
        if (lantern.swingTicks >= maxTicks) {
            lantern.swinging = false;
            lantern.swingTicks = 0;
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, LanternBlockEntity lantern) {
        lantern.tickClient(world);
        tick(lantern);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, LanternBlockEntity lantern) {
        BlockState originalState = state;

        boolean canTick = true;

        if (!lantern.collisions.isEmpty()) {
            List<Entity> toRemove = new ArrayList<>();
            for (Map.Entry<Entity, Direction.Axis> entry : lantern.collisions.entrySet()) {
                if (entry.getKey().isRemoved()) {
                    toRemove.add(entry.getKey());
                } else {
                    if (lantern.getLanternCollisionBox(entry.getValue()).intersects(entry.getKey().getBoundingBox())) {
                        canTick = false;
                    } else {
                        toRemove.add(entry.getKey());
                    }
                }
            }

            toRemove.forEach(lantern.collisions::remove);
        }
        if (lantern.collisions.isEmpty() && lantern.isColliding()) {
            lantern.colliding = false;
            world.addSyncedBlockEvent(pos, state.getBlock(), 2, 0);
        }

        if (state.get(WallLanternBlock.LIGHT) != lantern.getLanternState().getLuminance())
            state = state.with(WallLanternBlock.LIGHT, lantern.getLanternState().getLuminance());

        if (originalState != state)
            world.setBlockState(pos, state);

        if (canTick)
            tick(lantern);
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.updateShape();
    }
}
