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

package dev.lambdaurora.aurorasdeco.mixin;

import com.google.common.collect.ImmutableSet;
import dev.lambdaurora.aurorasdeco.accessor.BlockEntityTypeAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin implements BlockEntityTypeAccessor {
    @Mutable
    @Shadow
    @Final
    private Set<Block> blocks;

    @Override
    public Set<Block> aurorasdeco$getMutableSupportedBlocks() {
        if (this.blocks instanceof ImmutableSet)
            this.blocks = new HashSet<>(this.blocks);
        return this.blocks;
    }

    @Override
    public void aurorasdeco$addSupportedBlock(Block block) {
        this.aurorasdeco$getMutableSupportedBlocks().add(block);
    }
}
