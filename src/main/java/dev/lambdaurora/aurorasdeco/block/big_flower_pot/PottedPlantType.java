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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

public class PottedPlantType {
    private final String id;
    private final @Nullable Block plant;
    private final @Nullable Item item;

    public PottedPlantType(String id, @Nullable Block plant, @Nullable Item item) {
        this.id = id;
        this.plant = plant;
        this.item = item;
    }

    public String getId() {
        return this.id;
    }

    public @Nullable Block getPlant() {
        return this.plant;
    }

    public @Nullable Item getItem() {
        return this.item;
    }
}
