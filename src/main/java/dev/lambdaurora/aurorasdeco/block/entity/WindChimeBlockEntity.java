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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.block.WindChimeBlock;
import dev.lambdaurora.aurorasdeco.client.Wind;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WindChimeBlockEntity extends SwayingBlockEntity {
	private final Box collisionBox;
	private float prevPitch;
	private float pitch;
	private float prevRoll;
	private float roll;

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

	public float getPitch(float tickDelta) {
		return MathHelper.lerp(tickDelta, this.prevPitch, this.pitch);
	}

	public float getRoll(float tickDelta) {
		return MathHelper.lerp(tickDelta, this.prevRoll, this.roll);
	}

	/* Ticking */

	@Environment(EnvType.CLIENT)
	@Override
	protected void tickClient(World world) {
		super.tickClient(world);

		this.prevPitch = this.pitch;
		this.prevRoll = this.roll;

		if (!this.canNaturallySway()) {
			this.pitch = 0.f;
			this.roll = 0.f;
		} else {
			var wind = Wind.get();
			this.pitch = MathHelper.sin(-wind.getWindZ()) * 0.85f;
			this.roll = MathHelper.sin(wind.getWindX()) * 0.85f;

			if (wind.getState() == Wind.State.STORMY) {
				this.pitch += MathHelper.sin((this.prevPitch - this.pitch) * this.getSwingTicks() / 4.f) * 0.02f;
				this.roll += MathHelper.sin((this.prevRoll - this.roll) * this.getSwingTicks() / 4.f) * 0.02f;
			} else {
				this.pitch += MathHelper.sin((this.prevPitch - this.pitch) * this.getSwingTicks() / 5.f) * 0.015f;
				this.roll += MathHelper.sin((this.prevRoll - this.roll) * this.getSwingTicks() / 5.f) * 0.015f;
			}
		}

		if (this.isSwinging() || this.isColliding()) {
			float ticks = this.getAdjustedSwingTicks();
			float shiftedTicks = ticks - 100;

			this.pitch *= ticks / this.getMaxSwingTicks();
			this.roll *= ticks / this.getMaxSwingTicks();
			float angle = (shiftedTicks * shiftedTicks) / 5000 * MathHelper.sin(ticks / MathHelper.PI) / (4 + ticks / 3);

			switch (this.getSwingBaseDirection()) {
				case NORTH -> this.pitch -= angle;
				case SOUTH -> this.pitch += angle;
				case EAST -> this.roll -= angle;
				case WEST -> this.roll += angle;
			}
		}
	}
}
