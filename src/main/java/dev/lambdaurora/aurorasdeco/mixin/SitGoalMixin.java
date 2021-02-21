package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SitGoal.class)
public class SitGoalMixin {
    @Shadow
    @Final
    private TameableEntity tameable;

    @Inject(method = "start", at = @At("RETURN"))
    private void onStart(CallbackInfo ci) {
        if (this.tameable instanceof CatEntity) {
            BlockState state = this.tameable.getEntityWorld().getBlockState(this.tameable.getBlockPos());
            if (state.isIn(AurorasDecoRegistry.PET_BEDS)) {
                this.tameable.setInSittingPose(false);
                ((CatEntity) this.tameable).setSleepingWithOwner(true);
            }
        }
    }

    @Inject(method = "stop", at = @At("RETURN"))
    private void onStop(CallbackInfo ci) {
        if (this.tameable instanceof CatEntity) {
            ((CatEntity) this.tameable).setSleepingWithOwner(false);
        }
    }
}
