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

import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a blackboard block entity, stores the pixels of a blackboard.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardBlockEntity extends BlockEntity implements BlockEntityClientSerializable, Nameable,
        RenderAttachmentBlockEntity {
    @Environment(EnvType.CLIENT)
    private Mesh mesh = null;

    private final Blackboard blackboard = new AssignedBlackboard();
    private @Nullable Text customName;

    public BlackboardBlockEntity(BlockPos pos, BlockState state) {
        super(AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, pos, state);
    }

    public byte getColor(int x, int y) {
        return this.blackboard.getPixel(x, y);
    }

    /**
     * Sets the pixel color at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param color the color
     */
    public void setPixel(int x, int y, Blackboard.Color color, int shade) {
        if (this.blackboard.setPixel(x, y, color, shade)) {
            if (this.getWorld() instanceof ServerWorld) {
                this.sync();
                this.markDirty();
            }
        }
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

    /* Client */

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return this.mesh;
    }

    /* Serialization */

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.readBlackBoardNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return this.writeBlackBoardNbt(super.writeNbt(nbt));
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

    @Override
    public void fromClientTag(NbtCompound nbt) {
        this.readBlackBoardNbt(nbt);

        int light = this.blackboard.isLit() ? 0xf000f0 : 0;
        this.mesh = this.blackboard.buildMesh(this.getCachedState().get(BlackboardBlock.FACING), light);
        ((ClientWorld) this.world).scheduleBlockRenders(
                ChunkSectionPos.getSectionCoord(this.getPos().getX()),
                ChunkSectionPos.getSectionCoord(this.getPos().getY()),
                ChunkSectionPos.getSectionCoord(this.getPos().getZ())
        );
    }

    @Override
    public NbtCompound toClientTag(NbtCompound nbt) {
        return this.writeBlackBoardNbt(nbt);
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
