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

import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Represents a Lantern Block Entity for the wall lanterns.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanternBlockEntity extends SwayingBlockEntity {
	private Box lanternCollisionBoxX;
	private Box lanternCollisionBoxZ;
	public float prevAngle;
	public float angle;

	public LanternBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public void setWorld(World world) {
		super.setWorld(world);
		this.updateCollisionBoxes();
	}

	/**
	 * Returns the lantern of this wall lantern as block state.
	 *
	 * @return the lantern as block state
	 */
	public BlockState getLanternState() {
		var cachedState = this.getCachedState();
		return ((WallLanternBlock) cachedState.getBlock()).getLanternState(cachedState);
	}

	@Override
	public Box getCollisionBox() {
		var swingAxis = this.getCachedState().get(WallLanternBlock.FACING).rotateYClockwise().getAxis();
		return swingAxis == Direction.Axis.X ? this.lanternCollisionBoxX : this.lanternCollisionBoxZ;
	}

	@Override
	public int getMaxSwingTicks() {
		return this.getCachedState().getFluidState().isEmpty() ? 60 : 100;
	}

	private void updateCollisionBoxes() {
		if (this.getWorld() == null)
			return;

		var pos = this.getPos();

		var lanternState = this.getLanternState();
		var box = lanternState.getOutlineShape(this.getWorld(), pos, ShapeContext.absent())
				.offset(0, 2.0 / 16.0, 0).getBoundingBox();

		this.lanternCollisionBoxX = box.expand(0.1, 0, 0).offset(pos);
		this.lanternCollisionBoxZ = box.expand(0, 0, 0.1).offset(pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setCachedState(BlockState state) {
		super.setCachedState(state);
		this.updateCollisionBoxes();
	}

	/* Ticking */

	@Override
	protected void tickClient(World world) {
		super.tickClient(world);

		this.prevAngle = this.angle;

		if (this.isSwinging() || this.isColliding()) {
			boolean fluid = !this.getCachedState().getFluidState().isEmpty();
			float ticks = (float) this.getSwingTicks();

			if (this.isColliding() && ticks > 4) {
				ticks = 4.f;
			}
			if (fluid)
				ticks /= 2.f;

			float shiftedTicks = ticks - 100;
			this.angle = (shiftedTicks * shiftedTicks) / 5000 * MathHelper.sin(ticks / MathHelper.PI) / (4 + ticks / 3);
		} else {
			this.angle = this.getNaturalSwayingAngle();
		}
	}

	public float getNaturalSwayingAngle() {
		if (!this.canNaturallySway())
			return 0.f;

		var pos = this.getPos();

		long time = 0;
		if (this.getWorld() != null) {
			time = this.getWorld().getTime();
		}

		int period = 125;
		float n = ((float) Math.floorMod(pos.getX() * 7L + pos.getY() * 9L + pos.getZ() * 13L + time, (long) period))
				/ period;
		return (float) ((.01f * MathHelper.cos((float) (Math.PI * 2 * n))) * Math.PI);
	}
}
