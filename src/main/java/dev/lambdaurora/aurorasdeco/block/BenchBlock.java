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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a bench.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BenchBlock extends Block implements SeatBlock, Waterloggable {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    public static final BooleanProperty EAST_LEGS = BooleanProperty.of("east_legs");
    public static final BooleanProperty WEST_LEGS = BooleanProperty.of("west_legs");
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public static final Identifier BENCH_SEAT_MODEL = AurorasDeco.id("block/template/bench_seat");
    public static final Identifier BENCH_LEGS_MODEL = AurorasDeco.id("block/template/bench_legs");
    public static final Identifier BENCH_BETTERGRASS_DATA = AurorasDeco.id("bettergrass/data/bench");

    private static final List<BenchBlock> BENCHES = new ArrayList<>();

    protected static final VoxelShape X_SHAPE = createCuboidShape(0, 0, 2, 16, 8, 14);
    protected static final VoxelShape Z_SHAPE = createCuboidShape(2, 0, 0, 14, 8, 16);

    private final WoodType woodType;

    public BenchBlock(WoodType woodType) {
        super(settings(woodType));
        this.woodType = woodType;

        this.setDefaultState(this.getDefaultState()
                .with(AXIS, Direction.Axis.X)
                .with(EAST_LEGS, true)
                .with(WEST_LEGS, true)
                .with(WATERLOGGED, false)
        );

        BENCHES.add(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS, EAST_LEGS, WEST_LEGS, WATERLOGGED);
    }

    public static Stream<BenchBlock> streamBenches() {
        return BENCHES.stream();
    }

    public WoodType getWoodType() {
        return this.woodType;
    }

    @Override
    public float getSitYOffset() {
        return 0.3f;
    }

    /**
     * Returns whether this bench can connect to the given block state.
     *
     * @param other the other block to try to connect to
     * @param benchAxis this bench axis
     * @return {@code true} if this bench can connect to the given block, else {@code false}
     */
    public boolean canConnect(BlockState other, Direction.Axis benchAxis) {
        return other.getBlock() == this && benchAxis == other.get(AXIS);
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(AXIS) == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
    }

    /* Placement */

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var fluid = world.getFluidState(pos);

        var benchAxis = ctx.getPlayerFacing().rotateYClockwise().getAxis();

        var relativeEast = benchAxis == Direction.Axis.Z ? pos.south() : pos.east();
        var relativeWest = benchAxis == Direction.Axis.Z ? pos.north() : pos.west();

        return this.getDefaultState()
                .with(AXIS, benchAxis)
                .with(EAST_LEGS, !this.canConnect(world.getBlockState(relativeEast), benchAxis))
                .with(WEST_LEGS, !this.canConnect(world.getBlockState(relativeWest), benchAxis))
                .with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
                                                WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        var newSelf = super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
        var benchAxis = state.get(AXIS);

        if (direction.getAxis() == benchAxis) {
            newSelf = newSelf.with(switch (direction) {
                case EAST, SOUTH -> EAST_LEGS;
                default -> WEST_LEGS;
            }, !this.canConnect(newState, benchAxis));
        }

        return newSelf;
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (this.sit(world, pos, state, player, stack))
            return ActionResult.success(world.isClient());
        return super.onUse(state, world, pos, player, hand, hit);
    }

    /* Fluid */

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    private static FabricBlockSettings settings(WoodType woodType) {
        return FabricBlockSettings.of(woodType.material, woodType.getMapColor())
                .nonOpaque()
                .strength(2.f, 3.f)
                .sounds(woodType.logSoundGroup);
    }
}
