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

package dev.lambdaurora.aurorasdeco.mixin.entity;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin extends AbstractDecorationEntity {
	@Shadow
	private boolean fixed;

	@Shadow
	public abstract ItemStack getHeldItemStack();

	protected ItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(
			method = "dropHeldStack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;setHeldItemStack(Lnet/minecraft/item/ItemStack;)V"
			)
	)
	private void onDropHeldStack(Entity entity, boolean alwaysDrop, CallbackInfo ci) {
		this.setInvisible(false);
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (!this.fixed) {
			var stack = player.getStackInHand(hand);
			var heldStack = this.getHeldItemStack();
			if (!heldStack.isEmpty() && stack.isOf(Items.SHEARS) && !this.isInvisible()) {
				if (!this.getWorld().isClient()) {
					this.setInvisible(true);
					this.playSound(AurorasDecoSounds.ITEM_FRAME_HIDE_BACKGROUND_SOUND_EVENT, 1.f, 1.f);
					stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
					world.emitGameEvent(player, GameEvent.SHEAR, this.getBlockPos());
					cir.setReturnValue(ActionResult.CONSUME);
				} else {
					cir.setReturnValue(ActionResult.SUCCESS);
				}
			}
		}
	}
}
