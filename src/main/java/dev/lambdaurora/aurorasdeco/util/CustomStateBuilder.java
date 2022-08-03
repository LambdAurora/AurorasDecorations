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

package dev.lambdaurora.aurorasdeco.util;

import dev.lambdaurora.aurorasdeco.mixin.StateManagerBuilderAccessor;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CustomStateBuilder<O, S extends State<O, S>> extends StateManager.Builder<O, S> {
	private final StateManager.Builder<O, S> parent;
	private final List<String> excludes = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public CustomStateBuilder(StateManager.Builder<O, S> parent) {
		super(((StateManagerBuilderAccessor<O, S>) parent).getOwner());
		this.parent = parent;
	}

	public CustomStateBuilder<O, S> exclude(String... excludes) {
		Collections.addAll(this.excludes, excludes);
		return this;
	}

	@Override
	public CustomStateBuilder<O, S> add(Property<?>... properties) {
		for (var property : properties) {
			if (this.excludes.contains(property.getName()))
				continue;
			this.parent.add(property);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public CustomStateBuilder<O, S> safeAdd(Property<?>... properties) {
		for (var property : properties) {
			if (this.excludes.contains(property.getName())
					|| ((StateManagerBuilderAccessor<O, S>) this.parent).getNamedProperties().containsKey(property.getName()))
				continue;
			this.parent.add(property);
		}
		return this;
	}

	@Override
	public StateManager<O, S> build(Function<O, S> ownerToStateFunction, StateManager.Factory<O, S> factory) {
		return this.parent.build(ownerToStateFunction, factory);
	}
}
