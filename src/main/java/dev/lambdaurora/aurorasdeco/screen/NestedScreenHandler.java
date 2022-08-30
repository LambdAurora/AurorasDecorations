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

import dev.lambdaurora.aurorasdeco.screen.slot.LockedSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

public abstract class NestedScreenHandler extends ScreenHandler {
	protected final OriginType originType;
	protected final int lockedSlot;

	protected NestedScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, OriginType originType, int lockedSlot) {
		super(type, syncId);
		this.originType = originType;
		this.lockedSlot = lockedSlot;
	}

	protected void addPlayerInventory(PlayerInventory playerInventory, int columnStart, int playerInventoryStart) {
		// Player inventory.
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				if (originType == OriginType.PLAYER && column + row * 9 + 9 == lockedSlot) {
					this.addSlot(new LockedSlot(playerInventory, column + row * 9 + 9, columnStart + column * 18, playerInventoryStart + row * 18));
				} else {
					this.addSlot(new Slot(playerInventory, column + row * 9 + 9, columnStart + column * 18, playerInventoryStart + row * 18));
				}
			}
		}

		for (int column = 0; column < 9; column++) {
			if (originType == OriginType.PLAYER && column == lockedSlot) {
				this.addSlot(new LockedSlot(playerInventory, column, columnStart + column * 18, playerInventoryStart + 58));
			} else {
				this.addSlot(new Slot(playerInventory, column, columnStart + column * 18, playerInventoryStart + 58));
			}
		}
	}

	@Override
	public void close(PlayerEntity player) {
		super.close(player);

		if (!player.getWorld().isClient()) {
			var affectedInventory = switch (this.originType) {
				case PLAYER -> player.getInventory();
				case ENDER_CHEST -> player.getEnderChestInventory();
			};
			var stack = affectedInventory.getStack(this.lockedSlot);

			if (this.saveToOriginItem(stack)) {
				affectedInventory.markDirty();
			}
		}
	}

	protected abstract boolean saveToOriginItem(ItemStack stack);

	public enum OriginType {
		PLAYER,
		ENDER_CHEST
	}
}
