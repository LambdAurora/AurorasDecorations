/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.registry;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;

/**
 * Represents the registered particles in Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoParticles {
	private AurorasDecoParticles() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}

	public static final DefaultParticleType AMETHYST_GLINT = register("amethyst_glint");
	public static final DefaultParticleType COPPER_SULFATE_FLAME = register("copper_sulfate_flame");
	public static final DefaultParticleType COPPER_SULFATE_LAVA = register("copper_sulfate_lava");
	public static final DefaultParticleType LAVENDER_PETAL = register("lavender_petal");

	private static DefaultParticleType register(String name) {
		return Registry.register(Registry.PARTICLE_TYPE, AurorasDeco.id(name), FabricParticleTypes.simple());
	}
}
