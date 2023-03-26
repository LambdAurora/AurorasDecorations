/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.entity;

import dev.lambdaurora.aurorasdeco.block.SeatBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Represents a placeholder entity to make another entity seat on a {@link SeatBlock}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SeatEntity extends Entity {
	private boolean timeout = false;

	public SeatEntity(EntityType<?> type, World world) {
		super(type, world);

		this.noClip = true;
	}

	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}

	@Override
	protected void initDataTracker() {
	}

	@Override
	public double getMountedHeightOffset() {
		return 0;
	}

	@Override
	public Vec3d updatePassengerForDismount(LivingEntity passenger) {
		var vec = super.updatePassengerForDismount(passenger);

		if (this.getWorld().getBlockState(this.getBlockPos().up()).isAir()) {
			return new Vec3d(vec.x, this.getBlockY() + 1, vec.z);
		}

		return vec;
	}

	@Override
	public boolean hasNoGravity() {
		return true;
	}

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		if (movementType == MovementType.PISTON)
			return;
		super.move(movementType, movement);
	}

	/* Serialization */

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
	}

	/* Networking */

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}

	/* Ticking */

	@Override
	public void tick() {
		super.tick();

		if (!this.world.isClient()) {
			var state = this.getWorld().getBlockState(this.getBlockPos());
			if (!(state.getBlock() instanceof SeatBlock || this.timeout) || !this.hasPassengers())
				this.discard();
		}
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
	}
}
