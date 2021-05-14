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

package dev.lambdaurora.aurorasdeco.mixin;

import com.google.common.collect.ImmutableSet;
import dev.lambdaurora.aurorasdeco.accessor.PointOfInterestTypeAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(PointOfInterestType.class)
public class PointOfInterestTypeMixin implements PointOfInterestTypeAccessor {
    @Mutable
    @Shadow
    @Final
    private Set<BlockState> blockStates;

    @Inject(
            method = "<init>(Ljava/lang/String;Ljava/util/Set;II)V",
            at = @At("RETURN")
    )
    private void onInit(String id, Set<BlockState> blockStates, int ticketCount, int searchDistance, CallbackInfo ci) {
        if (id.equals("home") && blockStates instanceof ImmutableSet) {
            // We need this one to be mutable.
            this.blockStates = new HashSet<>(blockStates);
        }
    }

    @Override
    public Set<BlockState> getBlockStates() {
        return this.blockStates;
    }
}
