/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.world.gen.feature;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import net.minecraft.class_3226;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.ConfiguredFeatureUtil;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

public final class AurorasDecoVegetationConfiguredFeatures {
	private AurorasDecoVegetationConfiguredFeatures() {
		throw new UnsupportedOperationException("AurorasDecoVegetationConfiguredFeatures only contains static definitions.");
	}

	public static final ConfiguredFeature<RandomPatchFeatureConfig, ?> PATCH_LAVENDER = ConfiguredFeatureUtil.register(
			AurorasDeco.id("patch_lavender").toString(),
			Feature.RANDOM_PATCH
					.configure(
							ConfiguredFeatureUtil.createRandomPatchFeatureConfig(
									Feature.SIMPLE_BLOCK.configure(new SimpleBlockFeatureConfig(BlockStateProvider.of(AurorasDecoPlants.LAVENDER)))
							)
					)
	);

	public static final ConfiguredFeature<RandomFeatureConfig, ?> TREES_LAVENDER_PLAINS = ConfiguredFeatureUtil.register(
			AurorasDeco.id("trees_lavender_plains").toString(),
			Feature.RANDOM_SELECTOR
					.configure(
							new RandomFeatureConfig(
									List.of(
											new class_3226(AurorasDecoTreeConfiguredFeatures.BIRCH_BEES_015.withPlacement(), 0.5f),
											new class_3226(AurorasDecoTreeConfiguredFeatures.FANCY_OAK_BEES_015.withPlacement(), 0.33333334F)
									),
									AurorasDecoTreeConfiguredFeatures.OAK_BEES_015.withPlacement()
							)
					)
	);
}
