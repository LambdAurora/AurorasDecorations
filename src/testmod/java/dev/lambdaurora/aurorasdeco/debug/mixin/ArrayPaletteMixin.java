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

package dev.lambdaurora.aurorasdeco.debug.mixin;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.palette.ArrayPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ArrayPalette.class)
public abstract class ArrayPaletteMixin<T> {
	@Shadow
	@Final
	private T[] array;

	@Shadow
	@Final
	private IndexedIterable<T> idList;

	@Inject(
			method = "write",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;getRawId(Ljava/lang/Object;)I"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onWrite(PacketByteBuf buf, CallbackInfo ci, int i) {
		var obj = this.array[i];
		if (this.idList.getRawId(obj) == -1) {
			AurorasDeco.error("Detected invalid ID!!! {} {} {}", this.idList.getRawId(obj), obj, obj.getClass().getSimpleName());
		}
	}
}
