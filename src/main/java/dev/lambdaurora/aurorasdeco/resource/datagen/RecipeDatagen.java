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

import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.recipe.api.RecipeManagerHelper;

public final class RecipeDatagen {
	public static Recipe<?> registerRecipe(Recipe<?> recipe, String category) {
		RecipeManagerHelper.registerStaticRecipe(recipe);

		var advancementId = new Identifier(recipe.getId().getNamespace(), "recipes/" + category + "/" + recipe.getId().getPath());
		AdvancementDatagen.register(advancementId, () -> AdvancementDatagen.simpleRecipeUnlock(recipe));

		return recipe;
	}
}
