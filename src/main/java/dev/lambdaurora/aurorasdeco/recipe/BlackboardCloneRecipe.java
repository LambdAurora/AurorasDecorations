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

package dev.lambdaurora.aurorasdeco.recipe;

import dev.lambdaurora.aurorasdeco.blackboard.Blackboard;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.recipe.CraftingCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * Represents the blackboard clone recipe.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardCloneRecipe extends SpecialCraftingRecipe {
	private static final Ingredient INPUT = Ingredient.ofTag(AurorasDecoTags.BLACKBOARD_ITEMS);
	private static final Ingredient OUTPUT = Ingredient.ofItems(
			AurorasDecoRegistry.BLACKBOARD_BLOCK,
			AurorasDecoRegistry.CHALKBOARD_BLOCK,
			AurorasDecoRegistry.GLASSBOARD_BLOCK
	);

	public BlackboardCloneRecipe(Identifier id, CraftingCategory craftingCategory) {
		super(id, craftingCategory);
	}

	@Override
	public boolean matches(RecipeInputInventory inv, World world) {
		boolean hasInput = false, hasOutput = false;
		int count = 0;

		for (int slot = 0; slot < inv.size(); ++slot) {
			var stack = inv.getStack(slot);

			if (INPUT.test(stack)) {
				if (OUTPUT.test(stack) && !this.isInput(stack))
					hasOutput = true;
				else if (this.isInput(stack))
					hasInput = true;
				count++;
			}
		}
		return hasInput && hasOutput && count == 2;
	}

	@Override
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager registryManager) {
		Blackboard blackboard = null;
		ItemStack output = null;
		Text customName = null;

		for (int slot = 0; slot < inv.size(); ++slot) {
			var craftStack = inv.getStack(slot);
			if (!craftStack.isEmpty()) {
				if (OUTPUT.test(craftStack) && !this.isInput(craftStack)) {
					output = craftStack;
				} else if (this.isInput(craftStack)) {
					var nbt = BlockItem.getBlockEntityNbtFromStack(craftStack);
					blackboard = Blackboard.fromNbt(nbt);
					if (craftStack.hasCustomName())
						customName = craftStack.getName();
				}
			}
		}


		var out = output.copy();
		out.setCount(1);
		var nbt = AuroraUtil.getOrCreateBlockEntityNbt(out, AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE);
		blackboard.writeNbt(nbt);

		if (customName != null)
			out.setCustomName(customName);

		return out;
	}

	private boolean isInput(ItemStack stack) {
		var nbt = BlockItem.getBlockEntityNbtFromStack(stack);
		if (nbt != null) {
			if (nbt.contains("pixels", NbtElement.BYTE_ARRAY_TYPE)) {
				byte[] pixels = nbt.getByteArray("pixels");
				for (byte pixel : pixels) {
					if (pixel != 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public DefaultedList<ItemStack> getRemainder(RecipeInputInventory craftingInventory) {
		DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingInventory.size(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); ++i) {
			ItemStack invStack = craftingInventory.getStack(i);
			if (!invStack.isEmpty()) {
				if (invStack.getItem().hasRecipeRemainder()) {
					defaultedList.set(i, new ItemStack(invStack.getItem().getRecipeRemainder()));
				} else if (this.isInput(invStack)) {
					ItemStack remainder = invStack.copy();
					remainder.setCount(1);
					defaultedList.set(i, remainder);
				}
			}
		}

		return defaultedList;
	}


	@Override
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return AurorasDecoRegistry.BLACKBOARD_CLONE_RECIPE_SERIALIZER;
	}
}
