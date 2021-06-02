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

import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.block.entity.LanternBlockEntity;
import dev.lambdaurora.aurorasdeco.hook.LBGHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class LanternBlockEntityRenderer extends SwayingBlockEntityRenderer<LanternBlockEntity> {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Random random = new Random();

    public LanternBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(LanternBlockEntity lantern, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                       int light, int overlay) {
        var pos = lantern.getPos();
        boolean fluid = !lantern.getCachedState().getFluidState().isEmpty();
        float ticks = (float) lantern.swingTicks + tickDelta;

        if (lantern.isColliding() && ticks > 4) {
            ticks = 4.f;
        }
        if (fluid)
            ticks /= 2.f;

        float pitch = 0.0F;
        float roll = 0.0F;
        if (lantern.isSwinging() || lantern.isColliding()) {
            float angle = MathHelper.sin(ticks / (float) Math.PI) / (4.f + ticks / 3.f);
            if (lantern.lastSideHit == Direction.NORTH) {
                pitch = -angle;
            } else if (lantern.lastSideHit == Direction.SOUTH) {
                pitch = angle;
            } else if (lantern.lastSideHit == Direction.EAST) {
                roll = -angle;
            } else if (lantern.lastSideHit == Direction.WEST) {
                roll = angle;
            }
        } else {
            float angle = this.getNaturalSwayingAngle(lantern, tickDelta);
            if (lantern.getCachedState().get(WallLanternBlock.FACING).getAxis() == Direction.Axis.Z) roll = angle;
            else pitch = angle;
        }

        var lanternState = lantern.getLanternState();
        var consumer = vertexConsumers.getBuffer(RenderLayers.getBlockLayer(lanternState));
        matrices.push();

        matrices.translate(8.f / 16.f, 12.f / 16.f, 8.f / 16.f);
        if (roll != 0.f)
            matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(roll));
        if (pitch != 0.f)
            matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(pitch));

        var facing = lantern.getCachedState().get(WallLanternBlock.FACING);
        int angle = switch (facing) {
            case NORTH -> 90;
            case EAST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };

        int extension = lantern.getCachedState().get(WallLanternBlock.EXTENSION).getOffset();
        matrices.translate((-facing.getOffsetX() * extension) / 16.f,
                0.f,
                (-facing.getOffsetZ() * extension) / 16.f);

        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(angle));

        matrices.translate(-8.f / 16.f, -10.f / 16.f, -8.f / 16.f);

        LBGHooks.pushDisableBetterLayer();
        this.client.getBlockRenderManager().renderBlock(lanternState, pos, lantern.getWorld(), matrices, consumer,
                false, this.random);
        LBGHooks.popDisableBetterLayer();
        matrices.pop();
    }
}
