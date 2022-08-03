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
import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/**
 * Represents a simple feature for placing plant-like blocks.
 * <p>
 * This feature is very similar to the {@link net.minecraft.world.gen.feature.SimpleBlockFeature}
 * but with some tweaks around the placement of tall plant blocks to allow placement of waterlogged plants.
 * Also adds a configuration option to require air above the plant.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SimplePlantFeature extends Feature<SimplePlantFeature.Config> {
	public SimplePlantFeature(Codec<Config> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeatureContext<Config> context) {
		Config config = context.getConfig();
		StructureWorldAccess world = context.getWorld();
		BlockPos pos = context.getOrigin();
		BlockState state = config.toPlace().getBlockState(context.getRandom(), pos);

		var above = pos.mutableCopy().move(Direction.UP);

		if (state.canPlaceAt(world, pos)) {
			if (state.getBlock() instanceof TallPlantBlock) {
				if (!state.getProperties().contains(Properties.WATERLOGGED) && !world.isAir(above)) {
					return false;
				}

				if (config.shouldHaveAirAbove() && !world.isAir(above.move(Direction.UP))) {
					return false;
				}

				TallPlantBlock.placeAt(world, state, pos, Block.NOTIFY_LISTENERS);
			} else {
				if (config.shouldHaveAirAbove() && !world.isAir(above)) {
					return false;
				}

				world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
			}

			return true;
		} else
			return false;
	}

	public record Config(BlockStateProvider toPlace, boolean shouldHaveAirAbove) implements FeatureConfig {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(
				instance -> instance.group(
						BlockStateProvider.TYPE_CODEC.fieldOf("to_place").forGetter(Config::toPlace),
						Codec.BOOL.fieldOf("should_have_air_above").forGetter(Config::shouldHaveAirAbove)
				).apply(instance, Config::new)
		);
	}
}
