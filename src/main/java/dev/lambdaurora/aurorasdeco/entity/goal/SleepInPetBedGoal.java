/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.entity.goal;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.WorldView;

/**
 * Makes pets go towards pet beds when tamed.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class SleepInPetBedGoal extends MoveToTargetPosGoal {
	public SleepInPetBedGoal(PathAwareEntity mob, double speed) {
		super(mob, speed, 8);
	}

	/**
	 * Sets the entity in sleeping position.
	 *
	 * @param value {@code true} if the entity is in sleeping position, else {@code false}
	 */
	public abstract void setInSleepingPosition(boolean value);

	@Override
	public boolean canStart() {
		if (this.mob instanceof AnimalEntity animal) {
			if (animal.isInLove())
				return false;
		}
		if (this.mob instanceof TameableEntity tameable) {
			if (!tameable.isTamed())
				return false;
			if (tameable.isSitting())
				return false;
		}

		boolean result = super.canStart();
		if (result) {
			if (!this.mob.getWorld().getOtherEntities(this.mob, new Box(this.getTargetPos())).isEmpty())
				result = false;
		}

		return result;
	}

	@Override
	public void start() {
		super.start();
		this.setInSleepingPosition(false);
	}

	@Override
	public void stop() {
		super.stop();
		this.setInSleepingPosition(false);
	}

	@Override
	public void tick() {
		boolean reached;
		var targetPos = this.getTargetPos().down();
		if (!targetPos.isWithinDistance(this.mob.getBlockPos(), this.getDesiredSquaredDistanceToTarget())) {
			reached = false;
			++this.tryingTime;
			if (this.shouldResetPath()) {
				this.mob.getNavigation().startMovingTo(
						targetPos.getX() + 0.5,
						targetPos.getY() + 0.25,
						targetPos.getZ() + 0.5,
						this.speed
				);
			}
		} else {
			reached = true;
			--this.tryingTime;

			AurorasDecoRegistry.PET_USE_PET_BED_CRITERION.trigger(this.mob, (ServerWorld) this.mob.getWorld(), targetPos);
		}

		this.setInSleepingPosition(reached);
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		if (!world.isAir(pos.up())) {
			return false;
		}

		var state = world.getBlockState(pos);
		return state.isIn(AurorasDecoTags.PET_BEDS);
	}
}
