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

public final class SmoothNoise {
	private final int tickInterval;
	private final Float2FloatFunction nextNoise;
	private float leftNoise;
	private float rightNoise;
	private int ticks = 0;
	private float t;

	/**
	 * Smoothly goes from 0 to 1 when t increases from 0 and 1. Defined for t in [0, 1].
	 */
	public static float smoothStep(float t) {
		return t * t * (3 - 2 * t);
	}

	public SmoothNoise(int tickInterval, float initial, Float2FloatFunction nextNoise) {
		if (tickInterval < 1)
			throw new IllegalArgumentException("Tick interval must be greater or equal to 1.");

		this.tickInterval = tickInterval;
		this.nextNoise = nextNoise;
		this.leftNoise = initial;
		this.rightNoise = nextNoise.apply(this.leftNoise);
	}

	/**
	 * {@return the smooth interpolation between left and right noise values}
	 */
	public float getNoise() {
		return this.leftNoise + smoothStep(this.t) * (this.rightNoise - this.leftNoise);
	}

	/**
	 * {@return the linear interpolation between left and right noise values}
	 */
	public float getLerp() {
		return this.leftNoise + this.t * (this.rightNoise - this.leftNoise);
	}

	public void tick() {
		this.ticks++;

		if (this.ticks == this.tickInterval) {
			this.ticks = 0;
			this.leftNoise = this.rightNoise;
			this.rightNoise = this.nextNoise.apply(this.leftNoise);
		}

		this.t = this.ticks / (float) this.tickInterval;
	}
}
