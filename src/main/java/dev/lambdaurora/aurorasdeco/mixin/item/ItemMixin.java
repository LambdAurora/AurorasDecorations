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

package dev.lambdaurora.aurorasdeco.mixin.item;

import dev.lambdaurora.aurorasdeco.accessor.ItemExtensions;
import dev.lambdaurora.aurorasdeco.mixin.SimpleRegistryAccessor;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin implements ItemExtensions {
	@Shadow
	@Final
	@Nullable
	private FoodComponent foodComponent;

	@Unique
	private BlockItem aurorasdeco$placeable;
	@Unique
	private boolean aurorasdeco$requireSneaking;

	@SuppressWarnings("unchecked")
	@Override
	public void makePlaceable(Block block, boolean requireSneaking) {
		this.aurorasdeco$placeable = new BlockItem(block, new QuiltItemSettings()
				.food(this.foodComponent));
		this.aurorasdeco$requireSneaking = requireSneaking;

		var cache = ((SimpleRegistryAccessor<Item>) Registry.ITEM).getIntrusiveHolderCache();

		if (cache != null) {
			cache.remove(this.aurorasdeco$placeable);
		}
	}

	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		if (this.aurorasdeco$placeable != null && (!this.aurorasdeco$requireSneaking || context.shouldCancelInteraction())) {
			// This item is placeable despite it not being an actual BlockItem.
			cir.setReturnValue(this.aurorasdeco$placeable.useOnBlock(context));
		}
	}
}
