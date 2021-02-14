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

import com.google.common.collect.ImmutableList;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a plant property. Values represents different types of plants that can be planted.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class PlantProperty extends Property<PlantProperty.Value> {
    private static final List<Value> VALUES = new ArrayList<>();
    private static final Map<Item, Value> ITEM_VALUE_MAP = new Object2ObjectOpenHashMap<>();
    public static final Value NONE = new Value(null);

    public PlantProperty(String name) {
        super(name, Value.class);
    }

    @Override
    public Collection<Value> getValues() {
        return ImmutableList.copyOf(VALUES);
    }

    @Override
    public String name(Value value) {
        return value.asString();
    }

    @Override
    public Optional<Value> parse(String name) {
        return this.getValues().stream().filter(value -> value.asString().equals(name)).findFirst();
    }

    public static @Nullable PlantProperty.Value fromItem(Item item) {
        return ITEM_VALUE_MAP.get(item);
    }

    public static Value registerValue(Identifier id, Block block) {
        return registerValue(new Value(id, block));
    }

    public static Value registerValue(Block block) {
        return registerValue(new Value(block));
    }

    private static Value registerValue(Value value) {
        VALUES.add(value);
        Item item = value.getItem();
        if (item != Items.AIR)
            ITEM_VALUE_MAP.put(item, value);
        return value;
    }

    public static boolean isValidBlock(Block block) {
        if (block instanceof PlantBlock
                && !(block instanceof CropBlock)
                && !(block instanceof LilyPadBlock)
                && !(block instanceof StemBlock)
                && !(block instanceof AttachedStemBlock)) {
            return true;
        } else if (block instanceof CactusBlock) {
            return true;
        }
        return false;
    }

    static {
        ITEM_VALUE_MAP.put(Items.AIR, NONE);
        VALUES.add(NONE);
        Registry.BLOCK.forEach(block -> {
            if (isValidBlock(block)) {
                registerValue(block);
            }
        });

        registerValue(Blocks.BAMBOO);
        registerValue(AurorasDeco.id("tater"), null);
    }

    public static class Value implements Comparable<Value>, StringIdentifiable {
        private final String id;
        private final Block plant;

        private Value(String id, Block plant) {
            this.id = id;
            this.plant = plant;
        }

        public Value(Identifier id, Block plant) {
            this.id = PropertyUtil.toValueId(id);
            this.plant = plant;
        }

        public Value(Block plant) {
            this(plant == null ? "none" : PropertyUtil.toValueId(Registry.BLOCK.getId(plant)), plant);
        }

        public @Nullable Block getPlant() {
            return this.plant;
        }

        public Item getItem() {
            if (this.plant != null)
                return this.plant.asItem();
            else
                return Items.AIR;
        }

        public boolean isEmpty() {
            return this.plant == null;
        }

        @Override
        public String asString() {
            return this.id;
        }

        @Override
        public String toString() {
            return "PlantProperty$Value{" + this.id + ",plant=" + this.plant + ")}";
        }

        @Override
        public int compareTo(@NotNull PlantProperty.Value other) {
            return this.plant == other.plant ? 0 : this.asString().compareTo(other.asString());
        }
    }
}
