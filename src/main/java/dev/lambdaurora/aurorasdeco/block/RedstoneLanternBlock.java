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

import dev.lambdaurora.aurorasdeco.block.behavior.RedstoneLanternBehavior;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.Random;

@SuppressWarnings("deprecation")
public class RedstoneLanternBlock extends LanternBlock {
	private final RedstoneLanternBehavior behavior = new RedstoneLanternBehavior(state -> state.get(HANGING) ? Direction.DOWN : Direction.UP);

	public RedstoneLanternBlock() {
		super(QuiltBlockSettings.copyOf(Blocks.LANTERN).luminance(state -> state.get(RedstoneLanternBehavior.LIT) ? 7 : 0));

		this.setDefaultState(this.getDefaultState().with(RedstoneLanternBehavior.LIT, true));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(RedstoneLanternBehavior.LIT);
	}

	/* Updates */

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		for (var direction : AuroraUtil.DIRECTIONS) {
			world.updateNeighborsAlways(pos.offset(direction), this);
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!moved) {
			for (var direction : AuroraUtil.DIRECTIONS) {
				world.updateNeighborsAlways(pos.offset(direction), this);
			}
		}
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		super.neighborUpdate(state, world, pos, block, fromPos, notify);
		this.behavior.neighborUpdate(state, world, pos);
	}

	/* Ticking */

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		super.scheduledTick(state, world, pos, random);
		this.behavior.scheduledTick(state, world, pos);
	}

	/* Redstone */

	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return this.behavior.getWeakRedstonePower(state, world, pos, direction);
	}

	@Override
	public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return this.behavior.getStrongRedstonePower(state, world, pos, direction);
	}

	/* Visual */

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (RedstoneLanternBehavior.isLit(state)) {
			double x = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.75;
			double y = (double) pos.getY() + 0.25 + (random.nextDouble() - 0.5) * 0.4;
			double z = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.75;
			world.addParticle(DustParticleEffect.DEFAULT, x, y, z, 0.0, 0.0, 0.0);
		}
	}
}
