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

package dev.lambdaurora.aurorasdeco.mixin.item;

import dev.lambdaurora.aurorasdeco.block.BrazierBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShovelItem.class)
public class ShovelItemMixin {
    @Inject(
            method = "useOnBlock",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir,
                              World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (BrazierBlock.canBeUnlit(state)) {
            BrazierBlock.extinguish(context.getPlayer(), world, pos, state);
            state = state.with(BrazierBlock.LIT, false);
            if (!world.isClient()) {
                world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                if (player != null) {
                    context.getStack().damage(1, player, p -> p.sendToolBreakStatus(context.getHand()));
                }
            }

            cir.setReturnValue(ActionResult.success(world.isClient()));
        }
    }
}
