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
import dev.lambdaurora.aurorasdeco.world.gen.feature.config.FallenTreeFeatureConfig;
import dev.lambdaurora.aurorasdeco.world.gen.foliage.JacarandaFoliagePlacer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class AurorasDecoFeatures {
	private AurorasDecoFeatures() {
		throw new UnsupportedOperationException("AurorasDecoFeatures only contains static definitions.");
	}

	/* Features */

	public static final SimplePlantFeature SIMPLE_PLANT = register("simple_plant", new SimplePlantFeature(SimplePlantFeature.Config.CODEC));
	public static final AquaticSurfacePatchFeature AQUATIC_SURFACE_PATCH = register("aquatic_surface_patch",
			new AquaticSurfacePatchFeature(AquaticSurfacePatchFeature.Config.CODEC));
	public static final FallenTreeFeature FALLEN_TREE = register("fallen_tree", new FallenTreeFeature(FallenTreeFeatureConfig.CODEC));
	public static final WaySignFeature WAY_SIGN = register("way_sign", new WaySignFeature(WaySignFeature.Config.CODEC));

	/* Placed Features */

	public static final RegistryKey<PlacedFeature> SWAMP_DUCKWEED = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, AurorasDeco.id("swamp/duckweed")
	);
	public static final RegistryKey<PlacedFeature> SWAMP_GIANT_MUSHROOMS = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, AurorasDeco.id("swamp/giant_mushrooms")
	);
	public static final RegistryKey<PlacedFeature> SWAMP_SMALL_DRIPLEAF = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, AurorasDeco.id("swamp/small_dripleaf")
	);

	private static <C extends FeatureConfig, F extends Feature<C>> F register(String name, F feature) {
		return Registry.register(Registries.FEATURE, AurorasDeco.id(name), feature);
	}

	public static void poke() {
		JacarandaFoliagePlacer.poke();
	}
}
