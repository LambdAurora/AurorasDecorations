/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.world.gen;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.world.gen.feature.AurorasDecoFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents the dynamic world generation part of Aurora's Decorations.
 * <p>
 * Features may be defined in data-packs then dynamically injected into the corresponding biomes if the features exist.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicWorldGen {
	private static final DynamicWorldGen INSTANCE = new DynamicWorldGen();
	private final List<Entry> dynamicWorldGenerations = new ArrayList<>();

	private DynamicWorldGen() {
		this.register(new Entry(AurorasDeco.id("swamp"), BiomeSelectors.includeByKey(BiomeKeys.SWAMP), List.of(
				AurorasDecoFeatures.SWAMP_DUCKWEED,
				AurorasDecoFeatures.SWAMP_GIANT_MUSHROOMS,
				AurorasDecoFeatures.SWAMP_SMALL_DRIPLEAF
		)));
	}

	private void register(Entry entry) {
		this.dynamicWorldGenerations.add(entry);
	}

	public static void setupRegistries(DynamicRegistryManager registryManager) {
		AurorasDeco.debug("Setting up dynamic registries...");

		for (var entry : INSTANCE.dynamicWorldGenerations) {
			entry.setupRegistries(registryManager);
		}
	}

	private static class Entry {
		private final Identifier modificationsId;
		private final Predicate<BiomeSelectionContext> biomeSelector;
		private final List<RegistryKey<PlacedFeature>> toCheck;
		private final List<RegistryKey<PlacedFeature>> toAdd = new ArrayList<>();

		public Entry(Identifier modificationsId, Predicate<BiomeSelectionContext> biomeSelector, List<RegistryKey<PlacedFeature>> toCheck) {
			this.modificationsId = modificationsId;
			this.biomeSelector = biomeSelector;
			this.toCheck = toCheck;
			this.setupApplication();
		}

		private void setupRegistries(DynamicRegistryManager registryManager) {
			this.toAdd.clear();

			var placedFeatureRegistry = (MutableRegistry<PlacedFeature>) registryManager.get(Registry.PLACED_FEATURE_KEY);

			for (var feature : this.toCheck) {
				if (placedFeatureRegistry.contains(feature)) {
					this.toAdd.add(feature);
				}
			}
		}

		private void setupApplication() {
			BiomeModifications.create(this.modificationsId)
					.add(ModificationPhase.ADDITIONS, this.biomeSelector, context -> {
						for (var feature : this.toAdd) {
							context.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, feature);
						}
					});
		}
	}
}
