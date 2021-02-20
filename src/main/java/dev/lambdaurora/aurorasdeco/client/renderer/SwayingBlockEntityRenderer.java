/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.client.renderer;

import dev.lambdaurora.aurorasdeco.block.entity.SwayingBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public abstract class SwayingBlockEntityRenderer<T extends SwayingBlockEntity> implements BlockEntityRenderer<T> {
    public float getNaturalSwayingAngle(T entity, float tickDelta) {
        if (!entity.canNaturallySway())
            return 0.f;

        BlockPos pos = entity.getPos();

        long time = 0;
        if (entity.getWorld() != null) {
            time = entity.getWorld().getTime();
        }

        int period = 125;
        float n = ((float) Math.floorMod(pos.getX() * 7L + pos.getY() * 9L + pos.getZ() * 13L + time, (long) period) + tickDelta)
                / period;
        return (float) ((.01f * MathHelper.cos((float) (Math.PI * 2 * n))) * Math.PI);
    }
}
