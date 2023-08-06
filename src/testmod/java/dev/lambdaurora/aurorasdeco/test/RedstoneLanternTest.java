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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;
import org.quiltmc.qsl.testing.api.game.TestStructureNamePrefix;

@TestStructureNamePrefix("aurorasdeco:redstone_lantern/")
public class RedstoneLanternTest {
	private static final BlockState LIT_REDSTONE_LAMP = Blocks.REDSTONE_LAMP.getDefaultState().with(Properties.LIT, true);
	private static final BlockState UNLIT_REDSTONE_LAMP = Blocks.REDSTONE_LAMP.getDefaultState().with(Properties.LIT, false);

	@GameTest(structureName = "basic_connectivity", batchId = "redstone_lantern")
	public void testBasicConnectivity(QuiltTestContext context) {
		context.runAtTick(1, () -> {
			// Lamps
			context.expectBlockState(LIT_REDSTONE_LAMP, BlockPos.create(1, 1, 2));
			context.expectBlockState(UNLIT_REDSTONE_LAMP, BlockPos.create(1, 3, 2));
			context.expectBlockState(UNLIT_REDSTONE_LAMP, BlockPos.create(5, 1, 2));
			context.expectBlockState(UNLIT_REDSTONE_LAMP, BlockPos.create(5, 3, 2));

			// Repeaters
			context.expectBlockState(litRepeater(Direction.NORTH), BlockPos.create(1, 2, 3));
			context.expectBlockState(litRepeater(Direction.EAST), BlockPos.create(0, 2, 2));
			context.expectBlockState(litRepeater(Direction.SOUTH), BlockPos.create(1, 2, 1));
			context.expectBlockState(litRepeater(Direction.WEST), BlockPos.create(2, 2, 2));

			context.expectBlockState(litRepeater(Direction.NORTH), BlockPos.create(5, 2, 3));
			context.expectBlockState(litRepeater(Direction.EAST), BlockPos.create(4, 2, 2));
			context.expectBlockState(litRepeater(Direction.SOUTH), BlockPos.create(5, 2, 1));
			context.expectBlockState(litRepeater(Direction.WEST), BlockPos.create(6, 2, 2));

			context.complete();
		});
	}

	@GameTest(structureName = "wall_connectivity", batchId = "redstone_lantern")
	public void testWallConnectivity(QuiltTestContext context) {
		context.runAtTick(1, () -> {
			BlockState xRedstone = Blocks.REDSTONE_WIRE.getDefaultState()
					.with(Properties.POWER, 15)
					.with(Properties.EAST_WIRE_CONNECTION, WireConnection.SIDE)
					.with(Properties.WEST_WIRE_CONNECTION, WireConnection.SIDE)
					.with(Properties.NORTH_WIRE_CONNECTION, WireConnection.NONE)
					.with(Properties.SOUTH_WIRE_CONNECTION, WireConnection.NONE);

			context.expectBlockState(LIT_REDSTONE_LAMP, BlockPos.create(1, 1, 1));
			context.expectBlockState(LIT_REDSTONE_LAMP, BlockPos.create(1, 3, 1));
			context.expectBlockState(UNLIT_REDSTONE_LAMP, BlockPos.create(1, 2, 2));

			context.expectBlockState(xRedstone, BlockPos.create(0, 2, 1));
			context.expectBlockState(xRedstone, BlockPos.create(2, 2, 1));
			context.expectBlockState(Blocks.REDSTONE_WIRE.getDefaultState()
							.with(Properties.POWER, 15)
							.with(Properties.NORTH_WIRE_CONNECTION, WireConnection.SIDE)
							.with(Properties.SOUTH_WIRE_CONNECTION, WireConnection.SIDE)
							.with(Properties.EAST_WIRE_CONNECTION, WireConnection.NONE)
							.with(Properties.WEST_WIRE_CONNECTION, WireConnection.NONE),
					BlockPos.create(1, 2, 0));

			context.complete();
		});
	}

	@GameTest(structureName = "tower", batchId = "redstone_lantern")
	public void testTower(QuiltTestContext context) {
		BlockState lit = AurorasDecoRegistry.REDSTONE_LANTERN_BLOCK.getDefaultState()
				.with(Properties.HANGING, true)
				.with(Properties.LIT, true);
		BlockState unlit = AurorasDecoRegistry.REDSTONE_LANTERN_BLOCK.getDefaultState()
				.with(Properties.HANGING, true)
				.with(Properties.LIT, false);

		context.expectBlockState(lit, BlockPos.create(1, 4, 1));
		context.expectBlockState(unlit, BlockPos.create(1, 2, 1));

		context.pushButton(BlockPos.create(1, 6, 1));

		context.succeedAtTickIf(4, () -> {
			context.expectBlockState(unlit, BlockPos.create(1, 4, 1));
			context.expectBlockState(lit, BlockPos.create(1, 2, 1));
		});
	}

	private static BlockState litRepeater(Direction facing) {
		return Blocks.REPEATER.getDefaultState()
				.with(Properties.POWERED, true)
				.with(Properties.LOCKED, false)
				.with(RepeaterBlock.FACING, facing)
				.with(Properties.DELAY, 1);
	}
}
