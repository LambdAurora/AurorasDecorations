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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.item.DerivedBlockItem;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

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
	private final String normalBaseName;
	private final String singularBaseName;

	public Derivator(BlockState base) {
		this.base = base;
		this.normalBaseName = Registry.BLOCK.getId(base.getBlock()).getPath();
		this.singularBaseName = this.normalBaseName.endsWith("bricks")
				? this.normalBaseName.substring(0, this.normalBaseName.length() - 1)
				: this.normalBaseName;
	}

	public Block mossy() {
		var derivative = new Derivative("mossy", true);
		var item = base.getBlock().asItem();
		return registerWithItem(this.normalBaseName, derivative,
				new Block(QuiltBlockSettings.copyOf(this.base.getBlock())),
				this.derivativeSearcher(new Derivative(this.normalBaseName, false)).build(),
				new QuiltItemSettings().group(item.getGroup()));
	}

	public Block cracked() {
		var derivative = new Derivative("cracked", true);
		var item = base.getBlock().asItem();
		return registerWithItem(this.normalBaseName, derivative,
				new Block(QuiltBlockSettings.copyOf(this.base.getBlock())),
				this.derivativeSearcher(new Derivative(this.normalBaseName, false)).build(),
				new QuiltItemSettings().group(item.getGroup()));
	}

	public Block chiseled() {
		var derivative = new Derivative("chiseled", true);
		var item = base.getBlock().asItem();
		return registerWithItem(this.normalBaseName, derivative,
				new Block(QuiltBlockSettings.copyOf(this.base.getBlock())),
				this.derivativeSearcher(new Derivative(this.normalBaseName, false)).build(),
				new QuiltItemSettings().group(item.getGroup()));
	}

	public WallBlock wall() {
		var derivative = new Derivative("wall", false);
		var name = derivative.apply(this.singularBaseName);
		var block = register(name, new WallBlock(QuiltBlockSettings.copyOf(this.base.getBlock())));
		register(name, new DerivedBlockItem(block, KindSearcher.WALL_SEARCHER, KindSearcher::findLastOfGroup,
				new QuiltItemSettings().group(ItemGroup.DECORATIONS)));
		return block;
	}

	public SlabBlock slab() {
		var derivative = new Derivative("slab", false);
		var item = base.getBlock().asItem();
		return registerWithItem(this.singularBaseName, derivative,
				new SlabBlock(QuiltBlockSettings.copyOf(this.base.getBlock())),
				derivativeSearcher(derivative).build(),
				new QuiltItemSettings()
						.group(item.getGroup()));
	}

	public SlabBlock slab(Item after) {
		var derivative = new Derivative("slab", false);
		var item = base.getBlock().asItem();
		return registerWithItem(this.singularBaseName, derivative,
				new SlabBlock(QuiltBlockSettings.copyOf(this.base.getBlock())),
				closeDerivativeSearcher(derivative).afterMapped(after, ItemStack::getItem).build(),
				new QuiltItemSettings()
						.group(item.getGroup()));
	}

	public StairsBlock stairs() {
		var derivative = new Derivative("stairs", false);
		var item = base.getBlock().asItem();
		return registerWithItem(this.singularBaseName, derivative, new StairsBlock(this.base,
						QuiltBlockSettings.copyOf(this.base.getBlock())),
				derivativeSearcher(derivative).build(),
				new QuiltItemSettings()
						.group(item.getGroup()));
	}

	public StairsBlock stairs(Item after) {
		var derivative = new Derivative("stairs", false);
		var item = base.getBlock().asItem();
		return registerWithItem(this.singularBaseName, derivative, new StairsBlock(this.base,
						QuiltBlockSettings.copyOf(this.base.getBlock())),
				closeDerivativeSearcher(derivative).afterMapped(after, ItemStack::getItem).build(),
				new QuiltItemSettings()
						.group(item.getGroup()));
	}

	private static <T extends Block> T register(String name, T block) {
		return Registry.register(Registry.BLOCK, id(name), block);
	}

	private static <T extends Item> T register(String name, T item) {
		return Registry.register(Registry.ITEM, id(name), item);
	}

	private KindSearcher.Builder<ItemStack, KindSearcher.StackEntry> derivativeSearcher(Derivative derivative) {
		return KindSearcher.itemIdentifierSearcher(entry ->
				(entry.id().getNamespace().equals("minecraft") || entry.id().getNamespace().equals(AurorasDeco.NAMESPACE))
						&& derivative.matches(entry.id().getPath()));
	}

	private static KindSearcher.Builder<ItemStack, KindSearcher.StackEntry> closeDerivativeSearcher(Derivative derivative) {
		return KindSearcher.itemIdentifierSearcher(entry ->
				(entry.id().getNamespace().equals("minecraft") || entry.id().getNamespace().equals(AurorasDeco.NAMESPACE))
						&& derivative.matches(entry.id().getPath()) && entry.stack().getItem() instanceof DerivedBlockItem);
	}

	private static <T extends Block> T registerWithItem(String base, Derivative derivative, T block,
			KindSearcher<ItemStack, KindSearcher.StackEntry> searcher,
			Item.Settings settings) {
		var name = derivative.apply(base);
		register(name, block);
		register(name, new DerivedBlockItem(block, searcher, settings));
		return block;
	}

	public record Derivative(String name, boolean prefix) {
		public String apply(String base) {
			if (this.prefix) return this.name + '_' + base;
			else return base + '_' + this.name;
		}

		public boolean matches(String path) {
			if (this.prefix) return path.startsWith(this.name);
			else return path.endsWith(this.name);
		}
	}
}
