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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.MathHelper;

import java.util.stream.Collectors;

/**
 * Utilities for color manipulation.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ColorUtil {
    private ColorUtil() {
        throw new UnsupportedOperationException("ColorUtil only contains static definitions.");
    }

    public static final int BLACK = 0xff000000;
    public static final int WHITE = 0xffffffff;
    public static final int TEXT_COLOR = 0xffe0e0e0;
    public static final int UNEDITABLE_COLOR = 0xff707070;

    /**
     * Unpacks the given ARGB color into an array of 4 integers in the following format: {@code {red, green, blue, alpha}}.
     *
     * @param color the ARGB color
     * @return the 4 color components as a RGBA array
     */
    public static int[] unpackARGBColor(int color) {
        return new int[]{
                argbUnpackRed(color),
                argbUnpackGreen(color),
                argbUnpackBlue(color),
                argbUnpackAlpha(color)
        };
    }

    /**
     * Extracts and unpacks the red component of the ARGB color.
     *
     * @param color the ARGB color
     * @return the unpacked red component
     */
    public static int argbUnpackRed(int color) {
        return (color >> 16) & 255;
    }

    /**
     * Extracts and unpacks the green component of the ARGB color.
     *
     * @param color the ARGB color
     * @return the unpacked green component
     */
    public static int argbUnpackGreen(int color) {
        return (color >> 8) & 255;
    }

    /**
     * Extracts and unpacks the blue component of the ARGB color.
     *
     * @param color the ARGB color
     * @return the unpacked blue component
     */
    public static int argbUnpackBlue(int color) {
        return color & 255;
    }

    /**
     * Extracts and unpacks the alpha component of the ARGB color.
     *
     * @param color the ARGB color
     * @return the unpacked alpha component
     */
    public static int argbUnpackAlpha(int color) {
        return (color >> 24) & 255;
    }

    public static float luminance(int color) {
        return luminance(argbUnpackRed(color), argbUnpackGreen(color), argbUnpackBlue(color));
    }

    public static float luminance(int red, int green, int blue) {
        return (0.2126f * red + 0.7152f * green + 0.0722f * blue);
    }

    public static float[] rgbToHsb(int r, int g, int b) {
        var hsb = new float[3];

        int cMax = Math.max(r, g);
        if (b > cMax) {
            cMax = b;
        }

        int cMin = Math.min(r, g);
        if (b < cMin) {
            cMin = b;
        }

        float brightness = (float) cMax / 255.f;
        float saturation;
        if (cMax != 0) {
            saturation = (float) (cMax - cMin) / (float) cMax;
        } else {
            saturation = 0.f;
        }

        float hue;
        if (saturation == 0.f) {
            hue = 0.f;
        } else {
            float redC = (float) (cMax - r) / (float) (cMax - cMin);
            float greenC = (float) (cMax - g) / (float) (cMax - cMin);
            float blueC = (float) (cMax - b) / (float) (cMax - cMin);
            if (r == cMax) {
                hue = blueC - greenC;
            } else if (g == cMax) {
                hue = 2.f + redC - blueC;
            } else {
                hue = 4.f + greenC - redC;
            }

            hue /= 6.f;
            if (hue < 0.f) {
                ++hue;
            }
        }

        hsb[0] = hue;
        hsb[1] = saturation;
        hsb[2] = brightness;
        return hsb;
    }

    public static IntSet getColorsFromImage(NativeImage image) {
        var colors = new IntOpenHashSet();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getPixelColor(x, y);

                if (argbUnpackAlpha(color) == 255) {
                    colors.add(color);
                }
            }
        }

        return colors;
    }

    public static IntList getPaletteFromImage(NativeImage image) {
        var colors = getColorsFromImage(image);

        return new IntArrayList(colors.stream().sorted((color0, color1) -> {
            var lum0 = luminance(color0);
            var lum1 = luminance(color1);

            return Float.compare(lum0, lum1);
        }).collect(Collectors.toList()));
    }

    public static IntList getPaletteFromImage(NativeImage image, int expectColors) {
        var palette = getPaletteFromImage(image);

        if (expectColors + 2 < palette.size()) {
            var reducedPalette = new IntArrayList();

            float lastLuminance = -1.f;

            for (int i = 0; i < palette.size(); i++) {
                int color = palette.getInt(i);
                float luminance = luminance(color);

                if (MathHelper.abs(luminance - lastLuminance) < 3.1f) continue;

                lastLuminance = luminance;

                reducedPalette.add(color);
            }

            return reducedPalette;
        }

        return palette;
    }
}
