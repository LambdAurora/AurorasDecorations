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

import dev.lambdaurora.aurorasdeco.util.KindSearcher;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.function.ToIntBiFunction;

/**
 * Represents an block item which got derived from another.
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
        if (this.isIn(group)) {
            stacks.add(this.searchMethod.applyAsInt(this.searcher, stacks), new ItemStack(this));
        }
    }

    public interface SearchMethod extends ToIntBiFunction<KindSearcher<ItemStack, KindSearcher.StackEntry>, List<ItemStack>> {
    }
}
