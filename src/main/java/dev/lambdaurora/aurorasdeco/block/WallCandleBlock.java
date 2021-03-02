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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents wall candles.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class WallCandleBlock extends ExtendedCandleBlock {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    private static final Int2ObjectMap<Map<Direction, VoxelShape>> SHAPES;
    private static final Int2ObjectMap<Map<Direction, List<Vec3d>>> CANDLES_TO_PARTICLE_OFFSETS;

    private static final VoxelShape HOLDER_SHAPE = createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    public WallCandleBlock(CandleBlock candleBlock) {
        super(candleBlock);

        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(FACING));
    }

    /* Shape */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(CANDLES).intValue()).get(state.get(FACING));
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
        BlockState state = super.getPlacementState(ctx);
        if (state == null)
            return null;

        WorldView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        Direction[] directions = ctx.getPlacementDirections();

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

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
                                                BlockPos pos, BlockPos posFrom) {
        state = super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
        return direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
    }

    /* Client */

    @Environment(EnvType.CLIENT)
    protected Iterable<Vec3d> getParticleOffsets(BlockState state) {
        return CANDLES_TO_PARTICLE_OFFSETS.get(state.get(CANDLES).intValue()).get(state.get(FACING));
    }

    static {
        SHAPES = Util.make(() -> {
            Int2ObjectMap<Map<Direction, VoxelShape>> shapes = new Int2ObjectOpenHashMap<>();

            double highest = 12.0;
            shapes.put(1, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, createCuboidShape(6.0, 2.0, 10.0, 10.0, highest, 16.0),
                    Direction.EAST, createCuboidShape(0.0, 2.0, 6.0, 6.0, highest, 10.0),
                    Direction.SOUTH, createCuboidShape(6.0, 2.0, 0.0, 10.0, highest, 6.0),
                    Direction.WEST, createCuboidShape(10.0, 2.0, 6.0, 16.0, highest, 10.0)
            )));

            shapes.put(2, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, createCuboidShape(2.0, 2.0, 10.0, 14.0, highest, 16.0),
                    Direction.EAST, createCuboidShape(0.0, 2.0, 2.0, 6.0, highest, 14.0),
                    Direction.SOUTH, createCuboidShape(2.0, 2.0, 0.0, 14.0, highest, 6.0),
                    Direction.WEST, createCuboidShape(10.0, 2.0, 2.0, 16.0, highest, 14.0)
            )));

            shapes.put(3, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, createCuboidShape(1.0, 2.0, 8.0, 15.0, highest, 16.0),
                    Direction.EAST, createCuboidShape(0.0, 2.0, 1.0, 8.0, highest, 15.0),
                    Direction.SOUTH, createCuboidShape(1.0, 2.0, 0.0, 15.0, highest, 8.0),
                    Direction.WEST, createCuboidShape(8.0, 2.0, 1.0, 16.0, highest, 15.0)
            )));

            highest = 14.0;
            shapes.put(4, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, createCuboidShape(1.0, 2.0, 6.0, 15.0, highest, 16.0),
                    Direction.EAST, createCuboidShape(0.0, 2.0, 1.0, 10.0, highest, 15.0),
                    Direction.SOUTH, createCuboidShape(1.0, 2.0, 0.0, 15.0, highest, 10.0),
                    Direction.WEST, createCuboidShape(6.0, 2.0, 1.0, 16.0, highest, 15.0)
            )));

            return Int2ObjectMaps.unmodifiable(shapes);
        });

        CANDLES_TO_PARTICLE_OFFSETS = Util.make(() -> {
            Int2ObjectMap<Map<Direction, List<Vec3d>>> offsets = new Int2ObjectOpenHashMap<>();

            double highestPoint = 0.875;
            double second = 0.8125;
            double third = 0.75;
            offsets.put(1, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, ImmutableList.of(new Vec3d(0.5, highestPoint, 0.75)),
                    Direction.EAST, ImmutableList.of(new Vec3d(0.25, highestPoint, 0.5)),
                    Direction.SOUTH, ImmutableList.of(new Vec3d(0.5, highestPoint, 0.25)),
                    Direction.WEST, ImmutableList.of(new Vec3d(0.75, highestPoint, 0.5))
            )));

            offsets.put(2, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, ImmutableList.of(new Vec3d(0.75, highestPoint, 0.75), new Vec3d(0.25, second, 0.75)),
                    Direction.EAST, ImmutableList.of(new Vec3d(0.25, highestPoint, 0.75), new Vec3d(0.25, second, 0.25)),
                    Direction.SOUTH, ImmutableList.of(new Vec3d(0.25, highestPoint, 0.25), new Vec3d(0.75, second, 0.25)),
                    Direction.WEST, ImmutableList.of(new Vec3d(0.75, highestPoint, 0.25), new Vec3d(0.75, second, 0.75))
            )));

            offsets.put(3, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, ImmutableList.of(
                            new Vec3d(0.8125, highestPoint, 0.75),
                            new Vec3d(0.1875, second, 0.75),
                            new Vec3d(0.5, third, 0.625)),
                    Direction.EAST, ImmutableList.of(
                            new Vec3d(0.25, highestPoint, 0.8125),
                            new Vec3d(0.25, second, 0.1875),
                            new Vec3d(0.375, third, 0.5)),
                    Direction.SOUTH, ImmutableList.of(
                            new Vec3d(0.1875, highestPoint, 0.25),
                            new Vec3d(0.8125, second, 0.25),
                            new Vec3d(0.5, third, 0.375)),
                    Direction.WEST, ImmutableList.of(
                            new Vec3d(0.75, highestPoint, 0.1875),
                            new Vec3d(0.75, second, 0.8125),
                            new Vec3d(0.625, third, 0.5))
            )));

            highestPoint = 1.0;
            double four = second;
            second = 0.9375;
            third = 0.6875;
            offsets.put(4, new EnumMap<>(ImmutableMap.of(
                    Direction.NORTH, ImmutableList.of(
                            new Vec3d(0.8125, highestPoint, 0.75),
                            new Vec3d(0.1875, second, 0.75),
                            new Vec3d(0.3125, third, 0.5),
                            new Vec3d(0.6875, four, 0.5)),
                    Direction.EAST, ImmutableList.of(
                            new Vec3d(0.25, highestPoint, 0.8125),
                            new Vec3d(0.25, second, 0.1875),
                            new Vec3d(0.5, third, 0.3125),
                            new Vec3d(0.5, four, 0.6875)),
                    Direction.SOUTH, ImmutableList.of(
                            new Vec3d(0.1875, highestPoint, 0.25),
                            new Vec3d(0.8125, second, 0.25),
                            new Vec3d(0.6875, third, 0.5),
                            new Vec3d(0.3125, four, 0.5)),
                    Direction.WEST, ImmutableList.of(
                            new Vec3d(0.75, highestPoint, 0.1875),
                            new Vec3d(0.75, second, 0.8125),
                            new Vec3d(0.5, third, 0.6875),
                            new Vec3d(0.5, four, 0.3125))
            )));

            return Int2ObjectMaps.unmodifiable(offsets);
        });
    }
}
