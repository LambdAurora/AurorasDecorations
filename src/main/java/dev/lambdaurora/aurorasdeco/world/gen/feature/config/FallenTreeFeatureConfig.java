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

package dev.lambdaurora.aurorasdeco.world.gen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.DataPool;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

import java.util.List;
import java.util.Optional;

/**
 * Represents the configuration of a {@link dev.lambdaurora.aurorasdeco.world.gen.feature.FallenTreeFeature}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record FallenTreeFeatureConfig(
		BlockStateProvider trunkProvider,
		int baseLength,
		int variance,
		LayerType layerType,
		BlockStateProvider vineProvider,
		BlockStateProvider mushroomProvider
) implements FeatureConfig {
	public static final Codec<FallenTreeFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockStateProvider.TYPE_CODEC.fieldOf("trunk_provider").forGetter(FallenTreeFeatureConfig::trunkProvider),
					Codec.INT.fieldOf("base_length").forGetter(FallenTreeFeatureConfig::baseLength),
					Codec.INT.fieldOf("variance").forGetter(FallenTreeFeatureConfig::variance),
					LayerType.CODEC.fieldOf("layer").forGetter(FallenTreeFeatureConfig::layerType),
					BlockStateProvider.TYPE_CODEC.fieldOf("vine_provider").forGetter(FallenTreeFeatureConfig::vineProvider),
					BlockStateProvider.TYPE_CODEC.fieldOf("mushroom_provider").forGetter(FallenTreeFeatureConfig::mushroomProvider)
			)
			.apply(instance, FallenTreeFeatureConfig::new)
	);

	public enum LayerType {
		SNOW(Blocks.SNOW),
		MOSS(Blocks.MOSS_CARPET),
		NONE(Blocks.AIR);

		private static final List<LayerType> LAYER_TYPES = List.of(values());
		private static final Codec<LayerType> CODEC = new PrimitiveCodec<>() {
			@Override
			public <T> DataResult<LayerType> read(DynamicOps<T> ops, T input) {
				return ops.getStringValue(input).map(id -> byId(id).orElse(NONE));
			}

			@Override
			public <T> T write(DynamicOps<T> ops, LayerType value) {
				return ops.createString(value.name());
			}

			@Override
			public String toString() {
				return "FallenTreeFeatureConfig$LayerType";
			}
		};
		private final Block block;

		LayerType(Block block) {
			this.block = block;
		}

		public Block getBlock() {
			return this.block;
		}

		public static Optional<LayerType> byId(String id) {
			return LAYER_TYPES.stream().filter(mode -> mode.name().equalsIgnoreCase(id)).findFirst();
		}
	}

	public static Builder builder(BlockStateProvider trunkProvider) {
		return new Builder(trunkProvider);
	}

	public static class Builder {
		private final BlockStateProvider trunkProvider;
		private int baseHeight = 3;
		private int variance = 2;
		private LayerType layerType = LayerType.NONE;
		private BlockStateProvider vineProvider = new WeightedBlockStateProvider(DataPool.<BlockState>builder()
				.add(Blocks.VINE.getDefaultState(), 5)
				.add(Blocks.GLOW_LICHEN.getDefaultState(), 3)
		);
		private BlockStateProvider mushroomProvider = new WeightedBlockStateProvider(DataPool.<BlockState>builder()
				.add(Blocks.BROWN_MUSHROOM.getDefaultState(), 5)
				.add(Blocks.RED_MUSHROOM.getDefaultState(), 2)
		);

		public Builder(BlockStateProvider trunkProvider) {
			this.trunkProvider = trunkProvider;
		}

		public Builder baseHeight(int baseHeight) {
			this.baseHeight = baseHeight;
			return this;
		}

		public Builder variance(int variance) {
			this.variance = variance;
			return this;
		}

		public Builder layerProvider(LayerType layerType) {
			this.layerType = layerType;
			return this;
		}

		public Builder vineProvider(BlockStateProvider vineProvider) {
			this.vineProvider = vineProvider;
			return this;
		}

		public Builder mushroomProvider(BlockStateProvider mushroomProvider) {
			this.mushroomProvider = mushroomProvider;
			return this;
		}

		public Builder noMushrooms() {
			return this.mushroomProvider(BlockStateProvider.of(Blocks.AIR));
		}

		public FallenTreeFeatureConfig build() {
			return new FallenTreeFeatureConfig(
					this.trunkProvider,
					this.baseHeight,
					this.variance,
					this.layerType,
					this.vineProvider,
					this.mushroomProvider
			);
		}
	}
}
