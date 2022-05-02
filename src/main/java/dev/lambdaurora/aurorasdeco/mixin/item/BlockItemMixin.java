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

package dev.lambdaurora.aurorasdeco.mixin.item;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.accessor.BlockItemAccessor;
import dev.lambdaurora.aurorasdeco.block.ChandelierBlock;
import dev.lambdaurora.aurorasdeco.block.WallCandleBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.LanternRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsage;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Adds wall lantern to the lantern block item and more.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Mixin(value = BlockItem.class, priority = 960)
public abstract class BlockItemMixin extends Item implements BlockItemAccessor {
	private BlockItemMixin(Settings settings) {
		super(settings);
	}

	@Shadow
	public abstract Block getBlock();

	@Unique
	private Block aurorasdeco$wallBlock = null;
	@Unique
	private Block aurorasdeco$ceilingBlock = null;

	@Override
	public void aurorasdeco$setWallBlock(Block block) {
		this.aurorasdeco$wallBlock = block;
	}

	@Override
	public void aurorasdeco$setCeilingBlock(Block block) {
		this.aurorasdeco$ceilingBlock = block;
	}

	@Inject(method = "appendBlocks", at = @At("RETURN"))
	private void onAppendBlocks(Map<Block, Item> map, Item item, CallbackInfo ci) {
		if (this.getBlock() instanceof LanternBlock) {
			var lanternBlock = LanternRegistry.fromItem(item);
			if (lanternBlock != null)
				this.aurorasdeco$setWallBlock(lanternBlock);
			map.put(lanternBlock, item);
		} else if (this.getBlock() instanceof CandleBlock candleBlock) {
			var candleId = Registry.BLOCK.getId(this.getBlock());
			if (candleId.getNamespace().equals("minecraft")) {
				this.aurorasdeco$wallBlock = Registry.register(
						Registry.BLOCK,
						AurorasDeco.id("wall_" + candleId.getPath()),
						new WallCandleBlock(candleBlock)
				);
				this.aurorasdeco$ceilingBlock = Registry.register(
						Registry.BLOCK,
						AurorasDeco.id("chandelier/" + candleId.getPath().replace("_candle", "")),
						new ChandelierBlock(candleBlock)
				);
				map.put(this.aurorasdeco$wallBlock, item);
				map.put(this.aurorasdeco$ceilingBlock, item);
			}
		}
	}

	@Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
	private void onGetPlacementState(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
		var placementDirections = context.getPlacementDirections();
		var world = context.getWorld();
		var pos = context.getBlockPos();
		var placedState = world.getBlockState(pos);

		if (this.aurorasdeco$ceilingBlock != null
				&& (placementDirections[0] == Direction.UP || placedState.isOf(this.aurorasdeco$ceilingBlock))) {
			var state = this.aurorasdeco$ceilingBlock.getPlacementState(context);

			if (state != null && state.canPlaceAt(world, pos)
					&& world.canPlace(state, pos, ShapeContext.absent())) {
				cir.setReturnValue(state);
				return;
			}
		}

		if (this.aurorasdeco$wallBlock != null) {
			var wallState = this.aurorasdeco$wallBlock.getPlacementState(context);
			BlockState resultState = null;

			for (var direction : context.getPlacementDirections()) {
				var state = direction.getAxis().isVertical() ? this.getBlock().getPlacementState(context) : wallState;
				if (state != null && state.canPlaceAt(world, pos)) {
					resultState = state;
					break;
				}
			}

			if (resultState != null && world.canPlace(resultState, pos, ShapeContext.absent()))
				cir.setReturnValue(resultState);
		}
	}

	@Inject(method = "onItemEntityDestroyed", at = @At("HEAD"), cancellable = true)
	private void onItemEntityExploded(ItemEntity entity, CallbackInfo ci) {
		var server = entity.getServer();
		if (server == null) return;
		var inv = new SimpleInventory(entity.getStack());
		server.getRecipeManager()
				.getFirstMatch(AurorasDecoRegistry.EXPLODING_RECIPE_TYPE, inv, entity.getWorld())
				.ifPresent(explodingRecipe -> {
					int count = entity.getStack().getCount();
					for (int i = 0; i < count; i++)
						ItemUsage.spawnItemContents(entity, Stream.of(explodingRecipe.craft(inv)));
					ci.cancel();
				});
	}
}
