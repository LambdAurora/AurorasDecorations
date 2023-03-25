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
import dev.lambdaurora.aurorasdeco.world.gen.WorldGenUtils;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/**
 * Represents a feature whose goal is to place a patch of something onto a liquid surface in a way it looks like a cluster.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.12
 * @since 1.0.0-beta.1
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
			success |= WorldGenUtils.generateCircle(context.getWorld(), random, pos, config.radius().get(random),
					config.toPlace(), config.additionFactor(), config.removalFactor());

			pos.move(WorldGenUtils.pickNextSpread(random, config.spread()), 0, WorldGenUtils.pickNextSpread(random, config.spread()));
		}

		return success;
	}

	public record Config(
			BlockStateProvider toPlace, int circleAmount, IntProvider radius, int spread, int yOffset,
			float removalFactor, float additionFactor
	) implements FeatureConfig {
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
