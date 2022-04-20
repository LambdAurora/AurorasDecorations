/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

import com.google.gson.JsonObject;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

/**
 * Represents a recipe made from an explosion.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ExplodingRecipe extends CuttingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public ExplodingRecipe(Identifier id, String group, Ingredient input, ItemStack output) {
		super(AurorasDecoRegistry.EXPLODING_RECIPE_TYPE, AurorasDecoRegistry.EXPLODING_RECIPE_SERIALIZER,
				id, group, input, output);
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		return this.input.test(inv.getStack(0));
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(Items.TNT);
	}

	public static class Serializer implements QuiltRecipeSerializer<ExplodingRecipe> {
		private Serializer() {
		}

		@Override
		public ExplodingRecipe read(Identifier identifier, JsonObject json) {
			var group = JsonHelper.getString(json, "group", "");
			Ingredient ingredient;
			if (JsonHelper.hasArray(json, "ingredient")) {
				ingredient = Ingredient.fromJson(JsonHelper.getArray(json, "ingredient"));
			} else {
				ingredient = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
			}

			var resultId = JsonHelper.getString(json, "result");
			int count = JsonHelper.getInt(json, "count");
			var itemStack = new ItemStack(Registry.ITEM.get(new Identifier(resultId)), count);
			return new ExplodingRecipe(identifier, group, ingredient, itemStack);
		}

		@Override
		public ExplodingRecipe read(Identifier identifier, PacketByteBuf buf) {
			var string = buf.readString(32767);
			var ingredient = Ingredient.fromPacket(buf);
			var itemStack = buf.readItemStack();
			return new ExplodingRecipe(identifier, string, ingredient, itemStack);
		}

		@Override
		public void write(PacketByteBuf buf, ExplodingRecipe recipe) {
			buf.writeString(recipe.group);
			recipe.input.write(buf);
			buf.writeItemStack(recipe.getOutput());
		}

		@Override
		public JsonObject toJson(ExplodingRecipe recipe) {
			var root = new JsonObject();
			root.addProperty("type", AurorasDecoRegistry.EXPLODING_RECIPE_ID.toString());
			if (!recipe.group.isEmpty())
				root.addProperty("group", recipe.group);

			root.add("ingredient", recipe.input.toJson());
			root.addProperty("result", Registry.ITEM.getId(recipe.getOutput().getItem()).toString());
			root.addProperty("count", recipe.getOutput().getCount());

			return root;
		}
	}
}
