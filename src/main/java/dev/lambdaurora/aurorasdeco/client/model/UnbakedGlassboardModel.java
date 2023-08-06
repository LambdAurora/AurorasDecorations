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

import com.mojang.logging.LogUtils;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.resource.Material;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@ClientOnly
public class UnbakedGlassboardModel extends UnbakedBlackboardModel {
	private static final Logger LOGGER = LogUtils.getLogger();
	static final int LEFT_UP_MASK = 0b00000001;
	static final int UP_MASK = 0b00000010;
	static final int RIGHT_UP_MASK = 0b00000100;
	static final int LEFT_MASK = 0b00001000;
	static final int RIGHT_MASK = 0b00010000;
	static final int LEFT_DOWN_MASK = 0b00100000;
	static final int DOWN_MASK = 0b01000000;
	static final int RIGHT_DOWN_MASK = 0b10000000;
	static final int ALL_MASK = LEFT_UP_MASK | UP_MASK | RIGHT_UP_MASK | LEFT_MASK | RIGHT_MASK | LEFT_DOWN_MASK | DOWN_MASK | RIGHT_DOWN_MASK;

	private final Int2ObjectMap<Identifier> identifiers = new Int2ObjectOpenHashMap<>();
	private final String variant;

	UnbakedGlassboardModel(
			ModelIdentifier id, UnbakedModel baseModel, ResourceManager resourceManager,
			ModelVariantMap.DeserializationContext deserializationContext, BiConsumer<Identifier, UnbakedModel> modelConsumer
	) {
		super(baseModel);
		this.variant = id.getVariant();

		String prefix = "";
		if (id.getPath().contains("waxed")) {
			prefix = "waxed/";
		}

		Block block = Registries.BLOCK.get(AurorasDeco.id(id.getPath()));

		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				var identifier = new ModelIdentifier(AurorasDeco.id("glassboard/" + prefix + "glassboard_" + corner.getShortName() + type.getSuffix()), variant);

				this.identifiers.put(this.getCornerDataIndex(corner, type), identifier);

				if (block != Blocks.AIR) {
					var resourceId = AurorasDeco.id("blockstates/" + identifier.getPath() + ".json");
					var resource = resourceManager.getResource(resourceId);

					if (resource.isEmpty()) {
						LOGGER.warn("Could not load glassboard model part (" + corner + ", " + type + "): could not locate the blockstate file.");
					} else {
						try (var reader = new InputStreamReader(resource.get().open())) {

							deserializationContext.setStateFactory(block.getStateManager());
							var map = ModelVariantMap.fromJson(deserializationContext, reader);

							map.getVariantMap().forEach((variant, model) -> modelConsumer.accept(
									new ModelIdentifier(identifier.getNamespace(), identifier.getPath(), this.variant.replaceFirst("facing=\\w+", variant)),
									model
							));
						} catch (IOException e) {
							LOGGER.warn("Could not load glassboard model part (" + corner + ", " + type + "):", e);
						}
					}
				}
			}
		}
	}

	@Override
	public BakedModel bake(
			ModelBaker modelBaker, Function<Material, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId
	) {
		var baseModel = this.bakeBaseModel(modelBaker, textureGetter, rotationContainer, modelId);

		return new BakedGlassboardModel(baseModel, this.bakeAllConnectingModels(modelBaker, textureGetter, rotationContainer, modelId, baseModel));
	}

	private Int2ObjectMap<List<BakedModel>> bakeAllConnectingModels(
			ModelBaker baker, Function<Material, Sprite> textureGetter, ModelBakeSettings rotationContainer,
			Identifier modelId, BakedModel baseModel
	) {
		var map = new Int2ObjectOpenHashMap<List<BakedModel>>();

		map.put(0, List.of(baseModel));

		var bakedModels = new Int2ObjectOpenHashMap<BakedModel>();
		for (var corner : Corner.CORNERS) {
			for (var type : Type.TYPES) {
				int id = this.getCornerDataIndex(corner, type);
				bakedModels.put(id, baker.getModel(this.identifiers.get(id))
						.bake(baker, textureGetter, rotationContainer, modelId));
			}
		}

		for (int i = 1; i <= ALL_MASK; i++) {
			var models = new ArrayList<BakedModel>();

			boolean left = (i & LEFT_MASK) != 0;
			boolean up = (i & UP_MASK) != 0;
			boolean right = (i & RIGHT_MASK) != 0;
			boolean down = (i & DOWN_MASK) != 0;

			// Left Up
			if (left && up) {
				if ((i & LEFT_UP_MASK) != 0)
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_UP, Type.CENTER)));
				else
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_UP, Type.INNER)));
			} else if (left) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_UP, Type.HORIZONTAL)));
			} else if (up) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_UP, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_UP, Type.NONE)));
			}

			// Right Up
			if (right && up) {
				if ((i & RIGHT_UP_MASK) != 0)
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_UP, Type.CENTER)));
				else
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_UP, Type.INNER)));
			} else if (right) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_UP, Type.HORIZONTAL)));
			} else if (up) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_UP, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_UP, Type.NONE)));
			}

			// Right Down
			if (right && down) {
				if ((i & RIGHT_DOWN_MASK) != 0)
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_DOWN, Type.CENTER)));
				else
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_DOWN, Type.INNER)));
			} else if (right) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_DOWN, Type.HORIZONTAL)));
			} else if (down) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_DOWN, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.RIGHT_DOWN, Type.NONE)));
			}

			// Left Down
			if (left && down) {
				if ((i & LEFT_DOWN_MASK) != 0)
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_DOWN, Type.CENTER)));
				else
					models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_DOWN, Type.INNER)));
			} else if (left) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_DOWN, Type.HORIZONTAL)));
			} else if (down) {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_DOWN, Type.VERTICAL)));
			} else {
				models.add(bakedModels.get(this.getCornerDataIndex(Corner.LEFT_DOWN, Type.NONE)));
			}

			map.put(i, models);
		}

		return map;
	}

	private int getCornerDataIndex(Corner corner, Type type) {
		return corner.ordinal() | (type.ordinal() << 2);
	}

	private enum Corner {
		LEFT_UP("lu"),
		RIGHT_UP("ru"),
		RIGHT_DOWN("rd"),
		LEFT_DOWN("ld");

		public static final List<Corner> CORNERS = List.of(values());
		private final String shortName;

		Corner(String shortName) {
			this.shortName = shortName;
		}

		public String getShortName() {
			return this.shortName;
		}
	}

	private enum Type {
		NONE(""),
		INNER("_inner"),
		HORIZONTAL("_horizontal"),
		VERTICAL("_vertical"),
		CENTER("_center");

		public static final List<Type> TYPES = List.of(values());
		private final String suffix;

		Type(String suffix) {
			this.suffix = suffix;
		}

		public String getSuffix() {
			return this.suffix;
		}
	}
}
