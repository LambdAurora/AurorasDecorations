package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
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
            ItemStack stack = player.getStackInHand(hand);
            ItemStack heldStack = this.getHeldItemStack();
            if (!heldStack.isEmpty() && stack.isOf(Items.SHEARS) && !this.isInvisible()) {
                if (!this.getEntityWorld().isClient()) {
                    this.setInvisible(true);
                    this.playSound(AurorasDecoRegistry.ITEM_FRAME_HIDE_BACKGROUND_EVENT, 1.f, 1.f);
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
