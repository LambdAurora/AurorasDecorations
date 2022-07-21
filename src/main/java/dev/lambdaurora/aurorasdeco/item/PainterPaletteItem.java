/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

/**
 * Represents a painter's palette item which can be used for easier painting on blackboards.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class PainterPaletteItem extends Item {
	public PainterPaletteItem(Settings settings) {
		super(settings);
	}

	public ItemStack getCurrentColorAsItem(ItemStack colorStack) {
		return ItemStack.EMPTY;
	}

	public ItemStack getCurrentModifierAsItem(ItemStack paletteStack) {
		// @TODO
		return ItemStack.EMPTY;
	}

	public boolean onScroll(PlayerEntity player, ItemStack paletteStack, double scrollDelta) {
		if (player.getWorld().isClient()) {
			var buffer = PacketByteBufs.create();
			buffer.writeDouble(scrollDelta);

			ClientPlayNetworking.send(AurorasDecoPackets.PAINTER_PALETTE_SCROLL, buffer);
		} else {

		}

		return true;
	}
}
