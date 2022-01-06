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
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ClampedIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.decorator.BiomePlacementModifier;
import net.minecraft.world.gen.decorator.BlockPredicateFilterPlacementModifier;
import net.minecraft.world.gen.decorator.CountPlacementModifier;
import net.minecraft.world.gen.decorator.InSquarePlacementModifier;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.util.PlacedFeatureUtil;

public final class AurorasDecoVegetationPlacedFeatures {
	private AurorasDecoVegetationPlacedFeatures() {
		throw new UnsupportedOperationException("AurorasDecoVegetationPlacedFeatures only contains static definitions.");
	}

	public static final PlacedFeature PATCH_LAVENDER = PlacedFeatureUtil.register(
			AurorasDeco.id("patch_lavender").toString(),
			AurorasDecoVegetationConfiguredFeatures.PATCH_LAVENDER
					.withPlacement(
							CountPlacementModifier.create(ClampedIntProvider.create(UniformIntProvider.create(-1, 5), 0, 5)),
							InSquarePlacementModifier.getInstance(),
							PlacedFeatureUtil.MOTION_BLOCKING_HEIGHTMAP,
							BiomePlacementModifier.getInstance()
					)
	);

	public static final PlacedFeature TREES_LAVENDER_PLAINS = PlacedFeatureUtil.register(
			AurorasDeco.id("trees_lavender_plains").toString(),
			AurorasDecoVegetationConfiguredFeatures.TREES_LAVENDER_PLAINS
					.withPlacement(
							PlacedFeatureUtil.method_39736(0, 0.05F, 1),
							InSquarePlacementModifier.getInstance(),
							VegetationPlacedFeatures.TREE_THRESHOLD,
							PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
							BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
							BiomePlacementModifier.getInstance()
					)
	);
}
