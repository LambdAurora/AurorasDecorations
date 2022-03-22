/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.mixin.item.AxeItemAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.PillarBlock;

public class StrippableLogBlock extends PillarBlock {
	public StrippableLogBlock(Settings settings) {
		super(settings);

		FlammableBlockRegistry.getDefaultInstance().add(this, 5, 5);
	}

	public static void register(Block normal, Block stripped) {
		var map = AxeItemAccessor.getStrippedBlocks();
		if (!(map instanceof Object2ObjectOpenHashMap)) {
			map = new Object2ObjectOpenHashMap<>(map);
			AxeItemAccessor.setStrippedBlocks(map);
		}

		map.put(normal, stripped);
	}
}
