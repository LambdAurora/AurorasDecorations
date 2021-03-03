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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.entity.SitEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SittableBlock {
    default boolean sit(World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
        if (world.isClient())
            return stack.isEmpty();
        else if (!stack.isEmpty())
            return false;

        SitEntity sitEntity = AurorasDecoRegistry.SIT_ENTITY_TYPE.create(world);
        if (sitEntity == null)
            return false;
        sitEntity.setPos(pos.getX() + .5f, pos.getY() + this.getSitYOffset(), pos.getZ() + .5f);
        world.spawnEntity(sitEntity);
        player.startRiding(sitEntity, true);

        return true;
    }

    float getSitYOffset();
}
