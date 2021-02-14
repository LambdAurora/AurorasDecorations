/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.block.state.LanternProperty;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Adds wall lantern to the lantern block item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Shadow
    public abstract Block getBlock();

    @Inject(method = "appendBlocks", at = @At("RETURN"))
    private void onAppendBlocks(Map<Block, Item> map, Item item, CallbackInfo ci) {
        if (this.getBlock() instanceof LanternBlock) {
            LanternProperty.registerValue(this.getBlock());
        }
    }

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void onGetPlacementState(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
        if (this.getBlock() instanceof LanternBlock) {
            BlockState wallState = AurorasDecoRegistry.WALL_LANTERN_BLOCK.getPlacementState(context);
            BlockState resultState = null;
            WorldView world = context.getWorld();
            BlockPos blockPos = context.getBlockPos();

            for (Direction direction : context.getPlacementDirections()) {
                BlockState state = direction.getAxis().isVertical() ? this.getBlock().getPlacementState(context) : wallState;
                if (state != null && state.canPlaceAt(world, blockPos)) {
                    resultState = state;
                    break;
                }
            }

            if (resultState != null && world.canPlace(resultState, blockPos, ShapeContext.absent()))
                cir.setReturnValue(resultState);
        }
    }
}
