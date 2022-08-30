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

package dev.lambdaurora.aurorasdeco.world.gen.foliage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.world.FoliagePlacerTypeAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

import java.util.function.BiConsumer;

public class JacarandaFoliagePlacer extends FoliagePlacer {
	public static final Codec<JacarandaFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> fillFoliagePlacerFields(instance).apply(instance, JacarandaFoliagePlacer::new));
	public static final FoliagePlacerType<JacarandaFoliagePlacer> TYPE = Registry.register(Registry.FOLIAGE_PLACER_TYPE,
			AurorasDeco.id("jacaranda"),
			FoliagePlacerTypeAccessor.create(CODEC)
	);

	public JacarandaFoliagePlacer(IntProvider fakeRadius, IntProvider fakeOffset) {
		super(ConstantIntProvider.create(2), ConstantIntProvider.ZERO);
	}

	@Override
	protected FoliagePlacerType<?> getType() {
		return TYPE;
	}

	@Override
	protected void generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, RandomGenerator random, TreeFeatureConfig config, int trunkHeight, TreeNode treeNode, int foliageHeight, int radius, int offset) {
		this.addCanopy(world, replacer, treeNode.getCenter(), config, treeNode.isGiantTrunk(), random);
	}

	@Override
	public int getRandomHeight(RandomGenerator random, int trunkHeight, TreeFeatureConfig config) {
		return 0;
	}

	@Override
	protected boolean isInvalidForLeaves(RandomGenerator random, int dx, int y, int dz, int radius, boolean giantTrunk) {
		if (y == -1 || y == 2) {
			return dx < -1 || dx > 1 || dz < -1 || dz > 1;
		} else if (y == 0 || y == 1) {
			return dx < -2 || dx > 2 || dz < -2 || dz > 2;
		} else {
			return true;
		}
	}

	private void addCanopy(TestableWorld world,
			BiConsumer<BlockPos, BlockState> replacer,
			BlockPos centerPos,
			TreeFeatureConfig config, boolean giantTrunk,
			RandomGenerator random) {
		var pos = new BlockPos.Mutable();
		cir1(world, replacer, centerPos, -1, pos, config, giantTrunk, random);
		cir2(world, replacer, centerPos, 0, pos, config, giantTrunk, random);
		cir2(world, replacer, centerPos, 1, pos, config, giantTrunk, random);
		cir1(world, replacer, centerPos, 2, pos, config, giantTrunk, random);
	}

	private void cir1(TestableWorld world,
			BiConsumer<BlockPos, BlockState> replacer,
			BlockPos centerPos,
			int y, BlockPos.Mutable mutablePos,
			TreeFeatureConfig config, boolean giantTrunk,
			RandomGenerator random) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				//if (Math.abs(x) != 1 || Math.abs(z) != 1 || rand.nextBoolean()) {
				if (!this.isPositionInvalid(random, x, y, z, 1, giantTrunk)) {
					mutablePos.set(centerPos, x, y, z);
					placeFoliageBlock(world, replacer, random, config, mutablePos);
				}
			}
		}
	}

	private void cir2(TestableWorld world,
			BiConsumer<BlockPos, BlockState> replacer,
			BlockPos centerPos,
			int y, BlockPos.Mutable mutablePos,
			TreeFeatureConfig config, boolean giantTrunk,
			RandomGenerator random) {
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				//if (Math.abs(x) != 1 || Math.abs(z) != 1 || rand.nextBoolean()) {
				if (Math.abs(x) == 2 && Math.abs(z) == 2 && random.nextBoolean())
					continue;

				if (!this.isPositionInvalid(random, x, y, z, 1, giantTrunk)) {
					mutablePos.set(centerPos, x, y, z);
					placeFoliageBlock(world, replacer, random, config, mutablePos);
				}
			}
		}
	}
}
