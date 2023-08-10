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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents a blackboard color.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardColor extends BlackboardDrawModifier {
	private static final Int2ObjectMap<BlackboardColor> COLORS = new Int2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Item, BlackboardColor> ITEM_TO_COLOR = new Object2ObjectOpenHashMap<>();

	/**
	 * The color identifier mask ({@value}) for the raw color format.
	 */
	public static final int COLOR_MASK /**/ = 0b1111111100000000;
	/**
	 * The saturation mask ({@value}) for the raw color format.
	 */
	public static final int SATURATION_MASK = 0b0000000010000000;
	/**
	 * The shade mask ({@value}) for the raw color format.
	 */
	public static final int SHADE_MASK /**/ = 0b0000000001110000;

	/**
	 * Represents the absence of color.
	 */
	public static final BlackboardColor EMPTY = new BlackboardColor(0, 0x00000000, Items.PAPER);
	public static final byte FREE_COLOR_SPACE = (byte) (DyeColor.values().length + 1);
	public static final BlackboardColor SWEET_BERRIES = new BlackboardColor(FREE_COLOR_SPACE, 0xffbb0000, Items.SWEET_BERRIES);
	public static final BlackboardColor GLOW_BERRIES = new BlackboardColor(FREE_COLOR_SPACE + 1, 0xffff9737, Items.GLOW_BERRIES);
	public static final BlackboardColor LAVENDER = new BlackboardColor(FREE_COLOR_SPACE + 3, 0xffb886db, AurorasDecoPlants.LAVENDER.item());

	public static final int BLUEBERRIES_COLOR = 0xff006ac6;

	private final byte id;
	private final Item item;

	private BlackboardColor(int id, int color, Item item) {
		super("", color);
		this.id = (byte) id;
		this.item = item;

		COLORS.put(id, this);
		ITEM_TO_COLOR.put(item, this);
	}

	/**
	 * {@return the color instance from its identifier}
	 *
	 * @param color the color identifier
	 */
	public static BlackboardColor byId(int color) {
		return COLORS.getOrDefault(color, EMPTY);
	}

	/**
	 * Extracts the color instance out of the given raw color format.
	 *
	 * @param color the raw color format
	 * @return the extracted color instance
	 */
	public static BlackboardColor fromRaw(int color) {
		return byId((color & COLOR_MASK) >> 8);
	}

	public static @Nullable BlackboardColor fromItem(Item item) {
		return ITEM_TO_COLOR.get(item);
	}

	public static @UnmodifiableView Collection<BlackboardColor> getColors() {
		return Collections.unmodifiableCollection(COLORS.values());
	}

	/**
	 * {@return the identifier of the color}
	 */
	public byte getId() {
		return this.id;
	}

	/**
	 * Returns the raw color format with shading and saturation of this color.
	 *
	 * @param shade the shade
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 * @return the raw color format
	 */
	public short toRawId(int shade, boolean saturated) {
		if (this == EMPTY) return 0;

		short id = (short) (this.getId() << 8);
		id |= MathHelper.clamp(shade, 0, 7) << 4;
		if (saturated) id |= SATURATION_MASK;
		return id;
	}

	public Item getItem() {
		return this.item;
	}

	/**
	 * {@return the render color in the ABGR format}
	 *
	 * @param shade the shade
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 */
	public int getRenderColor(int shade, boolean saturated) {
		if (this.getId() == 0)
			return this.getColor();

		int factor = switch (shade) {
			case 1 -> 220;
			case 2 -> 180;
			case 3 -> 135;
			case 4 -> 285;
			case 5 -> 320;
			default -> 255;
		};

		int color = saturated ? this.getSaturated() : this.getColor();
		int red = MathHelper.clamp((color >> 16 & 255) * factor / 255, 0, 255);
		int green = MathHelper.clamp((color >> 8 & 255) * factor / 255, 0, 255);
		int blue = MathHelper.clamp((color & 255) * factor / 255, 0, 255);
		return 0xff000000 | blue << 16 | green << 8 | red;
	}

	/**
	 * {@return the render color in the ABGR format}
	 *
	 * @param color the raw color format
	 */
	public static int getRenderColor(int color) {
		return fromRaw(color).getRenderColor(getShadeFromRaw(color), getSaturationFromRaw(color));
	}

	public static int getShadeFromRaw(int color) {
		return (color & SHADE_MASK) >> 4;
	}

	public static boolean getSaturationFromRaw(int color) {
		return (color & SATURATION_MASK) != 0;
	}

	public static int increaseDarkness(int shade) {
		return switch (shade) {
			case 0, 1, 2 -> shade + 1;
			case 4 -> 0;
			case 5 -> shade - 1;
			default -> shade;
		};
	}

	public static int decreaseDarkness(int shade) {
		return switch (shade) {
			case 1, 2, 3 -> shade - 1;
			case 0 -> 4;
			case 4 -> shade + 1;
			default -> shade;
		};
	}

	private int getSaturated() {
		final int value = 1;

		int color = this.getColor();
		int red = color >> 16 & 255;
		int green = color >> 8 & 255;
		int blue = color & 255;

		float gray = 0.2989f * red + 0.5870f * green + 0.1140f * blue;

		red = MathHelper.clamp((int) (-gray * value + red * (1 + value)), 0, 255);
		green = MathHelper.clamp((int) (-gray * value + green * (1 + value)), 0, 255);
		blue = MathHelper.clamp((int) (-gray * value + blue * (1 + value)), 0, 255);

		return 0xff000000 | red << 16 | green << 8 | blue;
	}

	public static BlackboardColor fromDye(DyeItem dyeItem) {
		var color = dyeItem.getColor();

		if (COLORS.containsKey(color.getId() + 1)) {
			return COLORS.get(color.getId() + 1);
		}

		int red = (int) (color.getColorComponents()[0] * 255.f);
		int green = (int) (color.getColorComponents()[1] * 255.f);
		int blue = (int) (color.getColorComponents()[2] * 255.f);
		return new BlackboardColor(color.getId() + 1, 0xff000000 | (red << 16) | (green << 8) | blue, dyeItem);
	}

	public static void tryRegisterColorFromItem(Identifier id, Item item) {
		if (item instanceof DyeItem dyeItem) {
			fromDye(dyeItem);
		} else if (id.getNamespace().equals("ecotones") && id.getPath().equals("blueberries")) {
			new BlackboardColor(FREE_COLOR_SPACE + 2, BLUEBERRIES_COLOR, item);
		}
	}

	@Override
	public boolean matchItem(Item item) {
		return this.item == item;
	}

	@Override
	public short apply(short colorData) {
		return this.toRawId(0, false);
	}
}
