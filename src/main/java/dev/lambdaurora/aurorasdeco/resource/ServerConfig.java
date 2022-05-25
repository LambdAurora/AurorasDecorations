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

package dev.lambdaurora.aurorasdeco.resource;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.world.gen.feature.AurorasDecoFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;

/**
 * Represents a configuration that is available server-wide.
 * <p>
 * The different configuration elements are loaded from data-packs.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ServerConfig {
	private static final ServerConfig INSTANCE = new ServerConfig();
	private final SwampConfig swampConfig = new SwampConfig();

	private ServerConfig() {
	}

	public void setupRegistries(DynamicRegistryManager registryManager) {
		AurorasDeco.debug("Setting up dynamic registries... {}", registryManager);
		this.swampConfig.setupRegistries(registryManager);
	}

	public static ServerConfig get() {
		return INSTANCE;
	}

	public static class SwampConfig {
		private boolean giantMushroomsFeature;

		public SwampConfig() {
			this.setupApplication();
		}

		private void setupRegistries(DynamicRegistryManager registryManager) {
			this.checkGiantMushroomsPlacedFeature(registryManager);
		}

		public void checkGiantMushroomsPlacedFeature(DynamicRegistryManager registryManager) {
			this.giantMushroomsFeature = false;

			var placedFeatureRegistry = (MutableRegistry<PlacedFeature>) registryManager.get(Registry.PLACED_FEATURE_KEY);

			var existing = placedFeatureRegistry.getHolder(AurorasDecoFeatures.SWAMP_GIANT_MUSHROOMS);
			if (existing.isPresent()) {
				this.giantMushroomsFeature = true;
			}
		}

		private void setupApplication() {
			BiomeModifications.create(AurorasDeco.id("swamp"))
					.add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(BiomeKeys.SWAMP), context -> {
						if (this.giantMushroomsFeature) {
							context.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, AurorasDecoFeatures.SWAMP_GIANT_MUSHROOMS);
						}
					});
		}

		public record GiantMushroom(Identifier mushroom, float weight) {
			public RegistryKey<ConfiguredFeature<?, ?>> getRegistryKey() {
				return RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, this.mushroom);
			}
		}
	}
}
