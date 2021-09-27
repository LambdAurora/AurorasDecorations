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

package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.block.DaffodilBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.stateprovider.ForestFlowerBlockStateProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ForestFlowerBlockStateProvider.class)
public class ForestFlowerBlockStateProviderMixin {
	@Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
	private void onGetBlockState(Random random, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		var state = cir.getReturnValue();
		if (state.isOf(AurorasDecoRegistry.DAFFODIL)) {
			var facing = Direction.random(random);
			cir.setReturnValue(state.with(DaffodilBlock.FACING, switch (facing) {
				case UP -> Direction.NORTH;
				case DOWN -> Direction.SOUTH;
				default -> facing;
			}));
		}
	}
}
