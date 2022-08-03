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

package dev.lambdaurora.aurorasdeco.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/**
 * Represents a feature whose goal is to place a patch of something onto a liquid surface in a way it looks like a cluster.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AquaticSurfacePatchFeature extends Feature<AquaticSurfacePatchFeature.Config> {
	public AquaticSurfacePatchFeature(Codec<Config> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeatureContext<Config> context) {
		var config = context.getConfig();
		var random = context.getRandom();

		var pos = context.getOrigin().mutableCopy();
		pos.move(0, config.yOffset(), 0);

		boolean success = false;

		for (int circle = 0; circle < config.circleAmount(); circle++) {
			success |= this.generateCircle(context, pos);

			pos.move(this.pickNextSpread(random, config.spread()), 0, this.pickNextSpread(random, config.spread()));
		}

		return success;
	}

	/**
	 * Generates one of the patch "circles".
	 *
	 * @param context the feature context
	 * @param origin the origin of this "circle"
	 * @return {@code true} if the circle successfully generated, or {@code false} otherwise
	 */
	private boolean generateCircle(FeatureContext<Config> context, BlockPos origin) {
		var config = context.getConfig();
		StructureWorldAccess world = context.getWorld();
		var random = context.getRandom();

		int radius = config.radius().get(random);
		int radiusSquared = radius * radius;
		int completeRadius = radius + 3;
		int completeRadiusSquared = completeRadius * completeRadius;

		var pos = origin.mutableCopy();
		boolean success = false;

		for (int iX = -completeRadius; iX <= completeRadius; iX++) {
			int dZ = (int) Math.sqrt(completeRadiusSquared - iX * iX);

			for (int iZ = -dZ; iZ <= dZ; iZ++) {
				pos.set(origin.getX() + iX, origin.getY(), origin.getZ() + iZ);
				var state = config.toPlace().getBlockState(random, pos);

				if (iX * iX + iZ * iZ > radiusSquared) { // We're in the outer border, it's additions
					if (random.nextFloat() < config.additionFactor() && state.canPlaceAt(world, pos)) {
						world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
						success = true;
					}
				} else if (random.nextFloat() > config.removalFactor() && state.canPlaceAt(world, pos)) {
					// We're inside and we passed the removal check.
					world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
					success = true;
				}
			}
		}

		return success;
	}

	private int pickNextSpread(RandomGenerator random, int spread) {
		return random.nextInt(spread) - random.nextInt(spread);
	}

	public record Config(BlockStateProvider toPlace, int circleAmount, IntProvider radius, int spread, int yOffset, float removalFactor, float additionFactor)
			implements FeatureConfig {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
						BlockStateProvider.TYPE_CODEC.fieldOf("to_place").forGetter(Config::toPlace),
						Codec.INT.fieldOf("circle_amount").orElse(1).forGetter(Config::circleAmount),
						IntProvider.POSITIVE_CODEC.fieldOf("radius").forGetter(Config::radius),
						Codec.INT.fieldOf("spread").forGetter(Config::spread),
						Codec.INT.fieldOf("y_offset").orElse(0).forGetter(Config::yOffset),
						Codec.FLOAT.fieldOf("removal_factor").forGetter(Config::removalFactor),
						Codec.FLOAT.fieldOf("addition_factor").forGetter(Config::additionFactor)
				).apply(instance, Config::new)
		);
	}
}
