/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(AxeItem.class)
public class AxeItemMixin {
	@Inject(
			method = "useOnBlock",
			at = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;"),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir,
	                          World world, BlockPos pos, PlayerEntity player, BlockState state, Optional<BlockState> strippedState,
	                          Optional<BlockState> decreasedOxidationState, Optional<BlockState> unwaxState) {
		if (unwaxState.isPresent()) {
			if (unwaxState.get().isIn(AurorasDecoTags.BLACKBOARD_BLOCKS))
				cir.setReturnValue(ActionResult.PASS);
		}
	}
}
