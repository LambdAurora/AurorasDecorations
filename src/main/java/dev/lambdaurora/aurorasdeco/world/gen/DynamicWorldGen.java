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

package dev.lambdaurora.aurorasdeco.world.gen;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.world.gen.feature.AurorasDecoFeatures;
import net.minecraft.registry.Holder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.registry.api.event.DynamicRegistryManagerSetupContext;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectors;
import org.quiltmc.qsl.worldgen.biome.api.ModificationPhase;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public class DynamicWorldGen{
	private static final List<String> WAY_SIGNS = List.of("birch", "desert", "oak", "taiga");
	private static final DynamicWorldGen INSTANCE = new DynamicWorldGen();
	private boolean canInjectBiomes = false;

	private DynamicWorldGen() {
		this.registerDynamicModifications(AurorasDeco.id("swamp"), BiomeSelectors.includeByKey(Biomes.SWAMP), List.of(
				AurorasDecoFeatures.SWAMP_DUCKWEED,
				AurorasDecoFeatures.SWAMP_GIANT_MUSHROOMS,
				AurorasDecoFeatures.SWAMP_SMALL_DRIPLEAF
		));

		var waySigns = BiomeModifications.create(AurorasDeco.id("way_signs"));
		for (var waySign : WAY_SIGNS) {
			waySigns.add(ModificationPhase.ADDITIONS,
					BiomeSelectors.isIn(TagKey.of(RegistryKeys.BIOME, AurorasDeco.id("feature/way_sign/" + waySign))),
					(selectionContext, context) -> {
						context.getGenerationSettings().addFeature(GenerationStep.Feature.SURFACE_STRUCTURES,
								RegistryKey.of(RegistryKeys.PLACED_FEATURE, AurorasDeco.id("way_sign/" + waySign))
						);
					}
			);
		}
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

	public static void markCanInjectBiomes(boolean canInjectBiomes) {
		INSTANCE.canInjectBiomes = canInjectBiomes;
	}

	public static void unmarkCanInjectBiomes() {
		INSTANCE.canInjectBiomes = false;
	}

	public static boolean canInjectBiomes() {
		return INSTANCE.canInjectBiomes;
	}

	public static void init() {}
}
