/*
 * Copyright (c) 2022 LambdAurora <email@lambdaurora.dev>
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

import dev.lambdaurora.aurorasdeco.item.PainterPaletteItem;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoScreenHandlers;
import dev.lambdaurora.aurorasdeco.screen.slot.BlackboardToolSlot;
import dev.lambdaurora.aurorasdeco.screen.slot.ColorSlot;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the painter's palette screen handler.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.6
 * @since 1.0.0-beta.6
 */
public class PainterPaletteScreenHandler extends NestedScreenHandler {
	private final PainterPaletteItem.PainterPaletteInventory inventory;

	public PainterPaletteScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		this(syncId, playerInventory, buf.readEnumConstant(OriginType.class), buf.readVarInt(), new PainterPaletteItem.PainterPaletteInventory());
	}

	public PainterPaletteScreenHandler(int syncId, PlayerInventory playerInventory, OriginType originType, int lockedSlot,
	                                   PainterPaletteItem.PainterPaletteInventory inventory) {
		super(AurorasDecoScreenHandlers.PAINTER_PALETTE_SCREEN_HANDLER_TYPE, syncId, originType, lockedSlot);
		this.inventory = inventory;
		this.inventory.onOpen(playerInventory.player);

		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 9; ++column) {
				this.addSlot(new ColorSlot(inventory, column + row * 9, 8 + column * 18, 18 + row * 18));
			}
		}

		for (int row = 0; row < 4; ++row) {
			this.addSlot(new BlackboardToolSlot(inventory, (inventory.size() - 4) + row, -16, 18 + row * 18));
		}

		this.addPlayerInventory(playerInventory, 8, 85);

		this.addProperties(inventory.getProperties());
	}

	public PainterPaletteItem.PainterPaletteInventory getInventory() {
		return this.inventory;
	}

	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		var slot = this.slots.get(id);

		if (slot instanceof ColorSlot && !slot.getStack().isEmpty()) {
			this.inventory.setSelectedColor(slot.getIndex());
			return true;
		} else if (slot instanceof BlackboardToolSlot) {
			if (slot.getStack().isEmpty()) this.inventory.setSelectedToolSlot(-1);
			else this.inventory.setSelectedToolSlot(slot.getIndex());
			return true;
		} else if (id == this.inventory.size()) {
			this.inventory.setSelectedToolSlot(-1);
			return true;
		}

		return false;
	}

	@Override
	public ItemStack quickTransfer(PlayerEntity player, int fromIndex) {
		var itemStack = ItemStack.EMPTY;
		var slot = this.slots.get(fromIndex);

		if (slot.isEnabled()) {
			var currentStack = slot.getStack();
			itemStack = currentStack.copy();
			if (fromIndex < this.inventory.size()) {
				if (!this.insertItem(currentStack, this.inventory.size(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				int playerInventoryEnd = this.inventory.size() + 27;
				int hotbarEnd = playerInventoryEnd + 9;

				if (!this.insertItem(currentStack, 0, this.inventory.size(), false)) {
					if (fromIndex >= playerInventoryEnd && fromIndex < hotbarEnd) {
						if (!this.insertItem(currentStack, this.inventory.size(), playerInventoryEnd, false)) {
							return ItemStack.EMPTY;
						}
					} else if (fromIndex < playerInventoryEnd) {
						if (!this.insertItem(currentStack, playerInventoryEnd, hotbarEnd, false)) {
							return ItemStack.EMPTY;
						}
					} else if (!this.insertItem(currentStack, playerInventoryEnd, playerInventoryEnd, false)) {
						return ItemStack.EMPTY;
					}

					return ItemStack.EMPTY;
				}
			}

			if (currentStack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
		}

		return itemStack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	protected boolean saveToOriginItem(ItemStack stack) {
		var nbt = inventory.toNbt();
		if (nbt != null) stack.setSubNbt("inventory", nbt);
		else {
			if (stack.getSubNbt("inventory") == null) {
				return false;
			}

			stack.removeSubNbt("inventory");
		}

		return true;
	}

	public record Factory(ItemStack self, OriginType type, int lockedSlot) implements ExtendedScreenHandlerFactory {
		@Override
		public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
			buf.writeEnumConstant(this.type);
			buf.writeVarInt(this.lockedSlot);
		}

		@Override
		public Text getDisplayName() {
			return this.self.getName();
		}

		@Override
		public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
			var inventory = PainterPaletteItem.PainterPaletteInventory.fromNbt(this.self.getSubNbt("inventory"));

			return new PainterPaletteScreenHandler(syncId, playerInventory, this.type, this.lockedSlot, inventory);
		}
	}
}
