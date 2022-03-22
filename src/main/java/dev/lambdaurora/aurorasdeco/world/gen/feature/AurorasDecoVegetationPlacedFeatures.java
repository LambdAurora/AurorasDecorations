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
import net.minecraft.tag.TagKey;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ClampedIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.decorator.*;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacementModifier;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.util.PlacedFeatureUtil;

import java.util.List;

public final class AurorasDecoVegetationPlacedFeatures {
	private AurorasDecoVegetationPlacedFeatures() {
		throw new UnsupportedOperationException("AurorasDecoVegetationPlacedFeatures only contains static definitions.");
	}

	public static final Holder<PlacedFeature> PATCH_LAVENDER = register(
			id("patch_lavender"),
			AurorasDecoVegetationConfiguredFeatures.PATCH_LAVENDER,
			List.of(
					CountPlacementModifier.create(ClampedIntProvider.create(UniformIntProvider.create(-1, 5), 0, 5)),
					InSquarePlacementModifier.getInstance(),
					PlacedFeatureUtil.MOTION_BLOCKING_HEIGHTMAP,
					BiomePlacementModifier.getInstance()
			)
	);

	public static final Holder<PlacedFeature> TREES_LAVENDER_PLAINS = register(
			id("trees_lavender_plains"),
			AurorasDecoVegetationConfiguredFeatures.TREES_LAVENDER_PLAINS,
			List.of(
					PlacedFeatureUtil.createCountExtraModifier(0, 0.05f, 1),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			)
	);

	public static final PlacedFeatureMetadata FALLEN_FOREST_TREES = new PlacedFeatureMetadata(id("fallen_trees/forest"),
			AurorasDecoVegetationConfiguredFeatures.FALLEN_FOREST_TREES,
			List.of(
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
			.setAllowedTag(TagKey.of(Registry.BIOME_KEY, AurorasDeco.id("feature/fallen_trees/forest")))
			.register();

	public static final PlacedFeatureMetadata FALLEN_BIRCH_FOREST_TREES = new PlacedFeatureMetadata(id("fallen_trees/birch_forest"),
			AurorasDecoTreeConfiguredFeatures.FALLEN_BIRCH_TREE,
			List.of(
					RarityFilterPlacementModifier.create(5),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.BIRCH_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			))
			.addAllowedBiomeCategory(Biome.Category.FOREST)
			.addAllowedNeighborFeature(new Identifier("birch_tall"))
			.setAllowedTag(TagKey.of(Registry.BIOME_KEY, AurorasDeco.id("feature/fallen_trees/birch_forest")))
			.register();

	public static final PlacedFeatureMetadata FALLEN_SPRUCE_TAIGA_TREES = new PlacedFeatureMetadata(id("fallen_trees/spruce_taiga"),
			AurorasDecoTreeConfiguredFeatures.FALLEN_SPRUCE_TREE,
			List.of(
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
			.setAllowedTag(TagKey.of(Registry.BIOME_KEY, AurorasDeco.id("feature/fallen_trees/spruce_taiga")))
			.register();

	public static final PlacedFeatureMetadata SNOWY_FALLEN_SPRUCE_TAIGA_TREES = new PlacedFeatureMetadata(id("fallen_trees/snowy_spruce_taiga"),
			AurorasDecoTreeConfiguredFeatures.SNOWY_FALLEN_SPRUCE_TREE,
			List.of(
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
			.setAllowedTag(TagKey.of(Registry.BIOME_KEY, AurorasDeco.id("feature/fallen_trees/snowy_spruce_taiga")))
			.register();

	public static final PlacedFeatureMetadata FALLEN_TREES_OLD_GROWTH_SPRUCE_TAIGA = new PlacedFeatureMetadata(
			id("fallen_trees/old_growth_spruce_taiga"),
			AurorasDecoTreeConfiguredFeatures.FALLEN_SPRUCE_TREE,
			List.of(
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
			.setAllowedTag(TagKey.of(Registry.BIOME_KEY, AurorasDeco.id("feature/fallen_trees/old_growth_spruce_taiga")))
			.register();

	public static final PlacedFeatureMetadata FALLEN_TREES_SPARSE_JUNGLE = new PlacedFeatureMetadata(id("fallen_trees/sparse_jungle"),
			AurorasDecoVegetationConfiguredFeatures.FALLEN_TREES_SPARSE_JUNGLE,
			List.of(
					RarityFilterPlacementModifier.create(8),
					InSquarePlacementModifier.getInstance(),
					VegetationPlacedFeatures.TREE_THRESHOLD,
					PlacedFeatureUtil.OCEAN_FLOOR_HEIGHTMAP,
					BlockPredicateFilterPlacementModifier.create(BlockPredicate.wouldSurvive(Blocks.JUNGLE_SAPLING.getDefaultState(), BlockPos.ORIGIN)),
					BiomePlacementModifier.getInstance()
			))
			.addAllowedBiomeCategory(Biome.Category.JUNGLE)
			.addAllowedPrecipitation(Biome.Precipitation.RAIN)
			.addAllowedNeighborFeature(new Identifier("trees_sparse_jungle"))
			.setAllowedTag(TagKey.of(Registry.BIOME_KEY, AurorasDeco.id("feature/fallen_trees/sparse_jungle")))
			.register();


	private static Identifier id(String name) {
		return AurorasDeco.id(name);
	}

	private static Holder<PlacedFeature> register(Identifier id, Holder<? extends ConfiguredFeature<?, ?>> configuredFeature,
	                                              List<PlacementModifier> modifiers) {
		return BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id,
				new PlacedFeature(Holder.upcast(configuredFeature), List.copyOf(modifiers)));
	}
}
