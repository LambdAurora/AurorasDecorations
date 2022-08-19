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
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Represents a potted sweet berry bush.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BigPottedSweetBerryBushBlock extends BigPottedProxyBlock implements Fertilizable {
	private static final Box SWEET_BERRY_BUSH_BOX;

	public BigPottedSweetBerryBushBlock(PottedPlantType type) {
		super(type);
	}

	/* Shapes */

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return BIG_FLOWER_POT_SHAPE;
	}

	/* Interaction */

	@Override
	protected boolean shouldAllowCustomUsageInAdventureMode() {
		return true;
	}

	@Override
	public ActionResult onCustomUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		var result = this.getPlant().onUse(state, world, pos, player, hand, hit);
		if (result.isAccepted())
			return result;
		else if (player.getStackInHand(hand).isOf(Items.BONE_MEAL))
			return ActionResult.FAIL;
		else
			return ActionResult.PASS;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		var selfBox = SWEET_BERRY_BUSH_BOX.offset(pos);
		if (selfBox.intersects(entity.getBoundingBox())) {
			this.getPlant().onEntityCollision(state, world, pos, entity);
		}
	}

	/* Fertilization */

	@Override
	public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
		return ((Fertilizable) this.getPlant()).isFertilizable(world, pos, state, isClient);
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return ((Fertilizable) this.getPlant()).canGrow(world, random, pos, state);
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		((Fertilizable) this.getPlant()).grow(world, random, pos, state);
	}

	static {
		VoxelShape largeSweetBerryBushShape = createCuboidShape(
				3.f, 14.f, 3.f,
				13.f, 23.5f, 13.f
		);
		SWEET_BERRY_BUSH_BOX = largeSweetBerryBushShape.getBoundingBox();
	}
}
