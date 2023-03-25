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

package dev.lambdaurora.aurorasdeco.mixin.item;

import dev.lambdaurora.aurorasdeco.entity.FakeLeashKnotEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.LeadItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeadItem.class)
public abstract class LeadItemMixin {
	@Shadow
	public static ActionResult attachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos) {
		throw new UnsupportedOperationException("Mixin injection failed.");
	}

	@Unique
	private final ThreadLocal<ItemUsageContext> aurorasdeco$usageCtx = new ThreadLocal<>();

	@Inject(method = "useOnBlock", at = @At("HEAD"))
	private void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		this.aurorasdeco$usageCtx.set(context);
	}

	@Redirect(
			method = "useOnBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/LeadItem;attachHeldMobsToBlock(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/ActionResult;"
			)
	)
	private ActionResult onAttachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos) {
		var result = attachHeldMobsToBlock(player, world, pos);

		if (result == ActionResult.PASS) {
			var stack = this.aurorasdeco$usageCtx.get().getStack();

			var knot = new FakeLeashKnotEntity(AurorasDecoEntities.FAKE_LEASH_KNOT_ENTITY_TYPE, world);
			knot.setPosition(pos.getX() + 0.5, pos.getY() + 0.5 - 1F / 8F, pos.getZ() + 0.5);
			world.spawnEntity(knot);
			knot.attachLeash(player, true);

			if (!player.isCreative())
				stack.decrement(1);
			world.playSound(null, pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 1.f, 1.f);
			return ActionResult.SUCCESS;
		}

		return result;
	}
}
