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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemTreeGroupNode implements ItemTreeNode {
	private final Identifier id;
	protected final List<ItemTreeNode> nodes = new ArrayList<>();
	private final Map<Identifier, ItemTreeGroupNode> groupNodes = new Object2ObjectOpenHashMap<>();
	private ItemGroup.Visibility visibility = ItemGroup.Visibility.PARENT_AND_SEARCH_TABS;

	public ItemTreeGroupNode(Identifier id) {this.id = id;}

	public static ItemTreeGroupNode create(Identifier id, Consumer<ItemTreeGroupNode> consumer) {
		var group = new ItemTreeGroupNode(id);
		consumer.accept(group);
		return group;
	}

	public boolean contains(ItemConvertible item) {
		return this.nodes.parallelStream().anyMatch(node -> {
			if (node instanceof ItemTreeItemNode itemNode) {
				return itemNode.stack().getItem() == item.asItem();
			} else {
				return false;
			}
		});
	}

	public void add(ItemStack stack, ItemGroup.Visibility visibility) {
		this.nodes.add(new ItemTreeItemNode(stack, visibility));
	}

	public void add(ItemStack stack) {
		this.add(stack, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	public void add(int index, ItemStack stack, ItemGroup.Visibility visibility) {
		this.nodes.add(index, new ItemTreeItemNode(stack, visibility));
	}

	public void add(int index, ItemStack stack) {
		this.add(index, stack, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	public void add(ItemConvertible item) {
		this.add(new ItemStack(item));
	}

	public void add(ItemConvertible item, ItemGroup.Visibility visibility) {
		this.add(new ItemStack(item), visibility);
	}

	public void add(ItemTreeGroupNode groupNode) {
		this.nodes.add(groupNode);
		this.groupNodes.put(groupNode.id, groupNode);

		this.groupNodes.putAll(groupNode.groupNodes);
	}

	public void add(int index, ItemTreeGroupNode groupNode) {
		this.nodes.add(index, groupNode);
		this.groupNodes.put(groupNode.id, groupNode);

		this.groupNodes.putAll(groupNode.groupNodes);
	}

	private int addRelative(ItemStack toFind, ItemTreeNode node, int offset) {
		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i) instanceof ItemTreeItemNode item) {
				if (ItemStack.canCombine(item.stack(), toFind)) {
					this.nodes.add(i + offset, node);
					return i + offset;
				}
			}
		}

		return -1;
	}

	public int addBefore(ItemStack toFind, ItemTreeNode node) {
		return this.addRelative(toFind, node, 0);
	}

	public int addBefore(ItemStack toFind, ItemStack toAdd, ItemGroup.Visibility visibility) {
		return this.addBefore(toFind, new ItemTreeItemNode(toAdd, visibility));
	}

	public int addBefore(ItemStack toFind, ItemStack toAdd) {
		return this.addBefore(toFind, toAdd, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	public void addBefore(ItemStack toFind, ItemStack... toAdd) {
		int inserted = this.addBefore(toFind, toAdd[0]);

		for (int i = 1; i < toAdd.length; i++) {
			nodes.add(inserted + i, new ItemTreeItemNode(toAdd[i]));
		}
	}

	public void addBefore(ItemConvertible toFind, ItemConvertible... toAdd) {
		this.addBefore(new ItemStack(toFind), Arrays.stream(toAdd).map(ItemStack::new).toArray(ItemStack[]::new));
	}

	public int addAfter(ItemStack toFind, ItemTreeNode node) {
		return this.addRelative(toFind, node, 1);
	}

	public int addAfter(ItemStack toFind, ItemStack toAdd, ItemGroup.Visibility visibility) {
		return this.addAfter(toFind, new ItemTreeItemNode(toAdd, visibility));
	}

	public int addAfter(ItemStack toFind, ItemStack toAdd) {
		return this.addAfter(toFind, toAdd, ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
	}

	public void addAfter(ItemStack toFind, ItemStack... toAdd) {
		int inserted = this.addAfter(toFind, toAdd[0]);

		for (int i = 1; i < toAdd.length; i++) {
			nodes.add(inserted + i, new ItemTreeItemNode(toAdd[i]));
		}
	}

	public int addAfter(ItemConvertible toFind, ItemTreeNode node) {
		return this.addAfter(new ItemStack(toFind), node);
	}

	public void addAfter(ItemConvertible toFind, ItemConvertible... toAdd) {
		this.addAfter(new ItemStack(toFind), Arrays.stream(toAdd).map(ItemStack::new).toArray(ItemStack[]::new));
	}

	public void addAfter(ItemStack toFind, ItemConvertible toAdd) {
		this.addAfter(toFind, new ItemStack(toAdd));
	}

	public void addAfter(ItemConvertible toFind, ItemConvertible toAdd) {
		this.addAfter(new ItemStack(toFind), toAdd);
	}

	public void addAfter(ItemTreeGroupNode toFind, ItemTreeGroupNode toAdd) {
		int index = this.nodes.indexOf(toFind);
		this.add(index + 1, toAdd);
	}

	public ItemTreeGroupNode getGroup(Identifier id) {
		return this.groupNodes.get(id);
	}

	public @Nullable ItemTreeGroupNode collectItemsAsGroup(Identifier id, ItemConvertible from, ItemConvertible to) {
		return this.collectItemsAsGroup(id, new ItemStack(from), new ItemStack(to));
	}

	public @Nullable ItemTreeGroupNode collectItemsAsGroup(Identifier id, ItemStack from, ItemStack to) {
		int start = -1, end = -1;

		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i) instanceof ItemTreeItemNode item) {
				if (ItemStack.canCombine(item.stack(), from)) {
					start = i;
				}
				if (ItemStack.canCombine(item.stack(), to)) {
					end = i;
					break;
				}
			}
		}

		if (start == -1 || end == -1) return null;
		if (end < start) return null;

		return this.replaceNodesWithGroup(id, start, end);
	}

	public @Nullable ItemTreeGroupNode collectItemsAsGroup(Identifier id, Predicate<ItemStack> collector) {
		int start = -1, end = -1;

		for (int i = 0; i < this.nodes.size(); i++) {
			if (this.nodes.get(i) instanceof ItemTreeItemNode item) {
				if (collector.test(item.stack())) {
					if (start == -1) {
						start = i;
					}
				} else if (start != -1) {
					end = i - 1;
					break;
				}
			}
		}

		if (start == -1 || end == -1) return null;
		if (end < start) return null;

		return this.replaceNodesWithGroup(id, start, end);
	}

	private ItemTreeGroupNode replaceNodesWithGroup(Identifier id, int start, int end) {
		var group = new ItemTreeGroupNode(id);
		group.nodes.addAll(this.nodes.subList(start, end + 1));
		group.detectGroups();

		this.nodes.removeAll(group.nodes);

		this.add(start, group);

		return group;
	}

	public List<ItemTreeNode> getNodes() {
		return Collections.unmodifiableList(this.nodes);
	}

	@Override
	public ItemGroup.Visibility getVisibility() {
		return this.visibility;
	}

	@Override
	public void build(Collection<ItemStack> stacks, FeatureFlagBitSet enabledFeatures, ItemGroup.Visibility visibility) {
		if (this.visibility == ItemGroup.Visibility.PARENT_AND_SEARCH_TABS || this.visibility == visibility) {
			for (var node : this.nodes) {
				node.build(stacks, enabledFeatures, visibility);
			}
		}
	}

	private void detectGroups() {
		for (var node : this.nodes) {
			if (node instanceof ItemTreeGroupNode groupNode) {
				this.groupNodes.putIfAbsent(groupNode.id, groupNode);
			}
		}
	}
}
