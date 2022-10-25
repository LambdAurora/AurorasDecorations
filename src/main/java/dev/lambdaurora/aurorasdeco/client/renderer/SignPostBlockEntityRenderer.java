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

package dev.lambdaurora.aurorasdeco.client.renderer;

import com.mojang.blaze3d.texture.NativeImage;
import dev.lambdaurora.aurorasdeco.block.entity.SignPostBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public class SignPostBlockEntityRenderer implements BlockEntityRenderer<SignPostBlockEntity> {
	private static final int RENDER_DISTANCE = MathHelper.square(16);
	private static final int GLOWING_BLACK_COLOR = 0xfff0ebcc;
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final TextRenderer textRenderer;

	public SignPostBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		this.textRenderer = ctx.getTextRenderer();
	}

	@Override
	public int getRenderDistance() {
		return 128;
	}

	@Override
	public void render(SignPostBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, int overlay) {
		var upData = entity.getUp();
		var downData = entity.getDown();

		if (upData != null || downData != null) {
			matrices.push();
			matrices.translate(0.5, 0.5, 0.5); // Center

			if (upData != null)
				this.renderSign(entity, upData, 5 / 16.f, matrices, vertexConsumers, light, overlay);

			if (downData != null)
				this.renderSign(entity, downData, -3 / 16.f, matrices, vertexConsumers, light, overlay);
			matrices.pop();
		}
	}

	private void renderSign(SignPostBlockEntity entity, SignPostBlockEntity.Sign sign, float yOffset,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, int overlay) {
		matrices.push();

		matrices.translate(0, yOffset, 0);

		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(sign.getYaw() - 90));

		matrices.push();
		if (!sign.isLeft()) {
			matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(180));
		} else
			matrices.translate(0, 0, 5 / 16.0);

		matrices.translate(-2 / 16.0, -.5 / 16.0, -2 / 16.0);
		this.client.getItemRenderer().renderItem(null, new ItemStack(sign.getSign()), ModelTransformation.Mode.FIXED,
				false, matrices, vertexConsumers,
				entity.getWorld(), light, overlay, 0);
		matrices.pop();

		int color = getColor(sign);
		boolean glowing = sign.isGlowing();
		boolean shouldRenderGlow = this.shouldRender(entity, color) && glowing;
		{
			int textLight = glowing ? LightmapTextureManager.MAX_LIGHT_COORDINATE : light;
			int backgroundColor = color;
			if (glowing) {
				color = sign.getColor().getSignColor();
			}

			matrices.translate(0.03125 * (sign.isLeft() ? -1 : 1), 0, 0.195);
			matrices.scale(0.010416667f, -0.010416667f, 0.010416667f);

			var list = this.textRenderer.wrapLines(sign.getText(), 90);
			var text = list.isEmpty() ? OrderedText.EMPTY : list.get(0);
			float x = -this.textRenderer.getWidth(text) / 2.f;
			if (shouldRenderGlow) {
				this.textRenderer.drawWithOutline(text, x, 0, color, backgroundColor, matrices.peek().getModel(), vertexConsumers, textLight);
			} else {
				this.textRenderer.draw(text, x, 0, color, false, matrices.peek().getModel(), vertexConsumers,
						false, 0, textLight);
			}
		}

		matrices.pop();
	}

	public static int getColor(SignPostBlockEntity.Sign sign) {
		int signColor = sign.getColor().getSignColor();
		// Why is it darkened?
		double d = 0.7;
		int red = (int) (NativeImage.getRed(signColor) * d);
		int green = (int) (NativeImage.getGreen(signColor) * d);
		int blue = (int) (NativeImage.getBlue(signColor) * d);
		return signColor == DyeColor.BLACK.getSignColor() && sign.isGlowing()
				? GLOWING_BLACK_COLOR
				: NativeImage.getAbgrColor(0, blue, green, red);
	}

	private boolean shouldRender(SignPostBlockEntity sign, int signColor) {
		if (signColor == DyeColor.BLACK.getSignColor()) {
			return true;
		} else {
			var player = this.client.player;
			if (player != null && this.client.options.getPerspective().isFirstPerson() && player.isUsingSpyglass()) {
				return true;
			} else {
				var camera = this.client.getCameraEntity();
				return camera != null && camera.squaredDistanceTo(Vec3d.ofCenter(sign.getPos())) < (double) RENDER_DISTANCE;
			}
		}
	}
}
