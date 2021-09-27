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

import dev.lambdaurora.aurorasdeco.block.AmethystLanternBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillagerEntity.class)
public class ZombieVillagerEntityMixin extends ZombieEntity {
	public ZombieVillagerEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "getConversionRate", at = @At("RETURN"), cancellable = true)
	private void onGetConversionRate(CallbackInfoReturnable<Integer> cir) {
		if (this.random.nextFloat() < .35f) {
			int lanterns = (int) ((ServerWorld) this.world).getPointOfInterestStorage().getInSquare(
					poiType -> poiType == AurorasDecoRegistry.AMETHYST_LANTERN_POI,
					this.getBlockPos(),
					AmethystLanternBlock.EFFECT_RADIUS,
					PointOfInterestStorage.OccupationStatus.ANY
			).filter(poi -> {
				int y = poi.getPos().getY();
				int entityY = this.getBlockY();
				return entityY <= y + AmethystLanternBlock.EFFECT_RADIUS && entityY >= y - AmethystLanternBlock.EFFECT_RADIUS;
			}).count();
			if (lanterns > 0)
				cir.setReturnValue(lanterns + Math.min(cir.getReturnValueI(), 14));
		}
	}
}
