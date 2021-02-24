/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.client.tooltip;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.client.renderer.BlackboardBlockEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

/**
 * Represents the blackboard tooltip component. Displays the blackboard's contents.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BlackboardTooltipComponent implements TooltipComponent, TooltipData {
    private static final Identifier LOCK_ICON_TEXTURE = new Identifier("textures/gui/container/cartography_table.png");
    private static final Identifier GLOW_TEXTURE = AurorasDeco.id("textures/gui/glowing_sprite.png");

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final BlackboardBlockEntityRenderer.BlackboardTexture texture;
    private final RenderLayer background;
    private final boolean glowing;
    private final boolean locked;

    public BlackboardTooltipComponent(String background, byte[] pixels, boolean glowing, boolean locked) {
        this.texture = BlackboardBlockEntityRenderer.getOrCreateTexture();
        this.background = RenderLayer.getText(AurorasDeco.id("textures/block/" + background + ".png"));
        ;
        this.glowing = glowing;
        this.locked = locked;
        this.texture.update(pixels);
    }

    @Override
    public int getHeight() {
        return 128 + 2;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return 128;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices,
                          ItemRenderer itemRenderer, int z, TextureManager textureManager) {
        VertexConsumerProvider.Immediate vertexConsumers = this.client.getBufferBuilders().getEntityVertexConsumers();
        matrices.push();
        matrices.translate(x, y, z);
        matrices.scale(128.f, 128.f, 1);

        int light = 15728880;
        Matrix4f model = matrices.peek().getModel();

        this.quad(this.background, 0.f, 0.f, 1.f, 1.f, model, vertexConsumers, light);

        this.texture.render(model, vertexConsumers, light);

        if (this.glowing) {
            matrices.push();
            matrices.translate(0, 0, 1);
            model = matrices.peek().getModel();

            RenderLayer glow = RenderLayer.getText(GLOW_TEXTURE);

            float speed = 600.f;
            float offset = ((System.currentTimeMillis() % (int) speed) / speed);

            offset *= 4.f;
            offset = (float) (Math.floor(offset) / 4.f);

            this.quad(glow, 0.f, offset, 1.f, offset + (0.25f), model, vertexConsumers, light);

            matrices.pop();
        }

        if (this.locked) {
            matrices.translate(.5f, .5f, 1);
            matrices.scale(.5f, .5f, 1.f);
            model = matrices.peek().getModel();
            RenderLayer locked = RenderLayer.getText(LOCK_ICON_TEXTURE);
            this.quad(locked, 0.f, .6484375f, .2421875f, .890625f, model, vertexConsumers, light);
        }

        vertexConsumers.draw();
        matrices.pop();
        this.texture.pop();
    }

    private void quad(RenderLayer renderLayer, float uMin, float vMin, float uMax, float vMax,
                      Matrix4f model, VertexConsumerProvider vertexConsumers, int light) {
        VertexConsumer vertices = vertexConsumers.getBuffer(renderLayer);
        vertices.vertex(model, 0.f, 1.f, 0.f).color(255, 255, 255, 255)
                .texture(uMin, vMax).light(light).next();
        vertices.vertex(model, 1.f, 1.f, 0.f).color(255, 255, 255, 255)
                .texture(uMax, vMax).light(light).next();
        vertices.vertex(model, 1.f, 0.f, 0.f).color(255, 255, 255, 255)
                .texture(uMax, vMin).light(light).next();
        vertices.vertex(model, 0.f, 0.f, 0.f).color(255, 255, 255, 255)
                .texture(uMin, vMin).light(light).next();
    }
}
