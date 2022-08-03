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

import net.minecraft.sound.MusicSound;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.SpawnSettings;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OverworldBiomeCreator.class)
public interface OverworldBiomeCreatorAccessor {
	@Invoker
	static void invokeAddBasicFeatures(GenerationSettings.Builder generationSettings) {
		throw new IllegalStateException("Mixin injection failed");
	}

	@Invoker("method_39152")
	static Biome invokeCreateBiome(
			Biome.Precipitation precipitation,
			float temperature,
			float downfall,
			SpawnSettings.Builder spawnSettings,
			GenerationSettings.Builder generationSettings,
			@Nullable MusicSound music
	) {
		throw new IllegalStateException("Mixin injection failed");
	}
}
