/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.hook.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.lambdaurora.aurorasdeco.mixin.TransformSmithingRecipeAccessor;
import dev.lambdaurora.aurorasdeco.recipe.ActuallyGoodTransformSmithingRecipe;

import java.util.List;

public class ActuallyGoodTransformSmithingEmiRecipe extends AuroraEmiRecipe<ActuallyGoodTransformSmithingRecipe> {
	private final EmiIngredient base;
	private final EmiIngredient addition;
	private final EmiStack output;

	public ActuallyGoodTransformSmithingEmiRecipe(ActuallyGoodTransformSmithingRecipe recipe) {
		super(recipe);

		this.base = EmiIngredient.of(((TransformSmithingRecipeAccessor) recipe).getBase());
		this.addition = EmiIngredient.of(((TransformSmithingRecipeAccessor) recipe).getAddition());
		this.output = EmiStack.of(recipe.getResult(null));
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.SMITHING;
	}


	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(this.base, this.addition);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(this.output);
	}

	@Override
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.PLUS, 27, 3);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 75, 1);
		widgets.addSlot(this.base, 0, 0);
		widgets.addSlot(this.addition, 49, 0);
		widgets.addSlot(this.output, 107, 0).recipeContext(this);
	}
}
