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

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.stateprovider.ForestFlowerBlockStateProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ForestFlowerBlockStateProvider.class)
public interface ForestFlowerBlockStateProviderAccessor {
	@Accessor("FLOWERS")
	static BlockState[] getFlowers() {
		throw new UnsupportedOperationException("Mixin injection failed.");
	}

	@Mutable
	@Accessor("FLOWERS")
	static void setFlowers(BlockState[] flowers) {
		throw new UnsupportedOperationException("Mixin injection failed.");
	}
}
