/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.StructureFeature;

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

	public static final TagKey<Item> BLACKBOARD_ITEMS = TagKey.of(RegistryKeys.ITEM, id("blackboards"));

	public static final TagKey<Block> BLACKBOARD_BLOCKS = TagKey.of(RegistryKeys.BLOCK, id("blackboards"));
	public static final TagKey<Block> GLASSBOARD_BLOCKS = TagKey.of(RegistryKeys.BLOCK, id("glassboards"));

	public static final TagKey<Block> BRAZIERS = TagKey.of(RegistryKeys.BLOCK, id("braziers"));
	public static final TagKey<Block> COPPER_SULFATE_DECOMPOSABLE = TagKey.of(RegistryKeys.BLOCK, id("copper_sulfate_decomposable"));
	public static final TagKey<Block> HOPPERS = TagKey.of(RegistryKeys.BLOCK, id("hoppers"));
	public static final TagKey<Block> PET_BEDS = TagKey.of(RegistryKeys.BLOCK, id("pet_beds"));
	public static final TagKey<Block> SHELVES = TagKey.of(RegistryKeys.BLOCK, id("shelves"));
	public static final TagKey<Block> SMALL_LOG_PILES = TagKey.of(RegistryKeys.BLOCK, id("small_log_piles"));
	public static final TagKey<Block> STUMPS = TagKey.of(RegistryKeys.BLOCK, id("stumps"));

	public static final TagKey<Block> VEGETATION_ON_WATER_SURFACE = TagKey.of(RegistryKeys.BLOCK,
			new Identifier("c", "vegetation/on_water_surface")
	);

	public static final TagKey<StructureFeature> WAY_SIGN_DESTINATION_STRUCTURES = TagKey.of(RegistryKeys.STRUCTURE_FEATURE,
			id("way_sign_destinations")
	);
}
