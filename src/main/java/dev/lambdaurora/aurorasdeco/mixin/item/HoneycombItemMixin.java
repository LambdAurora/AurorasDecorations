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

import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
	@Unique
	private static final ThreadLocal<NbtCompound> aurorasdeco$blockEntityData = new ThreadLocal<>();

	@Inject(
			method = "method_34719(Lnet/minecraft/item/ItemUsageContext;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;)Lnet/minecraft/util/ActionResult;",
			at = @At("HEAD")
	)
	private static void onBeforeReplace(ItemUsageContext context, BlockPos pos, World world, BlockState state, CallbackInfoReturnable<ActionResult> cir) {
		if (state.getBlock() instanceof BlackboardBlock) {
			var blockEntity = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
			if (blockEntity != null && !(blockEntity.isEmpty() && !blockEntity.hasCustomName())) {
				aurorasdeco$blockEntityData.set(blockEntity.writeBlackBoardNbt(new NbtCompound()));
			}
		}
	}

	@Inject(
			method = "method_34719(Lnet/minecraft/item/ItemUsageContext;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;)Lnet/minecraft/util/ActionResult;",
			at = @At("RETURN")
	)
	private static void onAfterReplace(ItemUsageContext context, BlockPos pos, World world, BlockState state, CallbackInfoReturnable<ActionResult> cir) {
		if (state.getBlock() instanceof BlackboardBlock) {
			var blockEntity = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
			if (blockEntity != null && aurorasdeco$blockEntityData.get() != null) {
				blockEntity.readBlackBoardNbt(aurorasdeco$blockEntityData.get());
				if (!world.isClient()) {
					blockEntity.markDirty();
					blockEntity.sync();
				}
				aurorasdeco$blockEntityData.remove();
			}
		}
	}
}
