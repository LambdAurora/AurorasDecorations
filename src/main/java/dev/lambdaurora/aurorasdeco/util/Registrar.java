/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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
import dev.lambdaurora.aurorasdeco.accessor.BlockEntityTypeAccessor;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Represents an over-engineered set of utilities to register block, items and more.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Registrar {
	private Registrar() {
	}

	public static <V, T extends V> RegistrationCompleter<T> register(Registry<V> registry, String name, T object) {
		return register(registry, AurorasDeco.id(name), object);
	}

	public static <V, T extends V> RegistrationCompleter<T> register(Registry<V> registry, Identifier id, T object) {
		return new RegistrationCompleter<>(id, Registry.register(registry, id, object));
	}

	public static <T extends Block> BlockRegistrationCompleter<T> register(String name, T block) {
		return register(AurorasDeco.id(name), block);
	}

	public static <T extends Block> BlockRegistrationCompleter<T> register(Identifier id, T block) {
		return new BlockRegistrationCompleter<>(id, Registry.register(Registry.BLOCK, id, block));
	}

	public static <T extends Item> RegistrationCompleter<T> register(String name, T item) {
		return register(Registry.ITEM, name, item);
	}

	public static <T extends Item> RegistrationCompleter<T> register(Identifier id, T item) {
		return register(Registry.ITEM, id, item);
	}

	/**
	 * Allows specifying actions to realize on the registered object.
	 *
	 * @param <T> the type of the registered object
	 */
	public static class RegistrationCompleter<T> {
		protected final Identifier id;
		protected final T registeredObject;

		protected RegistrationCompleter(Identifier id, T registeredObject) {
			this.id = id;
			this.registeredObject = registeredObject;
		}

		public RegistrationCompleter<T> then(Consumer<T> handler) {
			handler.accept(this.registeredObject);
			return this;
		}

		public T finish() {
			return this.registeredObject;
		}
	}

	public static class BlockRegistrationCompleter<T extends Block> extends RegistrationCompleter<T> {
		protected BlockRegistrationCompleter(Identifier id, T registeredObject) {
			super(id, registeredObject);
		}

		@Override
		public BlockRegistrationCompleter<T> then(Consumer<T> handler) {
			super.then(handler);
			return this;
		}

		public BlockRegistrationCompleter<T> withItem(Item.Settings settings) {
			return this.withItem(settings, BlockItem::new);
		}

		public BlockRegistrationCompleter<T> withItem(Item.Settings settings, BiFunction<T, Item.Settings, BlockItem> factory) {
			Registrar.register(this.id, factory.apply(this.registeredObject, settings));
			return this;
		}

		/**
		 * Adds the block as a supported block to the specified block entity type.
		 *
		 * @param type the entity type to add support to
		 * @return the instance of this completer
		 */
		public BlockRegistrationCompleter<T> addSelfTo(BlockEntityType<?> type) {
			((BlockEntityTypeAccessor) type).aurorasdeco$addSupportedBlock(this.registeredObject);
			return this;
		}

		public BlockRegistrationCompleter<T> flammable(int burn, int spread) {
			FlammableBlockRegistry.getDefaultInstance().add(this.registeredObject, burn, spread);
			return this;
		}

		public BlockRegistrationCompleter<T> flammable(FlammableBlockRegistry.Entry entry) {
			if (entry != null && entry.getBurnChance() != 0 && entry.getSpreadChance() != 0)
				return this.flammable(entry.getBurnChance(), entry.getSpreadChance());
			else return this;
		}
	}
}
