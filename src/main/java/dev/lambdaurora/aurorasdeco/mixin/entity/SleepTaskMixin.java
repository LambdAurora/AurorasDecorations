/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.SleepTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(SleepTask.class)
public class SleepTaskMixin {
	@Inject(
			method = "shouldKeepRunning",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;hasActivity(Lnet/minecraft/entity/ai/brain/Activity;)Z"),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onShouldKeepRunning(ServerWorld world, LivingEntity entity, long time, CallbackInfoReturnable<Boolean> cir,
			Optional<GlobalPos> homePos, BlockPos pos) {
		if (!(entity.getY() > pos.getY() + 0.4) && entity.getBrain().hasActivity(Activity.REST)) {
			cir.setReturnValue(entity.getY() > pos.getY() + 0.2 && pos.isWithinDistance(entity.getBlockPos(), 1.14));
		}
	}
}
