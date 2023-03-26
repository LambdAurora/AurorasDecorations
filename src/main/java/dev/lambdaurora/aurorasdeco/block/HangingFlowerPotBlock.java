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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.accessor.BlockItemAccessor;
import dev.lambdaurora.aurorasdeco.mixin.block.BlockAccessor;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a hanging flower pot block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class HangingFlowerPotBlock extends Block {
	private static final List<HangingFlowerPotBlock> HANGING_FLOWER_POT_BLOCKS = new ArrayList<>();
	private static final Map<Block, Block> CONTENT_TO_POTTED = new Object2ObjectOpenHashMap<>();
	protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
	private static final ThreadLocal<FlowerPotBlock> CURRENT_PROXY = new ThreadLocal<>();
	private static HangingFlowerPotBlock DEFAULT;

	public static final Identifier HANGING_FLOWER_POT_ATTACHMENT_MODEL = AurorasDeco.id("block/hanging_flower_pot_attachment");
	public static final Identifier BETTER_GRASS_DATA = new Identifier("bettergrass/data/flower_pot");

	private final FlowerPotBlock flowerPot;

	public HangingFlowerPotBlock(FlowerPotBlock flowerPot) {
		super(settings(flowerPot));
		this.flowerPot = flowerPot;
		CONTENT_TO_POTTED.put(flowerPot.getContent(), this);
		HANGING_FLOWER_POT_BLOCKS.add(this);

		this.setDefaultState(AuroraUtil.remapBlockState(flowerPot.getDefaultState(), this.stateManager.getDefaultState()));

		if (flowerPot == Blocks.FLOWER_POT)
			DEFAULT = this;
	}

	public static Stream<HangingFlowerPotBlock> stream() {
		return HANGING_FLOWER_POT_BLOCKS.stream();
	}

	public static @Nullable Block getFromFlowerPot(FlowerPotBlock flowerPot) {
		return getFromContent(flowerPot.getContent());
	}

	public static @Nullable Block getFromContent(Block content) {
		return CONTENT_TO_POTTED.get(content);
	}

	/**
	 * {@return the associated flower pot}
	 */
	public FlowerPotBlock getFlowerPot() {
		return this.flowerPot;
	}

	/**
	 * {@return the associated flower pot}
	 */
	public BlockState getFlowerPotState(BlockState state) {
		return AuroraUtil.remapBlockState(state, this.getFlowerPot().getDefaultState());
	}

	/**
	 * {@return the content of this flower pot}
	 */
	public Block getContent() {
		return this.flowerPot.getContent();
	}

	private boolean isEmpty() {
		return this.flowerPot.getContent() == Blocks.AIR;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);

		((BlockAccessor) CURRENT_PROXY.get()).aurorasdeco$appendProperties(builder);
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	/* Placement */

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return !VoxelShapes.matchesAnywhere(world.getBlockState(pos.up()).getSidesShape(world, pos).getFace(Direction.DOWN),
				VoxelShapes.fullCube(), BooleanBiFunction.ONLY_SECOND);
	}

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var state = super.getPlacementState(ctx);
		if (state == null) return null;
		var plantState = this.getContent().getPlacementState(ctx);
		if (plantState != null)
			return AuroraUtil.remapBlockState(plantState, state);
		return state;
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world,
			BlockPos pos, BlockPos neighborPos) {
		return direction == Direction.UP && !state.canPlaceAt(world, pos)
				? Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	/* Interaction */

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!player.getAbilities().allowModifyWorld) {
			return ActionResult.PASS;
		}

		var handStack = player.getStackInHand(hand);
		var blockState = (handStack.getItem() instanceof BlockItem blockItem ?
				CONTENT_TO_POTTED.getOrDefault(blockItem.getBlock(), Blocks.AIR)
				: Blocks.AIR
		).getPlacementState(new ItemPlacementContext(player, hand, handStack, hit));
		boolean empty = this.isEmpty();
		if ((blockState == null || blockState.isOf(Blocks.AIR)) != empty) {
			if (!world.isClient()) {
				if (empty) {
					world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
					player.incrementStat(Stats.POT_FLOWER);
					if (!player.getAbilities().creativeMode) {
						handStack.decrement(1);
					}
				} else {
					var contentStack = new ItemStack(this.getContent());
					if (handStack.isEmpty())
						player.setStackInHand(hand, contentStack);
					else if (!player.giveItemStack(contentStack))
						player.dropItem(contentStack, false);

					world.setBlockState(pos, DEFAULT.getDefaultState(), Block.NOTIFY_ALL);
				}

				world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			}

			return ActionResult.success(world.isClient());
		} else return ActionResult.CONSUME;
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return this.isEmpty() ? super.getPickStack(world, pos, state) : new ItemStack(this.getContent());
	}

	/* Ticking */

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return this.flowerPot.hasRandomTicks(state);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		this.flowerPot.randomTick(state, world, pos, random);
	}

	/* Entity Stuff */

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	public static HangingFlowerPotBlock initEmpty() {
		HangingFlowerPotBlock block = Registry.register(Registries.BLOCK, AurorasDeco.id("hanging_flower_pot"),
				new HangingFlowerPotBlock((FlowerPotBlock) Blocks.FLOWER_POT)
		);

		((BlockItemAccessor) Items.FLOWER_POT).aurorasdeco$setCeilingBlock(block);
		Item.BLOCK_ITEMS.put(block, Items.FLOWER_POT);

		return block;
	}

	private static QuiltBlockSettings settings(FlowerPotBlock block) {
		CURRENT_PROXY.set(block);
		return QuiltBlockSettings.copyOf(block).dropsLike(block);
	}
}
