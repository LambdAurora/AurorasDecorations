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

package dev.lambdaurora.aurorasdeco.util;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.AuroraStairsBlock;
import dev.lambdaurora.aurorasdeco.item.DerivedBlockItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * An utility to make derivations of other blocks.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class Derivator {
    private final BlockState base;
    private final String baseName;

    public Derivator(BlockState base) {
        this.base = base;
        this.baseName = Registry.BLOCK.getId(base.getBlock()).getPath();
    }

    public SlabBlock slab() {
        var item = base.getBlock().asItem();
        return registerWithItem(this.baseName, "slab", new SlabBlock(FabricBlockSettings.copyOf(this.base.getBlock())),
                derivativeSearcher("slab").build(),
                new FabricItemSettings()
                        .group(item.getGroup()));
    }

    public SlabBlock slab(Item after) {
        var item = base.getBlock().asItem();
        return registerWithItem(this.baseName, "slab", new SlabBlock(FabricBlockSettings.copyOf(this.base.getBlock())),
                closeDerivativeSearcher("slab").afterMapped(after, ItemStack::getItem).build(),
                new FabricItemSettings()
                        .group(item.getGroup()));
    }

    public StairsBlock stairs() {
        var item = base.getBlock().asItem();
        return registerWithItem(this.baseName, "stairs", new AuroraStairsBlock(this.base,
                        FabricBlockSettings.copyOf(this.base.getBlock())),
                derivativeSearcher("stairs").build(),
                new FabricItemSettings()
                        .group(item.getGroup()));
    }

    public StairsBlock stairs(Item after) {
        var item = base.getBlock().asItem();
        return registerWithItem(this.baseName, "stairs", new AuroraStairsBlock(this.base,
                        FabricBlockSettings.copyOf(this.base.getBlock())),
                closeDerivativeSearcher("stairs").afterMapped(after, ItemStack::getItem).build(),
                new FabricItemSettings()
                        .group(item.getGroup()));
    }

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(Registry.BLOCK, id(name), block);
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registry.ITEM, id(name), item);
    }

    private static KindSearcher.Builder<ItemStack, KindSearcher.StackEntry> derivativeSearcher(String derivative) {
        return KindSearcher.itemIdentifierSearcher(entry ->
                (entry.id().getNamespace().equals("minecraft") || entry.id().getNamespace().equals(AurorasDeco.NAMESPACE))
                        && entry.id().getPath().endsWith(derivative));
    }

    private static KindSearcher.Builder<ItemStack, KindSearcher.StackEntry> closeDerivativeSearcher(String derivative) {
        return KindSearcher.itemIdentifierSearcher(entry ->
                (entry.id().getNamespace().equals("minecraft") || entry.id().getNamespace().equals(AurorasDeco.NAMESPACE))
                        && entry.id().getPath().endsWith(derivative) && entry.stack().getItem() instanceof DerivedBlockItem);
    }

    private static <T extends Block> T registerWithItem(String base, String derivative, T block,
                                                        KindSearcher<ItemStack, KindSearcher.StackEntry> searcher,
                                                        Item.Settings settings) {
        var name = base + '_' + derivative;
        register(name, block);
        register(name, new DerivedBlockItem(block, searcher, settings));
        return block;
    }
}
