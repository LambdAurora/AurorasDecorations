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

package dev.lambdaurora.aurorasdeco.registry;

import dev.lambdaurora.aurorasdeco.world.biome.AurorasDecoBiome;
import dev.lambdaurora.aurorasdeco.world.biome.LavenderPlainsBiome;
import dev.lambdaurora.aurorasdeco.world.gen.feature.AurorasDecoVegetationPlacedFeatures;
import dev.lambdaurora.aurorasdeco.world.gen.feature.PlacedFeatureMetadata;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;

/**
 * Contains the different biomes definitions added in Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoBiomes {
	private AurorasDecoBiomes() {
		throw new UnsupportedOperationException("AurorasDecoBiomes only contains static definitions.");
	}

	public static final LavenderPlainsBiome LAVENDER_PLAINS = register(new LavenderPlainsBiome());

	private static <T extends AurorasDecoBiome> T register(T biome) {
		register(biome.getKey(), biome.create());
		biome.init();
		return biome;
	}

	private static void register(RegistryKey<Biome> key, Biome biome) {
		BuiltinRegistries.set(BuiltinRegistries.BIOME, key, biome);
	}

	static void init() {
		addBiomeModification(GenerationStep.Feature.VEGETAL_DECORATION, AurorasDecoVegetationPlacedFeatures.FALLEN_FOREST_TREES);
		addBiomeModification(GenerationStep.Feature.VEGETAL_DECORATION, AurorasDecoVegetationPlacedFeatures.FALLEN_BIRCH_FOREST_TREES);
		addBiomeModification(GenerationStep.Feature.VEGETAL_DECORATION, AurorasDecoVegetationPlacedFeatures.FALLEN_SPRUCE_TAIGA_TREES);
		addBiomeModification(GenerationStep.Feature.VEGETAL_DECORATION, AurorasDecoVegetationPlacedFeatures.SNOWY_FALLEN_SPRUCE_TAIGA_TREES);
		addBiomeModification(GenerationStep.Feature.VEGETAL_DECORATION, AurorasDecoVegetationPlacedFeatures.FALLEN_OLD_GROWTH_SPRUCE_TAIGA_TREES);
	}

	private static void addBiomeModification(GenerationStep.Feature step, PlacedFeatureMetadata metadata) {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld()
						.and(metadata.getBiomeSelectionPredicate()),
				step, metadata.getKey()
		);
	}
}
