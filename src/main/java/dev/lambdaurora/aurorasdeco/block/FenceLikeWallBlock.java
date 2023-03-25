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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

/**
 * Represents a fence-like wall.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class FenceLikeWallBlock extends HorizontalConnectingBlock {
	public FenceLikeWallBlock(Settings settings) {
		super(4.f, 2.f, 16.f, 13.f, 24.f, settings);
		System.arraycopy(
				this.createShapes(4.f, 2.f, 16.f, 9.f, 13.f), 0,
				this.boundingShapes, 0,
				this.boundingShapes.length
		);

		this.setDefaultState(this.stateManager.getDefaultState()
				.with(NORTH, false)
				.with(EAST, false)
				.with(SOUTH, false)
				.with(WEST, false)
				.with(WATERLOGGED, false)
		);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
	}

	public boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir) {
		return !cannotConnect(state) && neighborIsFullSquare
				|| state.isIn(BlockTags.WALLS);
	}

	/* Placement */

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		var world = ctx.getWorld();
		var blockPos = ctx.getBlockPos();
		var fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		var northPos = blockPos.north();
		var eastPos = blockPos.east();
		var southPos = blockPos.south();
		var westPos = blockPos.west();
		var northState = world.getBlockState(northPos);
		var eastState = world.getBlockState(eastPos);
		var southState = world.getBlockState(southPos);
		var westState = world.getBlockState(westPos);
		return super.getPlacementState(ctx)
				.with(NORTH, this.canConnect(northState, northState.isSideSolidFullSquare(world, northPos, Direction.SOUTH), Direction.SOUTH))
				.with(EAST, this.canConnect(eastState, eastState.isSideSolidFullSquare(world, eastPos, Direction.WEST), Direction.WEST))
				.with(SOUTH, this.canConnect(southState, southState.isSideSolidFullSquare(world, southPos, Direction.NORTH), Direction.NORTH))
				.with(WEST, this.canConnect(westState, westState.isSideSolidFullSquare(world, westPos, Direction.EAST), Direction.EAST))
				.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	/* Updates */

	@SuppressWarnings("deprecation")
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
			WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return direction.getAxis().getType() == Direction.Type.HORIZONTAL ?
				state.with(FACING_PROPERTIES.get(direction),
						this.canConnect(neighborState,
								neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()),
								direction.getOpposite()))
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	/* Entity Stuff */

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}
}
