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

import dev.lambdaurora.aurorasdeco.block.WindChimeBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3f;

public class WindChimeBlockEntity extends SwayingBlockEntity {
	private final Vec3f collisionUnitVector = new Vec3f();
	private final Box collisionBox;

	public WindChimeBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE, pos, state);

		this.collisionBox = WindChimeBlock.COLLISION_BOX.offset(pos);
	}

	@Override
	public Box getCollisionBox() {
		return this.collisionBox;
	}

	@Override
	public int getMaxSwingTicks() {
		return this.getCachedState().getFluidState().isEmpty() ? 60 : 100;
	}

	public Vec3f getCollisionUnitVector() {
		return this.collisionUnitVector;
	}
}
