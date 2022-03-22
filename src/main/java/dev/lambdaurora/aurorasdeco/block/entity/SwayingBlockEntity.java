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

package dev.lambdaurora.aurorasdeco.block.entity;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Set;

public abstract class SwayingBlockEntity extends BlockEntity {
	protected boolean naturalSway = false;
	private int swingTicks;
	private boolean swinging;
	private Direction swingBaseDirection;
	private boolean colliding = false;
	private final Set<Entity> collisions = new ObjectOpenHashSet<>();

	public SwayingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * Returns whether this block entity can naturally sway.
	 *
	 * @return {@code true} if this block entity can naturally sway, else {@code false}
	 */
	public boolean canNaturallySway() {
		return this.naturalSway;
	}

	/**
	 * {@return the collision box of the swaying part of this block entity}
	 */
	public abstract Box getCollisionBox();

	public int getSwingTicks() {
		return this.swingTicks;
	}

	public float getAdjustedSwingTicks() {
		boolean fluid = !this.getCachedState().getFluidState().isEmpty();
		float ticks = (float) this.getSwingTicks();

		if (this.isColliding() && ticks > 4) {
			ticks = 4.f;
		}
		if (fluid)
			ticks /= 2.f;

		return ticks;
	}

	/**
	 * Returns the max swing ticks.
	 *
	 * @return the max swing ticks
	 */
	public abstract int getMaxSwingTicks();

	/**
	 * Returns whether this swaying block entity is being force to sway or not.
	 *
	 * @return {@code true} if this swaying block entity is swaying, else {@code false}
	 */
	public boolean isSwinging() {
		return this.swinging;
	}

	public Direction getSwingBaseDirection() {
		return this.swingBaseDirection;
	}

	/**
	 * Returns whether this swaying block entity is colliding with an entity or not.
	 *
	 * @return {@code true} if this swaying block entity is colliding with an entity, else {@code false}
	 */
	public boolean isColliding() {
		return this.colliding;
	}

	/**
	 * Swings the swaying block entity in a given direction.
	 *
	 * @param direction the direction to swing to
	 */
	public void activate(Direction direction) {
		var blockPos = this.getPos();
		this.swingBaseDirection = direction;
		if (this.swinging) {
			if (this.isColliding())
				this.swingTicks = 4;
			else
				this.swingTicks = 0;
		} else {
			this.swinging = true;
		}

		this.getWorld().addSyncedBlockEvent(blockPos, this.getCachedState().getBlock(), 1, direction.getId());
	}

	/**
	 * Swings the swaying block entity in a given direction. Caused by an entity collision.
	 *
	 * @param direction the direction to swing to
	 * @param entity the entity who made the swaying block entity swing
	 */
	public void activate(Direction direction, Entity entity) {
		this.collisions.add(entity);
		this.colliding = true;
		this.getWorld().addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 2, 1);
		this.getWorld().updateComparators(this.getPos(), this.getCachedState().getBlock());

		this.activate(direction);
	}

	/**
	 * Swings the swaying block entity in a given direction. Caused by an entity collision.
	 *
	 * @param entity the entity who made the swaying block entity swing
	 */
	public void activate(Entity entity) {
		var pos = this.getPos();
		double selfX = pos.getX() + 0.5;
		double selfZ = pos.getZ() + 0.5;

		double diffX = selfX - entity.getX();
		double diffZ = selfZ - entity.getZ();

		Direction direction;
		if (Math.abs(diffX) > Math.abs(diffZ)) {
			if (diffX > 0) direction = Direction.WEST;
			else direction = Direction.EAST;
		} else {
			if (diffZ > 0) direction = Direction.NORTH;
			else direction = Direction.SOUTH;
		}

		this.activate(direction, entity);
	}

	/* Syncing */

	@Override
	public boolean onSyncedBlockEvent(int type, int data) {
		if (type == 1) {
			this.swingBaseDirection = Direction.byId(data);
			if (!this.swinging || !this.isColliding()) {
				this.swingTicks = 0;
			}
			this.swinging = true;
			return true;
		} else if (type == 2) {
			this.colliding = data != 0;
			return true;
		} else {
			return super.onSyncedBlockEvent(type, data);
		}
	}

	/* Ticking */

	private void tick() {
		++this.swingTicks;

		if (this.swingTicks >= 4 && this.isColliding()) {
			this.swingTicks = 4;
		}

		if (this.swingTicks >= this.getMaxSwingTicks()) {
			this.swinging = false;
			this.swingTicks = 0;
		}
	}

	@Environment(EnvType.CLIENT)
	protected void tickClient(World world) {
		this.naturalSway = world.getLightLevel(LightType.SKY, this.pos) >= 12;
		this.tick();
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, SwayingBlockEntity swayingBlockEntity) {
		swayingBlockEntity.tickClient(world);
	}

	public static void serverTick(World world, BlockPos pos, BlockState state, SwayingBlockEntity swayingBlockEntity) {
		boolean canTick = true;

		if (!swayingBlockEntity.collisions.isEmpty()) {
			var it = swayingBlockEntity.collisions.iterator();

			while (it.hasNext()) {
				var entry = it.next();

				if (entry.isRemoved())
					it.remove();
				else {
					if (swayingBlockEntity.getCollisionBox().intersects(entry.getBoundingBox())) {
						canTick = false;
					} else {
						it.remove();
					}
				}
			}
		}
		if (swayingBlockEntity.collisions.isEmpty() && swayingBlockEntity.isColliding()) {
			swayingBlockEntity.colliding = false;
			world.addSyncedBlockEvent(pos, state.getBlock(), 2, 0);
			world.updateComparators(pos, state.getBlock());
		}

		if (canTick) {
			int oldSwingTicks = swayingBlockEntity.swingTicks;
			swayingBlockEntity.tick();
			if (oldSwingTicks != swayingBlockEntity.swingTicks) {
				world.updateComparators(pos, state.getBlock());
			}
		}
	}
}
