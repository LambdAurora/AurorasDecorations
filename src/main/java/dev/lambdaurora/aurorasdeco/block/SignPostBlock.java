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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import dev.lambdaurora.aurorasdeco.block.entity.SignPostBlockEntity;
import dev.lambdaurora.aurorasdeco.item.SignPostItem;
import dev.lambdaurora.aurorasdeco.mixin.block.BlockAccessor;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import dev.lambdaurora.aurorasdeco.util.CustomStateBuilder;
import dev.lambdaurora.aurorasdeco.world.gen.feature.WaySignFeature;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a sign post block.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.11
 * @since 1.0.0-beta.1
 */
@SuppressWarnings("deprecation")
public class SignPostBlock extends BlockWithEntity implements Waterloggable {
	public static final BooleanProperty GENERATE_DIRECTIONS = BooleanProperty.of("generate_directions");
	private static final List<SignPostBlock> SIGN_POSTS = new ArrayList<>();

	private final FenceBlock fenceBlock;

	public SignPostBlock(FenceBlock fenceBlock) {
		super(BlockPropertiesInjector.inject(settings(fenceBlock), new InjectedBlock(fenceBlock)));

		this.fenceBlock = fenceBlock;

		this.setDefaultState(
				AuroraUtil.remapBlockState(this.fenceBlock.getDefaultState(), this.stateManager.getDefaultState())
						.with(GENERATE_DIRECTIONS, false)
		);

		SIGN_POSTS.add(this);
		BlockPropertiesInjector.clear();
	}

	public static SignPostBlock byFence(FenceBlock fenceBlock) {
		for (var block : SIGN_POSTS) {
			if (block.getFenceBlock().equals(fenceBlock)) {
				return block;
			}
		}
		return SIGN_POSTS.get(0);
	}

	public static Stream<SignPostBlock> stream() {
		return SIGN_POSTS.stream();
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(GENERATE_DIRECTIONS);

		var customBuilder = new CustomStateBuilder<>(builder);
		customBuilder.exclude("north", "east", "south", "west", GENERATE_DIRECTIONS.getName());
		((BlockAccessor) Objects.requireNonNull(BlockPropertiesInjector.getInjectedData(InjectedBlock.class)).fenceBlock())
				.aurorasdeco$appendProperties(customBuilder);
	}

	public FenceBlock getFenceBlock() {
		return this.fenceBlock;
	}

	public BlockState getFenceState(BlockState signPostState) {
		return AuroraUtil.remapBlockState(signPostState, this.getFenceBlock().getDefaultState());
	}

	/* Shapes */

	@Override
	public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
		return this.getFenceState(state).getCullingShape(world, pos);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.getFenceState(state).getOutlineShape(world, pos, context);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.getFenceState(state).getCollisionShape(world, pos, context);
	}

	@Override
	public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.getFenceState(state).getCameraCollisionShape(world, pos, context);
	}

	/* Ticking */

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return this.fenceBlock.hasRandomTicks(state);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		this.fenceBlock.randomTick(state, world, pos, random);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, RandomGenerator random) {
		this.fenceBlock.randomDisplayTick(state, world, pos, random);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		if (state.get(GENERATE_DIRECTIONS) && !world.isClient())
			return checkType(type, AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE, WaySignFeature::generateDirections);

		return null;
	}

	/* Placement */

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var fenceState = this.getFenceBlock().getPlacementState(ctx);
		if (fenceState != null) return AuroraUtil.remapBlockState(fenceState, this.getDefaultState());
		else return null;
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
			WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (state.get(Properties.WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	/* Interaction */

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return this.getFenceBlock().getPickStack(world, pos, this.getFenceState(state));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (hit.getSide().getAxis() == Direction.Axis.Y) return ActionResult.PASS;

		var signPost = AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE.get(world, pos);
		if (signPost == null) return ActionResult.PASS;

		var stack = player.getStackInHand(hand);

		if (stack.getItem() instanceof SignPostItem)
			return ActionResult.PASS; // Let the item handle it.

		boolean handEmpty = stack.isEmpty();
		boolean dye = stack.getItem() instanceof DyeItem;
		boolean glowInkSac = stack.isOf(Items.GLOW_INK_SAC);
		boolean inkSac = stack.isOf(Items.INK_SAC);
		boolean compass = stack.isOf(Items.COMPASS);
		boolean canFlipSign = !dye && !glowInkSac && !inkSac && !compass && player.getMainHandStack().isEmpty()
				&& player.shouldCancelInteraction();
		boolean shouldSucceed = handEmpty || dye || glowInkSac || inkSac || compass || canFlipSign;
		boolean success = shouldSucceed && player.getAbilities().allowModifyWorld;
		if (world.isClient()) {
			return success ? ActionResult.SUCCESS : ActionResult.FAIL;
		}

		boolean up = isUp(hit.getPos().getY());
		var sign = signPost.getSign(up);

		if (sign == null || !player.getAbilities().allowModifyWorld)
			return shouldSucceed ? ActionResult.SUCCESS : ActionResult.FAIL;

		if (canFlipSign) {
			sign.setLeft(!sign.isLeft());
		} else if (handEmpty && !signPost.hasEditor() && player instanceof ServerPlayerEntity serverPlayerEntity &&
				(signPost.getUp() != null || signPost.getDown() != null)) {
			signPost.startEdit(serverPlayerEntity);
		} else if (dye || glowInkSac || inkSac) {
			boolean shouldConsume;
			if (dye) {
				shouldConsume = sign.setColor(((DyeItem) stack.getItem()).getColor());
				world.playSound(null, pos, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.f, 1.f);
			} else if (glowInkSac) {
				world.playSound(null, pos, SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.BLOCKS, 1.f, 1.f);
				shouldConsume = sign.setGlowing(true);
				if (shouldConsume && player instanceof ServerPlayerEntity serverPlayerEntity) {
					Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, pos, stack);
				}
			} else {
				world.playSound(null, pos, SoundEvents.ITEM_INK_SAC_USE, SoundCategory.BLOCKS, 1.f, 1.f);
				shouldConsume = sign.setGlowing(false);
			}

			if (shouldConsume) {
				if (!player.isCreative()) {
					stack.decrement(1);
				}

				player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
			}
		} else if (compass) {
			var pointingPos = CompassItem.hasLodestone(stack)
					? this.getLodestonePos(world, stack.getOrCreateNbt())
					: this.getWorldSpawnPos(world);

			if (pointingPos != null) {
				if (sign.pointToward(pointingPos))
					player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
			}
		}

		return ActionResult.success(world.isClient());
	}

	/**
	 * {@return {@code true} if the hit result is on the upper side, or {@code false} otherwise}
	 *
	 * @param y the Y-coordinate of the hit result
	 */
	public static boolean isUp(double y) {
		double absY = Math.abs(y);
		boolean up = absY % ((int) absY) > 0.5;

		if (y < 0) return !up;
		else return up;
	}

	private @Nullable BlockPos getLodestonePos(World world, NbtCompound nbt) {
		GlobalPos lodestonePos = CompassItem.getLodestonePosition(nbt);
		if (lodestonePos != null && world.getRegistryKey() == lodestonePos.getDimension()) {
			return lodestonePos.getPos();
		}
		return null;
	}

	private @Nullable BlockPos getWorldSpawnPos(World world) {
		var properties = world.getLevelProperties();
		return world.getDimension().natural()
				? new BlockPos(properties.getSpawnX(), properties.getSpawnY(), properties.getSpawnZ())
				: null;
	}

	/* Block Entity Stuff */

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE.instantiate(pos, state);
	}


	/* Loot table */

	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		var stacks = new ArrayList<>(this.getFenceState(state).getDroppedStacks(builder));

		var blockEntity = builder.get(LootContextParameters.BLOCK_ENTITY);
		if (blockEntity instanceof SignPostBlockEntity signPost) {
			var upSign = signPost.getUp();
			var downSign = signPost.getDown();

			if (upSign != null) {
				var stack = new ItemStack(upSign.getSign());
				var text = upSign.getText();
				if (!text.getString().isEmpty())
					stack.setCustomName(text);
				stacks.add(stack);
			}

			if (downSign != null) {
				var stack = new ItemStack(downSign.getSign());
				var text = downSign.getText();
				if (!text.getString().isEmpty())
					stack.setCustomName(text);
				stacks.add(stack);
			}
		}

		return stacks;
	}

	/* Piston */

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.BLOCK;
	}

	/* Entity Stuff */

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	/* Fluid */

	@Override
	public FluidState getFluidState(BlockState state) {
		return AuroraUtil.isWaterLogged(state) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	private static QuiltBlockSettings settings(FenceBlock fenceBlock) {
		return QuiltBlockSettings.copyOf(fenceBlock);
	}

	static {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			var offStack = player.getStackInHand(Hand.OFF_HAND);
			// Trigger the sign post block interaction to flip a sign.
			// But why is this necessary?
			// Because when you have an item in your offhand, it will try to interact with that one instead
			// and refuse to trigger the block. Which can be very annoying for players usually holding something
			// in their offhand.
			if (!(offStack.getItem() instanceof ShieldItem) && player.shouldCancelInteraction() && player.getMainHandStack().isEmpty()) {
				var state = world.getBlockState(hitResult.getBlockPos());
				if (state.getBlock() instanceof SignPostBlock) {
					return state.onUse(world, player, hand, hitResult);
				}
			}
			return ActionResult.PASS;
		});
	}

	/**
	 * A block state derivative made for the sign post block.
	 * <p>
	 * It allows to emit the block quads of a fence post block without a crash.
	 */
	@ClientOnly
	public static class State extends BlockState {
		public State(Block block, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<BlockState> mapCodec) {
			super(block, immutableMap, mapCodec);
		}

		@Override
		public <T extends Comparable<T>> T get(Property<T> property) {
			if (!this.getProperties().contains(property) && this.getBlock() instanceof SignPostBlock signPost) {
				return signPost.getFenceState(this).get(property);
			} else return super.get(property);
		}
	}

	private record InjectedBlock(FenceBlock fenceBlock) implements BlockPropertiesInjector.InjectData {
		@Override
		public StateManager.Factory<Block, BlockState> getStateFactory(StateManager.Factory<Block, BlockState> existing) {
			if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
				return State::new;
			}
			return existing;
		}
	}
}
