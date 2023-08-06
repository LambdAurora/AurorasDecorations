/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.test;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;
import org.quiltmc.qsl.testing.api.game.TestStructureNamePrefix;

@TestStructureNamePrefix("aurorasdeco:sturdy_stone/")
public class SturdyStoneTest {
	@GameTest(structureName = "propagation", batchId = "sturdy_stone")
	public void testSturdyStonePropagation(QuiltTestContext context) {
		context.pushButton(BlockPos.create(3, 3, 1));

		context.succeedAtTickIf(4, () -> {
			context.expectBlock(Blocks.AMETHYST_BLOCK, BlockPos.create(0, 4, 3));
			context.expectBlockState(Blocks.REDSTONE_LAMP.getDefaultState().with(Properties.LIT, true), BlockPos.create(3, 6, 3));
		});
	}

	@GameTest(structureName = "piston", batchId = "sturdy_stone")
	public void testSturdyStonePiston(@NotNull QuiltTestContext context) {
		context.pushButton(BlockPos.create(3, 2, 1));

		context.runAtTick(2, () -> {
			context.expectBlock(AurorasDecoRegistry.STURDY_STONE_BLOCK, BlockPos.create(2, 2, 2));
			context.complete();
		});
	}
}
