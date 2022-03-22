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

package dev.lambdaurora.aurorasdeco.mixin.entity;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GiantEntity.class, HostileEntity.class, PillagerEntity.class})
public class AvoidAmethystLanternMixin {
	@Inject(method = "getPathfindingFavor", at = @At("RETURN"), cancellable = true)
	private void onGetPathfindingFavor(BlockPos pos, WorldView world, CallbackInfoReturnable<Float> cir) {
		var state = world.getBlockState(pos);
		if (state.isOf(AurorasDecoRegistry.AMETHYST_LANTERN_BLOCK) || state.isOf(AurorasDecoRegistry.AMETHYST_WALL_LANTERN_BLOCK)) {
			cir.setReturnValue(-30.f);
		}
	}
}
