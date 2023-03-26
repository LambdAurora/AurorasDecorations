/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public final class AurorasDecoVegetationPlacedFeatures {
	private AurorasDecoVegetationPlacedFeatures() {
		throw new UnsupportedOperationException("AurorasDecoVegetationPlacedFeatures only contains static definitions.");
	}

	public static final PlacedFeatureMetadata FALLEN_FOREST_TREES = new PlacedFeatureMetadata(id("vegetation/fallen_tree/forest"))
			.addAllowedBiomeCategoryTag(BiomeTags.FOREST)
			.addAllowedNeighborFeature(new Identifier("trees_birch_and_oak"))
			.addAllowedNeighborFeature(new Identifier("trees_flower_forest"))
			.setAllowedTag(TagKey.of(RegistryKeys.BIOME, AurorasDeco.id("feature/fallen_trees/forest")));

	public static final PlacedFeatureMetadata FALLEN_BIRCH_FOREST_TREES = new PlacedFeatureMetadata(id("vegetation/fallen_tree/birch_forest"))
			.addAllowedBiomeCategoryTag(BiomeTags.FOREST)
			.addAllowedNeighborFeature(new Identifier("birch_tall"))
			.setAllowedTag(TagKey.of(RegistryKeys.BIOME, AurorasDeco.id("feature/fallen_trees/birch_forest")));

	public static final PlacedFeatureMetadata FALLEN_SPRUCE_TAIGA_TREES = new PlacedFeatureMetadata(id("vegetation/fallen_tree/spruce_taiga"))
			.addAllowedBiomeCategoryTag(BiomeTags.TAIGA)
			.addAllowedPrecipitation(Biome.Precipitation.RAIN)
			.addAllowedNeighborFeature(new Identifier("trees_taiga"))
			.setAllowedTag(TagKey.of(RegistryKeys.BIOME, AurorasDeco.id("feature/fallen_trees/spruce_taiga")));

	public static final PlacedFeatureMetadata SNOWY_FALLEN_SPRUCE_TAIGA_TREES = new PlacedFeatureMetadata(
			id("vegetation/fallen_tree/snowy_spruce_taiga"))
			.addAllowedBiomeCategoryTag(BiomeTags.TAIGA)
			.addAllowedPrecipitation(Biome.Precipitation.SNOW)
			.addAllowedNeighborFeature(new Identifier("trees_taiga"))
			.setAllowedTag(TagKey.of(RegistryKeys.BIOME, AurorasDeco.id("feature/fallen_trees/snowy_spruce_taiga")));

	public static final PlacedFeatureMetadata FALLEN_TREES_OLD_GROWTH_SPRUCE_TAIGA = new PlacedFeatureMetadata(
			id("vegetation/fallen_tree/old_growth_spruce_taiga"))
			.addAllowedBiomeCategoryTag(BiomeTags.TAIGA)
			.addAllowedNeighborFeature(new Identifier("trees_old_growth_spruce_taiga"))
			.addAllowedNeighborFeature(new Identifier("trees_old_growth_pine_taiga"))
			.setAllowedTag(TagKey.of(RegistryKeys.BIOME, AurorasDeco.id("feature/fallen_trees/old_growth_spruce_taiga")));

	public static final PlacedFeatureMetadata FALLEN_TREES_SPARSE_JUNGLE = new PlacedFeatureMetadata(
			id("vegetation/fallen_tree/sparse_jungle"))
			.addAllowedBiomeCategoryTag(BiomeTags.JUNGLE)
			.addAllowedPrecipitation(Biome.Precipitation.RAIN)
			.addAllowedNeighborFeature(new Identifier("trees_sparse_jungle"))
			.setAllowedTag(TagKey.of(RegistryKeys.BIOME, AurorasDeco.id("feature/fallen_trees/sparse_jungle")));


	private static Identifier id(String name) {
		return AurorasDeco.id(name);
	}
}
