/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.client.AurorasDecoClient;
import dev.lambdaurora.aurorasdeco.client.BlackboardTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;

/**
 * Represents the dynamic item renderer of blackboards.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BlackboardItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private final ModelIdentifier modelId;

	public BlackboardItemRenderer(ModelIdentifier modelId) {
		this.modelId = modelId;
	}

	@Override
	public void render(ItemStack stack, Mode mode, MatrixStack matrices,
	                   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		var model = MinecraftClient.getInstance().getBakedModelManager().getModel(this.modelId);

		matrices.push();

		matrices.translate(0.5, 0.5, 0.5);
		boolean leftHanded = mode == Mode.THIRD_PERSON_LEFT_HAND;
		MinecraftClient.getInstance().getItemRenderer().renderItem(stack, mode,
				leftHanded,
				matrices, vertexConsumers, light, overlay, model);
		if (mode == Mode.HEAD) {
			var maskModel = MinecraftClient.getInstance().getBakedModelManager().getModel(AurorasDecoClient.BLACKBOARD_MASK);
			MinecraftClient.getInstance().getItemRenderer().renderItem(stack, mode,
					false, matrices, vertexConsumers, light, overlay, maskModel);
		}

		var nbt = BlockItem.getBlockEntityNbt(stack);
		if (nbt != null && nbt.contains("pixels", NbtElement.BYTE_ARRAY_TYPE)) {
			float z = .933f;
			if (mode == Mode.HEAD) {
				matrices.translate(0.5, 0.5, z);
				matrices.scale(-1, -1, 1);
			} else if (mode == Mode.GUI) {
				matrices.translate(0.27, -0.08, 0);
				matrices.scale(-1, -1, 1);
			} else if (mode == Mode.GROUND) {
				matrices.translate(0.125, 0.5, 0.23333333);
				matrices.scale(-1, -1, 1);
			} else if (mode != Mode.THIRD_PERSON_RIGHT_HAND && mode != Mode.THIRD_PERSON_LEFT_HAND && !mode.isFirstPerson()) {
				matrices.scale(-1, -1, 1);
			}

			model.getTransformation().getTransformation(mode).apply(leftHanded, matrices);
			matrices.translate(0, 0, -0.5);

			if (mode == Mode.THIRD_PERSON_RIGHT_HAND || mode == Mode.THIRD_PERSON_LEFT_HAND || mode.isFirstPerson()) {
				matrices.translate(0.5, 0.5, z);
				matrices.scale(-1, -1, 1);
			}

			var blackboard = Blackboard.fromNbt(nbt);
			BlackboardTexture.fromBlackboard(blackboard)
					.render(matrices.peek().getPositionMatrix(), vertexConsumers, blackboard.isLit() ? LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE : light);
		}

		matrices.pop();
	}
}
