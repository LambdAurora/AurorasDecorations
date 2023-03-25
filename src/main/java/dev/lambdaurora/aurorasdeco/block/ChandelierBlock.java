/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.minecraft.block.*;
import net.minecraft.client.render.RenderLayer;
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
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

import java.util.List;

/**
 * Represents a chandelier.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChandelierBlock extends ExtendedCandleBlock {
	public static final VoxelShape ONE_CANDLE_SHAPE;
	public static final VoxelShape TWO_CANDLE_SHAPE;
	public static final VoxelShape THREE_CANDLE_SHAPE;

	private static final Int2ObjectMap<List<Vec3d>> CANDLES_TO_PARTICLE_OFFSETS;

	private static final VoxelShape HOLDER_SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 1.0, 10.0);

	public ChandelierBlock(CandleBlock candleBlock) {
		super(candleBlock);

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			BlockRenderLayerMap.put(RenderLayer.getCutout(), this);
		}
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(CANDLES)) {
			case 1 -> ONE_CANDLE_SHAPE;
			case 2 -> TWO_CANDLE_SHAPE;
			case 3, 4 -> THREE_CANDLE_SHAPE;
			default -> super.getOutlineShape(state, world, pos, context);
		};
	}

	/* Placement */

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		var upPos = pos.up();
		var upState = world.getBlockState(upPos);

		if (upState.getBlock() instanceof ChainBlock && upState.get(ChainBlock.AXIS) == Direction.Axis.Y)
			return true;

		return !VoxelShapes.matchesAnywhere(upState.getSidesShape(world, upPos).getFace(Direction.DOWN),
				HOLDER_SHAPE, BooleanBiFunction.ONLY_SECOND);
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
			BlockPos pos, BlockPos posFrom) {
		state = super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
		return direction == Direction.UP && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
	}

	/* Client */

	protected Iterable<Vec3d> getParticleOffsets(BlockState state) {
		return CANDLES_TO_PARTICLE_OFFSETS.get(state.get(CANDLES).intValue());
	}

	static {
		ONE_CANDLE_SHAPE = createCuboidShape(5.0, 3.0, 5.0, 11.0, 13.0, 11.0);
		TWO_CANDLE_SHAPE = createCuboidShape(2.0, 3.0, 6.0, 14.0, 12.0, 10.0);
		THREE_CANDLE_SHAPE = createCuboidShape(2.0, 3.0, 2.0, 14.0, 12.0, 14.0);

		CANDLES_TO_PARTICLE_OFFSETS = Util.make(() -> {
			Int2ObjectMap<List<Vec3d>> map = new Int2ObjectOpenHashMap<>();
			map.defaultReturnValue(ImmutableList.of());

			double highestPoint = 0.875;
			double second = 0.8125;
			double third = 0.6875;

			map.put(1, ImmutableList.of(new Vec3d(0.5, 0.6875, 0.5)));
			map.put(2, ImmutableList.of(new Vec3d(0.25, highestPoint, 0.5), new Vec3d(0.75, second, 0.5)));
			map.put(3, ImmutableList.of(new Vec3d(0.5, highestPoint, 0.25),
					new Vec3d(0.25, second, 0.75),
					new Vec3d(0.75, third, 0.75)));
			map.put(4, ImmutableList.of(new Vec3d(0.75, highestPoint, 0.25),
					new Vec3d(0.25, second, 0.25),
					new Vec3d(0.25, third, 0.75),
					new Vec3d(0.75, second, 0.75)));
			return Int2ObjectMaps.unmodifiable(map);
		});
	}
}
