/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecipeDatagen {
	public static final Logger LOGGER = LogManager.getLogger("aurorasdeco:datagen/recipe");

	private static final Map<RecipeType<?>, List<Recipe<?>>> RECIPES = new Object2ObjectOpenHashMap<>();
	private static final Map<Recipe<?>, String> RECIPES_CATEGORIES = new Object2ObjectOpenHashMap<>();

	private RecipeDatagen() {
		throw new UnsupportedOperationException("RecipeDatagen only contains static definitions.");
	}

	public static void applyRecipes(Map<Identifier, JsonElement> map,
	                                Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> builderMap) {
		var recipeCount = new int[]{0};
		RECIPES.forEach((key, recipes) -> {
			var recipeBuilder = builderMap.computeIfAbsent(key, o -> ImmutableMap.builder());

			recipes.forEach(recipe -> {
				if (!map.containsKey(recipe.getId())) {
					recipeBuilder.put(recipe.getId(), recipe);
					recipeCount[0]++;
				}
			});
		});

		LOGGER.info("Loaded {} additional recipes", recipeCount[0]);
	}

	public static Recipe<?> registerRecipe(Recipe<?> recipe, String category) {
		var recipes = RECIPES.computeIfAbsent(recipe.getType(), recipeType -> new ArrayList<>());

		for (var other : recipes) {
			if (other.getId().equals(recipe.getId()))
				return other;
		}

		recipes.add(recipe);
		RECIPES_CATEGORIES.put(recipe, category);

		var advancementId = new Identifier(recipe.getId().getNamespace(), "recipes/" + category + "/" + recipe.getId().getPath());
		AdvancementDatagen.register(advancementId, () -> AdvancementDatagen.simpleRecipeUnlock(recipe));

		return recipe;
	}
}
