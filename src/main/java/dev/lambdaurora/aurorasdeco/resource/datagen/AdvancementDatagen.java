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

package dev.lambdaurora.aurorasdeco.resource.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static dev.lambdaurora.aurorasdeco.util.AuroraUtil.jsonArray;

public final class AdvancementDatagen {
	private static final Map<Identifier, Supplier<Advancement.Task>> ADVANCEMENT_BUILDERS = new Object2ObjectOpenHashMap<>();
	private static final Map<Identifier, Advancement.Task> ADVANCEMENTS = new Object2ObjectOpenHashMap<>();
	private static final Pattern MISSING_TAG_REGEX = Pattern.compile("Unknown item tag '([a-z0-9_.-]+:[a-z0-9/._-]+)'");

	private AdvancementDatagen() {
		throw new UnsupportedOperationException("AdvancementDatagen only contains static definitions.");
	}

	public static void applyAdvancements(Map<Identifier, Advancement.Task> builder) {
		AurorasDeco.debug("Applying advancement injection...");

		if (!ADVANCEMENT_BUILDERS.isEmpty()) {
			AurorasDeco.debug("Building {} advancements...", ADVANCEMENT_BUILDERS.size());
			var it = ADVANCEMENT_BUILDERS.entrySet().iterator();

			while (it.hasNext()) {
				var advancementBuilder = it.next();

				try {
					ADVANCEMENTS.put(advancementBuilder.getKey(), advancementBuilder.getValue().get());
					it.remove();
				} catch (JsonSyntaxException e) {
					var matcher = MISSING_TAG_REGEX.matcher(e.getMessage());

					if (matcher.find()) {
						var badTag = new Identifier(matcher.group(1));
						AurorasDeco.error("Could not build advancement {} due to a missing item tag {}. " +
										"This probably means the mod {} is very likely to break Vanilla's expectations! Please report this issue!",
								advancementBuilder.getKey(), badTag, badTag.getNamespace());
					} else {
						throw e;
					}
				}
			}
		}

		ADVANCEMENTS.forEach((identifier, task) -> {
			task.parent((Advancement) null);

			builder.put(identifier, task);
		});
	}

	public static Supplier<Advancement.Task> register(Identifier id, Supplier<Advancement.Task> advancement) {
		ADVANCEMENT_BUILDERS.put(id, advancement);
		return advancement;
	}

	public static Advancement.Task simpleRecipeUnlock(Recipe<?> recipe) {
		var advancement = Advancement.Task.create();

		advancement.parent(new Identifier("recipes/root"));
		advancement.rewards(AdvancementRewards.Builder.recipe(recipe.getId()));
		advancement.criteriaMerger(CriterionMerger.OR);
		advancement.criterion("has_self", InventoryChangedCriterion.Conditions.items(recipe.getOutput().getItem()));
		advancement.criterion("has_the_recipe",
				new RecipeUnlockedCriterion.Conditions(EntityPredicate.Extended.EMPTY, recipe.getId())
		);

		int i = 0;
		for (var ingredient : recipe.getIngredients()) {
			if (ingredient.isEmpty())
				continue;
			advancement.criterion("has_" + i, inventoryChangedCriterion(ingredient));
			i++;
		}

		return advancement;
	}

	public static InventoryChangedCriterion.Conditions inventoryChangedCriterion(Ingredient item) {
		var items = new JsonArray();
		var ingredientJson = item.toJson();
		if (ingredientJson instanceof JsonObject ingredientJsonObject) {
			if (ingredientJsonObject.has("item")) {
				var child = new JsonObject();
				child.add("items", jsonArray(ingredientJsonObject.get("item").getAsString()));
				items.add(child);
			} else items.add(ingredientJson);
		}
		return new InventoryChangedCriterion.Conditions(EntityPredicate.Extended.EMPTY,
				NumberRange.IntRange.ANY, NumberRange.IntRange.ANY, NumberRange.IntRange.ANY,
				ItemPredicate.deserializeAll(items));
	}
}
