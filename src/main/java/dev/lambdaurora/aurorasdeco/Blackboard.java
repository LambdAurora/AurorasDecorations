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

package dev.lambdaurora.aurorasdeco;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Represents a blackboard drawing.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class Blackboard {
    public static final Int2ObjectMap<Color> COLORS = new Int2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<Item, Color> ITEM_TO_COLOR = new Object2ObjectOpenHashMap<>();

    private final byte[] pixels = new byte[256];
    private boolean lit;

    public Blackboard() {
    }

    /**
     * Gets the pixels of the blackboard.
     *
     * @return the pixels
     */
    public byte[] getPixels() {
        return this.pixels;
    }

    public byte getPixel(int x, int y) {
        return this.pixels[y * 16 + x];
    }

    public int getColor(int x, int y) {
        int id = this.getPixel(x, y);
        return getColor(id / 4).getRenderColor(id & 3);
    }

    /**
     * Sets the pixel color at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param color the color
     */
    public boolean setPixel(int x, int y, Color color, int shade) {
        byte id = (byte) (color.getId() * 4 + shade);
        if (color == Color.EMPTY)
            id = 0;
        if (this.pixels[y * 16 + x] != id) {
            this.pixels[y * 16 + x] = id;
            return true;
        }
        return false;
    }

    /**
     * Copies the blackboard data to this blackboard.
     *
     * @param source the blackboard to copy
     */
    public void copy(Blackboard source) {
        System.arraycopy(source.pixels, 0, this.pixels, 0, this.pixels.length);
        this.setLit(source.isLit());
    }

    /**
     * Clears the pixel at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    public boolean clearPixel(int x, int y) {
        if (this.pixels[y * 16 + x] != 0) {
            this.pixels[y * 16 + x] = 0;
            return true;
        }
        return false;
    }

    /**
     * Clears the blackboard.
     */
    public void clear() {
        Arrays.fill(this.pixels, (byte) 0);
    }

    /**
     * Returns whether this blackboard is empty or not.
     *
     * @return {@code true} if empty, else {@code false}
     */
    public boolean isEmpty() {
        for (byte b : this.pixels) {
            if (b != 0)
                return false;
        }
        return true;
    }

    public boolean isLit() {
        return this.lit;
    }

    public void setLit(boolean lit) {
        this.lit = lit;
    }

    /* Serialization */

    public void readNbt(NbtCompound nbt) {
        byte[] pixels = nbt.getByteArray("pixels");
        if (pixels.length == 256) {
            System.arraycopy(pixels, 0, this.pixels, 0, 256);
        }

        this.lit = nbt.getBoolean("lit");

        if (!nbt.contains("version", NbtType.INT)) {
            convert01(this);
        }
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putByteArray("pixels", this.pixels);
        nbt.putBoolean("lit", this.isLit());
        nbt.putInt("version", 1);
        return nbt;
    }

    public static Blackboard fromNbt(NbtCompound nbt) {
        Blackboard blackboard = new Blackboard();
        blackboard.readNbt(nbt);
        return blackboard;
    }

    public static boolean shouldConvert(NbtCompound nbt) {
        return !nbt.contains("version", NbtElement.INT_TYPE);
    }

    private static void convert01(Blackboard blackboard) {
        for (int i = 0; i < blackboard.pixels.length; i++) {
            blackboard.pixels[i] *= 4;
        }
    }

    public static Color getColor(int color) {
        return COLORS.getOrDefault(color, Color.EMPTY);
    }

    public static @Nullable Color getColorFromItem(Item item) {
        return ITEM_TO_COLOR.get(item);
    }

    /**
     * Represents a blackboard color.
     */
    public static class Color {
        public static final Color EMPTY = new Color(0, 0x00000000, Items.PAPER);
        public static final byte FREE_COLOR_SPACE = (byte) (DyeColor.values().length + 1);
        public static final Color SWEET_BERRIES = new Color(FREE_COLOR_SPACE, 0xffbb0000, Items.SWEET_BERRIES);
        public static final Color GLOW_BERRIES = new Color(FREE_COLOR_SPACE + 1, 0xffff9737, Items.GLOW_BERRIES);

        public static final int BLUEBERRIES_COLOR = 0xff006ac6;

        private final byte id;
        private final int color;
        private final Item item;

        private Color(int id, int color, Item item) {
            this.id = (byte) id;
            this.color = color;
            this.item = item;

            COLORS.put(id, this);
            ITEM_TO_COLOR.put(item, this);
        }

        public byte getId() {
            return this.id;
        }

        /**
         * Returns the color in the ABGR format.
         *
         * @return the color in the ABGR format
         */
        public int getColor() {
            return this.color;
        }

        /**
         * Returns the render color in the ABGR format.
         *
         * @param shade the shade
         * @return the render color in the ABGR format
         */
        public int getRenderColor(int shade) {
            if (this.getId() == 0)
                return this.getColor();

            int factor = 220;
            if (shade == 3) {
                factor = 135;
            }

            if (shade == 2) {
                factor = 180;
            }

            if (shade == 1) {
                factor = 220;
            }

            if (shade == 0) {
                factor = 255;
            }

            int color = this.getColor();
            int red = (color >> 16 & 255) * factor / 255;
            int green = (color >> 8 & 255) * factor / 255;
            int blue = (color & 255) * factor / 255;
            return 0xff000000 | blue << 16 | green << 8 | red;
        }

        public Item getItem() {
            return this.item;
        }

        public static Color fromDye(DyeItem dyeItem) {
            DyeColor color = dyeItem.getColor();

            if (COLORS.containsKey(color.getId() + 1)) {
                return COLORS.get(color.getId() + 1);
            }

            int red = (int) (color.getColorComponents()[0] * 255.f);
            int green = (int) (color.getColorComponents()[1] * 255.f);
            int blue = (int) (color.getColorComponents()[2] * 255.f);
            return new Color(color.getId() + 1, 0xff000000 | (red << 16) | (green << 8) | blue, dyeItem);
        }

        public static void tryRegisterColorFromItem(Identifier id, Item item) {
            if (item instanceof DyeItem) {
                fromDye((DyeItem) item);
            } else if (id.getNamespace().equals("ecotones") && id.getPath().equals("blueberries")) {
                new Color(FREE_COLOR_SPACE + 2, BLUEBERRIES_COLOR, item);
            }
        }
    }
}
