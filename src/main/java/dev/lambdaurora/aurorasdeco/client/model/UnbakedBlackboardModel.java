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
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.blackboard.Blackboard;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class UnbakedBlackboardModel implements AuroraUnbakedModel {
	private static final SpriteIdentifier WHITE = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
			AurorasDeco.id("special/white"));

	protected final UnbakedModel baseModel;

	public static UnbakedBlackboardModel of(ModelIdentifier id, UnbakedModel baseModel, ResourceManager resourceManager,
			ModelVariantMap.DeserializationContext variantMapDeserializationContext,
			BiConsumer<Identifier, UnbakedModel> modelConsumer) {
		if (id.getPath().contains("glass")) {
			return new UnbakedGlassboardModel(id, baseModel, resourceManager, variantMapDeserializationContext, modelConsumer);
		} else {
			return new UnbakedBlackboardModel(baseModel);
		}
	}

	protected UnbakedBlackboardModel(UnbakedModel baseModel) {
		this.baseModel = baseModel;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return this.baseModel.getModelDependencies();
	}

	@Override
	public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter,
			Set<Pair<String, String>> unresolvedTextureReferences) {
		var textures = new ObjectOpenHashSet<>(this.baseModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
		textures.add(WHITE);
		return textures;
	}

	@Override
	public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
			Identifier modelId) {
		return new BakedBlackboardModel(this.bakeBaseModel(loader, textureGetter, rotationContainer, modelId));
	}

	protected BakedModel bakeBaseModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer,
			Identifier modelId) {
		Blackboard.setWhiteSprite(textureGetter.apply(WHITE));
		return this.baseModel.bake(loader, textureGetter, rotationContainer, modelId);
	}
}
