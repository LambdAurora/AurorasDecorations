/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.TestOnly;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectors;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

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

	private DynamicWorldGen() {
		this.registerDynamicModifications(AurorasDeco.id("swamp"), BiomeSelectors.includeByKey(BiomeKeys.SWAMP), List.of(
				AurorasDecoFeatures.SWAMP_DUCKWEED,
				AurorasDecoFeatures.SWAMP_GIANT_MUSHROOMS,
				AurorasDecoFeatures.SWAMP_SMALL_DRIPLEAF
		));
	}

	private void registerDynamicModifications(Identifier modificationsId, Predicate<BiomeSelectionContext> selector, List<RegistryKey<PlacedFeature>> toPlace) {
		BiomeModifications.create(modificationsId)
				.add(ModificationPhase.ADDITIONS, selector, (selectionContext, context) -> {
					for (var feature : toPlace) {
						if (selectionContext.doesPlacedFeatureExist(feature)) {
							context.getGenerationSettings().addFeature(GenerationStep.Feature.VEGETAL_DECORATION, feature);
						}
					}
				});
	}

	@TestOnly
	public static void setupRegistries(DynamicRegistryManager registryManager) {
		//AurorasDeco.debug("Setting up dynamic registries...");
	}

	public static void init() {}
}
