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

package dev.lambdaurora.aurorasdeco.block.plant;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.minecraft.block.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.List;

/**
 * Represents duckweed as a block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class DuckweedBlock extends Block implements FluidFillable, Fertilizable {
	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);

	public DuckweedBlock() {
		super(QuiltBlockSettings.copyOf(Blocks.LILY_PAD).noCollision().sounds(BlockSoundGroup.MOSS_CARPET));
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	/* Placement */

	private boolean canPlaceAt(BlockView world, BlockPos pos, boolean allowExisting) {
		if (!world.getFluidState(pos).isEqualAndStill(Fluids.WATER))
			return false;
		BlockState existingState = world.getBlockState(pos);

		if ((!allowExisting && existingState.isOf(this)) && !world.getBlockState(pos).isOf(Blocks.WATER))
			return false;

		BlockState aboveState = world.getBlockState(pos.up());
		return aboveState.isAir() || aboveState.isIn(AurorasDecoTags.VEGETATION_ON_WATER_SURFACE);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return this.canPlaceAt(world, pos, true);
	}

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return fluidState.isIn(FluidTags.WATER) && fluidState.getLevel() == 8 ? super.getPlacementState(ctx) : null;
	}

	@Override
	public BlockState getStateForNeighborUpdate(
			BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
	) {
		if (this.canPlaceAt(state, world, pos)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
			return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
		}

		return Blocks.AIR.getDefaultState();
	}

	/* Interaction */

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		super.onEntityCollision(state, world, pos, entity);
		if (world instanceof ServerWorld && entity instanceof BoatEntity) {
			world.breakBlock(new BlockPos(pos), true, entity);
		}
	}

	/* Fluid handling */

	@Override
	public FluidState getFluidState(BlockState state) {
		return Fluids.WATER.getStill(false);
	}

	@Override
	public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		return false;
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		return false;
	}

	/* Fertilization */

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
		return AuroraUtil.HORIZONTAL_DIRECTIONS.stream().anyMatch(direction -> this.canPlaceAt(world, pos.offset(direction), false));
	}

	@Override
	public boolean canGrow(World world, RandomGenerator random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, RandomGenerator random, BlockPos pos, BlockState state) {
		var list = Util.toShuffledList(AuroraUtil.HORIZONTAL_DIRECTIONS.stream(), random);

		for (var direction : list) {
			BlockPos neighborPos = pos.offset(direction);

			if (this.canPlaceAt(world, neighborPos, false)) {
				world.setBlockState(neighborPos, this.getDefaultState(), Block.NOTIFY_LISTENERS);
				break;
			}
		}
	}

	/* Tooltip */

	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
		super.appendTooltip(stack, world, tooltip, options);
		tooltip.add(Text.literal("Lemnoideae").formatted(Formatting.GOLD, Formatting.ITALIC));
	}
}
