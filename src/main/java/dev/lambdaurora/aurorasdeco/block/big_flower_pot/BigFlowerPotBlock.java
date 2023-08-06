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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a big flower pot.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BigFlowerPotBlock extends Block/* implements FluidFillable*/ {
	private static final Identifier PLANT_LOOT_ID = AurorasDeco.id("plant");

	public static final VoxelShape BIG_FLOWER_POT_SHAPE = createCuboidShape(
			1.f, 0.f, 1.f,
			15.f, 14.f, 15.f
	);
	public static final VoxelShape PLANT_FULL_CUBE = createCuboidShape(
			2.8f, 14.f, 2.8f,
			13.2f, 23.1f, 13.2f
	);

	public static final VoxelShape BIG_FLOWER_POT_POTTED_FULL_CUBE_SHAPE = VoxelShapes.union(BIG_FLOWER_POT_SHAPE, PLANT_FULL_CUBE);

	public static final Identifier POT_BETTERGRASS_DATA = AurorasDeco.id("bettergrass/data/big_flower_pot");

	protected final PottedPlantType type;

	public BigFlowerPotBlock(PottedPlantType type, Settings settings) {
		super(settings);

		this.type = type;
	}

	public BigFlowerPotBlock(PottedPlantType type) {
		this(type, FabricBlockSettings.create()
				.notSolid().pistonBehavior(PistonBehavior.DESTROY)
				.strength(.1f).nonOpaque()
		);
	}

	public PottedPlantType getPlantType() {
		return this.type;
	}

	public Block getPlant() {
		return this.getPlantType().getPlant();
	}

	public BlockState getPlantState(BlockState potState) {
		return this.getPlant().getDefaultState();
	}

	public @Nullable ItemStack getEquivalentPlantStack(BlockState state) {
		var item = this.getPlantType().getItem();
		if (item == null) return null;
		return new ItemStack(item);
	}

	public boolean isEmpty() {
		return this.getPlantType().isEmpty();
	}

	public boolean hasDynamicModel() {
		return !this.isEmpty();
	}

	public float getScale() {
		return .65f;
	}

	public boolean allowBlocksOnTop() {
		if (this.getPlantType().isTall())
			return false;
		return this.getPlantType().getCategory().allowBlocksOnTop();
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return BIG_FLOWER_POT_SHAPE;
	}

	/* Interaction */

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		if (this.getPlantType().getItem() == Items.AIR)
			return super.getPickStack(world, pos, state);

		return new ItemStack(this.getPlantType().getItem());
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		boolean canDoCustomUsage = this.shouldAllowCustomUsageInAdventureMode() || player.getAbilities().allowModifyWorld;

		if (canDoCustomUsage) {
			var action = this.onCustomUse(state, world, pos, player, hand, hit);

			if (action.isAccepted() || action == ActionResult.FAIL) {
				return action;
			}
		}

		if (!player.getAbilities().allowModifyWorld) {
			return ActionResult.FAIL;
		}

		var handStack = player.getStackInHand(hand);
		var toPlace = PottedPlantType.getFlowerPotFromItem(handStack.getItem());
		boolean empty = this.isEmpty();
		boolean toPlaceEmpty = toPlace.isEmpty();
		if (empty != toPlaceEmpty) {
			var up = pos.up();
			if (empty) {
				if (!toPlace.allowBlocksOnTop() && !world.getBlockState(up).isAir())
					return ActionResult.PASS;

				if (!world.isClient()) {
					world.setBlockState(pos, toPlace.getPlacementState(new ItemPlacementContext(player, hand, handStack, hit)),
							Block.NOTIFY_ALL);
					player.incrementStat(Stats.POT_FLOWER);
					if (!player.getAbilities().creativeMode) {
						handStack.decrement(1);
					}

					if (!toPlace.allowBlocksOnTop())
						world.setBlockState(up, AurorasDecoRegistry.PLANT_AIR_BLOCK.getDefaultState());
					world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
				}
			} else if (!world.isClient()) {
				this.removePlant(world, pos, state, player, hand, true);
			}

			return ActionResult.success(world.isClient());
		} else {
			return toPlaceEmpty ? ActionResult.PASS : ActionResult.CONSUME;
		}
	}

	protected boolean shouldAllowCustomUsageInAdventureMode() {
		return false;
	}

	/**
	 * Triggered for custom usage of this block.
	 *
	 * @param state the block state interacted with
	 * @param world the world the interaction interacted in
	 * @param pos the block position of the interaction
	 * @param player the player who interacted
	 * @param hand the hand used to interact
	 * @param hit the hit result of the interaction
	 * @return the action result
	 */
	protected ActionResult onCustomUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		return ActionResult.PASS;
	}

	private void removePlant(World world, BlockPos pos, BlockState state, @Nullable PlayerEntity player, @Nullable Hand hand, boolean removeUp) {
		var droppedStack = this.getEquivalentPlantStack(state);

		if (droppedStack != null && !droppedStack.isEmpty()) {
			if (player != null) {
				var handStack = player.getStackInHand(hand);
				if (handStack.isEmpty()) {
					player.setStackInHand(hand, droppedStack);
				} else if (!player.giveItemStack(droppedStack)) {
					player.dropItem(droppedStack, false);
				}
			} else {
				var itemEntity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + 1.5f, pos.getZ() + .5f,
						droppedStack);
				float speed = world.random.nextFloat() * .5f;
				float angle = world.random.nextFloat() * 6.2831855f;
				itemEntity.setVelocity(-MathHelper.sin(angle) * speed, 0.20000000298023224D, MathHelper.cos(angle) * speed);
				world.spawnEntity(itemEntity);
			}
		}

		world.setBlockState(pos, AurorasDecoRegistry.BIG_FLOWER_POT_BLOCK.getDefaultState(), Block.NOTIFY_ALL);

		if (removeUp) {
			var up = pos.up();
			if (world.getBlockState(up).isOf(AurorasDecoRegistry.PLANT_AIR_BLOCK)) {
				world.setBlockState(up, Blocks.AIR.getDefaultState());
			}
		}

		world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	/* Visual */

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, RandomGenerator random) {
		if (this.getPlant() != null) {
			this.getPlant().randomDisplayTick(state, world, pos, random);
		}
	}

	/* Loot table */

	protected void acceptPlantDrops(BlockState state, LootContextParameterSet.Builder builder, Consumer<ItemStack> consumer) {
		var item = this.getEquivalentPlantStack(state);
		if (item != null) {
			consumer.accept(item);
		}
	}

	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
		if (this.isEmpty())
			return super.getDroppedStacks(state, builder);

		builder.withDynamicDrop(PLANT_LOOT_ID, (consumer) -> this.acceptPlantDrops(state, builder, consumer));

		return AurorasDecoRegistry.BIG_FLOWER_POT_BLOCK.getDroppedStacks(state, builder);
	}

	/* Entity Stuff */

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	/* Fluid */

    /*@Override
    public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.contains(Properties.WATERLOGGED) && !state.get(Properties.WATERLOGGED) && fluid == Fluids.WATER;
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.contains(Properties.WATERLOGGED) && !state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(Properties.WATERLOGGED, Boolean.TRUE), Block.NOTIFY_ALL);
                world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            }

            return true;
        } else
            return false;
    }*/

	public static class PlantAirBlock extends Block {
		public PlantAirBlock(Settings settings) {
			super(settings.pistonBehavior(PistonBehavior.DESTROY));
		}

		@Override
		public BlockRenderType getRenderType(BlockState state) {
			return BlockRenderType.INVISIBLE;
		}

		/* Shapes */

		@Override
		public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
			return VoxelShapes.empty();
		}

		/* Interaction */

		@Override
		public boolean canReplace(BlockState state, ItemPlacementContext context) {
			return false;
		}

		@Override
		public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
			super.onStateReplaced(state, world, pos, newState, moved);

			var downPos = pos.down();
			var downState = world.getBlockState(downPos);
			if (downState.getBlock() instanceof BigFlowerPotBlock block && !block.isEmpty()) {
				block.removePlant(world, downPos, downState, null, null, false);
			}
		}

		@Override
		public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
			var downPos = pos.down();
			var downState = world.getBlockState(downPos);
			if (downState.getBlock() instanceof BigFlowerPotBlock) {
				downState.onEntityCollision(world, downPos, entity);
			}
		}

		/* Updates */

		@Override
		public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
				BlockPos pos, BlockPos posFrom) {
			var downState = world.getBlockState(pos.down());
			if (downState.getBlock() instanceof BigFlowerPotBlock block && !block.isEmpty()) {
				return state;
			}

			return Blocks.AIR.getDefaultState();
		}
	}
}
