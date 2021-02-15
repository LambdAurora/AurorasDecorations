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

import com.google.common.collect.ImmutableMap;
import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Lantern Block Entity for the wall lanterns.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanternBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    public static final Block DEFAULT_LANTERN = Blocks.LANTERN;

    private static final Map<Block, Map<Direction, VoxelShape>> SHAPES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Block> ITEM_BLOCK_MAP = new Object2ObjectOpenHashMap<>();

    private Block lantern = DEFAULT_LANTERN;
    private VoxelShape outlineShape;

    public int swingTicks;
    public boolean swinging;
    public Direction lastSideHit;

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

    public static void registerLantern(Item item, Block block) {
        ITEM_BLOCK_MAP.put(item, block);

        if (!block.hasDynamicBounds()) {
            VoxelShape lanternShape = block.getDefaultState().getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
                    .offset(0, 2.0 / 16.0, 0);
            SHAPES.put(block, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, VoxelShapes.union(lanternShape, WallLanternBlock.ATTACHMENT_SHAPES.get(Direction.NORTH)),
                    Direction.SOUTH, VoxelShapes.union(lanternShape, WallLanternBlock.ATTACHMENT_SHAPES.get(Direction.SOUTH)),
                    Direction.WEST, VoxelShapes.union(lanternShape, WallLanternBlock.ATTACHMENT_SHAPES.get(Direction.WEST)),
                    Direction.EAST, VoxelShapes.union(lanternShape, WallLanternBlock.ATTACHMENT_SHAPES.get(Direction.EAST))
            )));
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.updateLantern();
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

        int luminance = this.getLanternState().getLuminance();
        if (this.getCachedState().get(WallLanternBlock.LIGHT) != luminance) {
            this.getWorld().setBlockState(pos, this.getCachedState().with(WallLanternBlock.LIGHT, luminance));
        }
    }

    private void updateShape() {
        if (this.lantern.hasDynamicBounds()) {
            this.outlineShape = null;
        } else {
            this.outlineShape = SHAPES.get(this.lantern).get(this.getCachedState().get(WallLanternBlock.FACING));
        }
    }

    /**
     * Returns the outline shape of this wall lantern.
     *
     * @return the outline shape
     */
    public VoxelShape getOutlineShape() {
        if (this.outlineShape == null) {
            return VoxelShapes.union(
                    this.lantern.getDefaultState().getOutlineShape(this.getWorld(), this.getPos()).offset(0, 2.0 / 16.0, 0),
                    WallLanternBlock.ATTACHMENT_SHAPES.get(this.getCachedState().get(WallLanternBlock.FACING))
            );
        }
        return this.outlineShape;
    }

    public static VoxelShape getOutlineShape(BlockView world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof LanternBlockEntity) {
            return ((LanternBlockEntity) blockEntity).getOutlineShape();
        }
        return SHAPES.get(DEFAULT_LANTERN).get(state.get(WallLanternBlock.FACING));
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
     * Returns whether this lantern is colliding with an entity or not.
     *
     * @return {@code true} if this lantern is colliding with an entity, else {@code false}
     */
    public boolean isColliding() {
        return this.getCachedState().get(WallLanternBlock.COLLISION);
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

        if (!this.getCachedState().get(WallLanternBlock.COLLISION)) {
            this.getWorld().setBlockState(this.getPos(), this.getCachedState().with(WallLanternBlock.COLLISION, true));
        }
        this.activate(direction);
    }

    private void updateCollisionBoxes() {
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
    public CompoundTag toTag(CompoundTag tag) {
        return this.toClientTag(super.toTag(tag));
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        Block lantern = DEFAULT_LANTERN;
        if (tag.contains("lantern", NbtType.STRING)) {
            Block block = Registry.BLOCK.get(new Identifier(tag.getString("lantern")));
            if (block instanceof LanternBlock) {
                lantern = block;
            }
        }

        this.lantern = lantern;
        this.updateLantern();
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putString("lantern", Registry.BLOCK.getId(this.lantern).toString());
        return tag;
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
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    /* Ticking */

    private static void tick(LanternBlockEntity blockEntity) {
        if (blockEntity.swinging) {
            ++blockEntity.swingTicks;
        }

        if (blockEntity.swingTicks >= 4 && blockEntity.isColliding()) {
            blockEntity.swingTicks = 4;
        }

        float maxTicks = blockEntity.getCachedState().getFluidState().isEmpty() ? 50 : 100;
        if (blockEntity.swingTicks >= maxTicks) {
            blockEntity.swinging = false;
            blockEntity.swingTicks = 0;
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, LanternBlockEntity blockEntity) {
        tick(blockEntity);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, LanternBlockEntity blockEntity) {
        if (!blockEntity.collisions.isEmpty()) {
            List<Entity> toRemove = new ArrayList<>();
            boolean canTick = true;
            for (Map.Entry<Entity, Direction.Axis> entry : blockEntity.collisions.entrySet()) {
                if (entry.getKey().isRemoved()) {
                    toRemove.add(entry.getKey());
                } else {
                    if (blockEntity.getLanternCollisionBox(entry.getValue()).intersects(entry.getKey().getBoundingBox())) {
                        canTick = false;
                    } else {
                        toRemove.add(entry.getKey());
                    }
                }
            }

            toRemove.forEach(blockEntity.collisions::remove);

            if (!canTick)
                return;
        } else if (state.get(WallLanternBlock.COLLISION)) {
            world.setBlockState(pos, state.with(WallLanternBlock.COLLISION, false));
        }

        tick(blockEntity);
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.updateShape();
    }
}
