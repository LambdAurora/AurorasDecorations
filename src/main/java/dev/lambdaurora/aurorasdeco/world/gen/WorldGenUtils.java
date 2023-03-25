/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.world.gen;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

/**
 * Represents some utilities for world generation.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.12
 * @since 1.0.0-beta.12
 */
public class WorldGenUtils {
	/**
	 * Generates one of the patch "circles".
	 *
	 * @param world the world this circle is generated in
	 * @param random the random generator
	 * @param origin the origin of this "circle"
	 * @param radius the radius of this circle
	 * @param stateProvider the block state provider for the blocks composing this circle
	 * @param additionFactor the chance for the circle generator to place blocks outside the circle radius but inside the square radius
	 * @param removalFactor the chance for the circle generator to skip blocks inside the circle
	 * @return {@code true} if the circle successfully generated, or {@code false} otherwise
	 */
	public static boolean generateCircle(StructureWorldAccess world, RandomGenerator random, BlockPos origin, int radius,
			BlockStateProvider stateProvider, float additionFactor, float removalFactor) {
		return generateCircle(world, random, origin, radius, stateProvider, additionFactor, removalFactor, PositionModifier.NOOP);
	}

	/**
	 * Generates one of the patch "circles".
	 *
	 * @param world the world this circle is generated in
	 * @param random the random generator
	 * @param origin the origin of this "circle"
	 * @param radius the radius of this circle
	 * @param stateProvider the block state provider for the blocks composing this circle
	 * @param additionFactor the chance for the circle generator to place blocks outside the circle radius but inside the square radius
	 * @param removalFactor the chance for the circle generator to skip blocks inside the circle
	 * @param positionModifier the modifier for the position
	 * @return {@code true} if the circle successfully generated, or {@code false} otherwise
	 */
	public static boolean generateCircle(StructureWorldAccess world, RandomGenerator random, BlockPos origin, int radius,
			BlockStateProvider stateProvider, float additionFactor, float removalFactor, PositionModifier positionModifier) {
		int radiusSquared = radius * radius;
		int completeRadius = radius + 3;
		int completeRadiusSquared = completeRadius * completeRadius;

		var pos = origin.mutableCopy();
		boolean success = false;

		for (int iX = -completeRadius; iX <= completeRadius; iX++) {
			int dZ = (int) Math.sqrt(completeRadiusSquared - iX * iX);

			for (int iZ = -dZ; iZ <= dZ; iZ++) {
				if ((Math.abs(iX) == completeRadius && Math.abs(iZ) == completeRadius)
						|| (additionFactor <= 0.f && Math.abs(iX) == radius && Math.abs(iZ) == radius)) continue;

				pos.set(origin.getX() + iX, origin.getY(), origin.getZ() + iZ);

				boolean outsideCircle = iX * iX + iZ * iZ > radiusSquared;
				boolean shouldPlace = outsideCircle ? additionFactor <= 0.f && random.nextFloat() < additionFactor
						: random.nextFloat() > removalFactor;

				if (shouldPlace) {
					pos = positionModifier.getPosition(pos);
					var state = stateProvider.getBlockState(random, pos);

					if (state.canPlaceAt(world, pos)) {
						world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
						success = true;
					}
				}
			}
		}

		return success;
	}

	public static int pickNextSpread(RandomGenerator random, int spread) {
		return random.nextInt(spread) - random.nextInt(spread);
	}

	public interface PositionModifier {
		BlockPos.Mutable getPosition(BlockPos.Mutable start);

		PositionModifier NOOP = (start) -> start;
	}
}
