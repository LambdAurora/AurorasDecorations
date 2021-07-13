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
import dev.lambdaurora.aurorasdeco.screen.CopperHopperScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a copper hopper block entity.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CopperHopperBlockEntity extends FilteredHopperBlockEntity {
    private final SimpleInventory filterInventory = new SimpleInventory(1);

    public CopperHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public ItemStack getFilter() {
        return this.filterInventory.getStack(0);
    }

    public void dropFilter() {
        ItemScatterer.spawn(this.getWorld(), this.getPos(), this.filterInventory);
    }

    @Override
    public boolean testItem(ItemStack stack) {
        return isItemAcceptedByFilter(stack, this.getFilter());
    }

    public static boolean isItemAcceptedByFilter(ItemStack stack, ItemStack filter) {
        if (filter.isEmpty())
            return true;

        // Simple mode
        return filter.getItem() == stack.getItem();
    }

    @Override
    public BlockEntityType<?> getType() {
        return AurorasDecoRegistry.COPPER_HOPPER_BLOCK_ENTITY_TYPE;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CopperHopperScreenHandler(syncId, playerInventory, this, this.filterInventory);
    }

    /* Serialization */

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("filter", NbtElement.COMPOUND_TYPE))
            this.filterInventory.setStack(0, ItemStack.fromNbt(nbt.getCompound("filter")));
        else this.filterInventory.setStack(0, ItemStack.EMPTY);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        var filter = this.getFilter();
        if (!filter.isEmpty())
            nbt.put("filter", filter.writeNbt(new NbtCompound()));
        return super.writeNbt(nbt);
    }

    public interface Filter {
        boolean test(ItemStack stack, ItemStack filter);
    }
}
