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

package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.accessor.PointOfInterestTypeExtensions;
import net.minecraft.block.BlockState;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mixin(PointOfInterestType.class)
public class PointOfInterestTypeMixin implements PointOfInterestTypeExtensions {
	@Mutable
	@Shadow
	@Final
	private Set<BlockState> blockStates;

	@Override
	public void aurorasdeco$addBlockStates(Collection<BlockState> states) {
		var set = new HashSet<>(this.blockStates);
		set.addAll(states);
		this.blockStates = Set.copyOf(set);
	}
}
