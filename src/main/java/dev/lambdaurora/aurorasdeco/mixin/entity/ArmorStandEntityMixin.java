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

package dev.lambdaurora.aurorasdeco.mixin.entity;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {
    @Shadow
    public abstract boolean shouldShowArms();

    @Shadow
    protected abstract void setShowArms(boolean showArms);

    @Shadow
    public abstract boolean shouldHideBasePlate();

    @Shadow
    protected abstract void setHideBasePlate(boolean hideBasePlate);

    protected ArmorStandEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "interactAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/decoration/ArmorStandEntity;isSlotDisabled(Lnet/minecraft/entity/EquipmentSlot;)Z",
                    ordinal = 1
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onInteractAt(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir,
                              ItemStack stack) {
        World world = this.getEntityWorld();
        if (stack.isOf(Items.STICK) && !this.shouldShowArms()) {
            this.setShowArms(true);
            this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.f, 1.f);
            if (!player.getAbilities().creativeMode)
                stack.decrement(1);
            world.emitGameEvent(player, GameEvent.MOB_INTERACT, this.getBlockPos());
            cir.setReturnValue(ActionResult.SUCCESS);
        } else if (stack.isOf(Items.SHEARS) && player.isSneaking() && !this.shouldHideBasePlate()) {
            this.setHideBasePlate(true);
            this.playSound(AurorasDecoRegistry.ARMOR_STAND_HIDE_BASE_PLATE_SOUND_EVENT, 1.f, 1.f);
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            world.emitGameEvent(player, GameEvent.SHEAR, this.getBlockPos());
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "breakAndDropItem", at = @At("HEAD"))
    private void onBreakAndDropItem(DamageSource damageSource, CallbackInfo ci) {
        if (this.shouldShowArms()) {
            Block.dropStack(this.getEntityWorld(), this.getBlockPos(), new ItemStack(Items.STICK));
        }
    }
}
