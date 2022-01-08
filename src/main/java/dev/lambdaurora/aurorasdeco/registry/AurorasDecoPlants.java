/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.block.DirectionalFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.plant.DaffodilBlock;
import dev.lambdaurora.aurorasdeco.block.plant.LavenderBlock;
import dev.lambdaurora.aurorasdeco.item.DerivedBlockItem;
import dev.lambdaurora.aurorasdeco.util.Registrar;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.Material;
import net.minecraft.item.ItemGroup;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.List;

import static dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry.register;

/**
 * Contains the different plants definitions added in Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoPlants {
	private AurorasDecoPlants() {
		throw new UnsupportedOperationException("AurorasDecoPlants only contains static definitions.");
	}

	static void init() {
	}

	public static final List<BlockState> FLOWER_FOREST_PLANTS;

	/* Plants */

	public static final DaffodilBlock DAFFODIL = Registrar.register("daffodil", new DaffodilBlock())
			.withItem(new FabricItemSettings().group(ItemGroup.DECORATIONS), DerivedBlockItem::flower)
			.finish();

	public static final FlowerBlock LAVENDER = Registrar.register("lavender", new LavenderBlock())
			.withItem(new FabricItemSettings().group(ItemGroup.DECORATIONS), DerivedBlockItem::flower)
			.finish();

	/* Potted Plants */

	public static final FlowerPotBlock POTTED_DAFFODIL = register("potted/daffodil",
			new DirectionalFlowerPotBlock(DAFFODIL, QuiltBlockSettings.of(Material.DECORATION).nonOpaque().breakInstantly()));

	public static final FlowerPotBlock POTTED_LAVENDER = register("potted/lavender",
			new FlowerPotBlock(LAVENDER, QuiltBlockSettings.of(Material.DECORATION).nonOpaque().breakInstantly()));

	static {
		FLOWER_FOREST_PLANTS = List.of(DAFFODIL.getDefaultState(), LAVENDER.getDefaultState());
	}
}
