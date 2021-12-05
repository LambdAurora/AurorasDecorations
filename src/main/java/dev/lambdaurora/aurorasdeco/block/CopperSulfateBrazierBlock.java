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

import dev.lambdaurora.aurorasdeco.block.behavior.CopperSulfateBehavior;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.Random;

/**
 * Represents a copper sulfate brazier brazier.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CopperSulfateBrazierBlock extends BrazierBlock {
	public CopperSulfateBrazierBlock(QuiltBlockSettings settings, int fireDamage, ParticleEffect particle) {
		super(settings.ticksRandomly(), fireDamage, particle);
	}

	/* Ticking */

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return state.get(LIT);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		CopperSulfateBehavior.attemptToDecompose(state, world, pos, random, 20);
	}
}
