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
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.block.entity.BlackboardBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Represents the blackboard block entity renderer.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BlackboardBlockEntityRenderer implements BlockEntityRenderer<BlackboardBlockEntity> {
    private static final Deque<BlackboardTexture> CACHED_RENDER_LAYERS = new ArrayDeque<>();

    public BlackboardBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {

    }

    @Override
    public void render(BlackboardBlockEntity blackboard, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = blackboard.getCachedState();

        if (state.get(BlackboardBlock.LIT)) {
            light = 15728880;
        }

        matrices.translate(0.5, 0.5, 0.5);
        float angle = -state.get(BlackboardBlock.FACING).asRotation();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(angle));
        matrices.translate(-0.5, 0.5, -0.435);

        matrices.scale(1.f, -1.f, 1.f);

        BlackboardTexture texture = blackboard.getTexture();
        if (texture != null) {
            texture.render(matrices.peek().getModel(), vertexConsumers, light);
        }
    }

    public static BlackboardTexture getOrCreateTexture() {
        if (CACHED_RENDER_LAYERS.isEmpty()) {
            return new BlackboardTexture();
        } else {
            synchronized (CACHED_RENDER_LAYERS) {
                return CACHED_RENDER_LAYERS.pop();
            }
        }
    }

    public static void cacheTexture(BlackboardTexture texture) {
        synchronized (CACHED_RENDER_LAYERS) {
            CACHED_RENDER_LAYERS.push(texture);
        }
    }

    public static class BlackboardTexture {
        private final NativeImageBackedTexture texture = new NativeImageBackedTexture(16, 16, true);
        private final RenderLayer renderLayer;

        public BlackboardTexture() {
            Identifier id = MinecraftClient.getInstance().getTextureManager()
                    .registerDynamicTexture("aurorasdeco/blackboard", this.texture);
            this.renderLayer = RenderLayer.getText(id);
        }

        public void render(Matrix4f model, VertexConsumerProvider vertexConsumers, int light) {
            VertexConsumer vertices = vertexConsumers.getBuffer(this.renderLayer);
            vertices.vertex(model, 0.f, 1.f, 0.f).color(255, 255, 255, 255)
                    .texture(0.f, 1.f).light(light).next();
            vertices.vertex(model, 1.f, 1.f, 0.f).color(255, 255, 255, 255)
                    .texture(1.f, 1.f).light(light).next();
            vertices.vertex(model, 1.f, 0.f, 0.f).color(255, 255, 255, 255)
                    .texture(1.f, 0.f).light(light).next();
            vertices.vertex(model, 0.f, 0.f, 0.f).color(255, 255, 255, 255)
                    .texture(0.f, 0.f).light(light).next();
        }

        public void update(Blackboard blackboard) {
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    this.texture.getImage().setPixelColor(x, y, blackboard.getColor(x, y));
                }
            }
            this.texture.upload();
        }

        public void pop() {
            cacheTexture(this);
        }
    }
}
