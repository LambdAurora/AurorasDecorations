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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.screen.SawmillScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

/**
 * Represents a sawmill block. Used to craft wood-related blocks.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public final class SawmillBlock extends Block {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	private static final VoxelShape SHAPE;

	public SawmillBlock() {
		super(QuiltBlockSettings.of(Material.WOOD).nonOpaque().strength(2.5f).sounds(BlockSoundGroup.WOOD));

		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return VoxelShapes.fullCube();
	}

	/* Placement */

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	/* Interaction */

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
	                          BlockHitResult hit) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		} else {
			player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			player.incrementStat(AurorasDecoRegistry.INTERACT_WITH_SAWMILL);
			return ActionResult.CONSUME;
		}
	}

	@Override
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) ->
				new SawmillScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos)),
				Text.translatable(this.getTranslationKey()));
	}

	static {
		SHAPE = VoxelShapes.union(
				createCuboidShape(0, 14, 0, 16, 16, 16),
				createCuboidShape(3, 12, 3, 13, 14, 13),
				createCuboidShape(0, 0, 0, 3, 14, 3),
				createCuboidShape(0, 0, 13, 3, 14, 16),
				createCuboidShape(13, 0, 13, 16, 14, 16),
				createCuboidShape(13, 0, 0, 16, 14, 3)
		);
	}
}
