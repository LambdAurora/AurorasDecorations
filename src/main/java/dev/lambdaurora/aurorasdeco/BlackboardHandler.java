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

/**
 * Represents a blackboard handler.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BlackboardHandler {
    byte getPixel(int x, int y);

    /**
     * Sets the pixel color at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param color the color
     */
    boolean setPixel(int x, int y, Blackboard.Color color, int shade);

    boolean brush(int x, int y, Blackboard.Color color, int shade);

    boolean replace(int x, int y, Blackboard.Color color, int shade);

    boolean line(int x1, int y1, int x2, int y2, Blackboard.Color color, int shade);

    boolean fill(int x, int y, Blackboard.Color color, int shade);
}
