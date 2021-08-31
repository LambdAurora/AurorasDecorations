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
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class WindChimeBlockEntityRenderer implements BlockEntityRenderer<WindChimeBlockEntity> {
    public static final EntityModelLayer WIND_CHIME_MODEL_LAYER = new EntityModelLayer(AurorasDeco.id("wind_chime"),
            "main");
    public static final SpriteIdentifier WIND_CHIME_TEXTURE = new SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            AurorasDeco.id("block/wind_chime"));

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

        addChime(root, 1, 10.f, 5.f, 10.f);
        addChime(root, 2, 9.f, 5.f, 7.f);
        addChime(root, 3, 8.f, 8.f, 5.f);
        addChime(root, 4, 6.f, 11.f, 6.f);
        addChime(root, 5, 9.f, 11.f, 9.f);
        addChime(root, 6, 7.f, 8.f, 11.f);

        return TexturedModelData.of(modelData, 16, 16);
    }

    private static void addChime(ModelPartData root, int number, float size, float x, float z) {
        var chimeBody = root.addChild("chime" + number + "_body", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-1.f, -(2.f + size), -1.f, 2.f, size, 2.f),
                ModelTransform.pivot(x, 12.f, z));
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
        float ticks = (float) entity.ticks + tickDelta;
        if (!entity.isColliding())
            ticks = 0.f;
        float angle = MathHelper.sin(ticks / (float) Math.PI) / (4.f + ticks / 3.f);

        this.chimes.forEach(model -> {
            model.pitch = angle;
            model.roll = angle;
        });

        this.root.render(matrices, WIND_CHIME_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid),
                light, overlay);
    }
}
