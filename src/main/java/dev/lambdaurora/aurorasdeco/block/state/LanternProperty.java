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

package dev.lambdaurora.aurorasdeco.block.state;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a lantern property. Values represent lantern blocks.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanternProperty extends Property<LanternProperty.Value> {
    private static final Map<String, Value> VALUES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Value> ITEM_VALUE_MAP = new Object2ObjectOpenHashMap<>();

    public static final Value NORMAL = registerValue(Blocks.LANTERN);

    public LanternProperty(String name) {
        super(name, Value.class);
    }

    @Override
    public Collection<Value> getValues() {
        return VALUES.values();
    }

    @Override
    public String name(Value value) {
        return value.asString();
    }

    @Override
    public Optional<Value> parse(String name) {
        return Optional.ofNullable(VALUES.get(name));
    }

    public static @Nullable LanternProperty.Value fromItem(Item item) {
        return ITEM_VALUE_MAP.get(item);
    }

    public static Value registerValue(Identifier id, Block block) {
        return registerValue(new Value(id, block));
    }

    public static Value registerValue(Block block) {
        return registerValue(new Value(block));
    }

    private static Value registerValue(Value value) {
        if (!VALUES.containsKey(value.asString())) {
            VALUES.put(value.asString(), value);
            Item item = value.getItem();
            if (item != Items.AIR)
                ITEM_VALUE_MAP.put(item, value);
        } else value = VALUES.get(value.asString());
        return value;
    }

    public static class Value implements Comparable<Value>, StringIdentifiable {
        private final String id;
        private final Block lantern;

        private Value(String id, Block lantern) {
            this.id = id;
            this.lantern = lantern;
        }

        public Value(Identifier id, Block lantern) {
            this.id = fromBlockIdentifier(id);
            this.lantern = lantern;
        }

        public Value(Block lantern) {
            this(fromBlockIdentifier(Registry.BLOCK.getId(lantern)), lantern);
        }

        public Block getLantern() {
            return this.lantern;
        }

        public Item getItem() {
            return this.lantern.asItem();
        }

        public boolean isEmpty() {
            return this.lantern == null;
        }

        @Override
        public String asString() {
            return this.id;
        }

        @Override
        public String toString() {
            return "LanternProperty$Value{" + this.id + ",lantern=" + this.lantern + ")}";
        }

        @Override
        public int compareTo(@NotNull LanternProperty.Value other) {
            return this.lantern == other.lantern ? 0 : this.asString().compareTo(other.asString());
        }

        private static String fromBlockIdentifier(Identifier id) {
            if (id.getNamespace().equals("minecraft")) {
                if (id.getPath().equals("lantern"))
                    return "normal";
            }
            String value = PropertyUtil.toValueId(id);
            if (value.endsWith("_lantern"))
                value = value.substring(0, value.length() - "_lantern".length());
            if (value.endsWith("_lantern_block"))
                value = value.substring(0, value.length() - "_lantern_block".length());
            return value;
        }
    }
}
