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

package dev.lambdaurora.aurorasdeco.world.gen.feature;

import com.mojang.serialization.Lifecycle;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.world.TreeConfiguredFeaturesAccessor;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.world.gen.feature.config.FallenTreeFeatureConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.feature.util.ConfiguredFeatureUtil;
import net.minecraft.world.gen.foliage.RandomSpreadFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;
import net.minecraft.world.gen.treedecorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.trunk.BendingTrunkPlacer;

import java.util.List;
import java.util.OptionalInt;

public final class AurorasDecoTreeConfiguredFeatures {
	private AurorasDecoTreeConfiguredFeatures() {
		throw new UnsupportedOperationException("AurorasDecoTreeConfiguredFeatures only contains static definitions.");
	}

	private static final BeehiveTreeDecorator BEES_015 = new BeehiveTreeDecorator(0.15f);

	public static final ConfiguredFeature<TreeFeatureConfig, ?> OAK_BEES_015 = ConfiguredFeatureUtil.register(
			AurorasDeco.id("oak_bees_15").toString(),
			Feature.TREE.configure(TreeConfiguredFeaturesAccessor.invokeOak().decorators(List.of(BEES_015)).build())
	);

	public static final ConfiguredFeature<TreeFeatureConfig, ?> FANCY_OAK_BEES_015 = ConfiguredFeatureUtil.register(
			AurorasDeco.id("fancy_oak_bees_15").toString(),
			Feature.TREE.configure(TreeConfiguredFeaturesAccessor.invokeFancyOak().decorators(List.of(BEES_015)).build())
	);

	public static final ConfiguredFeature<TreeFeatureConfig, ?> BIRCH_BEES_015 = ConfiguredFeatureUtil.register(
			AurorasDeco.id("birch_bees_15").toString(),
			Feature.TREE.configure(TreeConfiguredFeaturesAccessor.invokeBirch().decorators(List.of(BEES_015)).build())
	);

	public static final ConfiguredFeature<?, ?> AZALEA_TREE = replace( // I am sorry, but the mixin injection was just too hard.
			new Identifier("azalea_tree"),
			Feature.TREE
					.configure(
							(new TreeFeatureConfig.Builder(
									new WeightedBlockStateProvider(
											DataPool.<BlockState>builder()
													.add(AurorasDecoRegistry.AZALEA_LOG_BLOCK.getDefaultState(), 2)
													.add(AurorasDecoRegistry.FLOWERING_AZALEA_LOG_BLOCK.getDefaultState(), 1)
									),
									new BendingTrunkPlacer(4, 2, 0, 3, UniformIntProvider.create(1, 2)),
									new WeightedBlockStateProvider(
											DataPool.<BlockState>builder()
													.add(Blocks.AZALEA_LEAVES.getDefaultState(), 3)
													.add(Blocks.FLOWERING_AZALEA_LEAVES.getDefaultState(), 1)
									),
									new RandomSpreadFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), ConstantIntProvider.create(2), 50),
									new TwoLayersFeatureSize(1, 0, 1)
							))
									.dirtProvider(BlockStateProvider.of(Blocks.ROOTED_DIRT))
									.forceDirt()
									.build()
					)
	);

	/* Fallen Trees */

	public static final ConfiguredFeature<FallenTreeFeatureConfig, ?> FALLEN_OAK_TREE = ConfiguredFeatureUtil.register(
			AurorasDeco.id("fallen_oak_tree").toString(),
			AurorasDecoFeatures.FALLEN_TREE.configure(FallenTreeFeatureConfig.builder(BlockStateProvider.of(Blocks.OAK_LOG))
					.layerProvider(FallenTreeFeatureConfig.LayerType.MOSS)
					.build()
			)
	);

	public static final ConfiguredFeature<FallenTreeFeatureConfig, ?> FALLEN_BIRCH_TREE = ConfiguredFeatureUtil.register(
			AurorasDeco.id("fallen_birch_tree").toString(),
			AurorasDecoFeatures.FALLEN_TREE.configure(FallenTreeFeatureConfig.builder(BlockStateProvider.of(Blocks.BIRCH_LOG))
					.baseHeight(4)
					.layerProvider(FallenTreeFeatureConfig.LayerType.MOSS)
					.build()
			)
	);

	public static final ConfiguredFeature<FallenTreeFeatureConfig, ?> FALLEN_SPRUCE_TREE = ConfiguredFeatureUtil.register(
			AurorasDeco.id("fallen_spruce_tree").toString(),
			AurorasDecoFeatures.FALLEN_TREE.configure(FallenTreeFeatureConfig.builder(BlockStateProvider.of(Blocks.SPRUCE_LOG))
					.baseHeight(5)
					.variance(4)
					.layerProvider(FallenTreeFeatureConfig.LayerType.MOSS)
					.build()
			)
	);

	public static final ConfiguredFeature<FallenTreeFeatureConfig, ?> SNOWY_FALLEN_SPRUCE_TREE = ConfiguredFeatureUtil.register(
			AurorasDeco.id("snowy_fallen_spruce_tree").toString(),
			AurorasDecoFeatures.FALLEN_TREE.configure(FallenTreeFeatureConfig.builder(BlockStateProvider.of(Blocks.SPRUCE_LOG))
					.baseHeight(4)
					.variance(3)
					.layerProvider(FallenTreeFeatureConfig.LayerType.SNOW)
					.noMushrooms()
					.build()
			)
	);

	public static final ConfiguredFeature<FallenTreeFeatureConfig, ?> FALLEN_JUNGLE_TREE = ConfiguredFeatureUtil.register(
			AurorasDeco.id("fallen_jungle_tree").toString(),
			AurorasDecoFeatures.FALLEN_TREE.configure(FallenTreeFeatureConfig.builder(BlockStateProvider.of(Blocks.JUNGLE_LOG))
					.baseHeight(3)
					.variance(6)
					.layerProvider(FallenTreeFeatureConfig.LayerType.MOSS)
					.noMushrooms()
					.build()
			)
	);

	public static final ConfiguredFeature<FallenTreeFeatureConfig, ?> SAVANNA_FALLEN_OAK_TREE = ConfiguredFeatureUtil.register(
			AurorasDeco.id("savanna_fallen_oak_tree").toString(),
			AurorasDecoFeatures.FALLEN_TREE.configure(FallenTreeFeatureConfig.builder(BlockStateProvider.of(Blocks.OAK_LOG))
					.noMushrooms()
					.build()
			)
	);

	private static ConfiguredFeature<?, ?> replace(Identifier id, ConfiguredFeature<?, ?> feature) {
		((MutableRegistry<ConfiguredFeature<?, ?>>) BuiltinRegistries.CONFIGURED_FEATURE)
				.replace(
						OptionalInt.empty(),
						RegistryKey.of(BuiltinRegistries.CONFIGURED_FEATURE.getKey(), id),
						feature,
						Lifecycle.stable()
				);

		return feature;
	}

	static {
		TreeConfiguredFeaturesAccessor.setAzaleaTree(AZALEA_TREE);
	}
}
