/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.client.renderer;

import dev.lambdaurora.aurorasdeco.block.ShelfBlock;
import dev.lambdaurora.aurorasdeco.block.entity.ShelfBlockEntity;
import dev.lambdaurora.aurorasdeco.client.RenderRule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

/**
 * Represents the shelf block entity renderer.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShelfBlockEntityRenderer implements BlockEntityRenderer<ShelfBlockEntity> {
	public ShelfBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(ShelfBlockEntity shelf, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
	                   int light, int overlay) {
		var facing = shelf.getCachedState().get(ShelfBlock.FACING);

		matrices.push();
		matrices.translate(0.5, 0.8, 0.5);
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(facing.asRotation()));

		if (facing.getAxis() == Direction.Axis.Z) {
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
		}
		matrices.translate(0.35, 0, 0.38);

		matrices.scale(.24f, .24f, .24f);

		var renderer = MinecraftClient.getInstance().getItemRenderer();

		for (int y = 0; y < 2; y++) {
			if (y != 0) {
				matrices.translate(0, -.5 / .24, 0);
			}

			matrices.push();
			for (int x = 0; x < 4; x++) {
				if (x != 0) {
					matrices.translate(-1.f, 0.f, 0.f);
				}

				var stack = shelf.getStack(x + y * 4);
				if (stack.isEmpty())
					continue;

				var model = RenderRule.getModel(stack, shelf.getCachedState(), shelf.getWorld(), 0);

				matrices.push();
				if (model.hasDepth()) {
					matrices.translate(0, -0.2, 0);
				}

				renderer.renderItem(stack,
						ModelTransformation.Mode.FIXED, false,
						matrices, vertexConsumers,
						light, overlay,
						model);
				matrices.pop();
			}
			matrices.pop();
		}

		matrices.pop();
	}
}
