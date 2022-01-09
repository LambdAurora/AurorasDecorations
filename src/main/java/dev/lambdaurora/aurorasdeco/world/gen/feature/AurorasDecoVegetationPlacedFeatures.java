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
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ClampedIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.decorator.*;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.util.PlacedFeatureUtil;

public final class AurorasDecoVegetationPlacedFeatures {
	private AurorasDecoVegetationPlacedFeatures() {
		throw new UnsupportedOperationException("AurorasDecoVegetationPlacedFeatures only contains static definitions.");
	}

	public static final PlacedFeature PATCH_LAVENDER = register(
			key("patch_lavender"),
			AurorasDecoVegetationConfiguredFeatures.PATCH_LAVENDER
					.withPlacement(
							CountPlacementModifier.create(ClampedIntProvider.create(UniformIntProvider.create(-1, 5), 0, 5)),
							InSquarePlacementModifier.getInstance(),
							PlacedFeatureUtil.MOTION_BLOCKING_HEIGHTMAP,
							BiomePlacementModifier.getInstance()
					)
	);

	public static final PlacedFeature TREES_LAVENDER_PLAINS = register(
			key("trees_lavender_plains"),
			AurorasDecoVegetationConfiguredFeatures.TREES_LAVENDER_PLAINS
					.withPlacement(
							PlacedFeatureUtil.method_39736(0, 0.05f, 1),
							InSquarePlacementModifier.getInstance(),
							VegetationPlacedFeatures.TREE_THRESHOLD,
							PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
							BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
							BiomePlacementModifier.getInstance()
					)
	);

	public static final PlacedFeatureMetadata FALLEN_FOREST_TREES = new PlacedFeatureMetadata(key("fallen_forest_trees"),
			AurorasDecoVegetationConfiguredFeatures.FALLEN_FOREST_TREES.withPlacement(
					RarityFilterPlacementModifier.create(3),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			))
			.addAllowedBiomeCategory(Biome.Category.FOREST)
			.addAllowedNeighborFeature(new Identifier("trees_birch_and_oak"))
			.addAllowedNeighborFeature(new Identifier("trees_flower_forest"))
			.setAllowedTag(TagFactory.BIOME.create(AurorasDeco.id("feature/fallen_forest_trees")))
			.register();

	public static final PlacedFeatureMetadata FALLEN_BIRCH_FOREST_TREES = new PlacedFeatureMetadata(key("fallen_birch_forest_trees"),
			AurorasDecoTreeConfiguredFeatures.FALLEN_BIRCH_TREE.withPlacement(
					RarityFilterPlacementModifier.create(5),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.BIRCH_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			))
			.addAllowedBiomeCategory(Biome.Category.FOREST)
			.addAllowedNeighborFeature(new Identifier("birch_tall"))
			.setAllowedTag(TagFactory.BIOME.create(AurorasDeco.id("feature/fallen_birch_forest_trees")))
			.register();

	public static final PlacedFeatureMetadata FALLEN_SPRUCE_TAIGA_TREES = new PlacedFeatureMetadata(key("fallen_spruce_taiga_trees"),
			AurorasDecoTreeConfiguredFeatures.FALLEN_SPRUCE_TREE.withPlacement(
					RarityFilterPlacementModifier.create(4),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.SPRUCE_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			))
			.addAllowedBiomeCategory(Biome.Category.TAIGA)
			.addAllowedPrecipitation(Biome.Precipitation.RAIN)
			.addAllowedNeighborFeature(new Identifier("trees_taiga"))
			.setAllowedTag(TagFactory.BIOME.create(AurorasDeco.id("feature/fallen_spruce_taiga_trees")))
			.register();

	public static final PlacedFeatureMetadata SNOWY_FALLEN_SPRUCE_TAIGA_TREES = new PlacedFeatureMetadata(key("snowy_fallen_spruce_taiga_trees"),
			AurorasDecoTreeConfiguredFeatures.SNOWY_FALLEN_SPRUCE_TREE.withPlacement(
					RarityFilterPlacementModifier.create(3),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.SPRUCE_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			))
			.addAllowedBiomeCategory(Biome.Category.TAIGA)
			.addAllowedPrecipitation(Biome.Precipitation.SNOW)
			.addAllowedNeighborFeature(new Identifier("trees_taiga"))
			.setAllowedTag(TagFactory.BIOME.create(AurorasDeco.id("feature/snowy_fallen_spruce_taiga_trees")))
			.register();

	public static final PlacedFeatureMetadata FALLEN_OLD_GROWTH_SPRUCE_TAIGA_TREES = new PlacedFeatureMetadata(
			key("fallen_old_growth_spruce_taiga_trees"),
			AurorasDecoTreeConfiguredFeatures.FALLEN_SPRUCE_TREE.withPlacement(
					RarityFilterPlacementModifier.create(2),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.SPRUCE_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			))
			.addAllowedBiomeCategory(Biome.Category.TAIGA)
			.addAllowedNeighborFeature(new Identifier("trees_old_growth_spruce_taiga"))
			.addAllowedNeighborFeature(new Identifier("trees_old_growth_pine_taiga"))
			.setAllowedTag(TagFactory.BIOME.create(AurorasDeco.id("feature/fallen_old_growth_spruce_taiga_trees")))
			.register();


	private static RegistryKey<PlacedFeature> key(String name) {
		return RegistryKey.of(Registry.PLACED_FEATURE_KEY, AurorasDeco.id(name));
	}

	private static PlacedFeature register(RegistryKey<PlacedFeature> key, PlacedFeature feature) {
		return Registry.register(BuiltinRegistries.PLACED_FEATURE, key, feature);
	}
}
