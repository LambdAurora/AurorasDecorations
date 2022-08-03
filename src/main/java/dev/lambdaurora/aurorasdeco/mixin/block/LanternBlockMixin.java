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

package dev.lambdaurora.aurorasdeco.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Allows to place lanterns on leaves.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin {
	@Inject(method = "canPlaceAt", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir, Direction direction) {
		if (direction == Direction.DOWN) {
			var downState = world.getBlockState(pos.offset(direction));
			if (downState.isIn(BlockTags.LEAVES)) {
				cir.setReturnValue(true);
			}
		}
	}
}
