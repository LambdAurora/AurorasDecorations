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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;
import org.quiltmc.qsl.testing.api.game.TestStructureNamePrefix;

@TestStructureNamePrefix("aurorasdeco:copper_hopper/")
public class CopperHopperTest {
	@GameTest(structureName = "simple", batchId = "copper_hopper")
	public void testSimple(QuiltTestContext context) {
		context.spawnItemEntity(AurorasDecoPlants.LAVENDER.item(), BlockPos.create(1, 5, 1));
		context.spawnItemEntity(AurorasDecoPlants.DAFFODIL.item(), BlockPos.create(1, 5, 1));
		context.spawnItemEntity(Items.APPLE, BlockPos.create(1, 5, 1));
		context.spawnItemEntity(Items.POPPY, BlockPos.create(1, 5, 1));

		context.succeedAtTickIf(56, () -> {
			context.expectContainerWith(BlockPos.create(2, 2, 1), AurorasDecoPlants.LAVENDER.item());
			context.expectContainerWith(BlockPos.create(3, 2, 1), AurorasDecoPlants.DAFFODIL.item());
			context.expectContainerWith(BlockPos.create(4, 2, 1), Items.APPLE);
			context.expectContainerWith(BlockPos.create(4, 2, 1), Items.POPPY);
		});
	}
}
