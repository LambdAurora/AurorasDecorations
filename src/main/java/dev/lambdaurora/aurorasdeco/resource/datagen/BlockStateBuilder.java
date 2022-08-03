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

package dev.lambdaurora.aurorasdeco.resource.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.lambdaurora.aurorasdeco.client.AurorasDecoClient;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class BlockStateBuilder {
	private final JsonObject json = new JsonObject();
	private final Identifier id;
	private final JsonObject variantsJson = new JsonObject();
	private final Map<String, JsonArray> variants = new Object2ObjectOpenHashMap<>();

	public BlockStateBuilder(Block block) {
		var id = Registry.BLOCK.getId(block);
		this.id = new Identifier(id.getNamespace(), "blockstates/" + id.getPath());

		this.json.add("variants", variantsJson);
	}

	public BlockStateBuilder addToVariant(String variant, Identifier modelId) {
		return this.addToVariant(variant, modelId, 0);
	}

	public BlockStateBuilder addToVariant(String variant, Identifier modelId, int y) {
		return this.addToVariant(variant, new StateModel(modelId, y));
	}

	public BlockStateBuilder addToVariant(String variant, StateModel model) {
		this.variants.computeIfAbsent(variant, v -> {
			var array = new JsonArray();
			this.variantsJson.add(v, array);
			return array;
		}).add(model.toJson());

		return this;
	}

	public JsonObject toJson() {
		return this.json;
	}

	public void register() {
		AurorasDecoClient.RESOURCE_PACK.putJson(ResourceType.CLIENT_RESOURCES, this.id, this.toJson());
	}
}
