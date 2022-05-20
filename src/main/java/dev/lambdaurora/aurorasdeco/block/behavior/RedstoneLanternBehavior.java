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

package dev.lambdaurora.aurorasdeco.block.behavior;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.logic.RedstoneSignalLevels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Contains most of the redstone lantern behavior logic.
 * <p>
 * The logic should:
 * <ul>
 *     <li>never transmit power upward</li>
 *     <li>respect {@link RedstoneTorchBlock}'s burnout logic</li>
 *     <li>never power the attachment block</li>
 *     <li>if hung from the ceiling or from the sides, then it should transmit power to the surrounding and strongly downwards</li>
 * </ul>
 */
public final class RedstoneLanternBehavior {
	public static final BooleanProperty LIT = Properties.LIT;
	private final Map<BlockView, List<BurnoutEntry>> burnoutMap = new WeakHashMap<>();
	private final Function<BlockState, Direction> attachmentDirection;

	public RedstoneLanternBehavior(Function<BlockState, Direction> attachmentDirection) {
		this.attachmentDirection = attachmentDirection;
	}

	public static boolean isLit(BlockState state) {
		return state.get(LIT);
	}

	public void neighborUpdate(BlockState state, World world, BlockPos pos) {
		if (isLit(state) == this.shouldUnpower(world, pos, state) && !world.getBlockTickScheduler().willTick(pos, state.getBlock())) {
			world.scheduleBlockTick(pos, state.getBlock(), 2);
		}
	}

	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		// This is needed to never power anything upward.
		if (this.attachmentDirection.apply(state) == Direction.UP && direction == Direction.DOWN) {
			return RedstoneSignalLevels.SIGNAL_NONE;
		}

		return state.get(LIT) && this.attachmentDirection.apply(state) != direction ? RedstoneSignalLevels.SIGNAL_MAX : RedstoneSignalLevels.SIGNAL_NONE;
	}

	public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return direction == Direction.UP ? state.getWeakRedstonePower(world, pos, direction) : RedstoneSignalLevels.SIGNAL_NONE;
	}

	/**
	 * {@return {@code true} if the given redstone lantern state should power off, otherwise {@code false}}
	 *
	 * @param world the world the redstone lantern is in
	 * @param pos the position of the redstone lantern
	 * @param state the block state of the redstone lantern
	 */
	private boolean shouldUnpower(World world, BlockPos pos, BlockState state) {
		Direction direction = this.attachmentDirection.apply(state).getOpposite();
		return world.isEmittingRedstonePower(pos.offset(direction), direction);
	}

	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos) {
		boolean shouldUnpower = this.shouldUnpower(world, pos, state);
		List<BurnoutEntry> list = this.burnoutMap.get(world);

		while (list != null && !list.isEmpty() && world.getTime() - list.get(0).time > RedstoneTorchBlock.RECENT_TOGGLE_TIMER) {
			list.remove(0);
		}

		if (isLit(state)) {
			if (shouldUnpower) {
				world.setBlockState(pos, state.with(LIT, false), Block.NOTIFY_ALL);

				if (this.isBurnedOut(world, pos, true)) {
					world.syncWorldEvent(WorldEvents.REDSTONE_TORCH_BURNS_OUT, pos, 0);
					world.scheduleBlockTick(pos, world.getBlockState(pos).getBlock(), RedstoneTorchBlock.RESTART_DELAY);
				}
			}
		} else if (!shouldUnpower && !this.isBurnedOut(world, pos, false)) {
			world.setBlockState(pos, state.with(LIT, true), Block.NOTIFY_ALL);
		}
	}

	private boolean isBurnedOut(World world, BlockPos pos, boolean addNew) {
		var list = this.burnoutMap.computeIfAbsent(world, w -> new ArrayList<>());

		if (addNew) {
			list.add(new BurnoutEntry(pos.toImmutable(), world.getTime()));
		}

		int i = 0;

		for (var burnoutEntry : list) {
			if (burnoutEntry.pos().equals(pos) && ++i >= RedstoneTorchBlock.MAX_RECENT_TOGGLES) {
				return true;
			}
		}

		return false;
	}

	public record BurnoutEntry(BlockPos pos, long time) {
	}
}
