package dev.lambdaurora.aurorasdeco.hook;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import net.minecraft.util.Identifier;

public class WoodcuttingEmiRecipe implements EmiRecipe {
	private final Identifier id;
	private final EmiIngredient input;
	private final EmiStack output;
	
	public WoodcuttingEmiRecipe(WoodcuttingRecipe recipe) {
		this.id = recipe.getId();
		input = EmiIngredient.of(recipe.getIngredients().get(0));
		output = EmiStack.of(recipe.getOutput());
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return EmiHooks.WOODCUTTING;
	}
	

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}
	
	@Override
	public Identifier getId() {
		return id;
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
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 1);
		widgets.addSlot(input, 0, 0);
		widgets.addSlot(output, 58, 0).recipeContext(this);
	}

}
