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

package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.block.AmethystLanternBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
	@Inject(
			method = "canSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/SpawnSettings$SpawnEntry;Lnet/minecraft/util/math/BlockPos$Mutable;D)Z",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void onCanSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor,
	                               ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry,
	                               BlockPos.Mutable pos, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			if (!group.isPeaceful() && world.getPointOfInterestStorage().getInSquare(
					poiType -> poiType == AurorasDecoRegistry.AMETHYST_LANTERN_POI,
					pos,
					AmethystLanternBlock.EFFECT_RADIUS,
					PointOfInterestStorage.OccupationStatus.ANY
			).anyMatch(poi -> {
				int y = poi.getPos().getY();
				return pos.getY() <= y + AmethystLanternBlock.EFFECT_RADIUS && pos.getY() >= y - AmethystLanternBlock.EFFECT_RADIUS;
			})) {
				cir.setReturnValue(false);
			}
		}
	}
}
