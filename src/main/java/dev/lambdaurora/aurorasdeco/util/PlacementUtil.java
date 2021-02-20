/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

import net.minecraft.util.math.Box;

public final class PlacementUtil {
    private PlacementUtil() {
        throw new UnsupportedOperationException("Someone tried to instantiate a singleton. How?");
    }

    public static boolean isShapeEqual(Box s1, Box s2) {
        return s1.minX == s2.minX && s1.minY == s2.minY && s1.minZ == s2.minZ && s1.maxX == s2.maxX && s1.maxY == s2.maxY && s1.maxZ == s2.maxZ;
    }
}
