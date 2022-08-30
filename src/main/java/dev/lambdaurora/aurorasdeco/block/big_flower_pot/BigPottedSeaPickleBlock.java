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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import dev.lambdaurora.aurorasdeco.mixin.block.AbstractBlockAccessor;
import dev.lambdaurora.aurorasdeco.mixin.block.BlockAccessor;
import dev.lambdaurora.aurorasdeco.mixin.block.BlockSettingsAccessor;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.Map;

import static net.minecraft.block.SeaPickleBlock.PICKLES;

/**
 * Represents a potted version of the {@link SeaPickleBlock}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BigPottedSeaPickleBlock extends BigFlowerPotBlock implements Waterloggable {
	private final Map<BlockState, VoxelShape> shapeCache = new Object2ObjectOpenHashMap<>();

	public BigPottedSeaPickleBlock(PottedPlantType type) {
		super(type, QuiltBlockSettings.of(Material.DECORATION).strength(.1f).nonOpaque());

		var plantSettings = ((AbstractBlockAccessor) type.getPlant()).getSettings();
		((AbstractBlockAccessor) this).getSettings()
				.luminance(((BlockSettingsAccessor) plantSettings).getLuminance());

		var builder = new StateManager.Builder<Block, BlockState>(this);
		this.appendProperties(builder);
		((BlockAccessor) this.getPlant()).aurorasdeco$appendProperties(builder);
		((BlockAccessor) this).setStateManager(builder.build(Block::getDefaultState, BlockState::new));

		this.setDefaultState(AuroraUtil.remapBlockState(type.getPlant().getDefaultState(), this.stateManager.getDefaultState())
				.with(Properties.WATERLOGGED, false));
	}

	@Override
	public BlockState getPlantState(BlockState potState) {
		return AuroraUtil.remapBlockState(potState, super.getPlantState(potState));
	}

	@Override
	public @Nullable ItemStack getEquivalentPlantStack(BlockState state) {
		return new ItemStack(this.getPlantType().getItem(), state.get(PICKLES));
	}

	/* Shapes */

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.shapeCache.computeIfAbsent(state, s -> shape(s, world, pos));
	}

	/* Interaction */

	@Override
	public ActionResult onCustomUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		var handStack = player.getStackInHand(hand);
		if (handStack.isOf(this.getPlantType().getItem()) && state.get(PICKLES) < 4) {
			player.incrementStat(Stats.POT_FLOWER);
			if (!player.getAbilities().creativeMode) {
				handStack.decrement(1);
			}

			world.playSound(player, pos, this.getPlant().getSoundGroup(this.getPlantState(state)).getPlaceSound(), SoundCategory.BLOCKS,
					1.f, 1.f);

			if (!world.isClient()) {
				world.setBlockState(pos, state.with(PICKLES, state.get(PICKLES) + 1), Block.NOTIFY_ALL);
				world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
			}
			return ActionResult.success(world.isClient());
		} else if (state.get(PICKLES) > 1) {
			if (handStack.isEmpty()) {
				player.setStackInHand(hand, new ItemStack(this.getPlantType().getItem()));

				world.playSound(player, pos, this.getPlant().getSoundGroup(this.getPlantState(state)).getBreakSound(), SoundCategory.BLOCKS,
						1.f, 1.f);

				if (!world.isClient()) {
					world.setBlockState(pos, state.with(PICKLES, state.get(PICKLES) - 1), Block.NOTIFY_ALL);
					world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
				}
				return ActionResult.success(world.isClient());
			}
		}

		return ActionResult.PASS;
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

	/* Fluid */

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	private VoxelShape shape(BlockState state, BlockView world, BlockPos pos) {
		var plantShape = this.getPlant().getOutlineShape(state, world, pos, ShapeContext.absent());
		float ratio = .65f;
		float offset = (1.f - ratio) / 2.f;
		return VoxelShapes.union(BIG_FLOWER_POT_SHAPE, AuroraUtil.resizeVoxelShape(plantShape, ratio).offset(offset, .8f, offset));
	}
}
