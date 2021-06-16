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

package dev.lambdaurora.aurorasdeco.screen;

import dev.lambdaurora.aurorasdeco.block.PartType;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * Represents the shelf screen handler.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShelfScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PartType partType;

    public ShelfScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, new SimpleInventory(8), buf.readEnumConstant(PartType.class));
    }

    public ShelfScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PartType partType) {
        super(AurorasDecoRegistry.SHELF_SCREEN_HANDLER_TYPE, syncId);
        checkSize(inventory, 8);
        this.inventory = inventory;
        this.partType = partType;

        int y;
        int x;

        int max = this.partType == PartType.TOP ? 1 : 2;
        for (y = this.partType == PartType.BOTTOM ? 1 : 0; y < max; ++y) {
            int rowY = 17 + y * 18;

            if (this.partType != PartType.DOUBLE)
                rowY = 26;

            for (x = 0; x < 4; ++x) {
                this.addSlot(new Slot(inventory, x + y * 4, 53 + x * 18, rowY));
            }
        }

        // The player inventory.
        for (y = 0; y < 3; ++y) {
            for (x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 66 + y * 18));
            }
        }
        // The player hotbar.
        for (x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 124));
        }
    }

    public PartType getPartType() {
        return this.partType;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public ItemStack transferSlot(PlayerEntity player, int index) {
        var stack = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (slot.hasStack()) {
            var itemStack2 = slot.getStack();
            stack = itemStack2.copy();
            if (index < 8) {
                if (!this.insertItem(itemStack2, 8, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(itemStack2, 0, 8, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return stack;
    }
}
