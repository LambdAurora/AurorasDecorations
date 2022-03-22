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

import net.minecraft.item.BlockItem;
import net.minecraft.util.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Holder.Reference.class)
public abstract class HolderReferenceMixin<T> {
	@Shadow
	public abstract T value();

	@Inject(method = "toString", at = @At("RETURN"), cancellable = true)
	private void onToString(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(cir.getReturnValue() + " " + this.value().getClass().getSimpleName());

		if (this.value() instanceof BlockItem blockItem) {
			cir.setReturnValue(cir.getReturnValue() + " " + blockItem.getBlock() + " " + blockItem.getBlock().getClass().getSimpleName());
		}
	}
}
