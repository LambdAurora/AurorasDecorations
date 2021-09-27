/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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

import com.google.common.collect.ImmutableMap;
import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a blackboard that can be edited by players if not locked.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BlackboardBlock extends BlockWithEntity implements Waterloggable {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	private static final Map<Direction, VoxelShape> SHAPES;

	private final boolean locked;

	public BlackboardBlock(Settings settings, boolean locked) {
		super(settings);
		this.locked = locked;

		this.setDefaultState(this.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(LIT, false)
				.with(WATERLOGGED, false)
		);
	}

	/**
	 * Returns whether this blackboard block is locked or not.
	 *
	 * @return {@code true} if locked, else {@code false}
	 */
	public boolean isLocked() {
		return this.locked;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT, WATERLOGGED);
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPES.get(state.get(FACING));
	}

	/* Placement */

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return world.getBlockState(pos.offset(state.get(FACING).getOpposite())).getMaterial().isSolid();
	}

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var state = this.getDefaultState();
		var fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		var world = ctx.getWorld();
		var pos = ctx.getBlockPos();
		var directions = ctx.getPlacementDirections();

		var nbt = ctx.getStack().getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
		if (nbt != null && nbt.contains("lit")) {
			state = state.with(LIT, nbt.getBoolean("lit"));
		}

		for (var direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				var opposite = direction.getOpposite();
				state = state.with(FACING, opposite);
				if (state.canPlaceAt(world, pos)) {
					return state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
				}
			}
		}

		return null;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			if (stack.hasCustomName()) {
				blackboard.setCustomName(stack.getName());
			}

			var nbt = stack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY);
			if (state.get(WATERLOGGED) && !this.isLocked())
				return;

			if (nbt != null && Blackboard.shouldConvert(nbt)) {
				var blackboardData = new Blackboard();
				blackboardData.readNbt(nbt);
				blackboard.copy(blackboardData);
			}
		}
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
	                                            WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED)) {
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos))
			return Blocks.AIR.getDefaultState();

		if (!this.isLocked()) {
			var blackboard = this.getBlackboardEntity(world, pos);
			if (blackboard != null && !world.isClient()) {
				if (state.get(WATERLOGGED) && !blackboard.isEmpty()) {
					blackboard.clear();
				}
			}
		}

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	/* Interaction */

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos,
	                          PlayerEntity player, Hand hand, BlockHitResult hit) {
		var stack = player.getStackInHand(hand);
		var offhand = player.getStackInHand(Hand.OFF_HAND);
		var facing = state.get(FACING);

		if (!this.isLocked() && hit.getSide() == facing) {
			var blackboard = this.getBlackboardEntity(world, pos);
			if (blackboard != null) {
				if (blackboard.lastUser != null && blackboard.lastUser.isRemoved()) {
					blackboard.lastUser = null;
				}

				var color = Blackboard.getColorFromItem(stack.getItem());
				boolean isBoneMeal = stack.isOf(Items.BONE_MEAL);
				boolean isCoal = stack.isIn(ItemTags.COALS);
				if (stack.isOf(Items.WATER_BUCKET) && this.tryClear(world, blackboard, player)) {
					world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
							2.f, 1.f);
					return ActionResult.success(world.isClient());
				} else if (stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) == Potions.WATER
						&& this.tryClear(world, blackboard, player)) {
					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
					if (!player.getAbilities().creativeMode) {
						stack.decrement(1);

						if (stack.isEmpty()) {
							player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
						} else {
							player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
						}
					}
					world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS,
							2.f, 1.f);
					return ActionResult.success(world.isClient());
				} else if (offhand.isOf(Items.STICK) && (color != null || isBoneMeal || isCoal) && !state.get(WATERLOGGED)) {
					int x;
					int y = (int) (AuroraUtil.posMod(hit.getPos().getY(), 1) * 16.0);
					y = 15 - y;

					if (facing.getAxis() == Direction.Axis.Z) {
						x = (int) (AuroraUtil.posMod(hit.getPos().getX(), 1) * 16.0);
					} else {
						x = 15 - (int) (AuroraUtil.posMod(hit.getPos().getZ(), 1) * 16.0);
					}
					if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) {
						x = 15 - x;
					}

					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

					this.line(blackboard, player, x, y, color, isBoneMeal, isCoal);

					world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
					return ActionResult.success(world.isClient());
				} else if ((color != null || isBoneMeal || isCoal) && !state.get(WATERLOGGED)) {
					int x;
					int y = (int) (AuroraUtil.posMod(hit.getPos().getY(), 1) * 16.0);
					y = 15 - y;

					if (facing.getAxis() == Direction.Axis.Z) {
						x = (int) (AuroraUtil.posMod(hit.getPos().getX(), 1) * 16.0);
					} else {
						x = 15 - (int) (AuroraUtil.posMod(hit.getPos().getZ(), 1) * 16.0);
					}
					if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) {
						x = 15 - x;
					}

					Blackboard.DrawAction action = Blackboard.DrawAction.DEFAULT;
					for (var possibleAction : Blackboard.DrawAction.ACTIONS) {
						if (possibleAction.getOffhandTool() != null && offhand.isOf(possibleAction.getOffhandTool())) {
							action = possibleAction;
							break;
						}
					}

					if (action.execute(blackboard, x, y, color, isBoneMeal, isCoal)) {
						player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
						world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
						return ActionResult.success(world.isClient());
					}
				} else if (stack.isOf(Items.GLOW_INK_SAC) || stack.isOf(Items.INK_SAC)) {
					boolean lit = stack.isOf(Items.GLOW_INK_SAC);
					if (lit != state.get(LIT)) {
						if (lit) {
							world.playSound(null, pos, SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.BLOCKS,
									1.f, 1.f);
							world.setBlockState(pos, state.with(LIT, true));
						} else {
							world.playSound(null, pos, SoundEvents.ITEM_INK_SAC_USE, SoundCategory.BLOCKS,
									1.f, 1.f);
							world.setBlockState(pos, state.with(LIT, false));
						}

						if (!player.isCreative()) {
							stack.decrement(1);
						}

						return ActionResult.success(world.isClient());
					}
				}
			}
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}

	private void line(BlackboardBlockEntity blackboard, PlayerEntity player, int x, int y,
	                  @Nullable Blackboard.Color color, boolean isBoneMeal, boolean isCoal) {
		if (blackboard.lastUser != player) {
			blackboard.lastUser = player;
			blackboard.lastX = x;
			blackboard.lastY = y;
		} else {
			byte colorData = blackboard.getPixel(x, y);
			int shade = colorData & 3;
			if (color != null) {
				blackboard.line(blackboard.lastX, blackboard.lastY, x, y, color, 0);
			} else if (isBoneMeal) {
				if (shade > 0)
					blackboard.line(blackboard.lastX, blackboard.lastY, x, y, Blackboard.getColor(colorData / 4), shade - 1);
			} else if (isCoal) {
				if (shade < 3)
					blackboard.line(blackboard.lastX, blackboard.lastY, x, y, Blackboard.getColor(colorData / 4), shade + 1);
			}
			blackboard.lastUser = null;
		}
	}

	private boolean tryClear(World world, BlackboardBlockEntity blackboard, @Nullable PlayerEntity player) {
		if (!blackboard.isEmpty()) {
			if (!world.isClient())
				blackboard.clear();

			world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, blackboard.getPos());
			return true;
		}
		return false;
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity playerEntity) {
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			if (!world.isClient() && playerEntity.isCreative()) {
				var stack = new ItemStack(this);
				var nbt = blackboard.writeBlackBoardNbt(new NbtCompound());
				nbt.remove("custom_name");
				stack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, nbt);

				if (blackboard.hasCustomName()) {
					stack.setCustomName(blackboard.getCustomName());
				}

				var itemEntity = new ItemEntity(world,
						pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
				itemEntity.setToDefaultPickupDelay();
				world.spawnEntity(itemEntity);
			}
		}

		super.onBreak(world, pos, state, playerEntity);
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		var stack = super.getPickStack(world, pos, state);
		var blackboard = this.getBlackboardEntity(world, pos);
		if (blackboard != null) {
			var nbt = blackboard.writeBlackBoardNbt(new NbtCompound());
			nbt.remove("custom_name");
			stack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, nbt);
		}

		return stack;
	}

	/* Piston */

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.DESTROY;
	}

	/* Block Entity Stuff */

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.instantiate(pos, state);
	}

	public @Nullable BlackboardBlockEntity getBlackboardEntity(BlockView world, BlockPos pos) {
		var entity = world.getBlockEntity(pos);
		if (entity instanceof BlackboardBlockEntity blackboard)
			return blackboard;
		return null;
	}

	/* Fluid */

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
			boolean shouldEmitEvent = false;

			if (!world.isClient()) {
				world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), Block.NOTIFY_ALL);
				world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));

				var blackboard = this.getBlackboardEntity(world, pos);
				if (blackboard != null && !this.isLocked()) {
					if (!blackboard.isEmpty()) {
						blackboard.clear();
						shouldEmitEvent = true;
					}
				}
			}

			if (shouldEmitEvent) {
				world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos);
			}

			return true;
		} else {
			return false;
		}
	}

	static {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			UseItemCallback.EVENT.register((player, world, hand) -> {
				if (hand == Hand.OFF_HAND && !player.isSpectator()) {
					var target = MinecraftClient.getInstance().crosshairTarget;
					if (target != null && target.getType() == HitResult.Type.BLOCK) {
						var targetBlock = world.getBlockState(((BlockHitResult) target).getBlockPos());
						if (targetBlock.isOf(AurorasDecoRegistry.BLACKBOARD_BLOCK) || targetBlock.isOf(AurorasDecoRegistry.CHALKBOARD_BLOCK))
							return TypedActionResult.fail(ItemStack.EMPTY);
					}
				}

				return TypedActionResult.pass(ItemStack.EMPTY);
			});
		}

		var builder = ImmutableMap.<Direction, VoxelShape>builder();

		builder.put(Direction.NORTH, createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
		builder.put(Direction.EAST, createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
		builder.put(Direction.SOUTH, createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
		builder.put(Direction.WEST, createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));

		SHAPES = new EnumMap<>(builder.build());
	}
}
