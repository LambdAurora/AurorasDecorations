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

package dev.lambdaurora.aurorasdeco.client.model;

import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BakedBlackboardModel extends ForwardingBakedModel {
    private final Sprite white;

    public BakedBlackboardModel(BakedModel baseModel, Sprite white) {
        this.wrapped = baseModel;
        this.white = white;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        Object attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        if (attachment instanceof Blackboard && white != null) {
            Blackboard blackboard = (Blackboard) attachment;
            Direction facing = state.get(BlackboardBlock.FACING);

            boolean lit = blackboard.isLit();
            int light = LightmapTextureManager.pack(15, 15);
            if (!lit) {
                light = WorldRenderer.getLightmapCoordinates(blockView, pos);
            }

            RenderMaterial material = RendererAccess.INSTANCE.getRenderer().materialFinder()
                    .disableDiffuse(0, true)
                    .disableAo(0, true)
                    .find();
            QuadEmitter emitter = context.getEmitter();
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int color = blackboard.getColor(x, y);
                    if (color != 0) {
                        {
                            int red = color & 255;
                            int green = (color >> 8) & 255;
                            int blue = (color >> 16) & 255;
                            color = 0xff000000 | (red << 16) | (green << 8) | blue;
                        }

                        int squareY = 15 - y;
                        emitter.square(facing, x / 16.f, squareY / 16.f,
                                (x + 1) / 16.f, (squareY + 1) / 16.f, 0.925f)
                                .spriteBake(0, this.white, MutableQuadView.BAKE_LOCK_UV)
                                .spriteColor(0, color, color, color, color)
                                .lightmap(light, light, light, light)
                                .material(material)
                                .emit();
                    }
                }
            }
        }
    }
}
