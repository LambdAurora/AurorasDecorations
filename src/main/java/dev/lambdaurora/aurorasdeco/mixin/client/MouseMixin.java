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

package dev.lambdaurora.aurorasdeco.mixin.client;

import dev.lambdaurora.aurorasdeco.item.PainterPaletteItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@ClientOnly
@Mixin(Mouse.class)
public class MouseMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(
			method = "onMouseScroll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void onScroll(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci, double scrollDelta) {
		if (this.client.player != null && this.client.player.isSneaking()) {
			if (this.client.player.getMainHandStack().getItem() instanceof PainterPaletteItem paletteItem) {
				if (paletteItem.onScroll(this.client.player, this.client.player.getMainHandStack(), scrollDelta, Screen.hasControlDown())) {
					ci.cancel();
				}
			}
		}
	}
}
