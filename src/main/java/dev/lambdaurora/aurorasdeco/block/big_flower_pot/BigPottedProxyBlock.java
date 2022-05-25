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

import dev.lambdaurora.aurorasdeco.mixin.block.AbstractBlockAccessor;
import dev.lambdaurora.aurorasdeco.mixin.block.BlockAccessor;
import dev.lambdaurora.aurorasdeco.mixin.block.BlockSettingsAccessor;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Represents a proxied plant that is potted inside a big flower pot.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BigPottedProxyBlock extends BigFlowerPotBlock {
	private final Map<BlockState, VoxelShape> outlineShapeCache = new Object2ObjectOpenHashMap<>();

	public BigPottedProxyBlock(PottedPlantType type) {
		super(type);

		((AbstractBlockAccessor) this).getSettings()
				.luminance(((BlockSettingsAccessor) ((AbstractBlockAccessor) type.getPlant()).getSettings()).getLuminance());

		var builder = new StateManager.Builder<Block, BlockState>(this);
		this.appendProperties(builder);
		((BlockAccessor) this.getPlant()).aurorasdeco$appendProperties(builder);
		((BlockAccessor) this).setStateManager(builder.build(Block::getDefaultState, BlockState::new));

		this.setDefaultState(AuroraUtil.remapBlockState(type.getPlant().getDefaultState(), this.stateManager.getDefaultState()));
	}

	@Override
	public BlockState getPlantState(BlockState potState) {
		return AuroraUtil.remapBlockState(potState, super.getPlantState(potState));
	}

	/* Shapes */

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		var plantShape = this.getPlantState(state).getCollisionShape(world, pos, context);

		if (plantShape == VoxelShapes.empty()) {
			return BIG_FLOWER_POT_SHAPE;
		} else {
			float ratio = .65f;
			float offset = (1.f - ratio) / 2.f;
			return VoxelShapes.union(BIG_FLOWER_POT_SHAPE, AuroraUtil.resizeVoxelShape(plantShape, ratio).offset(offset, .8f, offset));
		}
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return this.outlineShapeCache.computeIfAbsent(state, s -> this.shape(s, world, pos));
	}

	private VoxelShape shape(BlockState state, BlockView world, BlockPos pos) {
		var plantShape = this.getPlant().getOutlineShape(state, world, pos, ShapeContext.absent());
		float ratio = .65f;
		float offset = (1.f - ratio) / 2.f;
		return VoxelShapes.union(BIG_FLOWER_POT_SHAPE, AuroraUtil.resizeVoxelShape(plantShape, ratio).offset(offset, .8f, offset));
	}

	/* Ticking */

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return this.getPlant().hasRandomTicks(state);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		this.getPlant().randomTick(state, world, pos, random);
	}

	/* Placement */

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		var state = super.getPlacementState(ctx);
		if (state == null) return null;
		var plantState = this.type.getPlant().getPlacementState(ctx);
		if (plantState != null)
			return AuroraUtil.remapBlockState(plantState, state);
		return state;
	}

	/* Loot table */

	@Override
	protected void acceptPlantDrops(BlockState state, LootContext.Builder builder, Consumer<ItemStack> consumer) {
		var stacks = this.getPlantState(state).getDroppedStacks(builder);
		for (var stack : stacks) {
			consumer.accept(stack);
		}
	}
}
