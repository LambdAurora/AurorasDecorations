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

package dev.lambdaurora.aurorasdeco.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class AmethystGlintParticle extends SpriteBillboardParticle {
	protected AmethystGlintParticle(ClientWorld clientWorld, double x, double y, double z,
	                                double velocityX, double velocityY, double velocityZ) {
		super(clientWorld, x, y, z, velocityX, velocityY, velocityZ);
		this.collidesWithWorld = false;

		this.velocityX = 0.f;
		this.velocityY *= 0.15f;
		this.velocityZ = 0.f;

		this.setBoundingBoxSpacing(0.01F, 0.01F);
		this.scale *= this.random.nextFloat() * 0.4F + 0.7F;
		this.maxAge = 40;
	}

	@Override
	protected int getBrightness(float tint) {
		return 0xf000f0;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
	}

	public record Factory(SpriteProvider spriteProvider) implements ParticleFactory<DefaultParticleType> {
		@Override
		public Particle createParticle(DefaultParticleType parameters, ClientWorld clientWorld, double x, double y, double z,
		                               double velocityX, double velocityY, double velocityZ) {
			var random = clientWorld.random;
			var particle = new AmethystGlintParticle(clientWorld, x, y, z,
					0.f, random.nextDouble() * -0.1, 0.f);
			particle.setSprite(this.spriteProvider());
			return particle;
		}
	}
}
