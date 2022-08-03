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

import com.google.gson.JsonObject;
import dev.lambdaurora.aurorasdeco.client.AurorasDecoClient;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModelBuilder {
	private final JsonObject json = new JsonObject();
	private JsonObject textures;

	public ModelBuilder(Identifier parent) {
		this.json.addProperty("parent", parent.toString());
	}

	public ModelBuilder texture(String name, Identifier id) {
		if (this.textures == null) {
			this.json.add("textures", this.textures = new JsonObject());
		}

		this.textures.addProperty(name, id.toString());

		return this;
	}

	public JsonObject toJson() {
		return this.json;
	}

	public Identifier register(Block block) {
		var id = Registry.BLOCK.getId(block);
		return this.register(new Identifier(id.getNamespace(), "block/" + id.getPath()));
	}

	public Identifier register(Item block) {
		var id = Registry.ITEM.getId(block);
		return this.register(new Identifier(id.getNamespace(), "item/" + id.getPath()));
	}


	public Identifier register(Identifier id) {
		AurorasDecoClient.RESOURCE_PACK.putJson(ResourceType.CLIENT_RESOURCES,
				new Identifier(id.getNamespace(), "models/" + id.getPath()),
				this.toJson());
		return id;
	}
}
