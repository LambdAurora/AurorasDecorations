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

package dev.lambdaurora.aurorasdeco.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.lambdaurora.aurorasdeco.mixin.TransformSmithingRecipeAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.TransformSmithingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

import java.util.stream.Stream;

/**
 * Represents a transform smithing recipe but that's actually good as it allows to dismiss the template item.
 * <p>
 * I just wanted to wax my blackboards Mojang.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.13
 * @since 1.0.0-beta.13
 */
public class ActuallyGoodTransformSmithingRecipe extends TransformSmithingRecipe {
	public static final Serializer SERIALIZER = new Serializer();

	public ActuallyGoodTransformSmithingRecipe(Identifier id, Ingredient base, Ingredient addition, ItemStack result) {
		super(id, Ingredient.EMPTY, base, addition, result);
	}

	@Override
	public boolean isEmpty() {
		return Stream.of(((TransformSmithingRecipeAccessor) this).getBase(), ((TransformSmithingRecipeAccessor) this).getAddition())
				.anyMatch(Ingredient::isEmpty);
	}

	public static class Serializer implements QuiltRecipeSerializer<ActuallyGoodTransformSmithingRecipe> {
		public ActuallyGoodTransformSmithingRecipe read(Identifier id, JsonObject json) {
			Ingredient base = Ingredient.fromJson(JsonHelper.getObject(json, "base"));
			Ingredient addition = Ingredient.fromJson(JsonHelper.getObject(json, "addition"));

			var result = JsonHelper.getObject(json, "result");
			String itemId = JsonHelper.getString(result, "item");
			Item item = Registries.ITEM.getOrEmpty(new Identifier(itemId))
					.orElseThrow(() -> new JsonSyntaxException("Unknown item '" + itemId + "'"));
			int count = JsonHelper.getInt(result, "count", 1);
			if (count < 1) {
				throw new JsonSyntaxException("Invalid output count: " + count);
			} else {
				return new ActuallyGoodTransformSmithingRecipe(id, base, addition, new ItemStack(item, count));
			}
		}

		@Override
		public JsonObject toJson(ActuallyGoodTransformSmithingRecipe recipe) {
			var json = new JsonObject();

			json.add("base", ((TransformSmithingRecipeAccessor) recipe).getBase().toJson());
			json.add("addition", ((TransformSmithingRecipeAccessor) recipe).getAddition().toJson());

			var result = new JsonObject();
			json.add("result", result);
			result.addProperty("result",
					Registries.ITEM.getId(((TransformSmithingRecipeAccessor) recipe).getResult().getItem()).toString()
			);
			result.addProperty("count", ((TransformSmithingRecipeAccessor) recipe).getResult().getCount());

			return json;
		}

		public ActuallyGoodTransformSmithingRecipe read(Identifier id, PacketByteBuf buf) {
			Ingredient base = Ingredient.fromPacket(buf);
			Ingredient addition = Ingredient.fromPacket(buf);
			ItemStack result = buf.readItemStack();
			return new ActuallyGoodTransformSmithingRecipe(id, base, addition, result);
		}

		public void write(PacketByteBuf buf, ActuallyGoodTransformSmithingRecipe recipe) {
			((TransformSmithingRecipeAccessor) recipe).getBase().write(buf);
			((TransformSmithingRecipeAccessor) recipe).getAddition().write(buf);
			buf.writeItemStack(((TransformSmithingRecipeAccessor) recipe).getResult());
		}
	}
}
