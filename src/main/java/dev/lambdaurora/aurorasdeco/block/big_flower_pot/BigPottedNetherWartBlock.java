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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/**
 * Represents a potted nether wart.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BigPottedNetherWartBlock extends BigPottedProxyBlock {
	public BigPottedNetherWartBlock(PottedPlantType type) {
		super(type);
	}

	/* Random Ticks */

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return this.getPlant().hasRandomTicks(state);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		this.getPlant().randomTick(state, world, pos, random);
	}

	/* Interaction */

	@Override
	protected boolean shouldAllowCustomUsageInAdventureMode() {
		return true;
	}

	@Override
	protected ActionResult onCustomUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		var handStack = player.getStackInHand(hand);
		if ((handStack.isEmpty() || handStack.getItem() instanceof HoeItem) && state.get(NetherWartBlock.AGE) >= 3) {
			if (!world.isClient()) {
				dropStacks(this.getPlantState(state), world, pos, null, player, handStack);
				world.setBlockState(pos, state.with(NetherWartBlock.AGE, 0), NOTIFY_ALL);
				world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.create(player, state));
			}

			return ActionResult.success(world.isClient());
		}

		return ActionResult.PASS;
	}
}
