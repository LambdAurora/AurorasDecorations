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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public final class AurorasDecoTreeConfiguredFeatures {
	private AurorasDecoTreeConfiguredFeatures() {
		throw new UnsupportedOperationException("AurorasDecoTreeConfiguredFeatures only contains static definitions.");
	}

	public static final RegistryKey<ConfiguredFeature<?, ?>> JACARANDA_TREE = key("tree/jacaranda");
	public static final RegistryKey<ConfiguredFeature<?, ?>> JACARANDA_TREE_BEES_015 = key("tree/jacaranda_bees_015");
	public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWERING_JACARANDA_TREE = key("tree/flowering_jacaranda");
	public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWERING_JACARANDA_TREE_BEES_015 = key("tree/flowering_jacaranda_bees_015");

	private static RegistryKey<ConfiguredFeature<?, ?>> key(String key) {
		return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, AurorasDeco.id(key));
	}
}
