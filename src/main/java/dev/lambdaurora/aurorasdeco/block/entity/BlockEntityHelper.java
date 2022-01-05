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

package dev.lambdaurora.aurorasdeco.block.entity;

import com.google.common.base.Preconditions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface BlockEntityHelper {
	default void sync() {
		World world = ((BlockEntity) this).getWorld();
		Preconditions.checkNotNull(world); //Maintain distinct failure case from below
		if (!(world instanceof ServerWorld)) throw new IllegalStateException("Cannot call sync() on the logical client! Did you check world.isClient first?");

		((ServerWorld) world).getChunkManager().markForUpdate(((BlockEntity) this).getPos());
	}
}
