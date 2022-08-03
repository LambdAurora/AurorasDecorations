/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
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
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.random.LegacySimpleRandom;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.random.RandomSeed;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.BiConsumer;

public class BlackboardPressBlockEntityRenderer implements BlockEntityRenderer<BlackboardPressBlockEntity> {
	public static final Identifier PRESS_PLATE_ID = AurorasDeco.id("blockstates/blackboard_press/press_plate.json");
	public static final Identifier SCREW_ID = AurorasDeco.id("blockstates/blackboard_press/screw.json");
	public static final ModelIdentifier PRESS_PLATE_MODEL_ID = new ModelIdentifier(AurorasDeco.id("blackboard_press/press_plate"), "special");
	public static final ModelIdentifier SCREW_MODEL_ID = new ModelIdentifier(AurorasDeco.id("blackboard_press/screw"), "special");
	private static final RandomGenerator RANDOM = new LegacySimpleRandom(RandomSeed.generateUniqueSeed());
	private final MinecraftClient client = MinecraftClient.getInstance();

	public BlackboardPressBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

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
				matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-(entity.getWorld().getTime() % 360)));
				matrices.translate(-0.5, 0, -0.5);

				client.getBlockRenderManager().getModelRenderer().render(entity.getWorld(), screwModel, state, pos,
						matrices, vertexConsumers.getBuffer(RenderLayer.getSolid()), true, RANDOM, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);

				matrices.pop();
			}

			matrices.pop();
		}
	}

	public static void initModels(ResourceManager resourceManager, ModelVariantMap.DeserializationContext deserializationContext,
	                              BiConsumer<Identifier, UnbakedModel> modelRegister) {
		var stateFactory = deserializationContext.getStateFactory();
		deserializationContext.setStateFactory(AurorasDecoRegistry.BLACKBOARD_PRESS_BLOCK.getStateManager());

		initModel(PRESS_PLATE_ID, PRESS_PLATE_MODEL_ID, resourceManager, deserializationContext, modelRegister);
		initModel(SCREW_ID, SCREW_MODEL_ID, resourceManager, deserializationContext, modelRegister);

		deserializationContext.setStateFactory(stateFactory);
	}

	private static void initModel(Identifier resourceId, Identifier modelId, ResourceManager resourceManager,
	                              ModelVariantMap.DeserializationContext deserializationContext, BiConsumer<Identifier, UnbakedModel> modelRegister) {
		resourceManager.getResource(resourceId).ifPresentOrElse(resource -> {
			try (var reader = new InputStreamReader(resource.open())) {
				var map = ModelVariantMap.fromJson(deserializationContext, reader);
				modelRegister.accept(modelId,
						new UnbakedVariantModel<>(AurorasDecoRegistry.BLACKBOARD_PRESS_BLOCK, map.getVariantMap(), List.of(BlackboardPressBlock.WATERLOGGED))
				);
			} catch (IOException e) {
				AurorasDeco.warn("Failed to load the blackboard \"{}\" model.", modelId, e);
			}
		}, () -> AurorasDeco.warn("Failed to load the blackboard \"{}\" model: missing file.", modelId));
	}
}
