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

package dev.lambdaurora.aurorasdeco.client.renderer;

import dev.lambdaurora.aurorasdeco.block.entity.BookPileBlockEntity;
import dev.lambdaurora.aurorasdeco.client.RenderRule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Axis;

import java.util.Random;

/**
 * Represents the book pile block entity renderer.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BookPileEntityRenderer implements BlockEntityRenderer<BookPileBlockEntity> {
	public BookPileEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

	@Override
	public void render(BookPileBlockEntity bookPile, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, int overlay) {
		long seed = bookPile.getPos().asLong();

		var random = new Random(seed);

		var renderer = MinecraftClient.getInstance().getItemRenderer();

		matrices.push();
		int i = 0;
		for (var stack : bookPile.getBooks()) {
			if (stack.isEmpty())
				continue;

			var model = RenderRule.getModel(stack, bookPile.getCachedState(), bookPile.getWorld(), seed + i * 20L);
			matrices.push();

			// Do the random rotation first on the Y axis.
			{
				matrices.translate(.5, 0, .5);
				int angle = random.nextInt(360);
				matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(angle));
				matrices.translate(-.5, 0, -.5);
			}

			// Makes the book lay on the floor.
			matrices.translate(.5, .025, .5);
			matrices.multiply(Axis.Z_POSITIVE.rotationDegrees(90));
			matrices.translate(3 / 16.f - .15, 0, 0);
			matrices.scale(.45f, .45f, .45f);

			if (model.hasDepth()) {
				matrices.translate(0, -0.2, 0);
			}

			renderer.renderItem(stack,
					ModelTransformationMode.FIXED, false,
					matrices, vertexConsumers,
					light, overlay,
					model);
			matrices.pop();

			// Translation for new book.
			matrices.translate(0, 0.12, 0);

			i++;
		}
		matrices.pop();
	}
}
