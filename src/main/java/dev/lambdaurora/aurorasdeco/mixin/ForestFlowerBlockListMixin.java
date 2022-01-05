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

package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;

@Mixin(VegetationConfiguredFeatures.class)
public class ForestFlowerBlockListMixin {
	@ModifyArg(method = "method_39726", at = @At(value = "INVOKE", target = "Ljava/util/List;of([Ljava/lang/Object;)Ljava/util/List;"), remap = false)
	private static Object[] addAurorasDecoFlowers(Object[] states) {
		var newStates = Arrays.copyOf((BlockState[]) states, states.length + AurorasDecoPlants.FLOWER_FOREST_PLANTS.size());
		for (int i = 0; i < AurorasDecoPlants.FLOWER_FOREST_PLANTS.size(); i++) {
			newStates[states.length + i] = AurorasDecoPlants.FLOWER_FOREST_PLANTS.get(i);
		}
		return newStates;
	}
}
