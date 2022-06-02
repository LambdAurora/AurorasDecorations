/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Blackboard} draw modifier.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BlackboardDrawModifier {
	BlackboardDrawModifier SHADE_INCREASE = new BlackboardDrawModifier() {
		@Override
		public boolean matchItem(ItemStack item) {
			return item.isIn(ItemTags.COALS);
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

	BlackboardDrawModifier SHADE_DECREASE = new BlackboardDrawModifier() {
		@Override
		public boolean matchItem(ItemStack item) {
			return item.isOf(Items.BONE_MEAL);
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

	BlackboardDrawModifier SATURATION = new BlackboardDrawModifier() {
		@Override
		public boolean matchItem(ItemStack item) {
			return item.isOf(Items.GLOWSTONE_DUST);
		}

		@Override
		public short apply(short colorData) {
			var color = BlackboardColor.fromRaw(colorData);

			int shade = BlackboardColor.getShadeFromRaw(colorData);
			boolean saturated = !BlackboardColor.getSaturationFromRaw(colorData);

			return color.toRawId(shade, saturated);
		}
	};

	boolean matchItem(ItemStack item);

	short apply(short colorData);

	static @Nullable BlackboardDrawModifier fromItem(ItemStack item) {
		for (var modifier : BlackboardColor.MODIFIERS) {
			if (modifier.matchItem(item))
				return modifier;
		}

		return null;
	}
}
