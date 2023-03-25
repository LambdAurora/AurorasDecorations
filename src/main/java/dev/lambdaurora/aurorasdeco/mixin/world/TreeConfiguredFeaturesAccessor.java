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

package dev.lambdaurora.aurorasdeco.mixin.world;

import net.minecraft.util.Holder;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TreeConfiguredFeatures.class)
public interface TreeConfiguredFeaturesAccessor {
	@Mutable
	@Accessor("AZALEA_TREE")
	static void setAzaleaTree(Holder<ConfiguredFeature<?, ?>> feature) {
		throw new IllegalStateException("Mixin injection failed");
	}

	@Invoker
	static TreeFeatureConfig.Builder invokeOak() {
		throw new IllegalStateException("Mixin injection failed");
	}

	@Invoker
	static TreeFeatureConfig.Builder invokeBirch() {
		throw new IllegalStateException("Mixin injection failed");
	}

	@Invoker
	static TreeFeatureConfig.Builder invokeFancyOak() {
		throw new IllegalStateException("Mixin injection failed");
	}
}
