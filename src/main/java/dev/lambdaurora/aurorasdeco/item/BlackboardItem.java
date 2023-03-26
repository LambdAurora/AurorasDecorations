/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

import dev.lambdaurora.aurorasdeco.blackboard.Blackboard;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.tooltip.BlackboardTooltipData;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
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
	public boolean onClicked(ItemStack self, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStack) {
		if (clickType == ClickType.RIGHT) {
			if (otherStack.isOf(Items.WATER_BUCKET)
					|| (otherStack.isOf(Items.POTION) && PotionUtil.getPotion(otherStack) == Potions.WATER)) {
				var nbt = AuroraUtil.getOrCreateBlockEntityNbt(self, AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE);
				var blackboard = Blackboard.fromNbt(nbt);
				if (blackboard.isEmpty())
					return false;
				blackboard.clear();
				blackboard.writeNbt(nbt);

				if (otherStack.isOf(Items.POTION)) {
					if (!player.getAbilities().creativeMode) {
						var newStack = new ItemStack(Items.GLASS_BOTTLE);
						if (otherStack.getCount() != 1) {
							otherStack.decrement(1);
							player.getInventory().insertStack(newStack);
						} else {
							cursorStack.set(newStack);
						}
					}
					player.playSound(SoundEvents.ITEM_BOTTLE_EMPTY, 1.f, 1.f);
				} else {
					player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.f, 1.f);
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

/*	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (this.isInGroup(group) || group == ItemGroup.SEARCH)
			stacks.add(this.getDefaultStack());
	}*/

	@Override
	public ItemStack getDefaultStack() {
		return this.ensureValidStack(new ItemStack(this));
	}

	private ItemStack ensureValidStack(ItemStack stack) {
		if (BlockItem.getBlockEntityNbtFromStack(stack) == null) {
			var nbt = AuroraUtil.getOrCreateBlockEntityNbt(stack, AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE);
			var blackboard = new Blackboard();
			blackboard.writeNbt(nbt);
		}
		return stack;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		var nbt = BlockItem.getBlockEntityNbtFromStack(stack);
		if (nbt != null && nbt.contains("pixels", NbtElement.BYTE_ARRAY_TYPE)) {
			var blackboard = Blackboard.fromNbt(nbt);
			return Optional.of(new BlackboardTooltipData(
					Registries.ITEM.getId(this).getPath().replace("waxed_", ""),
					blackboard, this.locked)
			);
		}
		return super.getTooltipData(stack);
	}
}
