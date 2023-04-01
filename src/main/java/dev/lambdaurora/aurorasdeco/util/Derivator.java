/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

import dev.lambdaurora.aurorasdeco.item.group.ItemTreeGroupNode;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * A utility to make derivations of other blocks.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class Derivator {
	private final BlockState base;
	private final String normalBaseName;
	private final String singularBaseName;
	private final ItemTreeGroupNode groupNode;

	public Derivator(BlockState base, @Nullable Derivator parent) {
		this.base = base;
		this.normalBaseName = Registries.BLOCK.getId(base.getBlock()).getPath();
		this.singularBaseName = this.normalBaseName.endsWith("bricks")
				? this.normalBaseName.substring(0, this.normalBaseName.length() - 1)
				: this.normalBaseName;
		this.groupNode = new ItemTreeGroupNode(id(normalBaseName));
		this.groupNode.add(base.getBlock());

		if (parent != null) {
			parent.groupNode.add(this.groupNode);
		}
	}

	public Derivator(BlockState base) {
		this(base, null);
	}

	public ItemTreeGroupNode getGroupNode() {
		return this.groupNode;
	}

	public Block mossy() {
		var derivative = new Derivative("mossy", true);
		return registerWithItem(this.normalBaseName, derivative,
				new Block(QuiltBlockSettings.copyOf(this.base.getBlock())),
				new QuiltItemSettings(), null);
	}

	public Block cracked() {
		var derivative = new Derivative("cracked", true);
		var item = base.getBlock().asItem();
		return registerWithItem(this.normalBaseName, derivative,
				new Block(QuiltBlockSettings.copyOf(this.base.getBlock())),
				new QuiltItemSettings(), this.groupNode);
	}

	public Block chiseled() {
		var derivative = new Derivative("chiseled", true);
		var item = base.getBlock().asItem();
		return registerWithItem(this.normalBaseName, derivative,
				new Block(QuiltBlockSettings.copyOf(this.base.getBlock())),
				new QuiltItemSettings(), this.groupNode);
	}

	public WallBlock wall() {
		var derivative = new Derivative("wall", false);
		var name = derivative.apply(this.singularBaseName);
		var block = register(name, new WallBlock(QuiltBlockSettings.copyOf(this.base.getBlock())));
		this.groupNode.add(register(name, new BlockItem(block, new QuiltItemSettings())));
		return block;
	}

	public SlabBlock slab() {
		var derivative = new Derivative("slab", false);
		return registerWithItem(this.singularBaseName, derivative,
				new SlabBlock(QuiltBlockSettings.copyOf(this.base.getBlock())),
				new QuiltItemSettings(), this.groupNode);
	}

	public StairsBlock stairs() {
		var derivative = new Derivative("stairs", false);
		return registerWithItem(this.singularBaseName, derivative, new StairsBlock(this.base,
						QuiltBlockSettings.copyOf(this.base.getBlock())),
				new QuiltItemSettings(), this.groupNode);
	}

	private static <T extends Block> T register(String name, T block) {
		return Registry.register(Registries.BLOCK, id(name), block);
	}

	private static <T extends Item> T register(String name, T item) {
		return Registry.register(Registries.ITEM, id(name), item);
	}

	private static <T extends Block> T registerWithItem(String base, Derivative derivative, T block,
			Item.Settings settings, @Nullable ItemTreeGroupNode groupNode) {
		var name = derivative.apply(base);
		register(name, block);
		var item = register(name, new BlockItem(block, settings));
		if (groupNode != null) groupNode.add(item);
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
