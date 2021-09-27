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

package dev.lambdaurora.aurorasdeco.util;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.SimpleRegistryAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RegistrationHelper<T> {
	public static final RegistrationHelper<Block> BLOCK = new RegistrationHelper<>(Registry.BLOCK);

	private final Map<Identifier, T> delayedRegistry = new Object2ObjectOpenHashMap<>();
	private final List<RegistrationCallback<T>> registrationCallbacks = new ArrayList<>();
	private final Registry<T> registry;
	private boolean early = true;

	private RegistrationHelper(Registry<T> registry) {
		this.registry = registry;
	}

	public static String getIdPath(String prefix, Identifier originalId, String replacerRegex) {
		var namespace = originalId.getNamespace();
		namespace = switch (namespace) {
			case "minecraft", AurorasDeco.NAMESPACE -> "";
			default -> namespace + '/';
		};
		return prefix + '/' + namespace + originalId.getPath().replaceAll(replacerRegex, "");
	}

	/**
	 * Registers the given object into the registry this helper is associated with.
	 * Registration may be delayed.
	 *
	 * @param name the identifier path
	 * @param obj the object to register
	 * @return the registered object
	 */
	public <V extends T> V register(String name, V obj) {
		return this.register(AurorasDeco.id(name), obj);
	}

	/**
	 * Registers the given object into the registry this helper is associated with.
	 * Registration may be delayed.
	 *
	 * @param id the identifier
	 * @param obj the object to register
	 * @return the registered object
	 */
	public <V extends T> V register(Identifier id, V obj) {
		if (this.early) {
			this.delayedRegistry.put(id, obj);
			return obj;
		} else
			return Registry.register(this.registry, id, obj);
	}

	public void addRegistrationCallback(RegistrationCallback<T> callback) {
		this.registrationCallbacks.add(callback);
		RegistryEntryAddedCallback.event(this.registry)
				.register((rawId, id, object) -> callback.onRegistration(this, id, object));
	}

	@SuppressWarnings("unchecked")
	public void init() {
		this.early = false;

		this.delayedRegistry.forEach((id, obj) -> Registry.register(this.registry, id, obj));

		((SimpleRegistryAccessor<T>) this.registry).getIdToEntry()
				.forEach((id, block) -> {
					for (var callback : this.registrationCallbacks)
						callback.onRegistration(this, id, block);
				});
	}

	public interface RegistrationCallback<T> {
		void onRegistration(RegistrationHelper<T> helper, Identifier id, T obj);
	}
}
