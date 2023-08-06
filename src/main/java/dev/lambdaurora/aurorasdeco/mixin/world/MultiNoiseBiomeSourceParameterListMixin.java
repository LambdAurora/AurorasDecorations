/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoBiomes;
import dev.lambdaurora.aurorasdeco.world.gen.DynamicWorldGen;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.HolderProvider;
import net.minecraft.registry.RegistrySetBuilder;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.util.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiNoiseBiomeSourceParameterList.class)
public class MultiNoiseBiomeSourceParameterListMixin {
	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/biome/util/MultiNoiseBiomeSourceParameterList$Preset$C_pttrytil;apply(Ljava/util/function/Function;)Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$ParameterRangeList;"
			)
	)
	private void aurorasdeco$onInitHead(MultiNoiseBiomeSourceParameterList.Preset preset, HolderProvider<Biome> holderProvider, CallbackInfo ci) {
		if (holderProvider instanceof HolderLookup<Biome> lookup) {
			DynamicWorldGen.markCanInjectBiomes(lookup.holders().anyMatch(holder -> holder.isRegistryKey(AurorasDecoBiomes.LAVENDER_PLAINS)));
		} else if (!holderProvider.getClass().getName().contains(RegistrySetBuilder.class.getName())) {
			try {
				holderProvider.getHolderOrThrow(AurorasDecoBiomes.LAVENDER_PLAINS);
				DynamicWorldGen.markCanInjectBiomes(true);
			} catch (IllegalStateException e) {
				/* Ignored */
			}
		}
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void aurorasdeco$onInitTail(MultiNoiseBiomeSourceParameterList.Preset preset, HolderProvider<Biome> holderProvider, CallbackInfo ci) {
		DynamicWorldGen.unmarkCanInjectBiomes();
	}
}
