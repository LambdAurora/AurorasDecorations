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
import net.minecraft.util.Holder;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.ConfiguredFeatureUtil;
import net.minecraft.world.gen.feature.util.PlacedFeatureUtil;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

import static dev.lambdaurora.aurorasdeco.world.gen.feature.AurorasDecoFeatures.register;

public final class AurorasDecoVegetationConfiguredFeatures {
	private AurorasDecoVegetationConfiguredFeatures() {
		throw new UnsupportedOperationException("AurorasDecoVegetationConfiguredFeatures only contains static definitions.");
	}

	public static final Holder<ConfiguredFeature<RandomPatchFeatureConfig, ?>> PATCH_LAVENDER = register(
			AurorasDeco.id("patch_lavender"),
			Feature.RANDOM_PATCH,
			ConfiguredFeatureUtil.createRandomPatchFeatureConfig(
					Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(AurorasDecoPlants.LAVENDER))
			)
	);

	public static final Holder<ConfiguredFeature<RandomFeatureConfig, ?>> TREES_LAVENDER_PLAINS = register(
			AurorasDeco.id("trees_lavender_plains"),
			Feature.RANDOM_SELECTOR,
			new RandomFeatureConfig(
					List.of(
							new WeightedPlacedFeature(PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.FALLEN_OAK_TREE), 0.25f),
							new WeightedPlacedFeature(PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.FALLEN_BIRCH_TREE), 0.20f),
							new WeightedPlacedFeature(PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.BIRCH_BEES_015), 0.5f),
							new WeightedPlacedFeature(PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.FANCY_OAK_BEES_015), 0.33333334F)
					),
					PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.OAK_BEES_015)
			)
	);

	public static final Holder<ConfiguredFeature<RandomFeatureConfig, ?>> FALLEN_FOREST_TREES = register(
			AurorasDeco.id("fallen_trees/average"),
			Feature.RANDOM_SELECTOR,
			new RandomFeatureConfig(
					List.of(
							new WeightedPlacedFeature(PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.FALLEN_BIRCH_TREE), 0.35f)
					),
					PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.FALLEN_OAK_TREE)
			)
	);

	public static final Holder<ConfiguredFeature<RandomFeatureConfig, ?>> FALLEN_TREES_SPARSE_JUNGLE = register(
			AurorasDeco.id("fallen_trees/sparse_jungle"),
			Feature.RANDOM_SELECTOR,
			new RandomFeatureConfig(
					List.of(
							new WeightedPlacedFeature(PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.FALLEN_OAK_TREE), 0.05f)
					),
					PlacedFeatureUtil.placedInline(AurorasDecoTreeConfiguredFeatures.FALLEN_JUNGLE_TREE)
			)
	);
}
