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

package dev.lambdaurora.aurorasdeco.client.model;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents an unbaked model which is using a variant map to adapt to depending on block states.
 *
 * @param <T> the type of the unbaked models of the variants
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class UnbakedVariantModel<T extends UnbakedModel> implements UnbakedModel {
	private final Block block;
	private final Map<String, T> unbakedVariantMap;
	private final List<Property<?>> ignoreProperties;

	public UnbakedVariantModel(Block block, Map<String, T> variantMap, List<Property<?>> ignoreProperties) {
		this.block = block;
		this.unbakedVariantMap = variantMap;
		this.ignoreProperties = ignoreProperties;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return this.unbakedVariantMap.values().stream().flatMap(model -> model.getModelDependencies().stream()).collect(Collectors.toSet());
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
	                                                           Set<Pair<String, String>> unresolvedTextureReferences) {
		return this.unbakedVariantMap.values().stream()
				.flatMap(model -> model.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences).stream())
				.collect(Collectors.toSet());
	}

	@Override
	public @Nullable BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		var map = new Object2ReferenceOpenHashMap<String, BlockState>();
		var models = new Reference2ObjectOpenHashMap<BlockState, BakedModel>();

		this.block.getStateManager().getStates().forEach(state -> {
			map.put(this.propertyMapToString(state.getEntries()), state);
		});

		this.unbakedVariantMap.forEach((variant, model) -> {
			models.put(map.get(variant), model.bake(loader, textureGetter, rotationContainer, modelId));
		});

		return new BakedVariantModel(models);
	}

	private String propertyMapToString(Map<Property<?>, Comparable<?>> map) {
		var builder = new StringBuilder();

		for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
			if (this.ignoreProperties.contains(entry.getKey())) {
				continue;
			}

			if (builder.length() != 0) {
				builder.append(',');
			}

			Property<?> property = entry.getKey();
			builder.append(property.getName());
			builder.append('=');
			builder.append(propertyValueToString(property, entry.getValue()));
		}

		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String propertyValueToString(Property<T> property, Comparable<?> value) {
		return property.name((T) value);
	}
}
