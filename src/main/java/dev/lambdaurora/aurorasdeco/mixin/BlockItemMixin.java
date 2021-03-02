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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.accessor.BlockItemAccessor;
import dev.lambdaurora.aurorasdeco.block.ChandelierBlock;
import dev.lambdaurora.aurorasdeco.block.WallCandleBlock;
import dev.lambdaurora.aurorasdeco.block.entity.LanternBlockEntity;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
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
public abstract class BlockItemMixin implements BlockItemAccessor {
    @Shadow
    public abstract Block getBlock();

    private Block aurorasdeco$wallBlock = null;
    private Block aurorasdeco$ceilingBlock = null;

    @Override
    public void aurorasdeco$setWallBlock(Block block) {
        this.aurorasdeco$wallBlock = block;
    }

    @Inject(method = "appendBlocks", at = @At("RETURN"))
    private void onAppendBlocks(Map<Block, Item> map, Item item, CallbackInfo ci) {
        if (this.getBlock() instanceof LanternBlock) {
            LanternBlockEntity.registerLantern(item, this.getBlock());
        } else if (this.getBlock() instanceof CandleBlock) {
            Identifier candleId = Registry.BLOCK.getId(this.getBlock());
            if (candleId.getNamespace().equals("minecraft")) {
                AurorasDeco.DELAYED_REGISTER_BLOCK.put(AurorasDeco.id("wall_" + candleId.getPath()),
                        this.aurorasdeco$wallBlock = new WallCandleBlock((CandleBlock) this.getBlock()));
                AurorasDeco.DELAYED_REGISTER_BLOCK.put(AurorasDeco.id("chandelier/" + candleId.getPath()
                                .replace("_candle", "")),
                        this.aurorasdeco$ceilingBlock = new ChandelierBlock((CandleBlock) this.getBlock()));
                map.put(this.aurorasdeco$wallBlock, item);
                map.put(this.aurorasdeco$ceilingBlock, item);
            }
        }
    }

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void onGetPlacementState(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
        Direction[] placementDirections = context.getPlacementDirections();
        WorldView world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState placedState = world.getBlockState(pos);

        if (this.aurorasdeco$ceilingBlock != null
                && (placementDirections[0] == Direction.UP || placedState.isOf(this.aurorasdeco$ceilingBlock))) {
            BlockState state = this.aurorasdeco$ceilingBlock.getPlacementState(context);

            if (state != null && state.canPlaceAt(world, pos)
                    && world.canPlace(state, pos, ShapeContext.absent())) {
                cir.setReturnValue(state);
                return;
            }
        }

        if (this.aurorasdeco$wallBlock != null) {
            BlockState wallState = this.aurorasdeco$wallBlock.getPlacementState(context);
            BlockState resultState = null;

            for (Direction direction : context.getPlacementDirections()) {
                BlockState state = direction.getAxis().isVertical() ? this.getBlock().getPlacementState(context) : wallState;
                if (state != null && state.canPlaceAt(world, pos)) {
                    resultState = state;
                    break;
                }
            }

            if (resultState != null && world.canPlace(resultState, pos, ShapeContext.absent()))
                cir.setReturnValue(resultState);
        }
    }
}
