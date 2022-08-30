/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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
import dev.lambdaurora.aurorasdeco.block.entity.BookPileBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a book pile.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BookPileBlock extends BlockWithEntity implements Waterloggable {
	private static final Identifier BOOKS_LOOT_ID = AurorasDeco.id("books");

	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public static final VoxelShape SHAPE = createCuboidShape(3, 0, 3, 11, 8, 11);

	public BookPileBlock(Settings settings) {
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
		return Block.sideCoversSmallSquare(world, pos.offset(Direction.DOWN), Direction.UP);
	}

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var fluid = ctx.getWorld().getFluidState(ctx.getBlockPos());
		return this.getDefaultState().with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
		var bookPile = AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE.get(world, pos);
		if (bookPile != null) {
			var book = stack.copy();
			book.setCount(1);
			bookPile.getBooks().set(0, book);
		}
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
			WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		if (!state.canPlaceAt(world, pos))
			return Blocks.AIR.getDefaultState();

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	/* Interaction */

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		var stack = player.getStackInHand(hand);

		if (stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK)) {
			var bookPile = AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE.get(world, pos);
			if (bookPile != null && !bookPile.isFull()) {
				bookPile.insertBook(stack);
				if (!player.getAbilities().creativeMode)
					stack.decrement(1);

				world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);

				return ActionResult.success(world.isClient());
			}
		} else if (stack.isEmpty()) {
			var bookPile = AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE.get(world, pos);
			if (bookPile != null) {
				int i;
				for (i = 0; i < bookPile.getBooks().size(); i++) {
					if (bookPile.getBooks().get(i).isEmpty()) {
						i--;
						break;
					}
				}

				if (i > 0) {
					if (i == 5)
						i--;
					player.setStackInHand(hand, bookPile.removeBook(i));

					world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);

					return ActionResult.success(world.isClient());
				}
			}
		}

		return super.onUse(state, world, pos, player, hand, hit);
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		var bookPile = AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE.get(world, pos);
		if (bookPile != null) {
			return bookPile.getBooks().get(0).copy();
		}
		return super.getPickStack(world, pos, state);
	}

	/* Loot table */

	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		var blockEntity = builder.get(LootContextParameters.BLOCK_ENTITY);
		if (blockEntity instanceof BookPileBlockEntity bookPile) {
			builder.putDrop(BOOKS_LOOT_ID, (context, consumer) -> {
				for (var stack : bookPile.getBooks()) {
					if (!stack.isEmpty()) consumer.accept(stack.copy());
				}
			});
		}
		return super.getDroppedStacks(state, builder);
	}

	/* Block entity stuff */

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE.instantiate(pos, state);
	}

	/* Fluid */

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}
}
