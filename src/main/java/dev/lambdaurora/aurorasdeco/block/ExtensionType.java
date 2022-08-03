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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.WorldView;

import static net.minecraft.block.Block.createCuboidShape;

/**
 * Represents an extension type for blocks placed on horizontal sides which can also be placed on fences or walls.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ExtensionType implements StringIdentifiable {
	NONE("none", 0),
	WALL("wall", 2),
	FENCE("fence", 4);

	private final String name;
	private final int offset;

	ExtensionType(String name, int offset) {
		this.name = name;
		this.offset = offset;
	}

	public int getOffset() {
		return this.offset;
	}

	@Override
	public String asString() {
		return this.name;
	}

	public static final Box FENCE_SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
			.getBoundingBox();
	public static final Box WALL_SHAPE = createCuboidShape(7.0, 0.0, 7.0, 12.0, 16.0, 12.0)
			.getBoundingBox();

	/**
	 * Returns the extension value.
	 *
	 * @param state the block to connect to
	 * @param pos the position of the block to connect to
	 * @param world the world
	 * @return the extension value
	 */
	public static ExtensionType getExtensionValue(BlockState state, BlockPos pos, WorldView world) {
		var block = state.getBlock();

		var shape = state.getSidesShape(world, pos);
		if (shape != VoxelShapes.empty()) {
			var box = shape.getBoundingBox();
			if (block instanceof FenceBlock || state.isIn(BlockTags.FENCES) || AuroraUtil.isShapeEqual(FENCE_SHAPE, box))
				return FENCE;
			if (block instanceof WallBlock || state.isIn(BlockTags.WALLS) || AuroraUtil.isShapeEqual(WALL_SHAPE, box))
				return WALL;
		}

		return NONE;
	}
}
