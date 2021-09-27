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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
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
			var state = this.tameable.getEntityWorld().getBlockState(this.tameable.getBlockPos());
			if (state.isIn(AurorasDecoTags.PET_BEDS)) {
				this.tameable.setInSittingPose(false);
				((CatEntity) this.tameable).setInSleepingPose(true);
			}
		}
	}

	@Inject(method = "stop", at = @At("RETURN"))
	private void onStop(CallbackInfo ci) {
		if (this.tameable instanceof CatEntity) {
			((CatEntity) this.tameable).setInSleepingPose(false);
		}
	}
}
