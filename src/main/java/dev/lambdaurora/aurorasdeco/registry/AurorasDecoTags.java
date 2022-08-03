/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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

	public static final TagKey<Item> BLACKBOARD_ITEMS = TagKey.of(Registry.ITEM_KEY, id("blackboards"));

	public static final TagKey<Block> BLACKBOARD_BLOCKS = TagKey.of(Registry.BLOCK_KEY, id("blackboards"));
	public static final TagKey<Block> GLASSBOARD_BLOCKS = TagKey.of(Registry.BLOCK_KEY, id("glassboards"));

	public static final TagKey<Block> BRAZIERS = TagKey.of(Registry.BLOCK_KEY, id("braziers"));
	public static final TagKey<Block> COPPER_SULFATE_DECOMPOSABLE = TagKey.of(Registry.BLOCK_KEY, id("copper_sulfate_decomposable"));
	public static final TagKey<Block> HOPPERS = TagKey.of(Registry.BLOCK_KEY, id("hoppers"));
	public static final TagKey<Block> PET_BEDS = TagKey.of(Registry.BLOCK_KEY, id("pet_beds"));
	public static final TagKey<Block> SHELVES = TagKey.of(Registry.BLOCK_KEY, id("shelves"));
	public static final TagKey<Block> SMALL_LOG_PILES = TagKey.of(Registry.BLOCK_KEY, id("small_log_piles"));
	public static final TagKey<Block> STUMPS = TagKey.of(Registry.BLOCK_KEY, id("stumps"));

	public static final TagKey<Block> VEGETATION_ON_WATER_SURFACE = TagKey.of(Registry.BLOCK_KEY, new Identifier("c", "vegetation/on_water_surface"));
}
