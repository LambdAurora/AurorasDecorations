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

import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a flower pot block with a facing property.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class DirectionalFlowerPotBlock extends FlowerPotBlock {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public DirectionalFlowerPotBlock(Block content, Settings settings) {
		super(content, settings);

		if (!content.getStateManager().getProperties().contains(FACING))
			throw new IllegalArgumentException("Content of a DirectionalFlowerPotBlock needs to contain the facing property from Properties.");

		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/* Placement */

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var state = super.getPlacementState(ctx);
		if (state == null) return null;
		var plantState = this.getContent().getPlacementState(ctx);
		if (plantState != null)
			return AuroraUtil.remapBlockState(plantState, state);
		return state;
	}
}
