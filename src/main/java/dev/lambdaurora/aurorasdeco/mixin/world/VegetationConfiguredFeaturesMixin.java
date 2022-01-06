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

package dev.lambdaurora.aurorasdeco.mixin.world;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(VegetationConfiguredFeatures.class)
public class VegetationConfiguredFeaturesMixin {
	@ModifyArg(method = "method_39726", at = @At(value = "INVOKE", target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"), remap = false)
	private static Object[] onCreateFlowerForestFlowerList(Object[] states) {
		var newStates = Arrays.copyOf((BlockState[]) states, states.length + AurorasDecoPlants.FLOWER_FOREST_PLANTS.size());
		for (int i = 0; i < AurorasDecoPlants.FLOWER_FOREST_PLANTS.size(); i++) {
			newStates[states.length + i] = AurorasDecoPlants.FLOWER_FOREST_PLANTS.get(i);
		}
		return newStates;
	}

	@ModifyArg(
			method = "method_39723",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/gen/stateprovider/DualNoiseBlockStateProvider;<init>(Lnet/minecraft/util/math/Range;Lnet/minecraft/util/math/noise/DoublePerlinNoiseSampler$NoiseParameters;FJLnet/minecraft/util/math/noise/DoublePerlinNoiseSampler$NoiseParameters;FLjava/util/List;)V"
			),
			index = 6,
			remap = false
	)
	private static List<BlockState> onCreateMeadownFlowerList(List<BlockState> states) {
		var newStates = new ArrayList<>(states);
		newStates.addAll(AurorasDecoPlants.DAFFODIL.getStateManager().getStates());
		return newStates;
	}
}
