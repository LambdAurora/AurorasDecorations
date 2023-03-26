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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.BiConsumer;

@ClientOnly
public class RestModelManager {
	private final Map<WoodType, RestModelEntry> models = new Reference2ObjectOpenHashMap<>();
	private final ModelLoader modelLoader;

	public RestModelManager(ModelLoader modelLoader) {
		this.modelLoader = modelLoader;
	}

	public RestModelEntry get(WoodType woodType) {
		return this.models.get(woodType);
	}

	public void init(ResourceManager resourceManager, ModelVariantMap.DeserializationContext deserializationContext,
			BiConsumer<Identifier, UnbakedModel> modelRegister) {
		this.models.clear();
		WoodType.forEach(woodType -> {
			var planksComponent = woodType.getComponent(WoodType.ComponentType.PLANKS);
			if (planksComponent == null) return;

			var entry = this.loadModelEntry(woodType, resourceManager, deserializationContext);
			this.models.put(woodType, entry);
			entry.register(modelRegister);
		});
	}

	private RestModelEntry loadModelEntry(WoodType woodType, ResourceManager resourceManager,
			ModelVariantMap.DeserializationContext deserializationContext) {
		var pathName = woodType.getPathName();

		// Bench rest
		UnbakedModel benchRest = null;
		var benchBlock = Registries.BLOCK.get(AurorasDeco.id("bench/" + pathName));
		var benchRestId = AurorasDeco.id("blockstates/bench/" + pathName + "_rest.json");
		if (benchBlock != Blocks.AIR) {
			var resource = resourceManager.getResource(benchRestId);

			if (resource.isEmpty()) {
				AurorasDeco.warn("Failed to load the bench rest models for the {} wood type. Could not locate the model.", woodType);
			} else {
				try (var reader = new InputStreamReader(resource.get().open())) {
					var stateFactory = deserializationContext.getStateFactory();
					deserializationContext.setStateFactory(benchBlock.getStateManager());
					var map = ModelVariantMap.fromJson(deserializationContext, reader);
					benchRest = map.getMultipartModel();
					deserializationContext.setStateFactory(stateFactory);
				} catch (IOException e) {
					AurorasDeco.warn("Failed to load the bench rest models for the {} wood type.", woodType, e);
				}
			}
		}

		return new RestModelEntry(AurorasDeco.id("bench/" + pathName + "_rest"), benchRest);
	}

	public class RestModelEntry {
		private final Identifier benchRestId;
		private UnbakedModel benchRest;
		private BakedModel bakedModel;

		public RestModelEntry(Identifier benchRestId, UnbakedModel benchRest) {
			this.benchRestId = benchRestId;
			this.benchRest = benchRest;
		}

		public void register(BiConsumer<Identifier, UnbakedModel> consumer) {
			if (this.getBenchRest() != null)
				consumer.accept(this.getBenchRestId(), this.getBenchRest());
		}

		public Identifier getBenchRestId() {
			return this.benchRestId;
		}

		public UnbakedModel getBenchRest() {
			return this.benchRest;
		}

		public BakedModel getBakedBenchRest() {
			return modelLoader.getBakedModelMap().get(this.getBenchRestId());
		}
	}
}
