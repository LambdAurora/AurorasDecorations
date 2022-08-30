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

package dev.lambdaurora.aurorasdeco.screen;

import dev.lambdaurora.aurorasdeco.block.entity.CopperHopperBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * Represents the copper hopper screen handler.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CopperHopperScreenHandler extends ScreenHandler {
	private final Inventory inventory;
	private final Inventory filterInventory;
	private final Slot filterSlot;

	public CopperHopperScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(HopperScreenHandler.SLOTS_COUNT), new SimpleInventory(1));
	}

	public CopperHopperScreenHandler(int syncId, PlayerInventory playerInventory,
			Inventory hopperInventory, Inventory filterInventory) {
		super(AurorasDecoScreenHandlers.COPPER_HOPPER_SCREEN_HANDLER_TYPE, syncId);

		checkSize(hopperInventory, HopperScreenHandler.SLOTS_COUNT);
		checkSize(filterInventory, 1);

		// Hopper inventory
		this.inventory = hopperInventory;
		for (int slot = 0; slot < HopperScreenHandler.SLOTS_COUNT; slot++) {
			this.addSlot(new FilteredSlot(this.inventory, slot, slot * 18 + 26, 20));
		}
		this.inventory.onOpen(playerInventory.player);

		// Filter
		this.filterInventory = filterInventory;
		this.filterSlot = this.addSlot(new Slot(this.filterInventory, 0, 134, 20) {
			@Override
			public int getMaxItemCount() {
				return 1;
			}
		});
		this.filterInventory.onOpen(playerInventory.player);

		// Player inventory
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(playerInventory, column + row * 9 + 9, column * 18 + 8, row * 18 + 51));
			}
		}

		for (int column = 0; column < 9; column++) {
			this.addSlot(new Slot(playerInventory, column, column * 18 + 8, 109));
		}
	}

	public Slot getFilterSlot() {
		return this.filterSlot;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickTransfer(PlayerEntity player, int fromIndex) {
		var slot = this.slots.get(fromIndex);
		if (!slot.hasStack()) return ItemStack.EMPTY;

		var currentStack = slot.getStack();
		var stack = currentStack.copy();

		// From hopper to player.
		if (fromIndex <= this.inventory.size()) {
			if (!this.insertItem(currentStack, this.inventory.size() + 1, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.insertItem(currentStack, 0, this.inventory.size(), false)) { // From player to inventory.
			return ItemStack.EMPTY;
		}

		if (currentStack.isEmpty()) {
			slot.setStack(ItemStack.EMPTY);
		} else {
			slot.markDirty();
		}

		return stack;
	}

	@Override
	public void close(PlayerEntity player) {
		super.close(player);

		this.inventory.onClose(player);
		this.filterInventory.onClose(player);
	}

	private class FilteredSlot extends Slot {
		public FilteredSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return CopperHopperBlockEntity.isItemAcceptedByFilter(stack, CopperHopperScreenHandler.this.getFilterSlot().getStack());
		}
	}
}
