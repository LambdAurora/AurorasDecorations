/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToIntBiFunction;

/**
 * Represents a block item which got derived from another.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class DerivedBlockItem extends BlockItem {
	private final KindSearcher<ItemStack, KindSearcher.StackEntry> searcher;
	private final SearchMethod searchMethod;

	public DerivedBlockItem(Block block, KindSearcher<ItemStack, KindSearcher.StackEntry> searcher, Settings settings) {
		this(block, searcher, KindSearcher::findAfter, settings);
	}

	public DerivedBlockItem(Block block, KindSearcher<ItemStack, KindSearcher.StackEntry> searcher, SearchMethod searchMethod,
			Settings settings) {
		super(block, settings);
		this.searcher = searcher;
		this.searchMethod = searchMethod;
	}

	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (this.isInGroup(group) || group == ItemGroup.SEARCH) {
			stacks.add(this.searchMethod.applyAsInt(this.searcher, stacks), new ItemStack(this));
		}
	}

	public static <B extends Block> BiFunction<B, Settings, BlockItem> itemWithStrictPositionFactory(Item after) {
		return (block, settings) -> newItemWithStrictPosition(block, after, settings);
	}

	public static DerivedBlockItem newItemWithStrictPosition(Block block, Item after, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.strictlyAfter(after), KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem campfire(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.CAMPFIRE_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem door(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.DOOR_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem fence(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.FENCE_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem fenceGate(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.FENCE_GATE_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem flower(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.FLOWER_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem hopper(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.HOPPER_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem lantern(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.LANTERN_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem leaves(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.LEAVES_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem log(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.LOG_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem planks(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.PLANKS_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem pressurePlate(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.PRESSURE_PLATE_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem redstoneTorch(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.HOPPER_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem sapling(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.SAPLING_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem strippedLog(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.STRIPPED_LOG_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem strippedWood(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.STRIPPED_WOOD_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem trapdoor(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.TRAPDOOR_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem wall(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.WALL_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem wood(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.WOOD_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem woodenButton(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.WOODEN_BUTTON_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem woodenSlab(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.WOODEN_SLAB_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public static DerivedBlockItem woodenStairs(Block block, Settings settings) {
		return new DerivedBlockItem(block, KindSearcher.WOODEN_STAIRS_SEARCHER, KindSearcher::findLastOfGroup, settings);
	}

	public interface SearchMethod extends ToIntBiFunction<KindSearcher<ItemStack, KindSearcher.StackEntry>, List<ItemStack>> {
	}
}
