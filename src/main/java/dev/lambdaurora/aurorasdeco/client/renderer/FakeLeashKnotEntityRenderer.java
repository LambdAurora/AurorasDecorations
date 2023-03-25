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

import dev.lambdaurora.aurorasdeco.entity.FakeLeashKnotEntity;
import dev.lambdaurora.aurorasdeco.mixin.client.MobEntityRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LeashKnotEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public class FakeLeashKnotEntityRenderer
		extends MobEntityRenderer<FakeLeashKnotEntity, LeashKnotEntityModel<FakeLeashKnotEntity>> {
	private static final Identifier TEXTURE = new Identifier("textures/entity/lead_knot.png");

	public FakeLeashKnotEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new LeashKnotEntityModel<>(context.getPart(EntityModelLayers.LEASH_KNOT)), 1.f);
		this.shadowRadius = 0.f;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(FakeLeashKnotEntity fakeLeashKnot, float f, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.scale(-1.f, -1.f, 1.f);

		this.scale(fakeLeashKnot, matrices, tickDelta);

		var client = MinecraftClient.getInstance();
		boolean visible = this.isVisible(fakeLeashKnot);
		boolean translucent = !visible && !fakeLeashKnot.isInvisibleTo(client.player);
		boolean outline = client.hasOutline(fakeLeashKnot);
		var renderLayer = this.getRenderLayer(fakeLeashKnot, visible, translucent, outline);
		if (renderLayer != null) {
			var vertices = vertexConsumers.getBuffer(renderLayer);
			int overlay = getOverlay(fakeLeashKnot, this.getAnimationCounter(fakeLeashKnot, tickDelta));
			this.model.render(matrices, vertices, light, overlay, 1.f, 1.f, 1.f, translucent ? .15f : 1.f);
		}

		matrices.pop();

		var holding = fakeLeashKnot.getHoldingEntity();
		if (holding != null) {
			((MobEntityRendererAccessor<FakeLeashKnotEntity>) this).aurorasdeco$renderLeash(
					fakeLeashKnot, tickDelta, matrices, vertexConsumers, holding
			);
		}
	}

	@Override
	public Identifier getTexture(FakeLeashKnotEntity entity) {
		return TEXTURE;
	}
}
