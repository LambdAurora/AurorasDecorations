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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * Represents a potted azalea.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BigPottedAzaleaBlock extends BigFlowerPotBlock {
	public static final VoxelShape AZALEA_SHAPE = createCuboidShape(
			2.8f, 14.f, 2.8f,
			13.2f, 23.2f, 13.2f
	);
	public static final VoxelShape POTTED_AZALEA_SHAPE = VoxelShapes.union(BIG_FLOWER_POT_SHAPE, AZALEA_SHAPE);

	public BigPottedAzaleaBlock(PottedPlantType type) {
		super(type);
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return POTTED_AZALEA_SHAPE;
	}
}
