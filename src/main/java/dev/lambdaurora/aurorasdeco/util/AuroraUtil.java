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
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public final class AuroraUtil {
    private AuroraUtil() {
        throw new UnsupportedOperationException("Someone tried to instantiate a singleton. How?");
    }

    public static double posMod(double n, double d) {
        double v = n % d;
        if (v < 0) v = d + v;
        return v;
    }

    public static boolean isShapeEqual(Box s1, Box s2) {
        return s1.minX == s2.minX && s1.minY == s2.minY && s1.minZ == s2.minZ && s1.maxX == s2.maxX && s1.maxY == s2.maxY && s1.maxZ == s2.maxZ;
    }

    public static JsonArray jsonArray(Object[] elements) {
        JsonArray array = new JsonArray();
        for (Object element : elements) {
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

    public static JsonObject inventoryChangedCriteria(String type, Identifier item) {
        JsonObject root = new JsonObject();
        root.addProperty("trigger", "minecraft:inventory_changed");
        JsonObject conditions = new JsonObject();
        JsonArray items = new JsonArray();
        JsonObject child = new JsonObject();
        child.addProperty(type, item.toString());
        conditions.add("items", items);
        root.add("conditions", conditions);
        return root;
    }

    public static JsonObject recipeUnlockedCriteria(Identifier recipe) {
        JsonObject root = new JsonObject();
        root.addProperty("trigger", "minecraft:recipe_unlocked");
        JsonObject conditions = new JsonObject();
        conditions.addProperty("recipe", recipe.toString());
        root.add("conditions", conditions);
        return root;
    }

    public static JsonObject simpleBlockLootTable(Identifier id, boolean copyName) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        JsonArray pools = new JsonArray();
        {
            JsonObject pool = new JsonObject();
            pool.addProperty("rolls", 1.0);
            pool.addProperty("bonus_rolls", 0.0);

            JsonArray entries = new JsonArray();

            JsonObject entry = new JsonObject();
            entry.addProperty("type", "minecraft:item");
            entry.addProperty("name", id.toString());

            if (copyName) {
                JsonObject function = new JsonObject();
                function.addProperty("function", "minecraft:copy_name");
                function.addProperty("source", "block_entity");
                entry.add("functions", jsonArray(new JsonObject[]{function}));
            }

            entries.add(entry);

            pool.add("entries", entries);

            pools.add(pool);
        }

        root.add("pools", pools);

        return root;
    }
}
