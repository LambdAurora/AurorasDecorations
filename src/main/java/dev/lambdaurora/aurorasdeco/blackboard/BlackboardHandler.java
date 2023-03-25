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

/**
 * Represents a blackboard handler.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BlackboardHandler {
	short getPixel(int x, int y);

	/**
	 * Sets the pixel color at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the raw color
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 */
	boolean setPixel(int x, int y, int color);

	/**
	 * Sets the pixel color at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the color
	 * @param shade the shade of the color
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 * @see #setPixel(int, int, int)
	 */
	default boolean setPixel(int x, int y, BlackboardColor color, int shade, boolean saturated) {
		return this.setPixel(x, y, color.toRawId(shade, saturated));
	}

	/**
	 * Sets the pixel color at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param color the color
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 */
	default boolean setPixel(int x, int y, BlackboardColor color) {
		return this.setPixel(x, y, color, 0, false);
	}

	/**
	 * Sets whether the given pixel is saturated or not.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param saturated {@code true} if the color is saturated, or {@code false} otherwise
	 * @return {@code true} if the pixel has been changed, or {@code false} otherwise
	 * @see #setPixel(int, int, int)
	 * @see #setPixel(int, int, BlackboardColor, int, boolean)
	 */
	default boolean setSaturated(int x, int y, boolean saturated) {
		int color = this.getPixel(x, y);
		if (BlackboardColor.getSaturationFromRaw(color) == saturated) return false;

		color &= ~BlackboardColor.SATURATION_MASK;
		if (saturated)
			color |= BlackboardColor.SATURATION_MASK;

		this.setPixel(x, y, color);

		return true;
	}

	boolean brush(int x, int y, int color);

	default boolean brush(int x, int y, BlackboardColor color, int shade) {
		return this.brush(x, y, color.toRawId(shade, BlackboardColor.getSaturationFromRaw(this.getPixel(x, y))));
	}

	boolean replace(int x, int y, int color);

	default boolean replace(int x, int y, BlackboardColor color, int shade) {
		return this.replace(x, y, color.getRenderColor(shade, false));
	}

	boolean line(int x1, int y1, int x2, int y2, BlackboardDrawModifier modifier);

	boolean fill(int x, int y, int color);

	default boolean fill(int x, int y, BlackboardColor color, int shade) {
		return this.fill(x, y, color.toRawId(shade, BlackboardColor.getSaturationFromRaw(this.getPixel(x, y))));
	}
}
