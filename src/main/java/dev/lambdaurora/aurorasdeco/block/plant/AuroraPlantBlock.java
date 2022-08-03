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

package dev.lambdaurora.aurorasdeco.block.plant;

import net.minecraft.block.PlantBlock;

/**
 * Represents a {@link PlantBlock} with extra methods for better integration.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AuroraPlantBlock extends PlantBlock {
	public AuroraPlantBlock(Settings settings) {
		super(settings);
	}

	public boolean canBePotted() {
		return true;
	}
}
