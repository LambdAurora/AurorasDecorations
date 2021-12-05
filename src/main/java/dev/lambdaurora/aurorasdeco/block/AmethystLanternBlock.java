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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoParticles;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.Random;

/**
 * Represents an amethyst lantern.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AmethystLanternBlock extends LanternBlock {
	public static final Identifier BLOCK_TEXTURE = AurorasDeco.id("block/amethyst_lantern");
	public static final Identifier HANGING_MODEL = AurorasDeco.id("block/hanging_amethyst_lantern");
	public static final int EFFECT_RADIUS = 32;

	public AmethystLanternBlock() {
		super(QuiltBlockSettings.copyOf(Blocks.LANTERN).luminance(14));
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextBoolean()) {
			double x = pos.getX() + random.nextFloat();
			double y = pos.getY() + random.nextFloat();
			double z = pos.getZ() + random.nextFloat();
			world.addParticle(AurorasDecoParticles.AMETHYST_GLINT, x, y, z, 0.f, 0.f, 0.f);
		}
	}
}
