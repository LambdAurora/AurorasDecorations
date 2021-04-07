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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
public class BookPileBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
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
                ItemStack copy = stack.copy();
                copy.setCount(1);
                this.books.set(i, copy);

                this.markDirty();
                return;
            }
        }
    }

    public ItemStack removeBook(int slot) {
        ItemStack stack = this.books.get(slot);
        ItemStack copy = stack.copy();
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
        this.fromClientTag(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return this.toClientTag(super.writeNbt(nbt));
    }

    @Override
    public void fromClientTag(NbtCompound nbt) {
        Inventories.writeNbt(nbt, this.books);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound nbt) {
        Inventories.readNbt(nbt, this.books);
        return nbt;
    }
}
