/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.block.sapling;

import dev.lambdaurora.aurorasdeco.world.gen.feature.AurorasDecoTreeConfiguredFeatures;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.util.Holder;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the Jacaranda sapling generator.
 */
public class JacarandaSaplingGenerator extends SaplingGenerator {
	@Override
	protected @Nullable Holder<? extends ConfiguredFeature<?, ?>> getTreeFeature(RandomGenerator random, boolean bees) {
		if (random.nextBoolean()) {
			return bees ? AurorasDecoTreeConfiguredFeatures.JACARANDA_TREE_BEES_015 : AurorasDecoTreeConfiguredFeatures.JACARANDA_TREE;
		} else {
			return bees ? AurorasDecoTreeConfiguredFeatures.FLOWERING_JACARANDA_TREE_BEES_015
					: AurorasDecoTreeConfiguredFeatures.FLOWERING_JACARANDA_TREE;
		}
	}
}
