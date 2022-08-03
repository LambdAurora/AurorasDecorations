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

package dev.lambdaurora.aurorasdeco.world.biome;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.world.OverworldBiomeCreatorAccessor;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a biome.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AurorasDecoBiome {
	private final RegistryKey<Biome> key;

	protected AurorasDecoBiome(RegistryKey<Biome> key) {
		this.key = key;
	}

	public RegistryKey<Biome> getKey() {
		return this.key;
	}

	public Biome.Precipitation getPrecipitation() {
		return Biome.Precipitation.RAIN;
	}

	public abstract float getTemperature();

	public abstract float getDownfall();

	public @Nullable MusicSound getMusic() {
		return null;
	}

	/**
	 * Initializes some of biome-specific hooks.
	 */
	public abstract void init();

	/**
	 * Creates the biome object.
	 *
	 * @param generationSettings the generation settings
	 * @param spawnSettings the spawn settings
	 */
	protected abstract void initSettings(GenerationSettings.Builder generationSettings, SpawnSettings.Builder spawnSettings);

	public Biome create() {
		var generationSettings = new GenerationSettings.Builder();
		var spawnSettings = new SpawnSettings.Builder();

		this.initSettings(generationSettings, spawnSettings);

		return OverworldBiomeCreatorAccessor.invokeCreateBiome(
				this.getPrecipitation(),
				this.getTemperature(),
				this.getDownfall(),
				spawnSettings,
				generationSettings,
				this.getMusic()
		);
	}

	protected void addBasicFeatures(GenerationSettings.Builder generationSettings) {
		OverworldBiomeCreatorAccessor.invokeAddBasicFeatures(generationSettings);
	}

	protected static RegistryKey<Biome> key(String name) {
		return RegistryKey.of(Registry.BIOME_KEY, AurorasDeco.id(name));
	}
}
