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
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents the shelf block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShelfBlock extends BlockWithEntity implements Waterloggable {
    public static final EnumProperty<PartType> TYPE = AurorasDecoProperties.PART_TYPE;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final Map<Direction, Map<PartType, VoxelShape>> SHAPES;
    private static final Map<PartType, VoxelShape> VALID_ATTACHMENTS;

    private static final List<ShelfBlock> SHELVES = new ArrayList<>();

    private final WoodType woodType;

    public ShelfBlock(WoodType woodType) {
        super(settings(woodType));

        this.woodType = woodType;

        this.setDefaultState(this.getDefaultState()
                .with(TYPE, PartType.BOTTOM)
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false));

        SHELVES.add(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE, FACING, WATERLOGGED);
    }

    public static Stream<ShelfBlock> streamShelves() {
        return SHELVES.stream();
    }

    public WoodType getWoodType() {
        return this.woodType;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING)).get(state.get(TYPE));
    }

    /* Placement */

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        var direction = state.get(FACING);
        var attachPos = pos.offset(direction.getOpposite());
        var attachState = world.getBlockState(attachPos);
        return !VoxelShapes.matchesAnywhere(attachState.getSidesShape(world, attachPos).getFace(direction),
                VALID_ATTACHMENTS.get(state.get(TYPE)), BooleanBiFunction.ONLY_SECOND);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var placedState = world.getBlockState(pos);

        if (placedState.isOf(this)) {
            placedState = placedState.with(TYPE, PartType.DOUBLE);
            if (this.canPlaceAt(placedState, world, pos))
                return placedState;
        } else {
            var fluid = world.getFluidState(pos);
            var state = this.getDefaultState().with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);

            var side = ctx.getSide();
            if (side == Direction.UP) {
                state = state.with(TYPE, PartType.TOP);
            } else if (side == Direction.DOWN) {
                state = state.with(TYPE, PartType.BOTTOM);
            } else {
                if (AuroraUtil.posMod(ctx.getHitPos().getY(), 1) > 0.5)
                    state = state.with(TYPE, PartType.TOP);
                else
                    state = state.with(TYPE, PartType.BOTTOM);
            }

            var directions = ctx.getPlacementDirections();

            for (var direction : directions) {
                if (direction.getAxis().isHorizontal()) {
                    var opposite = direction.getOpposite();
                    state = state.with(FACING, opposite);
                    if (state.canPlaceAt(world, pos)) {
                        return state;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        var stack = context.getStack();
        var type = state.get(TYPE);
        if (type != PartType.DOUBLE && stack.isOf(this.asItem())) {
            return !context.shouldCancelInteraction()
                    && this.canPlaceAt(state.with(TYPE, PartType.DOUBLE), context.getWorld(), context.getBlockPos());
        }
        return false;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            var shelf = AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE.get(world, pos);
            if (shelf != null) {
                ItemScatterer.spawn(world, pos, shelf);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        var shelf = AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE.get(world, pos);
        if (shelf != null) {
            if (stack.hasCustomName()) {
                shelf.setCustomName(stack.getName());
            }
        }
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
                                                WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState() : state;
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        if (!world.isClient()) {
            var shelf = AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE.get(world, pos);

            if (shelf != null) {
                var handStack = player.getStackInHand(hand);
                if (!handStack.isEmpty()) {
                    var facing = state.get(FACING);

                    int y = 0;
                    if (AuroraUtil.posMod(hit.getPos().getY(), 1) <= 0.5)
                        y = 1;

                    int x;
                    if (facing.getAxis() == Direction.Axis.Z) {
                        x = (int) (AuroraUtil.posMod(hit.getPos().getX(), 1) * 4.0);
                    } else {
                        x = 3 - (int) (AuroraUtil.posMod(hit.getPos().getZ(), 1) * 4.0);
                    }
                    if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) {
                        x = 3 - x;
                    }

                    int slot = y * 4 + x;
                    var stack = shelf.getStack(slot);
                    if (stack.isEmpty()
                            || (ItemStack.canCombine(stack, handStack) && stack.getCount() < stack.getMaxCount())) {
                        if (stack.isEmpty()) {
                            stack = handStack.copy();
                            stack.setCount(1);
                            if (!player.getAbilities().creativeMode)
                                handStack.decrement(1);
                        } else {
                            int difference = Math.min(stack.getMaxCount() - stack.getCount(), handStack.getCount());

                            stack.increment(difference);
                            handStack.decrement(difference);
                        }
                        shelf.setStack(slot, stack);
                        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        return ActionResult.SUCCESS;
                    }
                }

                player.openHandledScreen(shelf);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        var shelf = AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE.get(world, pos);

        if (shelf != null && !shelf.isEmpty()) {
            Direction facing = state.get(FACING);

            var cameraPosVec = player.getCameraPosVec(1.0F);
            var rotationVec = player.getRotationVec(1.0F);
            var extendedVec = cameraPosVec.add(rotationVec.x * 4.5F, rotationVec.y * 4.5F, rotationVec.z * 4.5F);
            var rayCtx = new RaycastContext(cameraPosVec,
                    extendedVec, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player);
            var hit = world.raycast(rayCtx);
            int y = 0;
            if (AuroraUtil.posMod(hit.getPos().getY(), 1) <= 0.5)
                y = 1;

            int x;
            if (facing.getAxis() == Direction.Axis.Z) {
                x = (int) (AuroraUtil.posMod(hit.getPos().getX(), 1) * 4.0);
            } else {
                x = 3 - (int) (AuroraUtil.posMod(hit.getPos().getZ(), 1) * 4.0);
            }
            if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) {
                x = 3 - x;
            }

            int slot = y * 4 + x;
            var stack = shelf.getStack(slot);

            if (!stack.isEmpty()) {
                if (player.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
                    player.setStackInHand(Hand.MAIN_HAND, stack.copy());
                    shelf.removeStack(slot);
                } else {
                    var item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            stack.copy());
                    float speed = world.random.nextFloat() * .5f;
                    float angle = world.random.nextFloat() * 6.2831855f;
                    item.setVelocity(-MathHelper.sin(angle) * speed,
                            0.20000000298023224,
                            MathHelper.cos(angle) * speed);
                    world.spawnEntity(item);

                    shelf.removeStack(slot);
                }
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
        }
    }

    /* Block Entity Stuff */

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE.instantiate(pos, state);
    }

    /* Piston */

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK;
    }

    /* Fluid */

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    /* Redstone */

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    private static FabricBlockSettings settings(WoodType woodType) {
        var planks = woodType.getComponent(WoodType.ComponentType.PLANKS);
        if (planks == null) throw new IllegalStateException("ShelfBlock attempted to be created while the wood type is invalid.");
        return FabricBlockSettings.copyOf(planks.block())
                .collidable(true)
                .nonOpaque();
    }

    private static Map<PartType, VoxelShape> createTypeShapes(int xMin, int zMin, int xMax, int zMax) {
        var builder = ImmutableMap.<PartType, VoxelShape>builder();
        builder.put(PartType.BOTTOM, createCuboidShape(xMin, 0, zMin, xMax, 8, zMax));
        builder.put(PartType.TOP, createCuboidShape(xMin, 8, zMin, xMax, 16, zMax));
        builder.put(PartType.DOUBLE, createCuboidShape(xMin, 0, zMin, xMax, 16, zMax));
        return new EnumMap<>(builder.build());
    }

    static {
        var facingBuilder = ImmutableMap.<Direction, Map<PartType, VoxelShape>>builder();

        facingBuilder.put(Direction.NORTH, createTypeShapes(0, 12, 16, 16));
        facingBuilder.put(Direction.EAST, createTypeShapes(0, 0, 4, 16));
        facingBuilder.put(Direction.SOUTH, createTypeShapes(0, 0, 16, 4));
        facingBuilder.put(Direction.WEST, createTypeShapes(12, 0, 16, 16));

        SHAPES = new EnumMap<>(facingBuilder.build());

        var builder = ImmutableMap.<PartType, VoxelShape>builder();
        builder.put(PartType.BOTTOM, createCuboidShape(0, 0, 0, 16, 8, 16));
        builder.put(PartType.TOP, createCuboidShape(0, 8, 0, 16, 16, 16));
        builder.put(PartType.DOUBLE, VoxelShapes.fullCube());

        VALID_ATTACHMENTS = new EnumMap<>(builder.build());
    }
}
