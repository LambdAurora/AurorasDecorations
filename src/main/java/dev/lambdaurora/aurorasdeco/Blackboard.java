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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DyeColor;

import java.util.Arrays;

/**
 * Represents a blackboard drawing.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class Blackboard {
    private final byte[] pixels = new byte[256];

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

    /**
     * Sets the pixel color at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param color the color
     */
    public boolean setPixel(int x, int y, DyeColor color) {
        byte colorId = (byte) (color.getId() + 1);
        if (this.pixels[y * 16 + x] != colorId) {
            this.pixels[y * 16 + x] = colorId;
            return true;
        }
        return false;
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

    /* Serialization */

    public void readNbt(CompoundTag nbt) {
        byte[] pixels = nbt.getByteArray("pixels");
        if (pixels.length == 256) {
            System.arraycopy(pixels, 0, this.pixels, 0, 256);
        }
    }

    public CompoundTag writeNbt(CompoundTag nbt) {
        nbt.putByteArray("pixels", this.pixels);
        return nbt;
    }

    public static Blackboard fromNbt(CompoundTag nbt) {
        Blackboard blackboard = new Blackboard();
        blackboard.readNbt(nbt);
        return blackboard;
    }
}
