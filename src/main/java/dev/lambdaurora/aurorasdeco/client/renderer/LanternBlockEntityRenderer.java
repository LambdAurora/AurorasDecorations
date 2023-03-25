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

import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.block.entity.LanternBlockEntity;
import dev.lambdaurora.aurorasdeco.hook.LBGHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.random.LegacySimpleRandom;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.random.RandomSeed;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public class LanternBlockEntityRenderer extends SwayingBlockEntityRenderer<LanternBlockEntity> {
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final RandomGenerator random = new LegacySimpleRandom(RandomSeed.generateUniqueSeed());

	public LanternBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

	@Override
	public int getRenderDistance() {
		return 128;
	}

	@Override
	public void render(LanternBlockEntity lantern, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, int overlay) {
		var pos = lantern.getPos();

		float pitch = 0.0F;
		float roll = 0.0F;
		float angle = MathHelper.lerp(tickDelta, lantern.prevAngle, lantern.angle);
		lantern.prevAngle = angle;
		if ((lantern.isSwinging() || lantern.isColliding()) && lantern.getSwingBaseDirection() != null) {
			switch (lantern.getSwingBaseDirection()) {
				case NORTH -> pitch = -angle;
				case SOUTH -> pitch = angle;
				case EAST -> roll = -angle;
				case WEST -> roll = angle;
			}
		} else {
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
		int lanternRotation = switch (facing) {
			case NORTH -> 90;
			case EAST -> 180;
			case SOUTH -> 270;
			default -> 0;
		};

		int extension = lantern.getCachedState().get(WallLanternBlock.EXTENSION).getOffset();
		matrices.translate((-facing.getOffsetX() * extension) / 16.f,
				0.f,
				(-facing.getOffsetZ() * extension) / 16.f);

		matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(lanternRotation));

		var lanternShape = lanternState.getOutlineShape(lantern.getWorld(), pos);
		var lanternShapeMaxY = lanternShape.getMax(Direction.Axis.Y);
		var lanternShapeMinY = lanternShape.getMin(Direction.Axis.Y);
		var size = lanternShapeMaxY - lanternShapeMinY;
		matrices.translate(-8.f / 16.f, -1.f / 16.f - size, -8.f / 16.f);

		LBGHooks.pushDisableBetterLayer();
		this.client.getBlockRenderManager().renderBlock(lanternState, pos, lantern.getWorld(), matrices, consumer,
				false, this.random);
		LBGHooks.popDisableBetterLayer();
		matrices.pop();
	}
}
