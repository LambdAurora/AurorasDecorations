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

import dev.lambdaurora.aurorasdeco.block.BrazierBlock;
import dev.lambdaurora.aurorasdeco.block.BurntVineBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {
	@Inject(
			method = "useOnBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/CampfireBlock;canBeLit(Lnet/minecraft/block/BlockState;)Z"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir,
	                          PlayerEntity player, World world, BlockPos pos, BlockState state) {
		if (state.isOf(Blocks.VINE) && (player == null || player.getAbilities().allowModifyWorld)) { // Burn vine tip interaction.
			world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS,
					1.f, world.getRandom().nextFloat() * .4f + .8f);
			world.setBlockState(pos, BurntVineBlock.fromVine(state), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
			world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
			if (player != null) {
				context.getStack().damage(1, player, p -> p.sendToolBreakStatus(context.getHand()));
			}

			if (world.isClient()) {
				double x = pos.getX() + 0.5;
				double y = pos.getY() + 0.5;
				double z = pos.getZ() + 0.5;
				for (int i = 0; i < 4; i++) {
					float random = world.getRandom().nextFloat() - .5f;
					world.addParticle(ParticleTypes.FLAME, x + random, y + random, z + random,
							0.0, 0.0, 0.0);
				}
			}

			cir.setReturnValue(ActionResult.success(world.isClient()));
		} else if (BrazierBlock.canBeLit(state)) {
			world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS,
					1.f, world.getRandom().nextFloat() * .4f + .8f);
			world.setBlockState(pos, state.with(Properties.LIT, true), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
			world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
			if (player != null) {
				context.getStack().damage(1, player, p -> p.sendToolBreakStatus(context.getHand()));
			}

			cir.setReturnValue(ActionResult.success(world.isClient()));
		}
	}
}
