package dev.lambdaurora.aurorasdeco.hook;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public abstract class AurorasDecoEmiRecipe implements EmiRecipe {
	private final EmiRecipeCategory category;
	private final Identifier id;
	protected List<EmiIngredient> input = List.of();
	protected List<EmiIngredient> catalysts = List.of();
	protected List<EmiStack> output = List.of();
	
	public AurorasDecoEmiRecipe(EmiRecipeCategory category, Recipe<?> recipe) {
		this.category = category;
		this.id = recipe.getId();
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return category;
	}
	
	@Override
	public @Nullable Identifier getId() {
		return id;
	}
	
	@Override
	public List<EmiIngredient> getInputs() {
		return input;
	}
	
	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}
	
	@Override
	public List<EmiStack> getOutputs() {
		return output;
	}
}
