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

package dev.lambdaurora.aurorasdeco.client.model;

import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;
import java.util.function.Supplier;

@ClientOnly
public class BakedGlassboardModel extends BakedBlackboardModel {
	private final Int2ObjectMap<List<BakedModel>> models;

	public BakedGlassboardModel(BakedModel baseModel, Int2ObjectMap<List<BakedModel>> models) {
		super(baseModel);
		this.models = models;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	private int isBlockSame(BlockRenderView blockView, BlockPos pos, BlockState state, int mask) {
		BlockState neighborState = blockView.getBlockState(pos);
		if (neighborState.isOf(state.getBlock())) {
			if (neighborState.get(BlackboardBlock.FACING) == state.get(BlackboardBlock.FACING)) {
				return mask;
			}
		}

		return 0;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		var facing = state.get(BlackboardBlock.FACING);
		int mask = 0;
		BlockPos.Mutable neighborPos = pos.mutableCopy();

		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.LEFT_MASK);
		this.move(neighborPos, facing, Direction.UP);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.LEFT_UP_MASK);
		this.move(neighborPos, facing, Direction.EAST);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.UP_MASK);
		this.move(neighborPos, facing, Direction.EAST);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.RIGHT_UP_MASK);
		this.move(neighborPos, facing, Direction.DOWN);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.RIGHT_MASK);
		this.move(neighborPos, facing, Direction.DOWN);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.RIGHT_DOWN_MASK);
		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.DOWN_MASK);
		this.move(neighborPos, facing, Direction.WEST);
		mask |= this.isBlockSame(blockView, neighborPos, state, UnbakedGlassboardModel.LEFT_DOWN_MASK);

		final int fixedMask = mask;

		context.pushTransform(quad -> {
			var cullFace = quad.cullFace();
			if (cullFace != null) {
				var adjacentPos = pos.offset(cullFace);
				return !blockView.getBlockState(adjacentPos).isIn(AurorasDecoTags.GLASSBOARD_BLOCKS); // Force the culling.
			}

			return true;
		});

		this.models.get(fixedMask).forEach(context.bakedModelConsumer());

		context.popTransform();

		this.emitBlockMesh(blockView, pos, context);

		context.pushTransform(quad -> {
			quad.nominalFace(quad.lightFace().rotateYClockwise());
			Direction direction = quad.lightFace();
			Vec3f quadPos = new Vec3f();

			float leftValue;
			float rightValue;

			if (direction.getAxis() == Direction.Axis.Z) {
				quad.copyPos(0, quadPos);
				leftValue = quadPos.getX();
				quad.copyPos(2, quadPos);
				rightValue = quadPos.getX();
			} else if (direction.getAxis() == Direction.Axis.X) {
				quad.copyPos(0, quadPos);
				leftValue = quadPos.getZ();
				quad.copyPos(2, quadPos);
				rightValue = quadPos.getZ();
			} else {
				leftValue = rightValue = 0;
			}

			for (int i = 0; i < 4; i++) {
				quad.copyPos(i, quadPos);

				if (direction.getAxis() == Direction.Axis.Z) {
					quad.pos(i, i < 2 ? rightValue : leftValue, quadPos.getY(), quadPos.getZ());
				} else if (direction.getAxis() == Direction.Axis.X) {
					quad.pos(i, quadPos.getX(), quadPos.getY(), i < 2 ? rightValue : leftValue);
				}
			}
			return true;
		});

		this.emitBlockMesh(blockView, pos, context);
		context.popTransform();
	}

	private void move(BlockPos.Mutable pos, Direction facing, Direction direction) {
		if (facing.getAxis().isHorizontal()) {
			if (direction.getAxis().isHorizontal()) {
				pos.move(direction == Direction.WEST ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
			} else {
				pos.move(direction);
			}
		} else {
			pos.move(switch (direction) {
				case UP -> Direction.NORTH;
				case DOWN -> Direction.SOUTH;
				default -> direction;
			});
		}
	}
}
