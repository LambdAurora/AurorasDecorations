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

package dev.lambdaurora.aurorasdeco.block.state;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a custom state manager builder that allows more control over what a block tries to add.
 *
 * @param <O> the base object which will have states appended by this builder
 * @param <S> the state type of the object
 */
public class CustomStateManagerBuilder<O, S extends State<O, S>> extends StateManager.Builder<O, S> {
	private final Map<Property<?>, Object> provided = new Object2ObjectOpenHashMap<>();

	public CustomStateManagerBuilder(O object) {
		super(object);
	}

	public <T extends Comparable<T>> CustomStateManagerBuilder<O, S> provides(Property<T> property, T defaultValue) {
		this.provided.put(property, defaultValue);

		return this;
	}

	@Override
	public CustomStateManagerBuilder<O, S> add(Property<?>... properties) {
		if (properties.length == 1) {
			if (this.provided.containsKey(properties[0]))
				return this;
		} else {
			var list = new ArrayList<Property<?>>();
			for (var p : properties) {
				if (!this.provided.containsKey(p)) {
					list.add(p);
				}
			}
			super.add(list.toArray(Property[]::new));
		}

		super.add(properties);
		return this;
	}
}
