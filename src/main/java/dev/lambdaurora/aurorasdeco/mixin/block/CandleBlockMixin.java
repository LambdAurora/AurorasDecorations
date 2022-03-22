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

package dev.lambdaurora.aurorasdeco.mixin.block;

import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CandleBlock.class)
public abstract class CandleBlockMixin extends AbstractCandleBlock {
	protected CandleBlockMixin(Settings settings) {
		super(settings);
	}

	@Inject(
			method = "getPlacementState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"
			),
			cancellable = true
	)
	private void onGetPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
		var placedState = ctx.getWorld().getBlockState(ctx.getBlockPos());

		if (!placedState.isOf(this) && placedState.getBlock() instanceof CandleBlock
				&& ctx.getStack().getItem() instanceof BlockItem
				&& ((BlockItem) ctx.getStack().getItem()).getBlock() instanceof CandleBlock)
			// If the placed state is not the same as self, but the held item is a candle, prevent further action.
			// Fix issues with different candles type replacing each other.
			cir.setReturnValue(null);
	}
}
