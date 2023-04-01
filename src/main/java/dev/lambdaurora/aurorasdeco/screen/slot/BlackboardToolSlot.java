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

package dev.lambdaurora.aurorasdeco.screen.slot;

import dev.lambdaurora.aurorasdeco.blackboard.Blackboard;
import dev.lambdaurora.aurorasdeco.blackboard.BlackboardDrawModifier;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

/**
 * Represents a slot that only accepts items that can be used as {@link BlackboardDrawModifier blackboard modifiers}.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.13
 * @since 1.0.0-beta.6
 */
public class BlackboardToolSlot extends Slot {
	private final FeatureFlagBitSet enabledFeatures;

	public BlackboardToolSlot(Inventory inventory, FeatureFlagBitSet enabledFeatures, int index, int x, int y) {
		super(inventory, index, x, y);
		this.enabledFeatures = enabledFeatures;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		if (stack.isOf(Items.STICK)) return true;
		return Blackboard.DrawAction.ACTIONS.stream()
				.filter(drawAction -> drawAction.getOffHandTool(this.enabledFeatures) != null)
				.anyMatch(drawAction -> drawAction.getOffHandTool(this.enabledFeatures) == stack.getItem());
	}
}
