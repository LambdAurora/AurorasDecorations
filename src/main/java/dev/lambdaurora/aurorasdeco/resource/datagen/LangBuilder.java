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

package dev.lambdaurora.aurorasdeco.resource.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

public class LangBuilder {
	private final Map<String, LangManifest> loadedLangs = new Object2ObjectOpenHashMap<>();
	private final Map<String, LangManifest> buildingLangs = new Object2ObjectOpenHashMap<>();

	public void load() {
		this.loadedLangs.clear();

		var root = FabricLoader.getInstance().getModContainer(AurorasDeco.NAMESPACE).get().getRootPath().toAbsolutePath().normalize();
		var separator = root.getFileSystem().getSeparator();

		var langPath = root.resolve("assets" + separator + AurorasDeco.NAMESPACE + separator + "lang" + separator)
				.toAbsolutePath().normalize();
		try {
			Files.walk(langPath, 1)
					.filter(Files::isRegularFile)
					.filter(p -> {
						var fileName = p.getFileName().toString();
						return fileName.endsWith(".json");
					})
					.map(path -> {
						var fileName = path.getFileName().toString();
						return fileName.substring(0, fileName.length() - ".json".length());
					})
					.map(LangManifest::readFromMod)
					.forEach(s -> this.loadedLangs.put(s.getLangCode(), s));
		} catch (IOException ignored) {
		}
	}

	public LangBuilder addEntry(String key, String from, String... formatKeys) {
		this.loadedLangs.forEach((langCode, loadedManifest) -> {
			var buildingManifest = this.buildingLangs.computeIfAbsent(langCode, LangManifest::new);
			buildingManifest.put(key, loadedManifest.getFormatted(from,
					(Object[]) Arrays.stream(formatKeys).map(loadedManifest::get).toArray(String[]::new)
			));
		});
		return this;
	}

	public void write(AurorasDecoPack pack) {
		this.buildingLangs.forEach((key, manifest) -> {
			pack.putJson(ResourceType.CLIENT_RESOURCES, AurorasDeco.id("lang/" + key), manifest.toJson());
		});
	}

	public static class LangManifest {
		private static LangManifest defaultLang;
		private final Map<String, String> values = new Object2ObjectOpenHashMap<>();

		private final String langCode;

		public LangManifest(String langCode) {
			this.langCode = langCode;
			if (langCode.equals("en_us"))
				defaultLang = this;
		}

		public String getLangCode() {
			return this.langCode;
		}

		public String get(String key) {
			var value = this.values.get(key);
			if (value == null) {
				if (defaultLang != null && defaultLang != this)
					return defaultLang.get(key);
				else return key;
			} else return value;
		}

		public String getFormatted(String key, Object... values) {
			return this.get(key).formatted(values);
		}

		public void put(String key, String value) {
			this.values.put(key, value);
		}

		public JsonObject toJson() {
			var json = new JsonObject();
			values.forEach(json::addProperty);
			return json;
		}

		public static LangManifest from(String langCode, InputStream stream) {
			var json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
			var manifest = new LangManifest(langCode);
			json.entrySet().forEach(entry -> {
				if (entry.getValue().isJsonPrimitive())
					manifest.put(entry.getKey(), entry.getValue().getAsString());
			});
			return manifest;
		}

		public static LangManifest readFromMod(String langCode) {
			var root = FabricLoader.getInstance().getModContainer(AurorasDeco.NAMESPACE).get().getRootPath().toAbsolutePath().normalize();
			var separator = root.getFileSystem().getSeparator();

			var langPath = root.resolve("assets" + separator + AurorasDeco.NAMESPACE + separator + "lang" + separator + langCode + ".json")
					.toAbsolutePath().normalize();
			try {
				return from(langCode, Files.newInputStream(langPath));
			} catch (IOException e) {
				return new LangManifest(langCode);
			}
		}
	}
}
