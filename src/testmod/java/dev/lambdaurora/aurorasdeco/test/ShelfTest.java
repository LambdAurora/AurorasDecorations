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

import dev.lambdaurora.aurorasdeco.block.ShelfBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;
import org.quiltmc.qsl.testing.api.game.TestStructureNamePrefix;

@TestStructureNamePrefix("aurorasdeco:shelf/")
public class ShelfTest {
	@GameTest(structureName = "insertion")
	public static void testInsertion(QuiltTestContext context) {
		var stack = new ItemStack(Items.BOOK);
		var player = context.createMockPlayer();

		player.setPosition(context.getAbsolute(BlockPos.ORIGIN.ofCenter()));

		interactWithShelfAt(context, player, BlockPos.create(1, 3, 1), stack);
		context.expectContainerWith(BlockPos.create(1, 3, 1), Items.BOOK);

		context.complete();
	}

	@GameTest(structureName = "insertion_lock")
	public static void testInsertionLock(QuiltTestContext context) {
		testLockRefuse(context);
	}

	@GameTest(structureName = "insertion_locked")
	public static void testInsertionLocked(QuiltTestContext context) {
		testLockRefuse(context);
	}

	private static void testLockRefuse(QuiltTestContext context) {
		var stack = new ItemStack(Items.BOOK);
		var player = context.createMockPlayer();

		player.setPosition(context.getAbsolute(BlockPos.ORIGIN.ofCenter()));

		interactWithShelfAt(context, player, BlockPos.create(1, 3, 1), stack);
		context.expectEmptyContainer(BlockPos.create(1, 3, 1));

		context.complete();
	}

	private static void interactWithShelfAt(
			QuiltTestContext context, PlayerEntity player, BlockPos pos, ItemStack stack
	) {
		var actualPos = context.getAbsolutePos(pos);
		BlockState state = context.getBlockState(pos);

		if (!state.isIn(AurorasDecoTags.SHELVES)) {
			context.throwPositionedException("Expected shelf block.", pos);
			return;
		}

		player.setStackInHand(Hand.MAIN_HAND, stack);

		Direction sideHit = state.get(ShelfBlock.FACING);
		var offset = sideHit.rotateYCounterclockwise().getUnitVector().mul(0.1f);
		Vec3d hitPos = Vec3d.ofCenter(actualPos).subtract(0, 0.2, 0)
				.subtract(offset.x(), offset.y(), offset.z());

		var blockHitResult = new BlockHitResult(hitPos, sideHit, actualPos, true);
		var itemUsageContext = new ItemUsageContext(player, Hand.MAIN_HAND, blockHitResult);

		state.onUse(context.getWorld(), player, Hand.MAIN_HAND, blockHitResult);
	}
}
