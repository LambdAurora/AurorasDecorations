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

package dev.lambdaurora.aurorasdeco.block.plant;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoParticles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Represents the lavender flower.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class LavenderBlock extends AuroraFlowerBlock {
	private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

	public LavenderBlock() {
		super(StatusEffects.REGENERATION, 10, defaultSettings());
	}

	/* Shape */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	/* Visual */

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, RandomGenerator random) {
		if (!world.getDimension().hasCeiling() && world.isSkyVisible(pos)) {
			if (random.nextBoolean()) {
				double x = pos.getX() + random.nextFloat() * 5;
				double y = pos.getY() + random.nextFloat();
				double z = pos.getZ() + random.nextFloat() * 5;
				world.addParticle(AurorasDecoParticles.LAVENDER_PETAL, x, y, z, 0.f, 0.f, 0.f);
			}
		}
	}
}
