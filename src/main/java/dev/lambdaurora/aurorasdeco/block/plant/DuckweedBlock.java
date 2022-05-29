/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.block.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.FluidTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
public final class DuckweedBlock extends Block implements FluidFillable {
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

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (!world.getFluidState(pos).isEqualAndStill(Fluids.WATER))
			return false;
		BlockState aboveState = world.getBlockState(pos.up());
		return aboveState.isAir() || aboveState.isIn(AurorasDecoTags.VEGETATION_ON_WATER_SURFACE);
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

	/* Tooltip */

	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
		super.appendTooltip(stack, world, tooltip, options);
		tooltip.add(new LiteralText("Lemnoideae").formatted(Formatting.GOLD, Formatting.ITALIC));
	}
}
