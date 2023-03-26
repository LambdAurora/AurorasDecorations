/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Represents a potted cactus.
 * <p><i>Spiky...</i></p>
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BigPottedCactusBlock extends BigFlowerPotBlock {
	public static final VoxelShape CACTUS_SHAPE = createCuboidShape(
			3.f, 14.f, 3.f,
			13.f, 23.1f, 13.f
	);
	public static final VoxelShape POCKET_CACTUS_SHAPE = VoxelShapes.union(
			Block.createCuboidShape(6.05, 14.0, 6.05, 9.95, 16.72, 9.95),
			Block.createCuboidShape(6.7, 16.72, 6.7, 9.3, 23.2, 9.3)
	);

	private final Box cactusBox;
	private final VoxelShape shape;

	public BigPottedCactusBlock(PottedPlantType plantType, VoxelShape cactusShape) {
		super(plantType);

		this.cactusBox = cactusShape.getBoundingBox();
		this.shape = VoxelShapes.union(cactusShape, BIG_FLOWER_POT_SHAPE);
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.shape;
	}

	/* Collision */

	@SuppressWarnings("deprecation")
	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		Box cactusBox = this.cactusBox.offset(pos).expand(0.1);
		Box entityBox = entity.getBoundingBox();

		if (cactusBox.intersects(entityBox)) {
			entity.damage(world.getDamageSources().cactus(), 1.f);
		}
	}
}
