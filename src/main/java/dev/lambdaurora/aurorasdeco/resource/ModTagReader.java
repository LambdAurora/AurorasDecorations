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

package dev.lambdaurora.aurorasdeco.resource;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.DefaultResourcePackBuilder;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Mojang removed Material, so I will read tags, I am a menace.
 */
public class ModTagReader {
	public static final ModTagReader INSTANCE = new ModTagReader();
	private final Map<TagKey<?>, Collection<Identifier>> tags = new Object2ObjectOpenHashMap<>();
	private boolean loaded = false;
	private ResourceManager resourceManager;

	public void load() {
		if (!this.loaded) {
			this.loaded = true;
			this.loadTags(RegistryKeys.BLOCK);
		}
	}

	public <T> void loadTags(RegistryKey<Registry<T>> registryKey) {
		var groupLoader = new TagGroupLoader<>(Optional::of, TagManagerLoader.getRegistryDirectory(registryKey));
		groupLoader.load(this.getResourceManager()).forEach((id, values) -> {
			this.tags.put(TagKey.of(registryKey, id), values);
		});
	}

	public Collection<Identifier> getValues(TagKey<?> tagKey) {
		this.load();
		return this.tags.get(tagKey);
	}

	private ResourceManager createResourceManager() {
		var resourcePacks = new ArrayList<ResourcePack>();
		resourcePacks.add(new DefaultResourcePackBuilder().withNamespaces(Identifier.DEFAULT_NAMESPACE).withDefaultPaths().build());
		ResourceLoaderImpl.appendModResourcePacks(resourcePacks, ResourceType.SERVER_DATA, null);

		return new MultiPackResourceManager(ResourceType.SERVER_DATA, resourcePacks);
	}

	private ResourceManager getResourceManager() {
		if (this.resourceManager == null) {
			this.resourceManager = this.createResourceManager();
		}

		return this.resourceManager;
	}
}
