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

package dev.lambdaurora.aurorasdeco.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.texture.NativeImage;
import com.mojang.logging.LogUtils;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.*;
import dev.lambdaurora.aurorasdeco.registry.LanternRegistry;
import net.minecraft.block.Material;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.resource.loader.api.InMemoryResourcePack;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.stream.Stream;

public class AurorasDecoPack extends InMemoryResourcePack {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final ResourceType type;

	private boolean hasRegisteredOneTimeResources = false;

	public AurorasDecoPack(ResourceType type) {
		this.type = type;
	}

	public AurorasDecoPack rebuild(ResourceType type, @Nullable ResourceManager resourceManager) {
		this.registerTag(new String[]{"blocks"}, new Identifier("flower_pots"), HangingFlowerPotBlock.stream()
				.map(Registry.BLOCK::getId));

		this.registerTag(new String[]{"blocks", "items"}, AurorasDeco.id("benches"), BenchBlock.streamBenches()
				.map(Registry.BLOCK::getId));
		this.registerTag(new String[]{"blocks", "items"}, AurorasDeco.id("shelves"), ShelfBlock.streamShelves()
				.map(Registry.BLOCK::getId));
		this.registerTag(new String[]{"blocks"}, new Identifier("mineable/axe"), SignPostBlock.stream()
				.filter(block -> block.getDefaultState().getMaterial() == Material.WOOD
						|| block.getDefaultState().getMaterial() == Material.NETHER_WOOD)
				.map(Registry.BLOCK::getId));
		this.registerTag(new String[]{"blocks", "items"}, AurorasDeco.id("small_log_piles"), SmallLogPileBlock.stream()
				.map(Registry.BLOCK::getId));
		this.registerTag(new String[]{"blocks", "items"}, AurorasDeco.id("stumps"), StumpBlock.streamLogStumps()
				.map(Registry.BLOCK::getId));
		this.registerTag(new String[]{"blocks"}, AurorasDeco.id("wall_lanterns"), LanternRegistry.streamIds());

		return type == ResourceType.CLIENT_RESOURCES ? this.rebuildClient(resourceManager) : this.rebuildData();
	}

	public AurorasDecoPack rebuildClient(ResourceManager resourceManager) {
		Datagen.generateClientData(resourceManager);

		return this;
	}

	private void registerTag(String[] types, Identifier id, Stream<Identifier> entries) {
		var root = new JsonObject();
		root.addProperty("replace", false);
		var values = new JsonArray();

		entries.forEach(value -> values.add(value.toString()));

		root.add("values", values);

		for (var type : types) {
			this.putJson(ResourceType.SERVER_DATA, new Identifier(id.getNamespace(), "tags/" + type + "/" + id.getPath()), root);
		}
	}

	public AurorasDecoPack rebuildData() {
		if (!this.hasRegisteredOneTimeResources) {
			Datagen.registerDefaultRecipes();
			Datagen.registerDefaultWoodcuttingRecipes();
			this.hasRegisteredOneTimeResources = true;
		}

		BenchBlock.streamBenches().forEach(Datagen::registerBenchBlockLootTable);
		ExtendedCandleBlock.stream().forEach(Datagen::registerCandleLikeBlockLootTable);
		ShelfBlock.streamShelves().forEach(Datagen::registerDoubleBlockLootTable);
		SmallLogPileBlock.stream().forEach(Datagen::registerDoubleBlockLootTable);
		StumpBlock.streamLogStumps().forEach(Datagen::dropsSelf);

		return this;
	}

	public void putJsonText(ResourceType type, Identifier id, String json) {
		this.putText(type, new Identifier(id.getNamespace(), id.getPath() + ".json"), json);
	}

	public void putJson(ResourceType type, Identifier id, JsonObject json) {
		if (!id.getPath().endsWith(".json")) id = new Identifier(id.getNamespace(), id.getPath() + ".json");

		var stringWriter = new StringWriter();
		var jsonWriter = new JsonWriter(stringWriter);
		jsonWriter.setLenient(true);
		jsonWriter.setIndent("  ");
		try {
			Streams.write(json, jsonWriter);
		} catch (IOException e) {
			LOGGER.error("Failed to write JSON at {}.", id, e);
		}

		this.putText(type, id, stringWriter.toString());
	}

	public void putImage(Identifier id, NativeImage image) {
		if (!id.getPath().endsWith(".png")) id = new Identifier(id.getNamespace(), "textures/" + id.getPath() + ".png");
		try {
			super.putImage(id, image);
		} catch (IOException e) {
			LOGGER.warn("Could not close output channel for texture " + id + ".", e);
		}
	}

	@Override
	public String getName() {
		return "Aurora's Decorations Virtual Pack";
	}
}
