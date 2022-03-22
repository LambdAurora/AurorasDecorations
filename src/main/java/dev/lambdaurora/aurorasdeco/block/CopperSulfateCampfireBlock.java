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

import dev.lambdaurora.aurorasdeco.block.behavior.CopperSulfateBehavior;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Represents a copper sulfate campfire block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CopperSulfateCampfireBlock extends CampfireBlock {
	public CopperSulfateCampfireBlock(Settings settings) {
		super(true, 2, settings);
	}

	/* Ticking */

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return state.get(LIT);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		CopperSulfateBehavior.attemptToDecompose(state, world, pos, random, 15);
	}

	/* Visual */

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(LIT)) {
			if (random.nextInt(10) == 0) {
				world.playSound(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS,
						.5f + random.nextFloat(), random.nextFloat() * .7f + .6f, false);
			}

			if (random.nextInt(5) == 0) {
				for (int i = 0; i < random.nextInt(1) + 1; ++i) {
					world.addParticle(AurorasDecoParticles.COPPER_SULFATE_LAVA,
							pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
							random.nextFloat() / 2.f, 5.0E-5d, random.nextFloat() / 2.f);
				}
			}
		}
	}
}
