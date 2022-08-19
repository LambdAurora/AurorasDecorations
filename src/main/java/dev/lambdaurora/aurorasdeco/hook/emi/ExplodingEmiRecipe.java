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

package dev.lambdaurora.aurorasdeco.hook.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.lambdaurora.aurorasdeco.hook.EmiHooks;
import dev.lambdaurora.aurorasdeco.recipe.ExplodingRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ExplodingEmiRecipe extends AuroraEmiCuttingRecipe<ExplodingRecipe> {
	private static final Text TOOLTIP = Text.translatable("tooltip.aurorasdeco.emi.as_item_entity").formatted(Formatting.GREEN);

	public ExplodingEmiRecipe(ExplodingRecipe recipe) {
		super(recipe);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return EmiHooks.EXPLODING;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(this.input);
		// Could also be appending an explosive somehow... hmm
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public int getDisplayWidth() {
		return 76;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		// Flair: Should render a fake lit TNT entity and fake item entity; maybe a fake entity widget? 
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 1);
		widgets.addSlot(this.input, 0, 0).appendTooltip(TOOLTIP);
		widgets.addSlot(this.output, 58, 0).recipeContext(this);
	}
}

