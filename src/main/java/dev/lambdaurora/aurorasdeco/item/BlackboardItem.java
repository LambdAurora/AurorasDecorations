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

package dev.lambdaurora.aurorasdeco.item;

import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.client.tooltip.BlackboardTooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Represents a blackboard item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardItem extends BlockItem {
    private final boolean locked;

    public BlackboardItem(BlackboardBlock blackboardBlock, Settings settings) {
        super(blackboardBlock, settings);
        this.locked = blackboardBlock.isLocked();
    }

    @Override
    public boolean onClicked(ItemStack self, ItemStack otherStack, Slot slot, ClickType clickType, PlayerInventory playerInventory) {
        if (clickType == ClickType.RIGHT) {
            if (otherStack.isOf(Items.WATER_BUCKET)
                    || (otherStack.isOf(Items.POTION) && PotionUtil.getPotion(otherStack) == Potions.WATER)) {
                CompoundTag nbt = self.getOrCreateSubTag("BlockEntityTag");
                Blackboard blackboard = Blackboard.fromNbt(nbt);
                if (blackboard.isEmpty())
                    return false;
                blackboard.clear();
                blackboard.writeNbt(nbt);

                if (otherStack.isOf(Items.POTION)) {
                    if (!playerInventory.player.getAbilities().creativeMode) {
                        ItemStack newStack = new ItemStack(Items.GLASS_BOTTLE);
                        if (otherStack.getCount() != 1) {
                            otherStack.decrement(1);
                            playerInventory.insertStack(newStack);
                        } else {
                            playerInventory.setCursorStack(newStack);
                        }
                    }
                    playerInventory.player.playSound(SoundEvents.ITEM_BOTTLE_EMPTY, 1.f, 1.f);
                } else {
                    playerInventory.player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.f, 1.f);
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        this.ensureValidStack(stack);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group))
            stacks.add(this.getDefaultStack());
    }

    @Override
    public ItemStack getDefaultStack() {
        return this.ensureValidStack(new ItemStack(this));
    }

    private ItemStack ensureValidStack(ItemStack stack) {
        if (stack.getSubTag("BlockEntityTag") == null) {
            CompoundTag nbt = stack.getOrCreateSubTag("BlockEntityTag");
            nbt.putBoolean("lit", false);
            nbt.putByteArray("pixels", new byte[256]);
        }
        return stack;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        CompoundTag nbt = stack.getSubTag("BlockEntityTag");
        if (nbt != null && nbt.contains("pixels", NbtType.BYTE_ARRAY)) {
            Blackboard blackboard = Blackboard.fromNbt(nbt);
            return Optional.of(new BlackboardTooltipComponent(
                    Registry.ITEM.getId(this).getPath().replace("waxed_", ""),
                    blackboard, this.locked));
        }
        return super.getTooltipData(stack);
    }
}
