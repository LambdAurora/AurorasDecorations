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

package dev.lambdaurora.aurorasdeco.accessor;

import net.minecraft.block.Block;

/**
 * Represents extensions made to {@link net.minecraft.item.Item}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ItemExtensions {
	/**
	 * Makes this item placeable as the specified block.
	 * <p>
	 * Replaces {@link net.minecraft.item.BlockItem} in the case of an already existing item.
	 *
	 * @param block the block
	 * @param requireSneaking {@code true} if sneaking is required to place the block, or {@code false} otherwise
	 */
	void makePlaceable(Block block, boolean requireSneaking);
}
