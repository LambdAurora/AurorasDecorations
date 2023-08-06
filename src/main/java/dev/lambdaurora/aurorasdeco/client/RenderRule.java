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

package dev.lambdaurora.aurorasdeco.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Represents a render rule.
 * <p>
 * Render rules can be used to change the default rendering of an item in a specific context like shelves.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
public record RenderRule(List<Model> models) {
	private static final Logger LOGGER = LogUtils.getLogger();
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
		var itemId = Registries.ITEM.getId(stack.getItem());

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

	public static void addModels(ModelLoadingPlugin.Context context) {
		ITEM_RULES.values().stream().flatMap(rule -> rule.models().stream()).map(Model::modelId).forEach(context::addModels);
		TAG_RULES.values().stream().flatMap(rule -> rule.models().stream()).map(Model::modelId).forEach(context::addModels);
	}

	public record Model(ModelIdentifier modelId, @Nullable Block restrictedBlock, @Nullable TagKey<Block> restrictedBlockTag) {
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
						restrictedBlock = Registries.BLOCK.get(blockId);
					}
				} else if (restrict.has("tag")) {
					var blockId = Identifier.tryParse(restrict.get("tag").getAsString());
					if (blockId == null) {
						LOGGER.error("Failed to parse tag identifier in render rule {}.", manifest);
					} else {
						restrictedBlockTag = TagKey.of(RegistryKeys.BLOCK, blockId);
					}
				}
			}
			return new Model(new ModelIdentifier(modelId, "inventory"), restrictedBlock, restrictedBlockTag);
		}
	}

	public record RenderRules(Map<Identifier, RenderRule> itemRules, Map<TagKey<Item>, RenderRule> tagRules) {
	}

	public static class Reloader implements SimpleResourceReloader<RenderRules> {
		public static final Identifier ID = AurorasDeco.id("render_rules");

		@Override
		public @NotNull Identifier getQuiltId() {
			return ID;
		}

		@Override
		public CompletableFuture<RenderRules> load(ResourceManager manager, Profiler profiler, Executor executor) {
			return CompletableFuture.supplyAsync(() -> {
				var rules = new RenderRules(new Object2ObjectOpenHashMap<>(), new Object2ObjectOpenHashMap<>());

				manager.findResources("aurorasdeco/render_rules", path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
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
							if (match.has("item")) {
								rules.itemRules.put(Identifier.tryParse(match.get("item").getAsString()), renderRule);
							} else if (match.has("items")) {
								var array = match.getAsJsonArray("items");
								for (var item : array) {
									rules.itemRules.put(Identifier.tryParse(item.getAsString()), renderRule);
								}
							} else if (match.has("tag")) {
								var tagId = Identifier.tryParse(match.get("tag").getAsString());
								rules.tagRules.put(TagKey.of(RegistryKeys.ITEM, tagId), renderRule);
							}
						}
					} catch (Exception e) {
						LOGGER.error("Failed to read render rule {}. {}", id, e);
					}
				});

				return rules;
			}, executor);
		}

		@Override
		public CompletableFuture<Void> apply(RenderRules data, ResourceManager manager, Profiler profiler, Executor executor) {
			return CompletableFuture.runAsync(() -> {
				ITEM_RULES.clear();
				ITEM_RULES.putAll(data.itemRules);
				TAG_RULES.clear();
				TAG_RULES.putAll(data.tagRules);
			}, executor);
		}
	}
}
