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

package dev.lambdaurora.aurorasdeco.item;

import dev.lambdaurora.aurorasdeco.registry.WoodType;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a seat rest item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SeatRestItem extends Item {
    private static final List<SeatRestItem> SEAT_RESTS = new ArrayList<>();

    private final WoodType woodType;

    public SeatRestItem(WoodType woodType, Settings settings) {
        super(settings);
        this.woodType = woodType;

        SEAT_RESTS.add(this);
    }

    public static Stream<SeatRestItem> streamSeatRests() {
        return SEAT_RESTS.stream();
    }

    /**
     * Gets the wood type of this rest item.
     *
     * @return the wood type
     */
    public WoodType getWoodType() {
        return this.woodType;
    }
}
