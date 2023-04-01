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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.PetBedBlock;
import dev.lambdaurora.aurorasdeco.block.SleepingBagBlock;
import dev.lambdaurora.aurorasdeco.item.SignPostItem;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BedBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry.*;

public class ItemTree extends ItemTreeGroupNode {
	private static final Identifier ROOT = AurorasDeco.id("root");
	private static final ItemTreeGroupNode BLACKBOARDS = ItemTreeGroupNode.create(AurorasDeco.id("blackboard"), groupNode -> {
		groupNode.add(BLACKBOARD_BLOCK);
		groupNode.add(WAXED_BLACKBOARD_BLOCK, ItemGroup.Visibility.SEARCH_TAB_ONLY);
		groupNode.add(CHALKBOARD_BLOCK);
		groupNode.add(WAXED_CHALKBOARD_BLOCK, ItemGroup.Visibility.SEARCH_TAB_ONLY);
		groupNode.add(GLASSBOARD_BLOCK);
		groupNode.add(WAXED_GLASSBOARD_BLOCK, ItemGroup.Visibility.SEARCH_TAB_ONLY);
		//groupNode.add(BLACKBOARD_PRESS_BLOCK);
	});
	public static final ItemTreeGroupNode BENCHES = new ItemTreeGroupNode(AurorasDeco.id("bench"));
	public static final ItemTreeGroupNode SEAT_RESTS = new ItemTreeGroupNode(AurorasDeco.id("seat_rest"));
	public static final ItemTreeGroupNode SHELVES = new ItemTreeGroupNode(AurorasDeco.id("shelf"));
	public static final ItemTreeGroupNode SMALL_LOG_PILES = new ItemTreeGroupNode(AurorasDeco.id("small_log_pile"));
	public static final ItemTreeGroupNode STUMPS = new ItemTreeGroupNode(AurorasDeco.id("stump"));

	public ItemTree() {super(ROOT);}

	public static ItemTree fromStacks(List<ItemStack> displayStacks, List<ItemStack> searchStacks) {
		var tree = new ItemTree();
		var nodes = new ArrayList<ItemTreeItemNode>();

		for (var stack : displayStacks) {
			nodes.add(new ItemTreeItemNode(stack, ItemGroup.Visibility.PARENT_TAB_ONLY));
		}

		for (int i = 0; i < searchStacks.size(); i++) {
			ItemStack current = searchStacks.get(i);
			ItemStack previous = i == 0 ? null : searchStacks.get(i - 1);
			int foundIndex = -1;

			for (int j = 0; j < nodes.size(); j++) {
				ItemTreeItemNode node = nodes.get(j);

				if (ItemStack.canCombine(node.stack(), current)) {
					node.setVisibility(ItemGroup.Visibility.PARENT_AND_SEARCH_TABS);
					foundIndex = -1;
					break;
				} else if (previous != null && ItemStack.canCombine(node.stack(), previous)) {
					foundIndex = j + 1;
				}
			}

			if (foundIndex != -1) {
				nodes.add(foundIndex, new ItemTreeItemNode(current, ItemGroup.Visibility.SEARCH_TAB_ONLY));
			}
		}

		tree.nodes.addAll(nodes);
		return tree;
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(modifyItems(ItemTree::modifyBuildingBlocks));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(modifyItems(ItemTree::modifyColoredBlocks));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL_BLOCKS).register(modifyItems(ItemTree::modifyNaturalBlocks));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL_BLOCKS).register(modifyItems(ItemTree::modifyFunctionalBlocks));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE_BLOCKS).register(modifyItems(ItemTree::modifyRedstoneBlocks));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS_AND_UTILITIES).register(modifyItems(ItemTree::modifyToolsAndUtilities));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
				.register(entries -> entries.addAfter(Items.NETHERITE_INGOT, COPPER_SULFATE_ITEM));
	}

	@SuppressWarnings("UnstableApiUsage")
	private static ItemGroupEvents.ModifyEntries modifyItems(Consumer<ItemTree> modifier) {
		return entries -> {
			var tree = fromStacks(entries.getDisplayStacks(), entries.getSearchTabStacks());

			modifier.accept(tree);

			entries.getDisplayStacks().clear();
			entries.getSearchTabStacks().clear();
			tree.build(entries.getDisplayStacks(), ItemGroup.Visibility.PARENT_TAB_ONLY);
			tree.build(entries.getSearchTabStacks(), ItemGroup.Visibility.SEARCH_TAB_ONLY);
		};
	}

	private static void modifyBuildingBlocks(ItemTree tree) {
		var cherry = tree.collectItemsAsGroup(new Identifier("minecraft", "cherry"),
				Items.CHERRY_LOG, Items.CHERRY_BUTTON
		);

		var azaleaGroup = new ItemTreeGroupNode(AurorasDeco.id("azalea"));
		azaleaGroup.add(AZALEA_LOG_BLOCK);
		azaleaGroup.add(AZALEA_WOOD_BLOCK);
		azaleaGroup.add(FLOWERING_AZALEA_LOG_BLOCK);
		azaleaGroup.add(FLOWERING_AZALEA_WOOD_BLOCK);
		azaleaGroup.add(STRIPPED_AZALEA_LOG_BLOCK);
		azaleaGroup.add(STRIPPED_AZALEA_WOOD_BLOCK);
		azaleaGroup.add(AZALEA_PLANKS_BLOCK);
		azaleaGroup.add(AZALEA_STAIRS_BLOCK);
		azaleaGroup.add(AZALEA_SLAB_BLOCK);
		azaleaGroup.add(AZALEA_FENCE_BLOCK);
		azaleaGroup.add(AZALEA_FENCE_GATE_BLOCK);
		azaleaGroup.add(AZALEA_DOOR);
		azaleaGroup.add(AZALEA_TRAPDOOR);
		azaleaGroup.add(AZALEA_PRESSURE_PLATE_BLOCK);
		azaleaGroup.add(AZALEA_BUTTON_BLOCK);
		tree.addAfter(cherry, azaleaGroup);

		var jacarandaGroup = new ItemTreeGroupNode(AurorasDeco.id("jacaranda"));
		jacarandaGroup.add(JACARANDA_LOG_BLOCK);
		jacarandaGroup.add(JACARANDA_WOOD_BLOCK);
		jacarandaGroup.add(STRIPPED_JACARANDA_LOG_BLOCK);
		jacarandaGroup.add(STRIPPED_JACARANDA_WOOD_BLOCK);
		jacarandaGroup.add(JACARANDA_PLANKS_BLOCK);
		jacarandaGroup.add(JACARANDA_STAIRS_BLOCK);
		jacarandaGroup.add(JACARANDA_SLAB_BLOCK);
		jacarandaGroup.add(JACARANDA_FENCE_BLOCK);
		jacarandaGroup.add(JACARANDA_FENCE_GATE_BLOCK);
		jacarandaGroup.add(JACARANDA_DOOR);
		jacarandaGroup.add(JACARANDA_TRAPDOOR);
		jacarandaGroup.add(JACARANDA_PRESSURE_PLATE_BLOCK);
		jacarandaGroup.add(JACARANDA_BUTTON_BLOCK);
		tree.addAfter(azaleaGroup, jacarandaGroup);

		tree.addAfter(Items.NETHER_BRICK_FENCE, NETHER_BRICK_FENCE_GATE);
		tree.addAfter(Items.POLISHED_BASALT, POLISHED_BASALT_WALL);

		var andesite = tree.collectItemsAsGroup(new Identifier("andesite"), Items.ANDESITE, Items.POLISHED_ANDESITE_SLAB);
		tree.addAfter(andesite, CALCITE_DERIVATOR.getGroupNode());
		tree.addAfter(CALCITE_DERIVATOR.getGroupNode(), TUFF_DERIVATOR.getGroupNode());

		var deepslateBricks = tree.collectItemsAsGroup(new Identifier("deepslate_bricks"),
				Items.DEEPSLATE_BRICKS, Items.DEEPSLATE_BRICK_WALL
		);
		tree.addAfter(deepslateBricks, MOSSY_DEEPSLATE_BRICKS_DERIVATOR.getGroupNode());
	}

	private static void modifyColoredBlocks(ItemTree tree) {
		insertBedStuff(tree);
	}

	private static void modifyNaturalBlocks(ItemTree tree) {
		var logs = tree.collectItemsAsGroup(new Identifier("minecraft", "logs"),
				Items.OAK_LOG, Items.WARPED_STEM
		);

		logs.addAfter(Items.CHERRY_LOG, AZALEA_LOG_BLOCK, FLOWERING_AZALEA_LOG_BLOCK, JACARANDA_LOG_BLOCK);

		var leaves = tree.collectItemsAsGroup(new Identifier("minecraft", "leaves"),
				Items.OAK_LEAVES, Items.FLOWERING_AZALEA_LEAVES
		);
		var saplings = tree.collectItemsAsGroup(new Identifier("minecraft", "saplings"),
				Items.OAK_SAPLING, Items.FLOWERING_AZALEA
		);

		leaves.add(AurorasDecoPlants.JACARANDA_LEAVES);
		leaves.add(AurorasDecoPlants.BUDDING_JACARANDA_LEAVES);
		leaves.add(AurorasDecoPlants.FLOWERING_JACARANDA_LEAVES);

		saplings.add(AurorasDecoPlants.JACARANDA_SAPLING);

		var smallFlowers = tree.collectItemsAsGroup(new Identifier("minecraft", "small_flowers"),
				stack -> stack.isIn(ItemTags.SMALL_FLOWERS)
		);
		smallFlowers.add(AurorasDecoPlants.DAFFODIL.item());
		smallFlowers.add(AurorasDecoPlants.LAVENDER.item());

		tree.addAfter(Items.LILY_PAD, AurorasDecoPlants.DUCKWEED.item());
	}

	private static void modifyFunctionalBlocks(ItemTree tree) {
		var torches = tree.collectItemsAsGroup(new Identifier("minecraft", "torch"),
				Items.TORCH, Items.REDSTONE_TORCH
		);

		torches.addAfter(Items.SOUL_TORCH, COPPER_SULFATE_TORCH_ITEM);

		var lanterns = tree.collectItemsAsGroup(new Identifier("minecraft", "lantern"),
				stack -> stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LanternBlock
		);

		lanterns.add(AMETHYST_LANTERN_BLOCK);
		lanterns.add(COPPER_SULFATE_LANTERN_BLOCK);
		lanterns.add(REDSTONE_LANTERN_BLOCK);

		var campfires = tree.collectItemsAsGroup(new Identifier("minecraft", "campfire"),
				stack -> stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CampfireBlock
		);

		campfires.add(COPPER_SULFATE_CAMPFIRE_BLOCK);

		var braziers = new ItemTreeGroupNode(AurorasDeco.id("braziers"));

		braziers.add(BRAZIER_BLOCK);
		braziers.add(SOUL_BRAZIER_BLOCK);
		braziers.add(COPPER_SULFATE_BRAZIER_BLOCK);

		tree.addAfter(campfires, braziers);

		tree.addAfter(Items.STONECUTTER, SAWMILL_BLOCK);
		tree.addAfter(Items.BELL, WIND_CHIME_BLOCK);
		tree.addAfter(Items.FLOWER_POT, BIG_FLOWER_POT_BLOCK);

		var itemFrames = tree.collectItemsAsGroup(new Identifier("minecraft", "item_frame"),
				Items.ITEM_FRAME, Items.GLOW_ITEM_FRAME
		);
		tree.addAfter(itemFrames, BLACKBOARDS);

		var signs = tree.collectItemsAsGroup(new Identifier("minecraft", "sign"),
				Items.OAK_SIGN, Items.WARPED_HANGING_SIGN
		);
		signs.addAfter(Items.CHERRY_HANGING_SIGN,
				AZALEA_SIGNS.signItem(), AZALEA_SIGNS.hangingSignItem(),
				JACARANDA_SIGNS.signItem(), JACARANDA_SIGNS.hangingSignItem()
		);
		SignPostItem.insertIntoSignsNode(signs);

		var storage = tree.collectItemsAsGroup(new Identifier("minecraft", "storage"),
				Items.CHEST, Items.PINK_SHULKER_BOX
		);
		storage.add(SHELVES);

		tree.addAfter(storage, SMALL_LOG_PILES);
		tree.addAfter(SMALL_LOG_PILES, STUMPS);
		tree.addAfter(STUMPS, BENCHES);

		insertBedStuff(tree);
	}

	private static void modifyRedstoneBlocks(ItemTree tree) {
		tree.addAfter(Items.REDSTONE_TORCH, REDSTONE_LANTERN_BLOCK);
		tree.addAfter(Items.HOPPER, COPPER_HOPPER_BLOCK);
		tree.addAfter(Items.OBSERVER, STURDY_STONE_BLOCK);
	}

	private static void modifyToolsAndUtilities(ItemTree tree) {
		tree.addAfter(Items.BRUSH, PAINTER_PALETTE_ITEM);
		tree.addAfter(Items.TNT_MINECART, SEAT_RESTS);

		var boats = tree.collectItemsAsGroup(new Identifier("minecraft", "boat"),
				Items.OAK_BOAT, Items.BAMBOO_CHEST_RAFT
		);
		boats.addAfter(Items.CHERRY_CHEST_BOAT,
				AZALEA_BOAT_ITEM, AZALEA_CHEST_BOAT_ITEM,
				JACARANDA_BOAT_ITEM, JACARANDA_CHEST_BOAT_ITEM
		);
	}

	private static void insertBedStuff(ItemTree tree) {
		var beds = tree.collectItemsAsGroup(new Identifier("minecraft", "bed"),
				stack -> stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof BedBlock
		);
		tree.addAfter(beds, SleepingBagBlock.SLEEPING_BAGS_ITEM_GROUP_NODE);
		tree.addAfter(SleepingBagBlock.SLEEPING_BAGS_ITEM_GROUP_NODE, PetBedBlock.PET_BEDS_ITEM_GROUP_NODE);
	}
}
