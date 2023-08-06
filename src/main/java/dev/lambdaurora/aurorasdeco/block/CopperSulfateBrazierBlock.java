/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.block.behavior.CopperSulfateBehavior;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.MapColor;
import net.minecraft.particle.ParticleEffect;

/**
 * Represents a copper sulfate brazier brazier.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class CopperSulfateBrazierBlock extends BrazierBlock {
	public CopperSulfateBrazierBlock(MapColor color, int fireDamage, int luminance, ParticleEffect particle) {
		super(FabricBlockSettings.create().ticksRandomly(), color, fireDamage, luminance, particle);
		this.tickComponent = new CopperSulfateBehavior(20);
	}
}
