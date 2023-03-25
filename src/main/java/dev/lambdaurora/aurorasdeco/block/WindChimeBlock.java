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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.block.entity.SwayingBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class WindChimeBlock extends BlockWithEntity implements Waterloggable {
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final VoxelShape SHAPE;
	public static final Box COLLISION_BOX;

	private static final VoxelShape HOLDER_SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 1.0, 10.0);

	public WindChimeBlock(Settings settings) {
		super(settings);

		this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	/* Placement */

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		var upPos = pos.up();
		var upState = world.getBlockState(upPos);

		if (upState.isIn(BlockTags.LEAVES))
			return true;

		if (upState.isOf(state.getBlock()))
			return false;

		return !VoxelShapes.matchesAnywhere(upState.getSidesShape(world, upPos).getFace(Direction.DOWN),
				HOLDER_SHAPE, BooleanBiFunction.ONLY_SECOND);
	}

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var state = super.getPlacementState(ctx);
		if (state != null) {
			var world = ctx.getWorld();
			var pos = ctx.getBlockPos();
			var fluidState = world.getFluidState(pos);

			return state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		}

		return null;
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
			BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		state = super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
		return direction == Direction.UP && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
	}

	/* Interaction */

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		return this.swing(world, hit, player, true) ? ActionResult.success(world.isClient()) : ActionResult.PASS;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (world.isClient())
			return;
		if (entity instanceof ProjectileEntity)
			return;

		var windChime = AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE.get(world, pos);
		if (windChime == null)
			return;

		if (windChime.getCollisionBox().intersects(entity.getBoundingBox()))
			this.swing(entity, world, pos, null, true);
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		var entity = projectile.getOwner();
		var playerEntity = entity instanceof PlayerEntity ? (PlayerEntity) entity : null;
		this.swing(world, hit, playerEntity, true);
	}

	private boolean isPointOnLantern(Direction side, double y) {
		return side.getAxis() != Direction.Axis.Y && y <= 0.8123999834060669D;
	}

	public boolean swing(World world, BlockHitResult hitResult, @Nullable PlayerEntity player,
			boolean hitResultIndependent) {
		var direction = hitResult.getSide();
		var blockPos = hitResult.getBlockPos();
		boolean canSwing = !hitResultIndependent
				|| this.isPointOnLantern(direction, hitResult.getPos().y - (double) blockPos.getY());
		if (canSwing) {
			this.swing(player, world, blockPos, direction, false);

			return true;
		} else {
			return false;
		}
	}

	public void swing(@Nullable Entity entity, World world, BlockPos pos, @Nullable Direction direction, boolean collision) {
		var blockEntity = AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE.get(world, pos);
		if (!world.isClient() && blockEntity != null) {
			if (!blockEntity.isColliding()) {
				/*world.playSound(null, pos, AurorasDecoSounds.LANTERN_SWING_SOUND_EVENT, SoundCategory.BLOCKS,
						2.f, 1.f);*/
				world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
			}

			if (!collision)
				blockEntity.activate(direction);
			else if (direction == null && entity != null)
				blockEntity.activate(entity);
		}
	}

	/* Block Entity Stuff */

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE.instantiate(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return checkType(type, AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE,
				world.isClient() ? SwayingBlockEntity::clientTick : SwayingBlockEntity::serverTick);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	/* Fluid */

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	static {
		VoxelShape up = createCuboidShape(4.0, 13.0, 4.0, 12.0, 16.0, 12.0);
		VoxelShape wood = createCuboidShape(3.0, 12.0, 3.0, 13.0, 13.0, 13.0);
		VoxelShape chimes = createCuboidShape(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);
		SHAPE = VoxelShapes.union(up, wood, chimes);
		COLLISION_BOX = chimes.getBoundingBox().expand(0.1);
	}
}
