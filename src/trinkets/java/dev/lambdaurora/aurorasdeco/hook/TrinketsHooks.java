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

package dev.lambdaurora.aurorasdeco.hook;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import static dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry.*;

/**
 * Represents hooks for Trinkets.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
public final class TrinketsHooks implements ClientModInitializer {
	private static final boolean HAS_TRINKETS = QuiltLoader.isModLoaded("trinkets");

	@Override
	public void onInitializeClient(ModContainer mod) {
		init(
				BLACKBOARD_BLOCK.asItem(), WAXED_BLACKBOARD_BLOCK.asItem(),
				CHALKBOARD_BLOCK.asItem(), WAXED_CHALKBOARD_BLOCK.asItem(),
				GLASSBOARD_BLOCK.asItem(), WAXED_GLASSBOARD_BLOCK.asItem()
		);
	}

	public static void init(Item... blackboards) {
		if (!HAS_TRINKETS)
			return;

		for (var item : blackboards)
			TrinketRendererRegistry.registerRenderer(item, TrinketsHooks::renderBlackboardInTrinketSlot);
	}

	private static void renderBlackboardInTrinketSlot(ItemStack stack, SlotReference slotReference,
			EntityModel<? extends LivingEntity> contextModel,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			LivingEntity entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress,
			float headYaw, float headPitch) {
		if (!slotReference.inventory().getSlotType().getGroup().equals("head"))
			return;

		boolean villager = entity instanceof VillagerEntity || entity instanceof ZombieVillagerEntity;
		if (entity.isBaby() && !(entity instanceof VillagerEntity)) {
			matrices.translate(0.0, 0.03125, 0.0);
			matrices.scale(.7f, .7f, .7f);
			matrices.translate(0.0, 1.0, 0.0);
		}
		if (contextModel instanceof ModelWithHead withHead)
			withHead.getHead().rotate(matrices);

		HeadFeatureRenderer.translate(matrices, villager);
		MinecraftClient.getInstance().getEntityRenderDispatcher().getHeldItemRenderer().renderItem(entity, stack, ModelTransformationMode.HEAD,
				false,
				matrices, vertexConsumers, light);
	}
}
