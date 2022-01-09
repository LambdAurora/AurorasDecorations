package dev.lambdaurora.aurorasdeco.world.biome;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.world.OverworldBiomeCreatorAccessor;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.sound.MusicSound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
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

	public abstract Biome.Category getCategory();

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
				this.getCategory(),
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

	protected void addStructure(Identifier key) {
		this.addStructure(RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, key));
	}

	protected void addStructure(RegistryKey<ConfiguredStructureFeature<?, ?>> key) {
		BiomeModifications.addStructure(BiomeSelectors.includeByKey(this.getKey()), key);
	}

	protected static RegistryKey<Biome> key(String name) {
		return RegistryKey.of(Registry.BIOME_KEY, AurorasDeco.id(name));
	}
}
