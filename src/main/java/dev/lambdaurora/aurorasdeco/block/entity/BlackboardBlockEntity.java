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

import dev.lambdaurora.aurorasdeco.client.renderer.BlackboardBlockEntityRenderer;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Represents a blackboard block entity, stores the pixels of a blackboard.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardBlockEntity extends BlockEntity implements BlockEntityClientSerializable, Nameable {
    @Environment(EnvType.CLIENT)
    private BlackboardBlockEntityRenderer.BlackboardTexture texture = null;

    private final byte[] pixels = new byte[256];
    private @Nullable Text customName;

    public BlackboardBlockEntity(BlockPos pos, BlockState state) {
        super(AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, pos, state);
    }

    /**
     * Sets the pixel color at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param color the color
     */
    public void setPixel(int x, int y, DyeColor color) {
        byte colorId = (byte) (color.getId() + 1);
        if (this.pixels[y * 16 + x] != colorId) {
            this.pixels[y * 16 + x] = colorId;
            if (this.getWorld() instanceof ServerWorld) {
                this.sync();
                this.markDirty();
            }
        }
    }

    /**
     * Clears the pixel at the specified coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    public void clearPixel(int x, int y) {
        if (this.pixels[y * 16 + x] != 0) {
            this.pixels[y * 16 + x] = 0;
            if (this.getWorld() instanceof ServerWorld) {
                this.sync();
                this.markDirty();
            }
        }
    }

    /**
     * Clears the blackboard.
     */
    public void clear() {
        Arrays.fill(this.pixels, (byte) 0);
        this.sync();
        this.markDirty();
    }

    /**
     * Returns whether this blackboard is empty or not.
     *
     * @return {@code true} if empty, else {@code false}
     */
    public boolean isEmpty() {
        for (byte b : this.pixels) {
            if (b != 0)
                return false;
        }
        return true;
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

    @Environment(EnvType.CLIENT)
    public BlackboardBlockEntityRenderer.BlackboardTexture getTexture() {
        return this.texture;
    }

    @Override
    public void fromTag(CompoundTag nbt) {
        super.fromTag(nbt);
        this.readBlackBoardNbt(nbt);
    }

    @Override
    public CompoundTag toTag(CompoundTag nbt) {
        return this.writeBlackBoardNbt(super.toTag(nbt));
    }

    public void readBlackBoardNbt(CompoundTag nbt) {
        byte[] pixels = nbt.getByteArray("pixels");
        if (pixels.length == 256) {
            System.arraycopy(pixels, 0, this.pixels, 0, 256);
        }

        if (nbt.contains("custom_name", NbtType.STRING)) {
            this.customName = Text.Serializer.fromJson(nbt.getString("custom_name"));
        }
    }

    public CompoundTag writeBlackBoardNbt(CompoundTag nbt) {
        nbt.putByteArray("pixels", this.pixels);
        if (this.customName != null) {
            nbt.putString("custom_name", Text.Serializer.toJson(this.customName));
        }
        return nbt;
    }

    @Override
    public void fromClientTag(CompoundTag nbt) {
        this.readBlackBoardNbt(nbt);
        if (this.texture == null)
            this.texture = BlackboardBlockEntityRenderer.getOrCreateTexture();
        this.texture.update(this.pixels);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag nbt) {
        return this.writeBlackBoardNbt(nbt);
    }
}
