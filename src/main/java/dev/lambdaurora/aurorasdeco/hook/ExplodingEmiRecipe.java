package dev.lambdaurora.aurorasdeco.hook;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.lambdaurora.aurorasdeco.recipe.ExplodingRecipe;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

public class ExplodingEmiRecipe implements EmiRecipe {
	
	//TODO: would be nice to have this abstracted a little
	private final Identifier id;
	//private final EmiStack tnt = EmiStack.of(Blocks.TNT);
	private final EmiIngredient input;
	private final EmiStack output;
	
	public ExplodingEmiRecipe(ExplodingRecipe recipe) {
		this.id = recipe.getId();
		input = EmiIngredient.of(recipe.getIngredients().get(0));
		output = EmiStack.of(recipe.getOutput());
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return EmiHooks.EXPLODING;
	}
	

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input);
		// Should also be appending an explosive somehow... hmm
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
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
		// Flair: Should render a fake lit TNT entity and fake item entity; maybe a fake entity widget? 
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 1);
		widgets.addSlot(input, 0, 0);
		widgets.addSlot(output, 58, 0).recipeContext(this);
	}

}

