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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.block.BenchBlock;
import dev.lambdaurora.aurorasdeco.item.SeatRestItem;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link BenchBlock} block entity.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BenchBlockEntity extends BasicBlockEntity implements RenderAttachmentBlockEntity {
	private SeatRestItem rest;

	public BenchBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasDecoRegistry.BENCH_BLOCK_ENTITY_TYPE, pos, state);
	}

	/**
	 * Gets the seat rest item of this bench.
	 *
	 * @return the seat rest item
	 */
	public SeatRestItem getRest() {
		return this.rest;
	}

	/**
	 * Sets the seat rest of this bench.
	 *
	 * @param rest the seat rest item
	 */
	public void setRest(SeatRestItem rest) {
		this.rest = rest;

		this.update();
		if (this.world != null && this.world.isClient()
				&& this.world instanceof ClientWorld clientWorld) {
			clientWorld.scheduleBlockRenders(
					ChunkSectionPos.getSectionCoord(this.getPos().getX()),
					ChunkSectionPos.getSectionCoord(this.getPos().getY()),
					ChunkSectionPos.getSectionCoord(this.getPos().getZ())
			);
		}
		this.markDirty();
	}

	/**
	 * Returns whether this bench has a rest or not.
	 *
	 * @return {@code true} if this bench has a rest, or {@code false} otherwise
	 */
	public boolean hasRest() {
		return this.rest != null;
	}

	/**
	 * Updates the block state and neighbors if needed.
	 */
	private void update() {
		if (this.world == null) return;
		var state = this.getCachedState();
		var block = (BenchBlock) state.getBlock();
		var facing = state.get(BenchBlock.FACING);
		var relativeRight = pos.offset(facing.rotateYCounterclockwise());
		var relativeLeft = pos.offset(facing.rotateYClockwise());

		var newState = state
				.with(BenchBlock.LEFT_LEGS, !block.canConnect(world, relativeLeft, facing, this.getRest()))
				.with(BenchBlock.RIGHT_LEGS, !block.canConnect(world, relativeRight, facing, this.getRest()));
		if (state != newState)
			this.world.setBlockState(this.getPos(), newState, Block.NOTIFY_ALL);
	}

	/* Client */

	@Override
	public @Nullable Object getRenderAttachmentData() {
		if (this.rest != null) return this.rest;
		else return null;
	}

	/* Serialization */

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.readBenchNbt(nbt);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		this.writeBenchNbt(nbt);
	}

	private void readBenchNbt(NbtCompound nbt) {
		var hadRest = this.rest != null;
		this.rest = null;
		if (nbt.contains("rest", NbtElement.STRING_TYPE)) {
			var restId = Identifier.tryParse(nbt.getString("rest"));

			if (restId != null) {
				var item = Registries.ITEM.get(restId);
				if (item instanceof SeatRestItem seatRestItem)
					this.rest = seatRestItem;
			}
		}
		if (hadRest == (this.rest == null) && this.world != null && !this.world.isClient()) {
			this.update();
		}
	}

	private void writeBenchNbt(NbtCompound nbt) {
		if (this.rest != null)
			nbt.putString("rest", Registries.ITEM.getId(this.rest).toString());
	}
}
