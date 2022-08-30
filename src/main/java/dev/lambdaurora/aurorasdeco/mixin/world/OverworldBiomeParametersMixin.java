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

package dev.lambdaurora.aurorasdeco.mixin.world;

import com.mojang.datafixers.util.Pair;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoBiomes;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.OverworldBiomeParameters;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

// @TODO Use a biomes API as soon as possible.
@Mixin(OverworldBiomeParameters.class)
public abstract class OverworldBiomeParametersMixin {
	@Shadow
	protected abstract void addSurfaceBiomeTo(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters, MultiNoiseUtil.ParameterRange temperature, MultiNoiseUtil.ParameterRange humidity, MultiNoiseUtil.ParameterRange continentalness, MultiNoiseUtil.ParameterRange erosion, MultiNoiseUtil.ParameterRange weirdness, float offset, RegistryKey<Biome> biome);

	@Shadow
	@Final
	private MultiNoiseUtil.ParameterRange COAST_CONTINENTALNESS;

	@Shadow
	@Final
	private MultiNoiseUtil.ParameterRange FAR_INLAND_CONTINENTALNESS;

	@Shadow
	@Final
	private MultiNoiseUtil.ParameterRange NEAR_INLAND_CONTINENTALNESS;

	@Shadow
	@Final
	private MultiNoiseUtil.ParameterRange[] EROSIONS;

	@Shadow
	@Final
	private MultiNoiseUtil.ParameterRange MID_INLAND_CONTINENTALNESS;

	@Inject(
			method = "addPeaksTo",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/biome/source/util/OverworldBiomeParameters;pickRegularBiome(IILnet/minecraft/world/biome/source/util/MultiNoiseUtil$ParameterRange;)Lnet/minecraft/util/registry/RegistryKey;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onAddPeaksTo(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters, MultiNoiseUtil.ParameterRange weirdness,
			CallbackInfo ci,
			int temperatureIndex, MultiNoiseUtil.ParameterRange temperature,
			int humidityIndex, MultiNoiseUtil.ParameterRange humidity) {
		if (temperatureIndex == 2 && humidityIndex == 0) {
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.COAST_CONTINENTALNESS, this.NEAR_INLAND_CONTINENTALNESS),
					MultiNoiseUtil.ParameterRange.combine(this.EROSIONS[2], this.EROSIONS[3]),
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.COAST_CONTINENTALNESS, this.FAR_INLAND_CONTINENTALNESS),
					this.EROSIONS[4],
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.COAST_CONTINENTALNESS, this.FAR_INLAND_CONTINENTALNESS),
					this.EROSIONS[6],
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
		}
	}

	@Inject(
			method = "addHighBiomesTo",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/biome/source/util/OverworldBiomeParameters;pickRegularBiome(IILnet/minecraft/world/biome/source/util/MultiNoiseUtil$ParameterRange;)Lnet/minecraft/util/registry/RegistryKey;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onAddHighBiomesTo(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters, MultiNoiseUtil.ParameterRange weirdness,
			CallbackInfo ci,
			int temperatureIndex, MultiNoiseUtil.ParameterRange temperature,
			int humidityIndex, MultiNoiseUtil.ParameterRange humidity) {
		if (temperatureIndex == 2 && humidityIndex == 0) {
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					this.COAST_CONTINENTALNESS,
					MultiNoiseUtil.ParameterRange.combine(this.EROSIONS[0], this.EROSIONS[1]),
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.COAST_CONTINENTALNESS, this.NEAR_INLAND_CONTINENTALNESS),
					MultiNoiseUtil.ParameterRange.combine(this.EROSIONS[2], this.EROSIONS[3]),
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.COAST_CONTINENTALNESS, this.FAR_INLAND_CONTINENTALNESS),
					this.EROSIONS[4],
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
		}
	}

	@Inject(
			method = "addMidBiomesTo",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/biome/source/util/OverworldBiomeParameters;pickRegularBiome(IILnet/minecraft/world/biome/source/util/MultiNoiseUtil$ParameterRange;)Lnet/minecraft/util/registry/RegistryKey;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onAddMidBiomesTo(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters, MultiNoiseUtil.ParameterRange weirdness,
			CallbackInfo ci,
			int temperatureIndex, MultiNoiseUtil.ParameterRange temperature,
			int humidityIndex, MultiNoiseUtil.ParameterRange humidity) {
		if (temperatureIndex == 2 && humidityIndex == 0) {
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					this.NEAR_INLAND_CONTINENTALNESS, this.EROSIONS[2], weirdness, 0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.COAST_CONTINENTALNESS, this.NEAR_INLAND_CONTINENTALNESS),
					this.EROSIONS[3],
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);

			if (weirdness.max() < 0L) {
				this.addSurfaceBiomeTo(
						parameters, temperature, humidity,
						MultiNoiseUtil.ParameterRange.combine(this.NEAR_INLAND_CONTINENTALNESS, this.FAR_INLAND_CONTINENTALNESS),
						this.EROSIONS[4],
						weirdness,
						0.f,
						AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
				);
			} else {
				this.addSurfaceBiomeTo(
						parameters, temperature, humidity,
						MultiNoiseUtil.ParameterRange.combine(this.COAST_CONTINENTALNESS, this.FAR_INLAND_CONTINENTALNESS),
						this.EROSIONS[4],
						weirdness,
						0.f,
						AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
				);
				this.addSurfaceBiomeTo(
						parameters, temperature, humidity,
						this.COAST_CONTINENTALNESS, this.EROSIONS[6], weirdness, 0.f,
						AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
				);
			}
		}
	}

	@Inject(
			method = "addLowBiomesTo",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/biome/source/util/OverworldBiomeParameters;pickRegularBiome(IILnet/minecraft/world/biome/source/util/MultiNoiseUtil$ParameterRange;)Lnet/minecraft/util/registry/RegistryKey;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onAddLowBiomesTo(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters, MultiNoiseUtil.ParameterRange weirdness,
			CallbackInfo ci,
			int temperatureIndex, MultiNoiseUtil.ParameterRange temperature,
			int humidityIndex, MultiNoiseUtil.ParameterRange humidity) {
		if (temperatureIndex == 2 && humidityIndex == 0) {
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					this.NEAR_INLAND_CONTINENTALNESS,
					MultiNoiseUtil.ParameterRange.combine(this.EROSIONS[2], this.EROSIONS[3]),
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.NEAR_INLAND_CONTINENTALNESS, this.FAR_INLAND_CONTINENTALNESS),
					this.EROSIONS[4],
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
			this.addSurfaceBiomeTo(
					parameters, temperature, humidity,
					MultiNoiseUtil.ParameterRange.combine(this.MID_INLAND_CONTINENTALNESS, this.FAR_INLAND_CONTINENTALNESS),
					this.EROSIONS[5],
					weirdness,
					0.f,
					AurorasDecoBiomes.LAVENDER_PLAINS.getKey()
			);
		}
	}
}
