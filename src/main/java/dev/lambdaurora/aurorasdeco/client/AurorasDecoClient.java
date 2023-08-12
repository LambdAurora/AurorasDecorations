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

package dev.lambdaurora.aurorasdeco.client;

import com.terraformersmc.terraform.boat.api.client.TerraformBoatClientHelper;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.BlackboardBlock;
import dev.lambdaurora.aurorasdeco.block.HangingFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.SignPostBlock;
import dev.lambdaurora.aurorasdeco.block.StumpBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorasdeco.client.model.*;
import dev.lambdaurora.aurorasdeco.client.particle.AmethystGlintParticle;
import dev.lambdaurora.aurorasdeco.client.particle.LavenderPetalParticle;
import dev.lambdaurora.aurorasdeco.client.renderer.*;
import dev.lambdaurora.aurorasdeco.client.screen.CopperHopperScreen;
import dev.lambdaurora.aurorasdeco.client.screen.PainterPaletteScreen;
import dev.lambdaurora.aurorasdeco.client.screen.SawmillScreen;
import dev.lambdaurora.aurorasdeco.client.screen.ShelfScreen;
import dev.lambdaurora.aurorasdeco.mixin.client.ModelLoaderAccessor;
import dev.lambdaurora.aurorasdeco.registry.*;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

import static dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry.*;

/**
 * Represents the Aurora's Decorations client mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
public class AurorasDecoClient implements ClientModInitializer {
	public static final AurorasDecoPack RESOURCE_PACK = new AurorasDecoPack(ResourceType.CLIENT_RESOURCES);
	public static final ModelIdentifier BLACKBOARD_MASK = new ModelIdentifier(AurorasDeco.id("blackboard_mask"),
			"inventory");

	@Override
	public void onInitializeClient(ModContainer mod) {
		this.initBlockEntityRenderers();
		this.initEntityRenderers();
		this.initBlockRenderLayers();

		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.AMETHYST_GLINT, AmethystGlintParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.COPPER_SULFATE_FLAME, FlameParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.COPPER_SULFATE_LAVA, LavaEmberParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(AurorasDecoParticles.LAVENDER_PETAL, LavenderPetalParticle.Factory::new);

		/* Signs */
		ClientPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.SIGN_POST_OPEN_GUI, AurorasDecoPackets.Client::handleSignPostOpenGuiPacket);

		ClientLifecycleEvents.READY.register(client -> {
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

		ClientWorldTickEvents.START.register((client, world) -> Wind.get().tick(world));

		this.registerBlackboardItemRenderer(BLACKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(CHALKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(GLASSBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_BLACKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_CHALKBOARD_BLOCK);
		this.registerBlackboardItemRenderer(WAXED_GLASSBOARD_BLOCK);

		HandledScreens.register(AurorasDecoScreenHandlers.COPPER_HOPPER_SCREEN_HANDLER_TYPE, CopperHopperScreen::new);
		HandledScreens.register(AurorasDecoScreenHandlers.PAINTER_PALETTE_SCREEN_HANDLER_TYPE, PainterPaletteScreen::new);
		HandledScreens.register(AurorasDecoScreenHandlers.SAWMILL_SCREEN_HANDLER_TYPE, SawmillScreen::new);
		HandledScreens.register(AurorasDecoScreenHandlers.SHELF_SCREEN_HANDLER_TYPE, ShelfScreen::new);

		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
						world != null && pos != null
								? BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefaultColor(),
				AurorasDecoPlants.BURNT_VINE_BLOCK);
		ColorProviderRegistry.ITEM.register(PAINTER_PALETTE_ITEM::getColor, PAINTER_PALETTE_ITEM);

		EntityModelLayerRegistry.registerModelLayer(WindChimeBlockEntityRenderer.WIND_CHIME_MODEL_LAYER,
				WindChimeBlockEntityRenderer::getTexturedModelData);

		ResourceLoader resourceLoader = ResourceLoader.get(ResourceType.CLIENT_RESOURCES);
		resourceLoader.getRegisterDefaultResourcePackEvent().register(context -> {
			context.addResourcePack(AurorasDecoClient.RESOURCE_PACK.rebuild(ResourceType.CLIENT_RESOURCES, context.resourceManager()));
		});
		resourceLoader.getRegisterTopResourcePackEvent().register(AurorasDeco.id("reload/render_rules"),
				context -> {
					RenderRule.reload(context.resourceManager());
				}
		);

		ModelLoadingPlugin.register(context -> {
			RenderRule.addModels(context);

			new RestModelManager().init(context);

			BlackboardPressBlockEntityRenderer.initModels(context);

			SignPostBlock.stream().forEach(signPostBlock -> {
				context.registerBlockStateResolver(signPostBlock, new BakedSignPostModel.Provider(signPostBlock));
			});

			context.modifyModelOnLoad().register((model, ctx) -> {
				if (ctx.id() instanceof ModelIdentifier modelId && !modelId.getVariant().equals("inventory"))
					if (modelId.getPath().startsWith("big_flower_pot/")) {
						var potBlock = PottedPlantType.fromId(modelId.getPath().substring("big_flower_pot/".length())).getPot();
						if (potBlock.hasDynamicModel()) {
							return new UnbakedForwardingModel(model, BakedBigFlowerPotModel::new);
						}
					} else if (modelId.getPath().startsWith("hanging_flower_pot")) {
						return new UnbakedForwardingModel(model, BakedHangingFlowerPotModel::new);
					} else if (modelId.getPath().endsWith("board")) {
						return UnbakedBlackboardModel.of(modelId, model,
								(partId, m) -> {
									var modelLoader = (ModelLoaderAccessor) ctx.loader();
									modelLoader.invokePutModel(partId, m);
									modelLoader.getModelsToBake().put(partId, m);
								}
						);
					}

				return model;
			});

			BlackboardBlockEntity.markAllMeshesDirty();
		});
	}

	private void initBlockEntityRenderers() {
		BlockEntityRendererFactories.register(BLACKBOARD_PRESS_BLOCK_ENTITY,
				BlackboardPressBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(AurorasDecoRegistry.BOOK_PILE_BLOCK_ENTITY_TYPE,
				BookPileEntityRenderer::new);
		BlockEntityRendererFactories.register(AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE,
				ShelfBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(SIGN_POST_BLOCK_ENTITY_TYPE,
				SignPostBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE,
				LanternBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE,
				WindChimeBlockEntityRenderer::new);
	}

	private void initEntityRenderers() {
		EntityRendererRegistry.register(AurorasDecoEntities.FAKE_LEASH_KNOT_ENTITY_TYPE,
				FakeLeashKnotEntityRenderer::new);
		EntityRendererRegistry.register(AurorasDecoEntities.SEAT_ENTITY_TYPE,
				SeatEntityRenderer::new);
		TerraformBoatClientHelper.registerModelLayers(AurorasDeco.id("azalea"), false);
		TerraformBoatClientHelper.registerModelLayers(AurorasDeco.id("jacaranda"), false);
	}

	private void initBlockRenderLayers() {
		BlockRenderLayerMap.put(RenderLayer.getCutoutMipped(),
				AurorasDecoPlants.BURNT_VINE_BLOCK);
		BlockRenderLayerMap.put(RenderLayer.getCutout(),
				AMETHYST_LANTERN_BLOCK,
				AZALEA_DOOR,
				AZALEA_TRAPDOOR,
				JACARANDA_DOOR,
				JACARANDA_TRAPDOOR,
				BRAZIER_BLOCK,
				COPPER_SULFATE_BRAZIER_BLOCK,
				COPPER_SULFATE_CAMPFIRE_BLOCK,
				COPPER_SULFATE_LANTERN_BLOCK,
				COPPER_SULFATE_TORCH_BLOCK,
				COPPER_SULFATE_WALL_TORCH_BLOCK,
				GLASSBOARD_BLOCK,
				WAXED_GLASSBOARD_BLOCK,
				AurorasDecoPlants.DAFFODIL.block(),
				AurorasDecoPlants.DUCKWEED.block(),
				AurorasDecoPlants.LAVENDER.block(),
				AurorasDecoPlants.POTTED_DAFFODIL,
				AurorasDecoPlants.POTTED_LAVENDER,
				AurorasDecoPlants.JACARANDA_SAPLING,
				AurorasDecoPlants.POTTED_JACARANDA_SAPLING,
				REDSTONE_LANTERN_BLOCK,
				SAWMILL_BLOCK,
				SOUL_BRAZIER_BLOCK,
				WIND_CHIME_BLOCK
		);

		BlockRenderLayerMap.put(RenderLayer.getCutout(), StumpBlock.streamLogStumps().toArray(Block[]::new));
	}

	private void registerBlackboardItemRenderer(BlackboardBlock blackboard) {
		@SuppressWarnings("deprecation") var id = blackboard.getBuiltInRegistryHolder().getRegistryKey().getValue();
		var modelId = new ModelIdentifier(new Identifier(id.getNamespace(), id.getPath() + "_base"),
				"inventory");
		BuiltinItemRendererRegistry.INSTANCE.register(blackboard, new BlackboardItemRenderer(modelId));
		ModelLoadingPlugin.register(context -> context.addModels(modelId, BLACKBOARD_MASK));
	}
}
