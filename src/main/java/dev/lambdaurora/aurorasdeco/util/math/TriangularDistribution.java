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

package dev.lambdaurora.aurorasdeco.util.math;

import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Represents a triangular distribution.
 */
public final class TriangularDistribution {
	private final float a, b, c;
	private final Random random;
	private final float f;

	public TriangularDistribution(float a, float b, float c, @Nullable Random random) {
		if (!(a < b) || !(a <= c && c <= b))
			throw new IllegalArgumentException("Parameters should be a <= b <= c, had a=" + a + " b=" + b + " c=" + c);

		this.a = a;
		this.b = b;
		this.c = c;
		this.f = (c - a) / (b - a);
		this.random = random == null ? new Random() : random;
	}

	public float sample() {
		float u = this.random.nextFloat();
		if (u < this.f) return this.a + (float) Math.sqrt(u * (this.b - this.a) * (this.c - this.a));
		return this.b - (float) Math.sqrt((1 - u) * (this.b - this.a) * (this.b - this.c));
	}
}
