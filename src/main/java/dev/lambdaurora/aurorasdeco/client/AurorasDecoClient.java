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

package dev.lambdaurora.aurorasdeco.client;

import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;
import com.terraformersmc.terraform.sign.SpriteIdentifierRegistry;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.block.HangingFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.StumpBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.client.model.BakedSignPostModel;
import dev.lambdaurora.aurorasdeco.client.particle.AmethystGlintParticle;
import dev.lambdaurora.aurorasdeco.client.particle.LavenderPetalParticle;
import dev.lambdaurora.aurorasdeco.client.renderer.*;
import dev.lambdaurora.aurorasdeco.client.screen.CopperHopperScreen;
import dev.lambdaurora.aurorasdeco.client.screen.SawmillScreen;
import dev.lambdaurora.aurorasdeco.client.screen.ShelfScreen;
import dev.lambdaurora.aurorasdeco.hook.TrinketsHooks;
import dev.lambdaurora.aurorasdeco.registry.*;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

import static dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry.*;

/**
 * Represents the Aurora's Decorations client mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class AurorasDecoClient implements ClientModInitializer {
	public static final AurorasDecoPack RESOURCE_PACK = new AurorasDecoPack(ResourceType.CLIENT_RESOURCES);
	public static final ModelIdentifier BLACKBOARD_MASK = new ModelIdentifier(AurorasDeco.id("blackboard_mask"),
			"inventory");

	@Override
	public void onInitializeClient() {
		this.initBlockEntityRenderers();
		this.initEntityRenderers();
		this.initBlockRenderLayers();

		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.AMETHYST_GLINT, AmethystGlintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.COPPER_SULFATE_FLAME, FlameParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.COPPER_SULFATE_LAVA, LavaEmberParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.LAVENDER_PETAL, LavenderPetalParticle.Factory::new);

		/* Signs */
		ClientPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.SIGN_POST_OPEN_GUI, AurorasDecoPackets.Client::handleSignPostOpenGuiPacket);
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, AZALEA_SIGN_BLOCK.getTexture()));
		SpriteIdentifierRegistry.INSTANCE.addIdentifier(new SpriteIdentifier(TexturedRenderLayers.SIGNS_ATLAS_TEXTURE, JACARANDA_SIGN_BLOCK.getTexture()));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			PottedPlantType.stream()
					.forEach(plantType -> {
						if (plantType.isEmpty()) return;

						BlockRenderLayerMap.put(RenderLayer.getCutoutMipped(), plantType.getPot());

						if (plantType.getPlant() instanceof TallPlantBlock) {
							ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
											world != null && pos != null ? BiomeColors.getGrassColor(world, pos)
													: GrassColors.getColor(0.5D, 1.0D),
									plantType.getPot());
						} else {
							var originalColorProvider = ColorProviderRegistry.BLOCK.get(plantType.getPlant());
							if (originalColorProvider != null)
								ColorProviderRegistry.BLOCK.register(originalColorProvider, plantType.getPot());
						}
					});
			HangingFlowerPotBlock.stream().forEach(block -> {
				BlockRenderLayerMap.put(RenderLayer.getCutout(), block);
				var colorProvider = ColorProviderRegistry.BLOCK.get(block.getFlowerPot());
				if (colorProvider != null)
					ColorProviderRegistry.BLOCK.register(colorProvider, block);
			});
			StumpBlock.streamLogStumps()
					.forEach(block -> {
						var leavesComponent = block.getWoodType().getComponent(WoodType.ComponentType.LEAVES);
						if (leavesComponent == null) return;

						var blockColorProvider = leavesComponent.getBlockColorProvider();
						if (blockColorProvider != null) {
							ColorProviderRegistry.BLOCK.register(blockColorProvider, block);
						}
						var itemColorProvider = leavesComponent.getItemColorProvider();
						if (itemColorProvider != null) {
							ColorProviderRegistry.ITEM.register(itemColorProvider, block);
						}
					});
		});

		ClientTickEvents.START_WORLD_TICK.register(world -> Wind.get().tick(world));

		this.registerBlackboardItemRenderer(BLACKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(CHALKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(GLASSBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_BLACKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_CHALKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_GLASSBOARD_BLOCK);

		HandledScreens.register(COPPER_HOPPER_SCREEN_HANDLER_TYPE, CopperHopperScreen::new);
		HandledScreens.register(SAWMILL_SCREEN_HANDLER_TYPE, SawmillScreen::new);
		HandledScreens.register(SHELF_SCREEN_HANDLER_TYPE, ShelfScreen::new);

		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
						world != null && pos != null
								? BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefaultColor(),
				AurorasDecoPlants.BURNT_VINE_BLOCK);

		EntityModelLayerRegistry.registerModelLayer(WindChimeBlockEntityRenderer.WIND_CHIME_MODEL_LAYER,
				WindChimeBlockEntityRenderer::getTexturedModelData);

		ModelLoadingRegistry.INSTANCE.registerModelProvider(RenderRule::reload);
		ModelLoadingRegistry.INSTANCE.registerVariantProvider(resourceManager -> new BakedSignPostModel.Provider());

		TrinketsHooks.init(
				BLACKBOARD_BLOCK.asItem(), WAXED_BLACKBOARD_BLOCK.asItem(),
				CHALKBOARD_BLOCK.asItem(), WAXED_CHALKBOARD_BLOCK.asItem(),
				GLASSBOARD_BLOCK.asItem(), WAXED_GLASSBOARD_BLOCK.asItem()
		);
	}

	private void initBlockEntityRenderers() {
		BlockEntityRendererRegistry.register(AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE,
				BookPileEntityRenderer::new);
		BlockEntityRendererRegistry.register(AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE,
				ShelfBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(SIGN_POST_BLOCK_ENTITY_TYPE,
				SignPostBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE,
				LanternBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE,
				WindChimeBlockEntityRenderer::new);
	}

	private void initEntityRenderers() {
		EntityRendererRegistry.register(AurorasDecoEntities.FAKE_LEASH_KNOT_ENTITY_TYPE,
				FakeLeashKnotEntityRenderer::new);
		EntityRendererRegistry.register(AurorasDecoEntities.SEAT_ENTITY_TYPE,
				SeatEntityRenderer::new);
		TerraformBoatClientHelper.registerModelLayer(AurorasDeco.id("azalea"));
		TerraformBoatClientHelper.registerModelLayer(AurorasDeco.id("jacaranda"));
	}

	private void initBlockRenderLayers() {
		BlockRenderLayerMap.put(RenderLayer.getCutoutMipped(),
				AurorasDecoPlants.BURNT_VINE_BLOCK);
		BlockRenderLayerMap.put(RenderLayer.getCutout(),
				AMETHYST_LANTERN_BLOCK,
				AZALEA_DOOR,
				AZALEA_TRAPDOOR,
				BRAZIER_BLOCK,
				COPPER_SULFATE_BRAZIER_BLOCK,
				COPPER_SULFATE_CAMPFIRE_BLOCK,
				COPPER_SULFATE_LANTERN_BLOCK,
				COPPER_SULFATE_TORCH_BLOCK,
				COPPER_SULFATE_WALL_TORCH_BLOCK,
				GLASSBOARD_BLOCK,
				WAXED_GLASSBOARD_BLOCK,
				AurorasDecoPlants.DAFFODIL.block(),
				AurorasDecoPlants.LAVENDER.block(),
				AurorasDecoPlants.POTTED_DAFFODIL,
				AurorasDecoPlants.POTTED_LAVENDER,
				AurorasDecoPlants.JACARANDA_SAPLING,
				AurorasDecoPlants.POTTED_JACARANDA_SAPLING,
				SAWMILL_BLOCK,
				SOUL_BRAZIER_BLOCK,
				WIND_CHIME_BLOCK
		);

		BlockRenderLayerMap.put(RenderLayer.getCutout(), StumpBlock.streamLogStumps().toArray(Block[]::new));
	}

	private void registerBlackboardItemRenderer(BlackboardBlock blackboard) {
		var id = blackboard.getBuiltInRegistryHolder().getRegistryKey().getValue();
		var modelId = new ModelIdentifier(new Identifier(id.getNamespace(), id.getPath() + "_base"),
				"inventory");
		BuiltinItemRendererRegistry.INSTANCE.register(blackboard, new BlackboardItemRenderer(modelId));
		ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
			out.accept(modelId);
			out.accept(BLACKBOARD_MASK);
		});
	}
}
