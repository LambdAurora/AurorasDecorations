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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.block.behavior.component.RandomTickComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;

public class AuroraBlock extends Block {
	protected RandomTickComponent tickComponent;

	public AuroraBlock(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		if (this.tickComponent == null) return super.hasRandomTicks(state);
		return this.tickComponent.hasRandomTicks(state);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		if (this.tickComponent != null) {
			this.tickComponent.randomTick(state, world, pos, random);
		}
	}
}
