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

package dev.lambdaurora.aurorasdeco.hook;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.client.screen.PainterPaletteScreen;
import dev.lambdaurora.aurorasdeco.hook.emi.ActuallyGoodTransformSmithingEmiRecipe;
import dev.lambdaurora.aurorasdeco.hook.emi.ExplodingEmiRecipe;
import dev.lambdaurora.aurorasdeco.hook.emi.WoodcuttingEmiRecipe;
import dev.lambdaurora.aurorasdeco.recipe.ActuallyGoodTransformSmithingRecipe;
import dev.lambdaurora.aurorasdeco.recipe.ExplodingRecipe;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public final class EmiHooks implements EmiPlugin {
	public static EmiRecipeCategory WOODCUTTING = new EmiRecipeCategory(AurorasDeco.id("woodcutting"),
			EmiStack.of(AurorasDecoRegistry.SAWMILL_BLOCK)
	);
	public static EmiRecipeCategory EXPLODING = new EmiRecipeCategory(AurorasDeco.id("exploding"),
			EmiStack.of(Blocks.TNT)
	);

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(WOODCUTTING);
		registry.addCategory(EXPLODING);

		registry.addWorkstation(WOODCUTTING, EmiStack.of(AurorasDecoRegistry.SAWMILL_BLOCK));
		registry.addWorkstation(EXPLODING, EmiStack.of(Blocks.TNT));

		for (WoodcuttingRecipe recipe : registry.getRecipeManager().listAllOfType(AurorasDecoRegistry.WOODCUTTING_RECIPE_TYPE)) {
			if (recipe.getIngredients().get(0).getMatchingStacks().length == 0) continue;

			registry.addRecipe(new WoodcuttingEmiRecipe(recipe));
		}

		for (ExplodingRecipe recipe : registry.getRecipeManager().listAllOfType(AurorasDecoRegistry.EXPLODING_RECIPE_TYPE)) {
			registry.addRecipe(new ExplodingEmiRecipe(recipe));
		}

		for (SmithingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMITHING)) {
			if (recipe instanceof ActuallyGoodTransformSmithingRecipe betterRecipe) {
				registry.removeRecipes(emiRecipe -> emiRecipe instanceof EmiSmithingRecipe && emiRecipe.getId().equals(recipe.getId()));
				registry.addRecipe(new ActuallyGoodTransformSmithingEmiRecipe(betterRecipe));
			}
		}

		registry.addExclusionArea(PainterPaletteScreen.class, (screen, consumer) -> {
			consumer.accept(new Bounds(screen.getBackgroundX(), screen.getBackgroundY() + 10, 24, 86));
		});
	}
}
