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

package dev.lambdaurora.aurorasdeco.util;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.StateIdTrackerAccessor;
import net.fabricmc.fabric.impl.registry.sync.trackers.StateIdTracker;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class RegistrationHelper<T> {
	public static final RegistrationHelper<Block> BLOCK = new RegistrationHelper<>(Registry.BLOCK);

	private final Registry<T> registry;
	private StateIdTracker<T, ?> fabricTracker;

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

	public void setFabricTracker(StateIdTracker<T, ?> tracker) {
		this.fabricTracker = tracker;
	}

	public void init() {
		if (this.fabricTracker != null) {
			((StateIdTrackerAccessor) (Object) this.fabricTracker).callRecalcStateMap();
		}
	}
}
