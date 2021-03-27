/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.lambdaurora.aurorasdeco.accessor.BlockItemAccessor;
import dev.lambdaurora.aurorasdeco.block.entity.LanternBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;

/**
 * Represents a wall lantern.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class WallLanternBlock extends BlockWithEntity implements Waterloggable {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<ExtensionType> EXTENSION = AurorasDecoProperties.EXTENSION;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final VoxelShape LANTERN_HANG_SHAPE = Block.createCuboidShape(7.0, 11.0, 7.0,
            9.0, 13.0, 9.0);
    public static final Map<Direction, Map<ExtensionType, VoxelShape>> ATTACHMENT_SHAPES;

    private static final VoxelShape HOLDER_SHAPE = createCuboidShape(0.0, 8.0, 0.0,
            16.0, 16.0, 16.0);

    private final LanternBlock lanternBlock;

    public WallLanternBlock(LanternBlock lantern) {
        super(FabricBlockSettings.copyOf(lantern).dropsLike(lantern));

        this.lanternBlock = lantern;

        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(EXTENSION, ExtensionType.NONE)
                .with(WATERLOGGED, false)
        );

        Item item = Item.fromBlock(lantern); // Avoid caching which could break stuff at this stage.
        if (item instanceof BlockItem) {
            ((BlockItemAccessor) item).aurorasdeco$setWallBlock(this);
        }
    }

    public LanternBlock getLanternBlock() {
        return this.lanternBlock;
    }

    public BlockState getLanternState() {
        return this.getLanternBlock().getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(EXTENSION);
        builder.add(WATERLOGGED);
    }

    @Override
    public String getTranslationKey() {
        return this.lanternBlock.getTranslationKey();
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(WallLanternBlock.FACING);
        ExtensionType extension = state.get(WallLanternBlock.EXTENSION);
        return VoxelShapes.union(
                this.getLanternState().getOutlineShape(world, pos)
                        .offset((-facing.getOffsetX() * extension.getOffset()) / 16.0,
                                2.0 / 16.0,
                                (-facing.getOffsetZ() * extension.getOffset()) / 16.0),
                WallLanternBlock.ATTACHMENT_SHAPES.get(facing)
                        .get(extension)
        );
    }

    /* Placement */

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos attachPos = pos.offset(direction.getOpposite());
        BlockState attachState = world.getBlockState(attachPos);
        return !VoxelShapes.matchesAnywhere(attachState.getSidesShape(world, attachPos).getFace(direction),
                HOLDER_SHAPE, BooleanBiFunction.ONLY_SECOND)
                || ExtensionType.getExtensionValue(attachState, attachPos, world) != ExtensionType.NONE;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = this.getDefaultState();
        WorldView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        FluidState fluidState = world.getFluidState(pos);
        Direction[] directions = ctx.getPlacementDirections();

        state = state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction2 = direction.getOpposite();
                state = state.with(FACING, direction2);
                if (state.canPlaceAt(world, pos)) {
                    BlockPos attachPos = pos.offset(direction);
                    return state.with(EXTENSION,
                            ExtensionType.getExtensionValue(world.getBlockState(attachPos), attachPos, world));
                }
            }
        }

        return null;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
                                                BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState() : state;
    }

    /* Interaction */

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return this.getLanternBlock().getPickStack(world, pos, this.getLanternState());
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        Entity entity = projectile.getOwner();
        PlayerEntity playerEntity = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
        this.swing(world, state, hit, playerEntity, true);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        return this.swing(world, state, hit, player, true)
                ? ActionResult.success(world.isClient()) : ActionResult.PASS;
    }

    public boolean swing(World world, BlockState state, BlockHitResult hitResult, @Nullable PlayerEntity player,
                         boolean hitResultIndependent) {
        Direction direction = hitResult.getSide();
        BlockPos blockPos = hitResult.getBlockPos();
        boolean canSwing = !hitResultIndependent
                || this.isPointOnLantern(state, direction, hitResult.getPos().y - (double) blockPos.getY());
        if (canSwing) {
            this.swing(player, world, blockPos, direction, null);

            return true;
        } else {
            return false;
        }
    }

    private boolean isPointOnLantern(BlockState state, Direction side, double y) {
        if (side.getAxis() != Direction.Axis.Y && y <= 0.8123999834060669D) {
            Direction direction = state.get(FACING);
            return direction.getAxis() != side.getAxis();
        } else {
            return false;
        }
    }

    public void swing(@Nullable Entity entity, World world, BlockPos pos, @Nullable Direction direction,
                      @Nullable Direction.Axis lanternCollisionAxis) {
        LanternBlockEntity blockEntity = AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
        if (!world.isClient() && blockEntity != null) {
            if (direction == null) {
                direction = world.getBlockState(pos).get(FACING);
            }

            boolean previousColliding = blockEntity.isColliding();
            if (lanternCollisionAxis == null)
                blockEntity.activate(direction);
            else
                blockEntity.activate(direction, entity, lanternCollisionAxis);
            if (!previousColliding) {
                world.playSound(null, pos, AurorasDecoRegistry.LANTERN_SWING_SOUND_EVENT, SoundCategory.BLOCKS,
                        2.f, 1.f);
                world.emitGameEvent(entity, GameEvent.RING_BELL, pos);
            }
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient())
            return;
        if (entity instanceof ProjectileEntity)
            return;

        LanternBlockEntity blockEntity = AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
        if (blockEntity == null)
            return;

        Direction.Axis swingAxis = state.get(FACING).rotateYClockwise().getAxis();

        Box lanternBox = blockEntity.getLanternCollisionBox(swingAxis);
        Box entityBox = entity.getBoundingBox();
        if (lanternBox.intersects(entityBox)) {
            Direction swingDirection = Direction.NORTH;
            if (swingAxis == Direction.Axis.X) {
                if ((pos.getX() + .5f) > entity.getX()) swingDirection = Direction.WEST;
                else swingDirection = Direction.EAST;
            } else if (swingAxis == Direction.Axis.Z) {
                if ((pos.getZ() + .5f) < entity.getZ()) swingDirection = Direction.SOUTH;
            }
            this.swing(entity, world, pos, swingDirection, swingAxis);
        }
    }

    /* Block Entity Stuff */

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.instantiate(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                            BlockEntityType<T> type) {
        return checkType(type, AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE,
                world.isClient() ? LanternBlockEntity::clientTick : LanternBlockEntity::serverTick);
    }

    /* Piston */

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    /* Fluid */

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    /* Entity Stuff */

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    /* Redstone */

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        LanternBlockEntity lantern = AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
        if (lantern != null) {
            if (lantern.isColliding()) {
                return 15;
            } else if (lantern.isSwinging()) {
                int max = lantern.getMaxSwingTicks();
                float progress = (max - lantern.swingTicks) / (float) max;
                return (int) (progress * 14);
            }
        }
        return 0;
    }

    /* Visual */

    @Environment(EnvType.CLIENT)
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        this.getLanternBlock().randomDisplayTick(state, world, pos, random);
    }

    static {
        double attachmentMaxY = 16.0;
        double attachmentMinY = 10.0;

        Map<ExtensionType, VoxelShape> northShape;
        {
            ImmutableMap.Builder<ExtensionType, VoxelShape> builder = ImmutableMap.builder();
            VoxelShape wallAttachment = createCuboidShape(6.0, attachmentMinY - 3.0, 15.0,
                    10.0, attachmentMaxY, 16.0);
            builder.put(ExtensionType.NONE, VoxelShapes.union(LANTERN_HANG_SHAPE,
                    createCuboidShape(7.0, 13.0, 7.0, 9.0, 15.0, 15.0),
                    createCuboidShape(6.0, attachmentMinY, 15.0, 10.0, attachmentMaxY, 16.0)));
            builder.put(ExtensionType.WALL, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(0, 0, 0.125),
                    createCuboidShape(7.0, 13.0, 7.0, 9.0, 15.0, 19.0),
                    wallAttachment.offset(0, 0, 0.25)));
            builder.put(ExtensionType.FENCE, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(0, 0, 0.25),
                    createCuboidShape(7.0, 13.0, 9.0, 9.0, 15.0, 21.0),
                    wallAttachment.offset(0, 0, 0.375)));

            northShape = builder.build();
        }

        Map<ExtensionType, VoxelShape> southShape;
        {
            ImmutableMap.Builder<ExtensionType, VoxelShape> builder = ImmutableMap.builder();
            VoxelShape wallAttachment = createCuboidShape(6.0, attachmentMinY - 3.0, 0.0,
                    10.0, attachmentMaxY, 1.0);
            builder.put(ExtensionType.NONE, VoxelShapes.union(LANTERN_HANG_SHAPE,
                    createCuboidShape(7.0, 13.0, 1.0, 9.0, 15.0, 9.0),
                    createCuboidShape(6.0, attachmentMinY, 0.0, 10.0, attachmentMaxY, 1.0)));
            builder.put(ExtensionType.WALL, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(0, 0, -.125),
                    createCuboidShape(7.0, 13.0, -3.0, 9.0, 15.0, 9.0),
                    wallAttachment.offset(0, 0, -.25)));
            builder.put(ExtensionType.FENCE, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(0, 0, -.25),
                    createCuboidShape(7.0, 13.0, -5.0, 9.0, 15.0, 7.0),
                    wallAttachment.offset(0, 0, -.375)));

            southShape = builder.build();
        }

        Map<ExtensionType, VoxelShape> westShape;
        {
            ImmutableMap.Builder<ExtensionType, VoxelShape> builder = ImmutableMap.builder();
            VoxelShape wallAttachment = createCuboidShape(15.0, attachmentMinY - 3.0, 6.0,
                    16.0, attachmentMaxY, 10.0);
            builder.put(ExtensionType.NONE, VoxelShapes.union(LANTERN_HANG_SHAPE,
                    createCuboidShape(7.0, 13.0, 7.0, 15.0, 15.0, 9.0),
                    createCuboidShape(15.0, attachmentMinY, 6.0, 16.0, attachmentMaxY, 10.0)));
            builder.put(ExtensionType.WALL, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(0.125, 0, 0),
                    createCuboidShape(7.0, 13.0, 7.0, 19.0, 15.0, 9.0),
                    wallAttachment.offset(0.25, 0, 0)));
            builder.put(ExtensionType.FENCE, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(0.25, 0, 0),
                    createCuboidShape(9.0, 13.0, 7.0, 21.0, 15.0, 9.0),
                    wallAttachment.offset(0.375, 0, 0)));

            westShape = builder.build();
        }

        Map<ExtensionType, VoxelShape> eastShape;
        {
            ImmutableMap.Builder<ExtensionType, VoxelShape> builder = ImmutableMap.builder();
            VoxelShape wallAttachment = createCuboidShape(0.0, attachmentMinY - 3.0, 6.0,
                    1.0, attachmentMaxY, 10.0);
            builder.put(ExtensionType.NONE, VoxelShapes.union(LANTERN_HANG_SHAPE,
                    createCuboidShape(1.0, 13.0, 7.0, 9.0, 15.0, 9.0),
                    createCuboidShape(0.0, attachmentMinY, 6.0, 1.0, attachmentMaxY, 10.0)));
            builder.put(ExtensionType.WALL, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(-.125, 0, 0),
                    createCuboidShape(-3.0, 13.0, 7.0, 9.0, 15.0, 9.0),
                    wallAttachment.offset(-.25, 0, 0)));
            builder.put(ExtensionType.FENCE, VoxelShapes.union(LANTERN_HANG_SHAPE.offset(-.25, 0, 0),
                    createCuboidShape(-5.0, 13.0, 7.0, 7.0, 15.0, 9.0),
                    wallAttachment.offset(-.375, 0, 0)));

            eastShape = builder.build();
        }

        ATTACHMENT_SHAPES = Maps.newEnumMap(
                ImmutableMap.of(
                        Direction.NORTH, northShape,
                        Direction.SOUTH, southShape,
                        Direction.WEST, westShape,
                        Direction.EAST, eastShape
                )
        );
    }
}
