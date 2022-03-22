/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.util.RegistrationHelper;
import net.fabricmc.fabric.impl.registry.sync.trackers.StateIdTracker;
import net.minecraft.block.Block;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.function.Function;

@Mixin(StateIdTracker.class)
public class StateIdTrackerMixin {
	@SuppressWarnings("unchecked")
	@Inject(method = "register", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
	private static <T, S> void onRegister(Registry<T> registry, IdList<S> stateList, Function<T, Collection<S>> stateGetter, CallbackInfo ci,
	                                      StateIdTracker<T, S> tracker) {
		if (registry == Registry.BLOCK) {
			RegistrationHelper.BLOCK.setFabricTracker((StateIdTracker<Block, ?>) tracker);
		}
	}
}
