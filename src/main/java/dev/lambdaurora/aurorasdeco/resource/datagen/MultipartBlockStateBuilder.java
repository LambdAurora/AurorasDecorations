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
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceType;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MultipartBlockStateBuilder {
	private final JsonObject json = new JsonObject();
	private final Identifier id;
	private final JsonArray multipartJson = new JsonArray();

	public MultipartBlockStateBuilder(Identifier id) {
		this.id = new Identifier(id.getNamespace(), "blockstates/" + id.getPath());

		this.json.add("multipart", multipartJson);
	}

	public MultipartBlockStateBuilder(Block block) {
		this(Registry.BLOCK.getId(block));
	}

	public MultipartBlockStateBuilder add(StateModel model) {
		var block = new JsonObject();
		block.add("apply", model.toJson());
		this.multipartJson.add(block);
		return this;
	}

	public MultipartBlockStateBuilder addWhen(StateModel model, Property.Value<?>... when) {
		var block = new JsonObject();
		block.add("apply", model.toJson());
		var whenBlock = new JsonObject();
		block.add("when", whenBlock);

		for (var val : when) {
			whenBlock.addProperty(val.property().getName(), val.toString().split("=")[1]);
		}

		this.multipartJson.add(block);

		return this;
	}

	public MultipartBlockStateBuilder addWhenOr(StateModel model, MultipartOr... conditions) {
		var block = new JsonObject();
		block.add("apply", model.toJson());
		var whenBlock = new JsonObject();
		block.add("when", whenBlock);
		var or = new JsonArray();
		whenBlock.add("OR", or);

		for (var condition : conditions) {
			var conditionBlock = new JsonObject();
			or.add(conditionBlock);
			for (var val : condition.when()) {
				conditionBlock.addProperty(val.property().getName(), val.toString().split("=")[1]);
			}
		}

		this.multipartJson.add(block);

		return this;
	}

	public JsonObject toJson() {
		return this.json;
	}

	public void register() {
		AurorasDecoClient.RESOURCE_PACK.putJson(ResourceType.CLIENT_RESOURCES, this.id, this.toJson());
	}
}
