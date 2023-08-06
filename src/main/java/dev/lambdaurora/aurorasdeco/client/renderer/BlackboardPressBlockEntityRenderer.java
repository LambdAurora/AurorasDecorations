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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.BlackboardPressBlock;
import dev.lambdaurora.aurorasdeco.block.entity.BlackboardPressBlockEntity;
import dev.lambdaurora.aurorasdeco.client.model.UnbakedVariantModel;
import dev.lambdaurora.aurorasdeco.mixin.client.ModelLoaderAccessor;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.LegacySimpleRandom;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.random.RandomSeed;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class BlackboardPressBlockEntityRenderer implements BlockEntityRenderer<BlackboardPressBlockEntity> {
	public static final Identifier PRESS_PLATE_ID = AurorasDeco.id("blockstates/blackboard_press/press_plate.json");
	public static final Identifier SCREW_ID = AurorasDeco.id("blockstates/blackboard_press/screw.json");
	public static final ModelIdentifier PRESS_PLATE_MODEL_ID = new ModelIdentifier(AurorasDeco.id("blackboard_press/press_plate"), "special");
	public static final ModelIdentifier SCREW_MODEL_ID = new ModelIdentifier(AurorasDeco.id("blackboard_press/screw"), "special");
	private static final RandomGenerator RANDOM = new LegacySimpleRandom(RandomSeed.generateUniqueSeed());
	private final MinecraftClient client = MinecraftClient.getInstance();

	public BlackboardPressBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

	@Override
	public void render(BlackboardPressBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if (!entity.hasWorld()) return;

		BlockState state = entity.getCachedState();
		BlockPos pos = entity.getPos();

		var pressPlateModel = client.getBakedModelManager().getModel(PRESS_PLATE_MODEL_ID);
		var screwModel = client.getBakedModelManager().getModel(SCREW_MODEL_ID);

		{
			matrices.push();

			// Woooo,,,, witness the WIP/debug code,,,,, -Lavender
			long aaa = 6 - entity.getWorld().getTime() % 12;
			if (aaa > 0) {
				aaa = -aaa;
			}
			matrices.translate(0, aaa / 32.f, 0);

			client.getBlockRenderManager().getModelRenderer().render(entity.getWorld(), pressPlateModel, state, pos,
					matrices, vertexConsumers.getBuffer(RenderLayer.getSolid()), true, RANDOM, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);

			{
				matrices.push();

				matrices.translate(0.5, 0, 0.5);
				matrices.multiply(Axis.Y_POSITIVE.rotationDegrees(-(entity.getWorld().getTime() % 360)));
				matrices.translate(-0.5, 0, -0.5);

				client.getBlockRenderManager().getModelRenderer().render(entity.getWorld(), screwModel, state, pos,
						matrices, vertexConsumers.getBuffer(RenderLayer.getSolid()), true, RANDOM, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);

				matrices.pop();
			}

			matrices.pop();
		}
	}

	public static void initModels(ModelLoadingPlugin.Context context) {
		boolean[] firstRun = {true};

		context.modifyModelOnLoad().register((model, ctx) -> {
			if (firstRun[0]) {
				firstRun[0] = false;

				var modelLoader = (ModelLoaderAccessor) ctx.loader();

				var pressModel = initModel(PRESS_PLATE_ID, PRESS_PLATE_MODEL_ID);
				modelLoader.invokePutModel(PRESS_PLATE_MODEL_ID, pressModel);
				modelLoader.getModelsToBake().put(PRESS_PLATE_MODEL_ID, pressModel);

				var screwModel = initModel(SCREW_ID, SCREW_MODEL_ID);
				modelLoader.invokePutModel(SCREW_MODEL_ID, screwModel);
				modelLoader.getModelsToBake().put(SCREW_MODEL_ID, screwModel);
			}

			return model;
		});
	}

	private static UnbakedModel initModel(Identifier resourceId, Identifier modelId) {
		var model = MinecraftClient.getInstance().getResourceManager().getResource(resourceId).map(resource -> {
			try (var reader = new InputStreamReader(resource.open())) {
				var context = new ModelVariantMap.DeserializationContext();
				context.setStateFactory(AurorasDecoRegistry.BLACKBOARD_PRESS_BLOCK.getStateManager());
				var map = ModelVariantMap.fromJson(context, reader);
				return new UnbakedVariantModel<>(AurorasDecoRegistry.BLACKBOARD_PRESS_BLOCK, map.getVariantMap(), List.of(BlackboardPressBlock.WATERLOGGED));
			} catch (IOException e) {
				AurorasDeco.warn("Failed to load the blackboard \"{}\" model.", modelId, e);
				return null;
			}
		});

		if (model.isEmpty()) {
			AurorasDeco.warn("Failed to load the blackboard \"{}\" model: missing file.", modelId);
			return null;
		} else {
			return model.get();
		}
	}
}
