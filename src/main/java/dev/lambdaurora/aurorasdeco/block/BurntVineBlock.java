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

import dev.lambdaurora.aurorasdeco.mixin.block.VineBlockAccessor;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.VineBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.Random;

/**
 * Represents a burnt vine tip block.
 * <p>
 * Prevents vine from growing further.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BurntVineBlock extends VineBlock {
	public BurntVineBlock() {
		super(
				QuiltBlockSettings.of(Material.REPLACEABLE_PLANT)
						.noCollision()
						.strength(.2f)
						.sounds(BlockSoundGroup.VINE)
		);

		FlammableBlockRegistry.getDefaultInstance().add(this, 15, 100);
		Item.BLOCK_ITEMS.put(this, Items.VINE);
	}

	public static BlockState fromVine(BlockState state) {
		BlockState self = AurorasDecoPlants.BURNT_VINE_BLOCK.getDefaultState();

		return self.with(UP, state.get(UP))
				.with(NORTH, state.get(NORTH))
				.with(EAST, state.get(EAST))
				.with(SOUTH, state.get(SOUTH))
				.with(WEST, state.get(WEST));
	}

	/* Ticking */

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		// No random tick.
	}

	/* Placement */

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return ((VineBlockAccessor) (Object) this).aurorasdeco$hasAdjacentBlocks(this.getPlacementShape(state, world, pos));
	}

	private boolean shouldHaveSide(BlockView world, BlockPos pos, Direction side) {
		if (side == Direction.DOWN) {
			return false;
		} else {
			var sidePos = pos.offset(side);
			if (shouldConnectTo(world, sidePos, side)) {
				return true;
			} else if (side.getAxis() == Direction.Axis.Y) {
				return false;
			} else {
				var facing = FACING_PROPERTIES.get(side);
				var upState = world.getBlockState(pos.up());
				return upState.isOf(Blocks.VINE) && upState.get(facing);
			}
		}
	}

	private BlockState getPlacementShape(BlockState state, BlockView world, BlockPos pos) {
		var upPos = pos.up();
		if (state.get(UP)) {
			state = state.with(UP, shouldConnectTo(world, upPos, Direction.DOWN));
		}

		BlockState blockState = null;
		var it = Direction.Type.HORIZONTAL.iterator();

		while (true) {
			Direction facing;
			BooleanProperty facingProperty;
			do {
				if (!it.hasNext()) {
					return state;
				}

				facing = it.next();
				facingProperty = getFacingProperty(facing);
			} while (!state.get(facingProperty));

			boolean shouldHaveSide = this.shouldHaveSide(world, pos, facing);
			if (!shouldHaveSide) {
				if (blockState == null) {
					blockState = world.getBlockState(upPos);
				}

				shouldHaveSide = blockState.isOf(Blocks.VINE) && blockState.get(facingProperty);
			}

			state = state.with(facingProperty, shouldHaveSide);
		}
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (direction == Direction.DOWN) {
			return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
		} else {
			var placementShape = this.getPlacementShape(state, world, pos);
			return !((VineBlockAccessor) (Object) this).aurorasdeco$hasAdjacentBlocks(placementShape) ?
					Blocks.AIR.getDefaultState() : placementShape;
		}
	}
}
