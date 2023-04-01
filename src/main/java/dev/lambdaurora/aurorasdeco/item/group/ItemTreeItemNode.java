/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.item.group;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.Collection;

public class ItemTreeItemNode implements ItemTreeNode {
	private final ItemStack stack;
	private ItemGroup.Visibility visibility;

	public ItemTreeItemNode(ItemStack stack, ItemGroup.Visibility visibility) {
		this.stack = stack;
		this.visibility = visibility;
	}

	public ItemTreeItemNode(ItemStack stack) {
		this(stack, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	public ItemStack stack() {
		return this.stack;
	}

	@Override
	public ItemGroup.Visibility getVisibility() {
		return this.visibility;
	}

	public void setVisibility(ItemGroup.Visibility visibility) {
		this.visibility = visibility;
	}

	@Override
	public void build(Collection<ItemStack> stacks, ItemGroup.Visibility visibility) {
		if (this.visibility == ItemGroup.Visibility.PARENT_AND_SEARCH_TABS || this.visibility == visibility) {
			stacks.add(stack);
		}
	}
}
