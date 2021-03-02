/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
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
    private static final Ingredient INPUT = Ingredient.ofItems(
            AurorasDecoRegistry.BLACKBOARD_BLOCK,
            AurorasDecoRegistry.CHALKBOARD_BLOCK,
            AurorasDecoRegistry.WAXED_BLACKBOARD_BLOCK,
            AurorasDecoRegistry.WAXED_CHALKBOARD_BLOCK
    );
    private static final Ingredient OUTPUT = Ingredient.ofItems(
            AurorasDecoRegistry.BLACKBOARD_BLOCK,
            AurorasDecoRegistry.CHALKBOARD_BLOCK
    );

    public BlackboardCloneRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        boolean hasInput = false, hasOutput = false;
        int count = 0;

        for (int slot = 0; slot < inv.size(); ++slot) {
            ItemStack stack = inv.getStack(slot);

            if (INPUT.test(stack)) {
                if (OUTPUT.test(stack))
                    hasOutput = true;
                if (isInput(stack))
                    hasInput = true;
                count++;
            }
        }
        return hasInput && hasOutput && count == 2;
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        byte[] pixels = new byte[256];
        ItemStack output = null;
        boolean lit = false;
        Text customName = null;

        for (int slot = 0; slot < inv.size(); ++slot) {
            ItemStack craftStack = inv.getStack(slot);
            if (!craftStack.isEmpty()) {
                if (OUTPUT.test(craftStack) && !isInput(craftStack)) {
                    output = craftStack;
                } else if (isInput(craftStack)) {
                    CompoundTag nbt = craftStack.getSubTag("BlockEntityTag");
                    System.arraycopy(nbt.getByteArray("pixels"), 0,
                            pixels, 0, 256);
                    lit = nbt.getBoolean("lit");
                    if (craftStack.hasCustomName())
                        customName = craftStack.getName();
                }
            }
        }


        ItemStack out = output.copy();
        CompoundTag nbt = out.getOrCreateSubTag("BlockEntityTag");
        nbt.putByteArray("pixels", pixels);
        nbt.putBoolean("lit", lit);

        if (customName != null)
            out.setCustomName(customName);

        return out;
    }

    private boolean isInput(ItemStack stack) {
        CompoundTag nbt = stack.getSubTag("BlockEntityTag");
        if (nbt != null) {
            if (nbt.contains("pixels", NbtType.BYTE_ARRAY)) {
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
    public DefaultedList<ItemStack> getRemainingStacks(CraftingInventory craftingInventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingInventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack invStack = craftingInventory.getStack(i);
            if (!invStack.isEmpty()) {
                if (invStack.getItem().hasRecipeRemainder()) {
                    defaultedList.set(i, new ItemStack(invStack.getItem().getRecipeRemainder()));
                } else if (isInput(invStack)) {
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

    @Override
    public ItemStack getOutput() {
        return super.getOutput();
    }
}
