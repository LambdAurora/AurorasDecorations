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

import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.BlackboardHandler;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Represents a blackboard block entity, stores the pixels of a blackboard.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardBlockEntity extends BasicBlockEntity implements Nameable,
		RenderAttachmentBlockEntity, BlackboardHandler {
	@Environment(EnvType.CLIENT)
	private static final Set<BlackboardBlockEntity> ACTIVE_BLACKBOARDS = new ObjectOpenHashSet<>();
	private final Blackboard blackboard = new AssignedBlackboard();
	private @Nullable Text customName;

	public PlayerEntity lastUser;
	public int lastX;
	public int lastY;

	@Environment(EnvType.CLIENT)
	private Mesh mesh = null;
	@Environment(EnvType.CLIENT)
	private boolean meshDirty = true;

	public BlackboardBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public byte getPixel(int x, int y) {
		return this.blackboard.getPixel(x, y);
	}

	@Override
	public boolean setPixel(int x, int y, Blackboard.Color color, int shade) {
		if (this.blackboard.setPixel(x, y, color, shade)) {
			if (this.getWorld() instanceof ServerWorld) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean brush(int x, int y, Blackboard.Color color, int shade) {
		if (this.blackboard.brush(x, y, color, shade)) {
			if (this.getWorld() instanceof ServerWorld) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean replace(int x, int y, Blackboard.Color color, int shade) {
		if (this.blackboard.replace(x, y, color, shade)) {
			if (this.getWorld() instanceof ServerWorld) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean fill(int x, int y, Blackboard.Color color, int shade) {
		if (this.blackboard.fill(x, y, color, shade)) {
			if (this.getWorld() instanceof ServerWorld) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean line(int x1, int y1, int x2, int y2, Blackboard.Color color, int shade) {
		if (this.blackboard.line(x1, y1, x2, y2, color, shade)) {
			if (this.getWorld() instanceof ServerWorld) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	public void copy(Blackboard source) {
		this.blackboard.copy(source);
		if (this.getWorld() instanceof ServerWorld) {
			this.sync();
			this.markDirty();
		}
	}

	/**
	 * Clears the blackboard.
	 */
	public void clear() {
		this.blackboard.clear();
		this.lastUser = null;
		this.sync();
		this.markDirty();
	}

	/**
	 * Returns whether this blackboard is empty or not.
	 *
	 * @return {@code true} if empty, else {@code false}
	 */
	public boolean isEmpty() {
		return this.blackboard.isEmpty();
	}

	@Override
	public @Nullable Text getCustomName() {
		return this.customName;
	}

	/**
	 * Sets the blackboard custom name.
	 *
	 * @param customName the custom name
	 */
	public void setCustomName(@Nullable Text customName) {
		this.customName = customName;
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null;
	}

	@Override
	public Text getName() {
		return this.customName != null ? this.customName
				: new TranslatableText(this.getCachedState().getBlock().getTranslationKey());
	}

	public boolean isLocked() {
		return ((BlackboardBlock) this.getCachedState().getBlock()).isLocked();
	}

	@Override
	public void markRemoved() {
		super.markRemoved();

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			this.markBlackboardRemoved();
		}
	}

	@Override
	public void cancelRemoval() {
		super.cancelRemoval();

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ACTIVE_BLACKBOARDS.add(this);
		}
	}

	/* Client */

	@Override
	public @Nullable Object getRenderAttachmentData() {
		if (this.meshDirty)
			this.rebuildMesh();
		return this.mesh;
	}

	@Environment(EnvType.CLIENT)
	public void markMeshDirty() {
		this.meshDirty = true;
	}

	@Environment(EnvType.CLIENT)
	private void rebuildMesh() {
		this.meshDirty = false;
		int light = this.blackboard.isLit() ? 0xf000f0 : 0;
		this.mesh = this.blackboard.buildMesh(this.getCachedState().get(BlackboardBlock.FACING), light);
	}

	@Environment(EnvType.CLIENT)
	public void markBlackboardRemoved() {
		ACTIVE_BLACKBOARDS.remove(this);
	}

	@Environment(EnvType.CLIENT)
	public static void markAllMeshesDirty() {
		ACTIVE_BLACKBOARDS.forEach(BlackboardBlockEntity::markMeshDirty);
	}

	@Environment(EnvType.CLIENT)
	public static void onWorldChange(@Nullable ClientWorld world) {
		ACTIVE_BLACKBOARDS.removeIf(blackboardBlockEntity -> blackboardBlockEntity.world == null
				|| blackboardBlockEntity.world != world);
	}

	/* Serialization */

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.readBlackBoardNbt(nbt);
		this.lastUser = null;
		if (this.world != null && this.world instanceof ClientWorld) {
			this.refreshRendering();
		}
	}

	public void refreshRendering() {
		this.rebuildMesh();
		((ClientWorld) this.world).scheduleBlockRenders(
				ChunkSectionPos.getSectionCoord(this.getPos().getX()),
				ChunkSectionPos.getSectionCoord(this.getPos().getY()),
				ChunkSectionPos.getSectionCoord(this.getPos().getZ())
		);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		this.writeBlackBoardNbt(nbt);
	}

	public void readBlackBoardNbt(NbtCompound nbt) {
		this.blackboard.readNbt(nbt);

		if (nbt.contains("custom_name", NbtType.STRING)) {
			this.customName = Text.Serializer.fromJson(nbt.getString("custom_name"));
		}
	}

	public NbtCompound writeBlackBoardNbt(NbtCompound nbt) {
		this.blackboard.writeNbt(nbt);
		if (this.customName != null) {
			nbt.putString("custom_name", Text.Serializer.toJson(this.customName));
		}
		return nbt;
	}

	private class AssignedBlackboard extends Blackboard {
		@Override
		public boolean isLit() {
			return BlackboardBlockEntity.this.getCachedState().get(BlackboardBlock.LIT);
		}

		@Override
		public void setLit(boolean lit) {
		}
	}
}
