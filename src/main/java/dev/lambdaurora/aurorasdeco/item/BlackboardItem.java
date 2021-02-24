/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.item;

import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.client.tooltip.BlackboardTooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

/**
 * Represents a blackboard item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardItem extends BlockItem {
    private final boolean locked;

    public BlackboardItem(BlackboardBlock blackboardBlock, Settings settings) {
        super(blackboardBlock, settings);
        this.locked = blackboardBlock.isLocked();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        CompoundTag nbt = stack.getSubTag("BlockEntityTag");
        if (nbt != null && nbt.contains("pixels", NbtType.BYTE_ARRAY)) {
            return Optional.of(new BlackboardTooltipComponent(
                    Registry.ITEM.getId(this).getPath().replace("waxed_", ""),
                    nbt.getByteArray("pixels"), this.locked));
        }
        return super.getTooltipData(stack);
    }
}
