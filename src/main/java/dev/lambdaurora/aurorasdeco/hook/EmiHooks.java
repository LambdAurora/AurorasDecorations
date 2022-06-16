package dev.lambdaurora.aurorasdeco.hook;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.lambdaurora.aurorasdeco.recipe.ExplodingRecipe;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

public class EmiHooks implements EmiPlugin {
	public static EmiRecipeCategory WOODCUTTING = new EmiRecipeCategory(new Identifier("aurorasdeco:woodcutting"),
			EmiStack.of(AurorasDecoRegistry.SAWMILL_BLOCK));
	public static EmiRecipeCategory EXPLODING = new EmiRecipeCategory(new Identifier("aurorasdeco:exploding"),
			EmiStack.of(Blocks.TNT));
	
	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(WOODCUTTING);
		registry.addCategory(EXPLODING);
		
		registry.addWorkstation(WOODCUTTING, EmiStack.of(AurorasDecoRegistry.SAWMILL_BLOCK));
		registry.addWorkstation(EXPLODING, EmiStack.of(Blocks.TNT));
		
		for (WoodcuttingRecipe recipe : registry.getRecipeManager().listAllOfType(AurorasDecoRegistry.WOODCUTTING_RECIPE_TYPE)) {
			registry.addRecipe(new WoodcuttingEmiRecipe(recipe));
		}
		
		for (ExplodingRecipe recipe : registry.getRecipeManager().listAllOfType(AurorasDecoRegistry.EXPLODING_RECIPE_TYPE)) {
			registry.addRecipe(new ExplodingEmiRecipe(recipe));
		}
	}

}
