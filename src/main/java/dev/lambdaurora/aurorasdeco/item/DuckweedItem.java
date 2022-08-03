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

package dev.lambdaurora.aurorasdeco.item;

import dev.lambdaurora.aurorasdeco.util.KindSearcher;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * Represents the item form of {@link dev.lambdaurora.aurorasdeco.block.plant.DuckweedBlock}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class DuckweedItem extends DerivedBlockItem {
	public DuckweedItem(Block block, Settings settings) {
		super(block, KindSearcher.strictlyAfter(Items.LILY_PAD), KindSearcher::findLastOfGroup, settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		return ActionResult.PASS;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		BlockHitResult waterSourceHit = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
		ActionResult result = super.useOnBlock(new ItemUsageContext(user, hand, waterSourceHit));
		return new TypedActionResult<>(result, user.getStackInHand(hand));
	}
}
