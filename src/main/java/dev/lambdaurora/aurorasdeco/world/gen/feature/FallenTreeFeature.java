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

package dev.lambdaurora.aurorasdeco.world.gen.feature;

import com.mojang.serialization.Codec;
import dev.lambdaurora.aurorasdeco.world.gen.feature.config.FallenTreeFeatureConfig;
import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Represents a fallen tree.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class FallenTreeFeature extends Feature<FallenTreeFeatureConfig> {
	public FallenTreeFeature(Codec<FallenTreeFeatureConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeatureContext<FallenTreeFeatureConfig> context) {
		var config = context.getConfig();
		var random = context.getRandom();

		int length = config.baseLength();
		if (config.variance() > 0) {
			length += random.nextInt(config.variance());
		}

		var axis = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
		var direction = Direction.from(axis, random.nextBoolean() ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);

		BlockPos below = context.getOrigin().down(1);
		if (!isSoil(context.getWorld(), below) || !this.isAirOrVegetation(context.getWorld(), context.getOrigin())) {
			return false;
		}

		boolean result;
		int i = 0;
		while (!(result = this.generate(context.getWorld(), random, context.getOrigin(), length, direction, config)) && i < 2) {
			direction = direction.rotateYClockwise();
			i++;
		}

		return result;
	}

	private boolean isVegetation(StructureWorldAccess world, BlockPos pos) {
		return world.testBlockState(pos, state -> !state.isSolidBlock(world, pos));
	}

	private boolean isAirOrVegetation(StructureWorldAccess world, BlockPos pos) {
		return world.testBlockState(pos, state -> state.isAir()
				|| !state.isSolidBlock(world, pos)
				|| state.isIn(BlockTags.LEAVES)
		);
	}

	private boolean generate(StructureWorldAccess world, RandomGenerator random, BlockPos origin, int length, Direction direction,
			FallenTreeFeatureConfig config) {
		Direction.Axis axis = direction.getAxis();
		BlockPos.Mutable pos = origin.mutableCopy();

		var blocks = new ArrayList<BlockPos>();

		int air = 0;
		pos.move(direction);
		Direction logDirection = direction;
		BlockPos last = pos;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

		for (int n = 0; n < length; n++) {
			var res = this.canGenerate(world, last, direction, logDirection);

			if (res == null)
				return false;

			if (res.air())
				air++;
			logDirection = res.logDirection();

			blocks.add(last = res.pos());

			minY = Math.min(minY, last.getY());
			maxY = Math.max(maxY, last.getY());
		}

		// No floating logs.
		if (air * 2 > length) {
			return false;
		}

		// Avoid tree staircase.
		if (Math.abs(maxY - minY) > 2) {
			return false;
		}

		pos.set(origin);

		if (blocks.size() != length) {
			return false;
		}

		// First log
		this.setBlockState(world, pos, config.trunkProvider().getBlockState(random, pos).with(PillarBlock.AXIS, Direction.Axis.Y));
		this.setBlockState(world, pos.move(0, -1, 0), Blocks.ROOTED_DIRT.getDefaultState());
		pos.move(0, 1, 0);
		if (config.layerType() != FallenTreeFeatureConfig.LayerType.SNOW) {
			Direction tmp = direction;
			for (int i = 0; i < 3; i++) {
				tmp = tmp.rotateYClockwise();
				if (random.nextBoolean() && this.isAirOrVegetation(world, origin.offset(tmp))) {
					var offset = origin.offset(tmp);
					this.setBlockState(world, offset,
							config.vineProvider().getBlockState(random, offset)
									.with(VineBlock.getFacingProperty(tmp.getOpposite()), true)
					);
				}
			}
		}

		for (var block : blocks) {
			this.setBlockState(world, block, config.trunkProvider().getBlockState(random, pos).with(PillarBlock.AXIS, axis));

			BlockPos offset = block.down();
			if (isSoil(world, offset)) {
				this.setBlockState(world, offset, random.nextBoolean()
						? Blocks.DIRT.getDefaultState()
						: Blocks.ROOTED_DIRT.getDefaultState()
				);
			}
		}

		if (config.layerType() != FallenTreeFeatureConfig.LayerType.SNOW) {
			for (var placeTo : blocks) {
				if (random.nextBoolean()) {
					var vineDirection = random.nextBoolean() ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
					var offset = placeTo.offset(vineDirection);

					if (this.isAirOrVegetation(world, offset)) {
						var vineState = config.vineProvider().getBlockState(random, offset)
								.with(VineBlock.getFacingProperty(vineDirection.getOpposite()), true);

						if (world.testFluidState(offset, fluidState -> fluidState.isIn(FluidTags.WATER))) {
							if (vineState.getProperties().contains(Properties.WATERLOGGED))
								vineState = vineState.with(Properties.WATERLOGGED, true);
							else
								vineState = null;
						}

						if (vineState != null) {
							this.setBlockState(world, offset, vineState);

							// Fix floating top double plant.
							var up = offset.up();
							if (this.isVegetation(world, up)) {
								this.setBlockState(world, up, Blocks.AIR.getDefaultState());
							}
						}
					}
				}
			}

			for (var block : blocks) {
				var offset = block.up();
				if (!this.isAirOrVegetation(world, offset))
					continue;

				var value = random.nextInt(4);
				if (value == 0) {
					var mushroomBlock = config.mushroomProvider().getBlockState(random, offset);
					if (!mushroomBlock.isAir()) {
						world.setBlockState(offset, mushroomBlock, Block.NOTIFY_LISTENERS);
					}
				} else if (value == 1) {
					var layerBlock = config.layerType().getBlock();
					if (layerBlock != Blocks.AIR) {
						world.setBlockState(offset, layerBlock.getDefaultState(), Block.NOTIFY_LISTENERS);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks if we can generate the fallen tree at the next position following the direction.
	 *
	 * @param world the world
	 * @param lastPos the last position of the log block
	 * @param direction the direction where the fallen tree goes
	 * @param logDirection the direction to define if the tree logs go up or down. At first run, the value is undefined and same as direction
	 * @return Aa optional value of the log position, if the block below is air and the log direction (may be non-equal to logDirection if logDirection was equals to direction).
	 */
	protected @Nullable LogPlacementResult canGenerate(StructureWorldAccess world, BlockPos lastPos, Direction direction, Direction logDirection) {
		var pos = lastPos.offset(direction).mutableCopy();

		// Check if the log block can be generated and is not conflicting with another block.
		if (!this.isAirOrVegetation(world, pos)) {
			if (logDirection != Direction.UP) {
				if (logDirection == direction)
					logDirection = Direction.UP;
				else
					return null;
			}

			if (!this.isAirOrVegetation(world, pos.move(logDirection)))
				return null;
		}

		// If the block under the log block is air.
		boolean air = false;

		// Check for air blocks (and go down if possible)
		if (!world.testBlockState(pos.move(Direction.DOWN), BlockState::isOpaque)) {
			// We can't go down so count the air block.
			if (logDirection != Direction.DOWN && logDirection != direction)
				air = true;
				// The log direction is undefined, as we go down, set it to down.
			else if (logDirection == direction) {
				logDirection = Direction.DOWN;
				// The block down is still air, so count the air.
				if (!world.testBlockState(pos.move(logDirection), BlockState::isOpaque))
					air = true;
				// Log direction is already defined, same as before: if the block down is still air then count the air.
			} else if (!world.testBlockState(pos.move(logDirection), BlockState::isOpaque))
				air = true;
		}

		// Goes up again for the log block as the old value is for the block below.
		pos.move(Direction.UP);
		return new LogPlacementResult(pos.toImmutable(), air, logDirection);
	}

	private boolean canGenerateLower(int i, int length, boolean previous) {
		if (previous) return true;
		else if (i >= length / 2) return true;
		else return i == 0;
	}

	protected record LogPlacementResult(BlockPos pos, boolean air, Direction logDirection) {}
}
