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

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntity;

/**
 * Represents a basic block entity with common serialization and update packet code.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BasicBlockEntity extends BlockEntity implements QuiltBlockEntity {
	public BasicBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return this.toNbt();
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.of(this);
	}
}
