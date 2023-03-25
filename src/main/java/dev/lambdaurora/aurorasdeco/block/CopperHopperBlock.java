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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Represents a copper hopper block.
 * <p>
 * A copper hopper can filter items.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CopperHopperBlock extends HopperBlock {
	public CopperHopperBlock(Settings settings) {
		super(settings);
	}

	/* Placement */

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && !moved) {
			var copperHopper = AurorasDecoRegistry.COPPER_HOPPER_BLOCK_ENTITY_TYPE.get(world, pos);
			if (copperHopper != null) {
				copperHopper.dropFilter();
			}

			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	/* Block Entity Stuff */

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasDecoRegistry.COPPER_HOPPER_BLOCK_ENTITY_TYPE.instantiate(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient() ? null : checkType(type, AurorasDecoRegistry.COPPER_HOPPER_BLOCK_ENTITY_TYPE, HopperBlockEntity::serverTick);
	}
}
