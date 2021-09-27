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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.block.WindChimeBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.Set;

public class WindChimeBlockEntity extends BlockEntity {
	private final Set<Entity> collisions = new ObjectOpenHashSet<>();
	private final Vec3f collisionUnitVector = new Vec3f();
	private final Box collisionBox;
	private boolean colliding;

	public int ticks = 0;

	public WindChimeBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE, pos, state);

		this.collisionBox = WindChimeBlock.COLLISION_BOX.offset(pos);
	}

	public Box getCollisionBox() {
		return this.collisionBox;
	}

	public Vec3f getCollisionUnitVector() {
		return this.collisionUnitVector;
	}

	public boolean isColliding() {
		return this.colliding;
	}

	public void startColliding(Entity entity) {
		this.collisions.add(entity);
		this.colliding = true;
	}

	/* Ticking */

	public static void clientTick(World world, BlockPos pos, BlockState state, WindChimeBlockEntity windChime) {
		if (!windChime.collisions.isEmpty()) {
			windChime.collisionUnitVector.set(0.f, 0.f, 0.f);

			float selfX = pos.getX() + .5f;
			float selfZ = pos.getX() + .5f;

			windChime.collisions.removeIf(entity -> {
				if (entity.isRemoved())
					return true;
				if (!entity.getBoundingBox().intersects(windChime.collisionBox))
					return true;

				windChime.collisionUnitVector.add((float) (selfX - entity.getX()), 0.f, (float) (selfZ - entity.getZ()));

				return false;
			});

			float multiple = 1.f / windChime.collisions.size();
			windChime.collisionUnitVector.multiplyComponentwise(multiple, 0.f, multiple);
		}
		if (windChime.collisions.isEmpty() && windChime.isColliding()) {
			windChime.colliding = false;
		}

		if (windChime.isColliding()) {
			windChime.ticks++;
			if (windChime.ticks > 50)
				windChime.ticks = 0;
		}
	}
}
