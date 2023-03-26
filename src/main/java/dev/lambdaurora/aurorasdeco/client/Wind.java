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

package dev.lambdaurora.aurorasdeco.client;

import dev.lambdaurora.aurorasdeco.util.math.SmoothNoise;
import dev.lambdaurora.aurorasdeco.util.math.TriangularDistribution;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;
import java.util.Random;

/**
 * Simulates wind.
 * <p>
 * All credits go to Fourmisain who wrote this code for Falling Leaves.
 * https://github.com/RandomMcSomethin/fallingleaves/blob/main/src/main/java/randommcsomethin/fallingleaves/util/Wind.java
 */
@ClientOnly
public class Wind {
	public static final long WIND_SEED = 0xa4505a;
	private static final Random RANDOM = new Random(WIND_SEED);
	private static Wind INSTANCE = new Wind();

	private float windX;
	private float windZ;
	private SmoothNoise velocityNoise;
	private SmoothNoise directionTrendNoise;
	private SmoothNoise directionNoise;

	private boolean wasRaining;
	private boolean wasThundering;
	private State state;
	private State originalState;
	private int stateDuration; // Ticks

	public static Wind get() {
		return INSTANCE;
	}

	public static void use(Wind wind) {
		INSTANCE = wind;
	}

	protected Wind() {
		this.reset();
	}

	public void reset() {
		this.state = State.CALM;
		this.stateDuration = 0;

		this.wasRaining = this.wasThundering = false;

		this.windX = this.windZ = 0;

		this.velocityNoise = new SmoothNoise(2 * 20, 0, old -> this.state.getVelocityDistribution().sample());
		this.directionTrendNoise = new SmoothNoise(30 * 60 * 20, RANDOM.nextFloat() * MathHelper.TAU,
				old -> RANDOM.nextFloat() * MathHelper.TAU
		);
		this.directionNoise = new SmoothNoise(10 * 20, 0, old -> (2f * RANDOM.nextFloat() - 1f) * MathHelper.TAU / 8f);
	}

	public float getWindX() {
		return this.windX;
	}

	public float getWindZ() {
		return this.windZ;
	}

	public State getState() {
		return this.state;
	}

	private void tickState(ClientWorld world) {
		this.stateDuration--;

		DimensionType dimensionType = world.getDimension();

		if (!dimensionType.natural() || dimensionType.hasCeiling()) {
			this.originalState = this.state;

			if (dimensionType.ultraWarm()) {
				// Nether-like
				this.state = State.WINDY;
			} else {
				// There's no wind
				this.state = State.CALM;
			}

			return;
		}

		if (this.originalState != null) {
			this.state = this.originalState;
			this.originalState = null;
		}

		boolean raining = world.getProperties().isRaining();
		boolean thundering = world.isThundering();
		boolean weatherChanged = this.wasRaining != raining || this.wasThundering != thundering;

		if (weatherChanged || this.stateDuration <= 0) {
			if (thundering) {
				state = State.STORMY;
			} else {
				// Windy and stormy when raining, calm and windy otherwise.
				int index = RANDOM.nextInt(2);
				state = State.VALUES.get(raining ? index + 1 : index);
			}

			this.stateDuration = 6 * 60 * 20; // Change state every 6 minutes.
		}

		this.wasRaining = raining;
		this.wasThundering = thundering;
	}

	public void tick(ClientWorld world) {
		this.tickState(world);

		this.velocityNoise.tick();
		this.directionTrendNoise.tick();
		this.directionNoise.tick();

		float strength = this.velocityNoise.getNoise();
		float direction = this.directionNoise.getLerp() + this.directionNoise.getNoise();

		this.windX = strength * MathHelper.cos(direction);
		this.windZ = strength * MathHelper.sin(direction);
	}

	public enum State {
		CALM(0.05f, 0.05f, 0.2f),
		WINDY(0.05f, 0.3f, 0.7f),
		STORMY(0.05f, 0.6f, 1.1f);

		public static final List<State> VALUES = List.of(values());

		private final TriangularDistribution velocityDistribution;

		State(float minSpeed, float likelySpeed, float maxSpeed) {
			this.velocityDistribution = new TriangularDistribution(minSpeed, maxSpeed, likelySpeed, RANDOM);
		}

		public TriangularDistribution getVelocityDistribution() {
			return this.velocityDistribution;
		}
	}
}
