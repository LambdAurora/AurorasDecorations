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

package dev.lambdaurora.aurorasdeco.entity;

import dev.lambdaurora.aurorasdeco.block.SeatBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SeatEntity extends Entity {
    public SeatEntity(EntityType<?> type, World world) {
        super(type, world);

        this.noClip = true;
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
        Vec3d vec = super.updatePassengerForDismount(passenger);

        if (this.getEntityWorld().getBlockState(this.getBlockPos().up()).isAir()) {
            return new Vec3d(vec.x, this.getBlockY() + 1, vec.z);
        }

        return vec;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    /* Serialization */

    @Override
    protected void readCustomDataFromTag(CompoundTag tag) {
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {
    }

    /* Networking */

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    /* Ticking */

    @Override
    public void tick() {
        super.tick();

        BlockState state = this.getEntityWorld().getBlockState(this.getBlockPos());
        if (!(state.getBlock() instanceof SeatBlock) || !this.hasPassengers())
            this.discard();
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
    }
}
