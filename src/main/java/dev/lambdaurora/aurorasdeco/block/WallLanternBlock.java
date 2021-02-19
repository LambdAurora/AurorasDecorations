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

package dev.lambdaurora.aurorasdeco.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.lambdaurora.aurorasdeco.accessor.BlockItemAccessor;
import dev.lambdaurora.aurorasdeco.block.entity.LanternBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
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

import java.util.List;
import java.util.Map;

/**
 * Represents a wall lantern.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class WallLanternBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final IntProperty LIGHT = IntProperty.of("light", 0, 15);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final VoxelShape LANTERN_HANG_SHAPE = Block.createCuboidShape(7.0, 11.0, 7.0, 9.0, 13.0, 9.0);
    public static final Map<Direction, VoxelShape> ATTACHMENT_SHAPES;

    private static final VoxelShape HOLDER_SHAPE = createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public WallLanternBlock() {
        super(FabricBlockSettings.copyOf(Blocks.LANTERN)
                .breakByTool(FabricToolTags.PICKAXES)
                .luminance(state -> state.get(LIGHT))
                .dynamicBounds());

        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIGHT, 0)
                .with(WATERLOGGED, false)
        );

        LanternBlockEntity.streamLanternItems().filter(item -> item instanceof BlockItemAccessor)
                .forEach(item -> ((BlockItemAccessor) item).aurorasdeco$setWallBlock(this));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(LIGHT);
        builder.add(WATERLOGGED);
    }

    @Override
    public String getTranslationKey() {
        return this.asItem().getTranslationKey();
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return LanternBlockEntity.getOutlineShape(world, pos, state);
    }

    /* Placement */

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return !VoxelShapes.matchesAnywhere(blockState.getSidesShape(world, blockPos).getFace(direction),
                HOLDER_SHAPE, BooleanBiFunction.ONLY_SECOND);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = this.getDefaultState();
        WorldView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        FluidState fluidState = world.getFluidState(pos);
        Direction[] directions = ctx.getPlacementDirections();

        state = state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
                .with(LIGHT, LanternBlockEntity.getLuminanceFromItem(ctx.getStack().getItem()));

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction2 = direction.getOpposite();
                state = state.with(FACING, direction2);
                if (state.canPlaceAt(world, pos)) {
                    return state;
                }
            }
        }

        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        LanternBlockEntity blockEntity = AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
        if (blockEntity != null) {
            blockEntity.setLanternFromItem(itemStack.getItem());
        }
    }

    /* Interaction */

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        LanternBlockEntity blockEntity = AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
        if (blockEntity != null)
            return blockEntity.getLantern().getPickStack(world, pos, state);
        else
            return LanternBlockEntity.DEFAULT_LANTERN.getPickStack(world, pos, state);
    }

    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        Entity entity = projectile.getOwner();
        PlayerEntity playerEntity = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
        this.swing(world, state, hit, playerEntity, true);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return this.swing(world, state, hit, player, true) ? ActionResult.success(world.isClient()) : ActionResult.PASS;
    }

    public boolean swing(World world, BlockState state, BlockHitResult hitResult, @Nullable PlayerEntity player, boolean hitResultIndependent) {
        Direction direction = hitResult.getSide();
        BlockPos blockPos = hitResult.getBlockPos();
        boolean canSwing = !hitResultIndependent || this.isPointOnLantern(state, direction, hitResult.getPos().y - (double) blockPos.getY());
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
        LanternBlockEntity blockEntity = AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
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
                world.playSound(null, pos, AurorasDecoRegistry.LANTERN_SWING_SOUND_EVENT, SoundCategory.BLOCKS, 2.0F, 1.0F);
                world.emitGameEvent(entity, GameEvent.RING_BELL, pos);
            }
        }
    }

    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient())
            return;
        if (entity instanceof ProjectileEntity)
            return;

        LanternBlockEntity blockEntity = AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
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

    /* Loot table */

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        BlockState lanternState = LanternBlockEntity.DEFAULT_LANTERN.getDefaultState();
        if (blockEntity instanceof LanternBlockEntity) {
            lanternState = ((LanternBlockEntity) blockEntity).getLanternState();
        }
        return lanternState.getDroppedStacks(builder);
    }

    /* Block Entity Stuff */

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE.instantiate(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE,
                world.isClient() ? LanternBlockEntity::clientTick : LanternBlockEntity::serverTick);
    }

    /* Piston */

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    /* Fluid */

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
                                                BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        LanternBlockEntity blockEntity = AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE.get(world, pos);
        if (blockEntity != null) {
            state = state.with(LIGHT, blockEntity.getLanternState().getLuminance());
        }

        return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    static {
        VoxelShape northShape;
        {
            VoxelShape wallAttachment = Block.createCuboidShape(6.0, 10.0, 15.0, 10.0, 16.0, 16.0);
            VoxelShape barShape = Block.createCuboidShape(7.0, 13.0, 7.0, 9.0, 15.0, 15.0);
            northShape = VoxelShapes.union(LANTERN_HANG_SHAPE, barShape, wallAttachment);
        }

        VoxelShape southShape;
        {
            VoxelShape wallAttachment = Block.createCuboidShape(6.0, 10.0, 0.0, 10.0, 16.0, 1.0);
            VoxelShape barShape = Block.createCuboidShape(7.0, 13.0, 1.0, 9.0, 15.0, 9.0);
            southShape = VoxelShapes.union(LANTERN_HANG_SHAPE, barShape, wallAttachment);
        }

        VoxelShape westShape;
        {
            VoxelShape wallAttachment = Block.createCuboidShape(15.0, 10.0, 6.0, 16.0, 16.0, 10.0);
            VoxelShape barShape = Block.createCuboidShape(7.0, 13.0, 7.0, 15.0, 15.0, 9.0);
            westShape = VoxelShapes.union(LANTERN_HANG_SHAPE, barShape, wallAttachment);
        }

        VoxelShape eastShape;
        {
            VoxelShape wallAttachment = Block.createCuboidShape(0.0, 10.0, 6.0, 1.0, 16.0, 10.0);
            VoxelShape barShape = Block.createCuboidShape(1.0, 13.0, 7.0, 9.0, 15.0, 9.0);
            eastShape = VoxelShapes.union(LANTERN_HANG_SHAPE, barShape, wallAttachment);
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
