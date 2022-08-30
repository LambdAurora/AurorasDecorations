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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.entity.WindChimeBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class WindChimeBlockEntityRenderer implements BlockEntityRenderer<WindChimeBlockEntity> {
	public static final EntityModelLayer WIND_CHIME_MODEL_LAYER = new EntityModelLayer(AurorasDeco.id("wind_chime"),
			"main");
	public static final SpriteIdentifier WIND_CHIME_TEXTURE = new SpriteIdentifier(
			PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
			AurorasDeco.id("block/wind_chime"));

	private static final float EASTERN_CHIME_X = 11.f;
	private static final float WESTERN_CHIME_X = 5.f;

	private static final List<ChimeData> CHIMES = List.of(
			new ChimeData(0, WESTERN_CHIME_X, 10, 10),
			new ChimeData(1, WESTERN_CHIME_X, 7, 9),
			new ChimeData(2, 8, 5, 8),
			new ChimeData(3, EASTERN_CHIME_X, 6, 6),
			new ChimeData(4, EASTERN_CHIME_X, 9, 9),
			new ChimeData(5, 8, 11, 7)
	);

	private final ModelPart root;
	private final List<ModelPart> chimes = new ArrayList<>();

	public WindChimeBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		this.root = ctx.getLayerModelPart(WIND_CHIME_MODEL_LAYER);

		for (int i = 0; i < 6; i++) {
			chimes.add(this.root.getChild("chime" + (i + 1) + "_body"));
		}
	}

	public static TexturedModelData getTexturedModelData() {
		var modelData = new ModelData();
		var root = modelData.getRoot();

		for (var chime : CHIMES) {
			addChime(root, chime);
		}

		return TexturedModelData.of(modelData, 16, 16);
	}

	private static void addChime(ModelPartData root, ChimeData chime) {
		var chimeBody = root.addChild("chime" + (chime.index() + 1) + "_body", ModelPartBuilder.create()
						.uv(0, 0)
						.cuboid(-1.f, -(2.f + chime.size()), -1.f, 2.f, chime.size(), 2.f),
				ModelTransform.pivot(chime.x(), 12.f, chime.z()));
		addString(chimeBody);
	}

	private static void addString(ModelPartData parent) {
		parent.addChild("string", ModelPartBuilder.create()
						.uv(8, 2)
						.cuboid(-.5f, -2.f, -.5f, 1.f, 2.f, 1.f),
				ModelTransform.NONE);
	}

	@Override
	public void render(WindChimeBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, int overlay) {
		this.chimes.forEach(model -> {
			model.pitch = entity.getPitch(tickDelta);
			model.roll = entity.getRoll(tickDelta);
		});
		CHIMES.forEach(chime -> chime.apply(this, entity.getPitch(tickDelta), entity.getRoll(tickDelta)));

		this.root.render(matrices, WIND_CHIME_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid),
				light, overlay);
	}

	record ChimeData(int index, float x, float z, float size) {
		float getDistanceX() {
			return 8 - this.x();
		}

		float getDistanceZ() {
			return 8 - this.z();
		}

		void apply(WindChimeBlockEntityRenderer renderer, float pitch, float roll) {
			var model = renderer.chimes.get(this.index());

			float distanceZ = this.getDistanceZ();
			if (distanceZ < 0 && pitch > 0 || distanceZ > 0 && pitch < 0) {
				pitch -= (pitch * pitch * distanceZ / 8.f) / pitch;
			}
			model.pitch = pitch;

			float distanceX = this.getDistanceX();

			if (distanceX == 0) {
				if (roll > 0) {
					distanceX = 2;
				} else {
					distanceX = -2;
				}
			}

			if (distanceX < 0 && roll < 0 || distanceX > 0 && roll > 0) {
				roll += (roll * roll * distanceX / 8.f) / roll;
			}
			model.roll = roll;
		}
	}
}
