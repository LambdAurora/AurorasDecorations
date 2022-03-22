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

package dev.lambdaurora.aurorasdeco.mixin.client;

import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigFlowerPotBlock;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockDustParticle.Factory.class)
public abstract class BlockDustParticleFactoryMixin {
	@Inject(method = "createParticle", at = @At("HEAD"))
	private void onCreateParticle(BlockStateParticleEffect parameters, ClientWorld world,
	                              double x, double y, double z, double velocityX, double velocityY, double velocityZ,
	                              CallbackInfoReturnable<Particle> cir) {
		var state = parameters.getBlockState();
		if (state.getBlock() instanceof BigFlowerPotBlock.PlantAirBlock) {
			state = world.getBlockState(new BlockPos.Mutable(x, y, z).move(0, -1, 0));

			((BlockStateParticleEffectAccessor) parameters).setBlockState(state);
		}
	}
}
