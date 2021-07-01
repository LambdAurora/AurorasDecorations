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

import net.minecraft.block.CampfireBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    public static final KindSearcher<ItemStack, StackEntry> LANTERN_SEARCHER = itemIdentifierSearcher(
            entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LanternBlock
    )
            .afterMapped(Items.SOUL_LANTERN, ItemStack::getItem)
            .build();
    public static final KindSearcher<ItemStack, StackEntry> TORCH_SEARCHER = itemIdentifierSearcher(
            entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof TorchBlock
    )
            .afterMapped(Items.SOUL_TORCH, ItemStack::getItem)
            .build();
    public static final KindSearcher<ItemStack, StackEntry> WALL_SEARCHER = itemIdentifierSearcher(
            entry -> entry.stack().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof WallBlock
    )
            .afterMapped(Items.COBBLESTONE_WALL, ItemStack::getItem)
            .build();
}
