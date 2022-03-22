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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a book pile block entity.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BookPileBlockEntity extends BasicBlockEntity {
	private final DefaultedList<ItemStack> books = DefaultedList.ofSize(5, ItemStack.EMPTY);

	public BookPileBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE, pos, state);
	}

	public DefaultedList<ItemStack> getBooks() {
		return this.books;
	}

	public boolean isFull() {
		return this.books.stream().noneMatch(ItemStack::isEmpty);
	}

	public void insertBook(ItemStack stack) {
		for (int i = 0; i < this.books.size(); i++) {
			if (this.books.get(i).isEmpty()) {
				var copy = stack.copy();
				copy.setCount(1);
				this.books.set(i, copy);

				this.markDirty();
				return;
			}
		}
	}

	public ItemStack removeBook(int slot) {
		var stack = this.books.get(slot);
		var copy = stack.copy();
		if (!stack.isEmpty()) {
			stack.setCount(0);
			this.markDirty();
		}
		return copy;
	}

	/* Serialization */

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, this.books);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, this.books);
	}
}
