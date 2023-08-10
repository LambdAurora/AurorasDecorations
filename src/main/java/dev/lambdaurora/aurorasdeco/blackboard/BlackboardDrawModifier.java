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

package dev.lambdaurora.aurorasdeco.blackboard;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@link Blackboard} draw modifier.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class BlackboardDrawModifier {
	private static final List<BlackboardDrawModifier> MODIFIERS = new ArrayList<>();

	public static final BlackboardDrawModifier SHADE_INCREASE = new BlackboardDrawModifier("aurorasdeco.blackboard.modifier.darken", 0xff444444) {
		@Override
		@SuppressWarnings("deprecated")
		public boolean matchItem(Item item) {
			return item.getBuiltInRegistryHolder().isIn(ItemTags.COALS);
		}

		@Override
		public short apply(short colorData) {
			var color = BlackboardColor.fromRaw(colorData);

			if (color == BlackboardColor.EMPTY) return 0;

			int shade = BlackboardColor.getShadeFromRaw(colorData);
			boolean saturated = BlackboardColor.getSaturationFromRaw(colorData);
			int newShade = BlackboardColor.increaseDarkness(shade);
			return color.toRawId(newShade, saturated);
		}
	};

	public static final BlackboardDrawModifier SHADE_DECREASE = new BlackboardDrawModifier("aurorasdeco.blackboard.modifier.lighten", 0xffeeeeee) {
		@Override
		public boolean matchItem(Item item) {
			return item == Items.BONE_MEAL;
		}

		@Override
		public short apply(short colorData) {
			var color = BlackboardColor.fromRaw(colorData);

			if (color == BlackboardColor.EMPTY) return 0;

			int shade = BlackboardColor.getShadeFromRaw(colorData);
			boolean saturated = BlackboardColor.getSaturationFromRaw(colorData);
			int newShade = BlackboardColor.decreaseDarkness(shade);
			return color.toRawId(newShade, saturated);
		}
	};

	public static final BlackboardDrawModifier SATURATION = new BlackboardDrawModifier("aurorasdeco.blackboard.modifier.saturation", 0xffffbc5e) {
		@Override
		public boolean matchItem(Item item) {
			return item == Items.GLOWSTONE_DUST;
		}

		@Override
		public short apply(short colorData) {
			var color = BlackboardColor.fromRaw(colorData);

			int shade = BlackboardColor.getShadeFromRaw(colorData);
			boolean saturated = !BlackboardColor.getSaturationFromRaw(colorData);

			return color.toRawId(shade, saturated);
		}
	};

	private final String translationKey;
	private final int color;

	protected BlackboardDrawModifier(String translationKey, int color) {
		this.translationKey = translationKey;
		this.color = color;
		MODIFIERS.add(this);
	}

	public Text getName() {
		return Text.translatable(this.translationKey);
	}

	/**
	 * {@return the color in the ARGB format}
	 */
	public int getColor() {
		return this.color;
	}

	public abstract boolean matchItem(Item item);

	public abstract short apply(short colorData);

	public static @Nullable BlackboardDrawModifier fromItem(Item item) {
		for (var modifier : MODIFIERS) {
			if (modifier.matchItem(item))
				return modifier;
		}

		return null;
	}

	public static @Nullable BlackboardDrawModifier fromItem(ItemStack item) {
		return fromItem(item.getItem());
	}
}
