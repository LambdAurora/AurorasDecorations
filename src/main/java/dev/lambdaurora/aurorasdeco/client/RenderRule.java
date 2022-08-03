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

package dev.lambdaurora.aurorasdeco.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a render rule.
 * <p>
 * Render rules can be used to change the default rendering of an item in a specific context like shelves.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public record RenderRule(List<Model> models) {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<Identifier, RenderRule> ITEM_RULES = new Object2ObjectOpenHashMap<>();
	private static final Map<TagKey<Item>, RenderRule> TAG_RULES = new Object2ObjectOpenHashMap<>();

	public @Nullable Model getModelId(ItemStack stack, BlockState state, long seed) {
		if (this.models.size() == 1) {
			Model model = this.models.get(0);
			return model.test(stack, state) ? model : null;
		} else {
			final int i = Math.abs(Objects.hash(stack.getCount(), stack.getName().getString(), seed) % this.models.size());
			int actualI = i;

			Model model;
			do {
				model = this.models.get(actualI);

				actualI++;
				if (actualI >= this.models.size())
					actualI = 0;

				if (actualI == i)
					return null;
			} while (!model.test(stack, state));
			return model;
		}
	}

	public @Nullable BakedModel getModel(ItemStack stack, BlockState state, long seed) {
		var model = this.getModelId(stack, state, seed);
		return model == null ? null : model.getModel();
	}

	public static @Nullable RenderRule getRenderRule(ItemStack stack) {
		var itemId = Registry.ITEM.getId(stack.getItem());

		var rule = ITEM_RULES.get(itemId);
		if (rule != null) {
			return rule;
		}

		for (var entry : TAG_RULES.entrySet()) {
			if (stack.isIn(entry.getKey())) {
				return entry.getValue();
			}
		}

		return null;
	}

	public static BakedModel getModel(ItemStack stack, BlockState state, World world, long seed) {
		BakedModel model = null;

		var rule = RenderRule.getRenderRule(stack);
		if (rule != null)
			model = rule.getModel(stack, state, seed);

		if (model == null)
			return MinecraftClient.getInstance().getItemRenderer().getHeldItemModel(stack, world, null, 0);
		return model;
	}

	public static void reload(ResourceManager manager, Consumer<Identifier> out) {
		ITEM_RULES.clear();
		TAG_RULES.clear();

		manager.findResources("aurorasdeco_render_rules", path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
			try (var reader = new InputStreamReader(resource.open())) {
				var element = JsonParser.parseReader(reader);
				if (element.isJsonObject()) {
					var root = element.getAsJsonObject();

					var models = new ArrayList<Model>();
					var modelsJson = root.getAsJsonArray("models");
					modelsJson.forEach(modelElement -> {
						var model = Model.readModelPredicate(id, modelElement);
						if (model != null)
							models.add(model);
					});

					if (models.isEmpty())
						return;

					var renderRule = new RenderRule(models);

					var match = root.getAsJsonObject("match");
					boolean success = false;
					if (match.has("item")) {
						ITEM_RULES.put(Identifier.tryParse(match.get("item").getAsString()), renderRule);
						success = true;
					} else if (match.has("items")) {
						var array = match.getAsJsonArray("items");
						for (var item : array) {
							ITEM_RULES.put(Identifier.tryParse(item.getAsString()), renderRule);
						}
						success = true;
					} else if (match.has("tag")) {
						var tagId = Identifier.tryParse(match.get("tag").getAsString());
						TAG_RULES.put(TagKey.of(Registry.ITEM_KEY, tagId), renderRule);
						success = true;
					}

					if (success)
						models.forEach(model -> out.accept(model.getModelId()));
				}
			} catch (Exception e) {
				LOGGER.error("Failed to read render rule {}. {}", id, e);
			}
		});
	}

	public record Model(ModelIdentifier modelId, @Nullable Block restrictedBlock, @Nullable TagKey<Block> restrictedBlockTag) {
		public ModelIdentifier getModelId() {
			return this.modelId;
		}

		public boolean test(ItemStack stack, BlockState state) {
			if (this.restrictedBlock != null) {
				return state.isOf(this.restrictedBlock);
			} else if (this.restrictedBlockTag != null) {
				return state.isIn(this.restrictedBlockTag);
			}
			return true;
		}

		public BakedModel getModel() {
			return MinecraftClient.getInstance().getBakedModelManager().getModel(this.modelId);
		}

		public static @Nullable Model readModelPredicate(Identifier manifest, JsonElement json) {
			if (json.isJsonPrimitive()) {
				var modelId = Identifier.tryParse(json.getAsString());
				if (modelId == null) {
					LOGGER.error("Failed to parse model identifier {} in render rule {}.", json.getAsString(), manifest);
					return null;
				}

				return new Model(new ModelIdentifier(modelId, "inventory"), null, null);
			}

			var object = json.getAsJsonObject();

			if (!object.has("model")) {
				LOGGER.error("Failed to parse model entry in render rule {}, missing model field.", manifest);
				return null;
			}

			var modelId = Identifier.tryParse(object.get("model").getAsString());
			if (modelId == null) {
				LOGGER.error("Failed to parse model identifier {} in render rule {}.", json.getAsString(), manifest);
				return null;
			}

			Block restrictedBlock = null;
			TagKey<Block> restrictedBlockTag = null;
			if (object.has("restrict_to")) {
				var restrict = object.getAsJsonObject("restrict_to");
				if (restrict.has("block")) {
					var blockId = Identifier.tryParse(restrict.get("block").getAsString());
					if (blockId == null) {
						LOGGER.error("Failed to parse block identifier in render rule {}.", manifest);
					} else {
						restrictedBlock = Registry.BLOCK.get(blockId);
					}
				} else if (restrict.has("tag")) {
					var blockId = Identifier.tryParse(restrict.get("tag").getAsString());
					if (blockId == null) {
						LOGGER.error("Failed to parse tag identifier in render rule {}.", manifest);
					} else {
						restrictedBlockTag = TagKey.of(Registry.BLOCK_KEY, blockId);
					}
				}
			}
			return new Model(new ModelIdentifier(modelId, "inventory"), restrictedBlock, restrictedBlockTag);
		}
	}
}
