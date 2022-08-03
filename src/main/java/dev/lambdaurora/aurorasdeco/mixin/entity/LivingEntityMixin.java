/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

import dev.lambdaurora.aurorasdeco.block.SleepingBagBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(
			method = "sleep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onSleep(BlockPos pos, CallbackInfo ci, BlockState state) {
		if (state.getBlock() instanceof SleepingBagBlock) {
			this.world.setBlockState(pos, state.with(BedBlock.OCCUPIED, true), Block.NOTIFY_ALL);
		}
	}

	@Dynamic("Lambda in LivingEntity#wakeUp")
	@Inject(
			method = "m_ljzijdub(Lnet/minecraft/util/math/BlockPos;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onWakeUp(BlockPos pos, CallbackInfo ci, BlockState state) {
		if (state.getBlock() instanceof SleepingBagBlock) {
			this.world.setBlockState(pos, state.with(SleepingBagBlock.OCCUPIED, false), Block.NOTIFY_ALL);
			Vec3d wakUpPos = BedBlock.findWakeUpPosition(this.getType(), this.world, pos, this.getYaw()).orElseGet(() -> {
				BlockPos upPos = pos.up();
				return new Vec3d(upPos.getX() + 0.5, upPos.getY() + 0.1, upPos.getZ() + 0.5);
			});
			Vec3d vec3d2 = Vec3d.ofBottomCenter(pos).subtract(wakUpPos).normalize();
			float yaw = (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d2.z, vec3d2.x) * 57.2957763671875D - 90);
			this.setPosition(wakUpPos.x, wakUpPos.y, wakUpPos.z);
			this.setYaw(yaw);
			this.setPitch(0.f);
		}
	}
}
