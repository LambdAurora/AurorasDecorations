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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.world.biome.AurorasDecoOverworldBiomeCreator;
import dev.lambdaurora.aurorasdeco.world.gen.feature.AurorasDecoVegetationPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
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

	public static final RegistryKey<Biome> LAVENDER_PLAINS_KEY = key("lavender_plains");
	public static final Biome LAVENDER_PLAINS = register(LAVENDER_PLAINS_KEY, AurorasDecoOverworldBiomeCreator.createLavenderPlains());

	private static RegistryKey<Biome> key(String name) {
		return RegistryKey.of(Registry.BIOME_KEY, AurorasDeco.id(name));
	}

	private static Biome register(RegistryKey<Biome> key, Biome biome) {
		return BuiltinRegistries.set(BuiltinRegistries.BIOME, key, biome);
	}

	static void init() {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld()
						.and(AurorasDecoVegetationPlacedFeatures.FALLEN_FOREST_TREES.getBiomeSelectionPredicate()),
				GenerationStep.Feature.VEGETAL_DECORATION,
				AurorasDecoVegetationPlacedFeatures.FALLEN_FOREST_TREES.getKey()
		);
	}
}
