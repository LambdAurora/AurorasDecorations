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

package dev.lambdaurora.aurorasdeco.util;

import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Utility class to search objects of the same "kind" in a list.
 *
 * @param <I> the input type
 * @param <O> the tested type
 */
public class KindSearcher<I, O> {
	private final Predicate<O> tester;
	private final Function<I, O> mapper;
	private final ToIntFunction<List<I>> after;

	public KindSearcher(Predicate<O> tester, Function<I, O> mapper, ToIntFunction<List<I>> after) {
		this.tester = tester;
		this.mapper = mapper;
		this.after = after;
	}

	public int findLastOfGroup(List<I> list) {
		int appendIndex = list.size();

		boolean found = false;
		for (int i = 0; i < list.size(); i++) {
			var obj = this.mapper.apply(list.get(i));

			if (this.tester.test(obj)) {
				found = true;
			} else if (found) {
				appendIndex = i;
				break;
			}
		}

		return appendIndex;
	}

	public int findLast(List<I> list) {
		int appendIndex = list.size();

		for (int i = 0; i < list.size(); i++) {
			var obj = this.mapper.apply(list.get(i));

			if (this.tester.test(obj))
				appendIndex = i + 1;
		}

		return appendIndex;
	}

	public int findAfter(List<I> list) {
		int start = this.after.applyAsInt(list);
		if (start == -1) return list.size();

		for (int i = start; i < list.size(); i++) {
			var obj = this.mapper.apply(list.get(i));

			if (this.tester.test(obj))
				start = i + 1;
		}

		return start;
	}

	public static <I> Builder<I, I> identitySearcher(Predicate<I> tester) {
		return new Builder<>(tester, Function.identity());
	}

	public static <I, O, B extends O> Builder<I, O> assignableSearcher(Class<B> baseClass, Function<I, O> mapper) {
		return new Builder<>(o -> baseClass.isAssignableFrom(o.getClass()), mapper);
	}

	public static Builder<ItemStack, StackEntry> itemIdentifierSearcher(Predicate<StackEntry> tester) {
		return new Builder<>(tester, stack -> new StackEntry(stack, Registry.ITEM.getId(stack.getItem())));
	}

	public static KindSearcher<ItemStack, StackEntry> strictlyAfter(Item item) {
		return KindSearcher.itemIdentifierSearcher(entry -> entry.stack().isOf(item))
				.afterMapped(item, ItemStack::getItem)
				.build();
	}

	public record StackEntry(ItemStack stack, Identifier id) {
	}

	public static class Builder<I, O> {
		private final Predicate<O> tester;
		private final Function<I, O> mapper;
		private ToIntFunction<List<I>> after = o -> 0;

		public Builder(Predicate<O> tester, Function<I, O> mapper) {
			this.tester = tester;
			this.mapper = mapper;
		}

		public Builder<I, O> after(ToIntFunction<List<I>> after) {
			this.after = after;
			return this;
		}

		public Builder<I, O> after(I after) {
			this.after = list -> list.indexOf(after);
			return this;
		}

		public <T> Builder<I, O> afterMapped(T after, Function<I, T> mapper) {
			this.after = list -> {
				for (int i = 0; i < list.size(); i++) {
					var obj = mapper.apply(list.get(i));

					if (after.equals(obj))
						return i + 1;
				}
				return list.size();
			};
			return this;
		}

		public KindSearcher<I, O> build() {
			return new KindSearcher<>(
					this.tester,
					this.mapper,
					this.after
			);
		}
	}

	public static final KindSearcher<ItemStack, StackEntry> CAMPFIRE_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CampfireBlock
	)
			.afterMapped(Items.SOUL_CAMPFIRE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> DOOR_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof DoorBlock
	)
			.afterMapped(Items.WARPED_DOOR, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> FENCE_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FenceBlock
	)
			.afterMapped(Items.WARPED_FENCE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> FENCE_GATE_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FenceGateBlock
	)
			.afterMapped(Items.WARPED_FENCE_GATE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> FLOWER_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FlowerBlock
	)
			.afterMapped(Items.WITHER_ROSE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> HOPPER_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof HopperBlock
	)
			.afterMapped(Items.HOPPER, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> LANTERN_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LanternBlock
	)
			.afterMapped(Items.SOUL_LANTERN, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> LEAVES_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LeavesBlock
	)
			.afterMapped(Items.FLOWERING_AZALEA_LEAVES, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> PRESSURE_PLATE_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PressurePlateBlock
	)
			.afterMapped(Items.WARPED_PRESSURE_PLATE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> TORCH_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TorchBlock
	)
			.afterMapped(Items.SOUL_TORCH, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> TRAPDOOR_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TrapdoorBlock
	)
			.afterMapped(Items.WARPED_TRAPDOOR, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> WALL_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WallBlock
	)
			.afterMapped(Items.COBBLESTONE_WALL, ItemStack::getItem)
			.build();

	/* Wood */

	public static final KindSearcher<ItemStack, StackEntry> LOG_SEARCHER = itemIdentifierSearcher(
			entry -> {
				if (entry.stack().getItem() instanceof BlockItem blockItem) {
					if (blockItem.getBlock() instanceof PillarBlock block) {
						Identifier id = Registry.ITEM.getId(blockItem);

						if (block.getDefaultState().getMaterial() == Material.WOOD || block.getDefaultState().getMaterial() == Material.NETHER_WOOD) {
							return !id.getPath().startsWith("stripped") && (id.getPath().endsWith("log") || id.getPath().endsWith("stem"));
						} else if (block.getDefaultState().getMaterial() == Material.SOIL) {
							return !id.getPath().startsWith("stripped") && id.getPath().endsWith("roots");
						}
					} else return blockItem.getBlock() instanceof MangroveRootsBlock;
				}

				return false;
			}
	)
			.afterMapped(Items.WARPED_STEM, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> PLANKS_SEARCHER = itemIdentifierSearcher(
			entry -> {
				if (entry.stack().getItem() instanceof BlockItem blockItem
						&& blockItem.getBlock().getSoundGroup(blockItem.getBlock().getDefaultState()) == BlockSoundGroup.WOOD) {
					Identifier id = Registry.ITEM.getId(blockItem);
					return id.getPath().contains("planks");
				}

				return false;
			}
	)
			.afterMapped(Items.WARPED_PLANKS, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> SAPLING_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock
	)
			.afterMapped(Items.DARK_OAK_SAPLING, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> STRIPPED_LOG_SEARCHER = itemIdentifierSearcher(
			entry -> {
				if (entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PillarBlock block
						&& (block.getDefaultState().getMaterial() == Material.WOOD || block.getDefaultState().getMaterial() == Material.NETHER_WOOD)) {
					Identifier id = Registry.ITEM.getId(blockItem);
					return id.getPath().startsWith("stripped") && (id.getPath().endsWith("log") || id.getPath().endsWith("stem"));
				}

				return false;
			}
	)
			.afterMapped(Items.STRIPPED_WARPED_STEM, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> STRIPPED_WOOD_SEARCHER = itemIdentifierSearcher(
			entry -> {
				if (entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PillarBlock block
						&& (block.getDefaultState().getMaterial() == Material.WOOD || block.getDefaultState().getMaterial() == Material.NETHER_WOOD)) {
					Identifier id = Registry.ITEM.getId(blockItem);
					return id.getPath().startsWith("stripped") && (id.getPath().endsWith("wood") || id.getPath().endsWith("hyphae"));
				}

				return false;
			}
	)
			.afterMapped(Items.STRIPPED_WARPED_HYPHAE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> WOOD_SEARCHER = itemIdentifierSearcher(
			entry -> {
				if (entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof PillarBlock block
						&& (block.getDefaultState().getMaterial() == Material.WOOD || block.getDefaultState().getMaterial() == Material.NETHER_WOOD)) {
					Identifier id = Registry.ITEM.getId(blockItem);
					return !id.getPath().startsWith("stripped") && (id.getPath().endsWith("wood") || id.getPath().endsWith("hyphae"));
				}

				return false;
			}
	)
			.afterMapped(Items.WARPED_HYPHAE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> WOODEN_BUTTON_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WoodenButtonBlock
	)
			.afterMapped(Items.WARPED_PRESSURE_PLATE, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> WOODEN_SLAB_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SlabBlock block && (
					block.getDefaultState().getMaterial() == Material.WOOD || block.getDefaultState().getMaterial() == Material.NETHER_WOOD
			)
	)
			.afterMapped(Items.WARPED_SLAB, ItemStack::getItem)
			.build();
	public static final KindSearcher<ItemStack, StackEntry> WOODEN_STAIRS_SEARCHER = itemIdentifierSearcher(
			entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof StairsBlock block && (
					block.getDefaultState().getMaterial() == Material.WOOD || block.getDefaultState().getMaterial() == Material.NETHER_WOOD
			)
	)
			.afterMapped(Items.WARPED_STAIRS, ItemStack::getItem)
			.build();
}
