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

package dev.lambdaurora.aurorasdeco.item;

import dev.lambdaurora.aurorasdeco.util.KindSearcher;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemGroupInjector implements ItemGroupEvents.ModifyEntries {
	private static final Map<ItemGroup, ItemGroupInjector> GROUPS = new Reference2ObjectOpenHashMap<>();
	private static final InjectMode ADD = ItemGroup.ItemStackCollector::addStack;

	private final ItemGroup group;
	private final List<InjectData> injections = new ArrayList<>();

	ItemGroupInjector(ItemGroup group) {
		this.group = group;
		this.register();
	}

	public static ItemGroupInjector of(ItemGroup group) {
		return GROUPS.computeIfAbsent(group, ItemGroupInjector::new);
	}

	public static <I extends Item> I withGroup(I item, ItemGroup group) {
		of(group).inject(item);
		return item;
	}

	public void inject(ItemStack stack) {
		this.injections.add(new InjectData(stack));
	}

	public void inject(ItemConvertible item) {
		this.inject(new ItemStack(item));
	}

	public void inject(ItemStack stack, KindSearcher<ItemStack, KindSearcher.StackEntry> searcher, DerivedBlockItem.SearchMethod searchMethod) {
		this.injections.add(new InjectData(stack, createComplexInjectMode(searcher, searchMethod)));
	}

	public void inject(ItemConvertible item,
			KindSearcher<ItemStack, KindSearcher.StackEntry> searcher, DerivedBlockItem.SearchMethod searchMethod
	) {
		this.inject(new ItemStack(item), searcher, searchMethod);
	}

	public void register() {
		ItemGroupEvents.modifyEntriesEvent(this.group).register(this);
	}

	@Override
	public void modifyEntries(FabricItemGroupEntries entries) {
		for (var data : this.injections) {
			data.injectMode.insert(entries, data.stack);
		}
	}

	record InjectData(ItemStack stack, InjectMode injectMode) {
		public InjectData(ItemStack item) {
			this(item, ADD);
		}

		public InjectData(ItemConvertible item) {
			this(new ItemStack(item));
		}
	}

	interface InjectMode {
		void insert(FabricItemGroupEntries entries, ItemStack stack);
	}

	private static InjectMode createComplexInjectMode(
			KindSearcher<ItemStack, KindSearcher.StackEntry> searcher, DerivedBlockItem.SearchMethod searchMethod
	) {
		return (entries, stack) -> {
			searchMethod.injectInto(entries.getDisplayStacks(), searcher, stack);
			searchMethod.injectInto(entries.getSearchTabStacks(), searcher, stack);
		};
	}
}
