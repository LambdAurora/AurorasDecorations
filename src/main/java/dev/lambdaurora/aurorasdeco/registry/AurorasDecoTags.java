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

package dev.lambdaurora.aurorasdeco.registry;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * Represents the tags used in Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoTags {
    private AurorasDecoTags() {
        throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
    }

    public static final Tag<Item> BLACKBOARD_ITEMS = TagRegistry.item(id("blackboards"));

    public static final Tag<Block> BRAZIERS = TagRegistry.block(id("braziers"));
    public static final Tag<Block> COPPER_SULFATE_DECOMPOSABLE = TagRegistry.block(id("copper_sulfate_decomposable"));
    public static final Tag<Block> HOPPERS = TagRegistry.block(id("hoppers"));
    public static final Tag<Block> PET_BEDS = TagRegistry.block(id("pet_beds"));
    public static final Tag<Block> SHELVES = TagRegistry.block(id("shelves"));
    public static final Tag<Block> SMALL_LOG_PILES = TagRegistry.block(id("small_log_piles"));
    public static final Tag<Block> STUMPS = TagRegistry.block(id("stumps"));
}
