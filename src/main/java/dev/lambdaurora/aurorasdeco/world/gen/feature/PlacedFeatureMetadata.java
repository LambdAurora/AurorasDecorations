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

package dev.lambdaurora.aurorasdeco.world.gen.feature;

import net.minecraft.tag.TagKey;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacementModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a registrable {@link PlacedFeature} to which metadata is assigned to ease biome selection.
 */
public class PlacedFeatureMetadata {
	private final RegistryKey<PlacedFeature> key;
	private final PlacedFeature feature;
	private final List<TagKey<Biome>> allowedCategoryTags = new ArrayList<>();
	private final List<Biome.Precipitation> allowedPrecipitations = new ArrayList<>();
	private final List<RegistryKey<ConfiguredFeature<?, ?>>> allowedNeighborFeatures = new ArrayList<>();
	private TagKey<Biome> allowedTag;

	public PlacedFeatureMetadata(Identifier key,
			Holder<? extends ConfiguredFeature<?, ?>> configuredFeature,
			List<PlacementModifier> modifiers) {
		this.key = RegistryKey.of(Registry.PLACED_FEATURE_KEY, key);
		this.feature = new PlacedFeature(Holder.upcast(configuredFeature), List.copyOf(modifiers));
	}

	public RegistryKey<PlacedFeature> getKey() {
		return this.key;
	}

	public PlacedFeature getFeature() {
		return this.feature;
	}

	@SafeVarargs
	public final PlacedFeatureMetadata addAllowedBiomeCategoryTag(TagKey<Biome>... categories) {
		this.allowedCategoryTags.addAll(Arrays.asList(categories));
		return this;
	}

	public Predicate<BiomeSelectionContext> getAllowedBiomeCategoriesPredicate() {
		if (this.allowedCategoryTags.isEmpty())
			return biomeSelectionContext -> true;

		Predicate<BiomeSelectionContext> last = null;

		for (var tag : this.allowedCategoryTags) {
			var p = BiomeSelectors.isIn(tag);

			if (last == null) {
				last = p;
				continue;
			}

			last = last.or(p);
		}

		return last;
	}

	public PlacedFeatureMetadata addAllowedPrecipitation(Biome.Precipitation... precipitations) {
		this.allowedPrecipitations.addAll(Arrays.asList(precipitations));
		return this;
	}

	public Predicate<BiomeSelectionContext> getAllowedPrecipitationsPredicate() {
		if (this.allowedPrecipitations.isEmpty())
			return biomeSelectionContext -> true;
		return biomeSelectionContext -> {
			for (var precipitation : this.allowedPrecipitations) {
				if (biomeSelectionContext.getBiome().getPrecipitation() == precipitation)
					return true;
			}

			return false;
		};
	}

	public PlacedFeatureMetadata addAllowedNeighborFeature(Identifier key) {
		return this.addAllowedNeighborFeature(RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, key));
	}

	public PlacedFeatureMetadata addAllowedNeighborFeature(RegistryKey<ConfiguredFeature<?, ?>> key) {
		this.allowedNeighborFeatures.add(key);
		return this;
	}

	public Predicate<BiomeSelectionContext> getAllowedNeighborFeaturesPredicate() {
		if (this.allowedNeighborFeatures.isEmpty())
			return biomeSelectionContext -> true;

		return biomeSelectionContext -> {
			for (var feature : this.allowedNeighborFeatures) {
				if (biomeSelectionContext.hasFeature(feature)) {
					return true;
				}
			}

			return false;
		};
	}

	public PlacedFeatureMetadata setAllowedTag(TagKey<Biome> tag) {
		this.allowedTag = tag;
		return this;
	}

	public Predicate<BiomeSelectionContext> getTagPredicate() {
		if (this.allowedTag != null)
			return BiomeSelectors.isIn(this.allowedTag);
		else
			return biomeSelectionContext -> false;
	}

	public Predicate<BiomeSelectionContext> getBiomeSelectionPredicate() {
		if (this.allowedCategoryTags.isEmpty() && this.allowedPrecipitations.isEmpty() && this.allowedNeighborFeatures.isEmpty())
			return this.getTagPredicate();

		return this.getAllowedBiomeCategoriesPredicate()
				.and(this.getAllowedPrecipitationsPredicate()
						.and(this.getAllowedNeighborFeaturesPredicate())
				)
				.or(this.getTagPredicate());
	}

	public PlacedFeatureMetadata register() {
		Registry.register(BuiltinRegistries.PLACED_FEATURE, this.key, this.feature);
		return this;
	}
}
