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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

import java.util.Collection;
import java.util.List;

public final class AuroraUtil {
    private AuroraUtil() {
        throw new UnsupportedOperationException("Someone tried to instantiate a class only containing static definitions. How?");
    }

    public static Identifier toResourcePackId(Identifier id, String prefix, String extension) {
        return new Identifier(id.getNamespace(), prefix + id.getPath() + '.' + extension);
    }

    public static Identifier toAbsoluteTexturesId(Identifier id) {
        return toResourcePackId(id, "textures/", "png");
    }

    public static <T> boolean contains(Collection<T> list, List<T> required) {
        int i = 0;
        var it = list.iterator();
        while (it.hasNext() && i < required.size()) {
            if (required.contains(it.next()))
                i++;
        }
        return i == required.size();
    }

    /**
     * Returns whether the given id is equal to the given {@code namespace}:{@code path} id without allocation.
     *
     * @param id the identifier to compare
     * @param namespace the namespace to compare to the identifier
     * @param path the path to compare to the identifier
     * @return {@code true} if both identifiers are equal, otherwise {@code false}
     */
    public static boolean idEqual(Identifier id, String namespace, String path) {
        return id.getNamespace().equals(namespace) && id.getPath().equals(path);
    }

    public static double posMod(double n, double d) {
        double v = n % d;
        if (v < 0) v = d + v;
        return v;
    }

    public static boolean isShapeEqual(Box s1, Box s2) {
        return s1.minX == s2.minX && s1.minY == s2.minY && s1.minZ == s2.minZ && s1.maxX == s2.maxX && s1.maxY == s2.maxY && s1.maxZ == s2.maxZ;
    }

    public static Identifier appendWithNamespace(String prefix, Identifier id) {
        var path = id.getPath();
        if (!id.getNamespace().equals("minecraft") && !id.getNamespace().equals("aurorasdeco"))
            path = id.getNamespace() + '/' + path;
        return AurorasDeco.id(prefix + '/' + path);
    }

    public static JsonArray jsonArray(Object... elements) {
        var array = new JsonArray();
        for (var element : elements) {
            if (element instanceof Number)
                array.add((Number) element);
            else if (element instanceof Boolean)
                array.add((Boolean) element);
            else if (element instanceof Character)
                array.add((Character) element);
            else if (element instanceof JsonElement)
                array.add((JsonElement) element);
            else
                array.add(String.valueOf(element));

        }
        return array;
    }
}
