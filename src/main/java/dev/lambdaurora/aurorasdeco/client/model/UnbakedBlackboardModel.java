/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.resource.Material;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

@ClientOnly
public class UnbakedBlackboardModel implements AuroraUnbakedModel {
	protected final UnbakedModel baseModel;

	public static UnbakedBlackboardModel of(ModelIdentifier id, UnbakedModel baseModel,
			BiConsumer<Identifier, UnbakedModel> modelConsumer) {
		if (id.getPath().contains("glass")) {
			return new UnbakedGlassboardModel(id, baseModel,
					MinecraftClient.getInstance().getResourceManager(), new ModelVariantMap.DeserializationContext(), modelConsumer
			);
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
	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		this.baseModel.resolveParents(models);
	}

	@Override
	public BakedModel bake(
			ModelBaker modelBaker, Function<Material, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId
	) {
		return new BakedBlackboardModel(this.bakeBaseModel(modelBaker, textureGetter, rotationContainer, modelId));
	}

	protected BakedModel bakeBaseModel(
			ModelBaker loader, Function<Material, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId
	) {
		return this.baseModel.bake(loader, textureGetter, rotationContainer, modelId);
	}
}
