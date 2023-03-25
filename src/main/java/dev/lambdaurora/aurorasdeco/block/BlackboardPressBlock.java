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

import dev.lambdaurora.aurorasdeco.block.entity.BlackboardPressBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link dev.lambdaurora.aurorasdeco.blackboard.Blackboard} press block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardPressBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	private static final VoxelShape SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 9 / 16., 1);
	private static final VoxelShape BASE_SHAPE = VoxelShapes.union(
			VoxelShapes.cuboid(0, 0, 0, 1, 2 / 16., 1),
			VoxelShapes.cuboid(1 / 16., 2 / 16., 1 / 16., 15 / 16., 3 / 16., 15 / 16.)
	);
	private static final VoxelShape X_SHAPE = VoxelShapes.union(
			BASE_SHAPE,
			VoxelShapes.cuboid(4 / 16., 2 / 16., 0, 12 / 16., 7 / 16., 1 / 16.),
			VoxelShapes.cuboid(4 / 16., 2 / 16., 15 / 16., 12 / 16., 7 / 16., 1),
			VoxelShapes.cuboid(4 / 16., 7 / 16., 0, 12 / 16., 9 / 16., 1)
	);
	private static final VoxelShape Z_SHAPE = VoxelShapes.union(
			BASE_SHAPE,
			VoxelShapes.cuboid(0, 2 / 16., 4 / 16., 1 / 16., 7 / 16., 12 / 16.),
			VoxelShapes.cuboid(15 / 16., 2 / 16., 4 / 16., 1, 7 / 16., 12 / 16.),
			VoxelShapes.cuboid(0, 7 / 16., 4 / 16., 1, 9 / 16., 12 / 16.)
	);

	public BlackboardPressBlock(Settings settings) {
		super(settings);

		this.setDefaultState(this.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(WATERLOGGED, false)
		);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(FACING).getAxis()) {
			case X -> X_SHAPE;
			case Z -> Z_SHAPE;
			default -> SHAPE; // Why..?
		};
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	/* Placement */

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		var state = super.getPlacementState(ctx);
		if (state != null)
			return state.with(FACING, ctx.getPlayerFacing().getOpposite());
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

	/* Block Entity Stuff */

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasDecoRegistry.BLACKBOARD_PRESS_BLOCK_ENTITY.instantiate(pos, state);
	}

	public @Nullable BlackboardPressBlockEntity getBlackboardPressEntity(BlockView world, BlockPos pos) {
		var entity = world.getBlockEntity(pos);
		if (entity instanceof BlackboardPressBlockEntity blackboardPress)
			return blackboardPress;
		return null;
	}

	/* Fluid */

	/* Entity Stuff */

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return switch (type) {
			case LAND, AIR -> false;
			case WATER -> world.getFluidState(pos).isIn(FluidTags.WATER);
		};
	}
}
