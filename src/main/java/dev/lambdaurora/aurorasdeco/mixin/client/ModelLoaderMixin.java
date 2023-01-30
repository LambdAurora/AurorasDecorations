/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.mixin.client;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorasdeco.client.model.*;
import dev.lambdaurora.aurorasdeco.client.renderer.BlackboardPressBlockEntityRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * Injects the big flower pot dynamic models.
 * <p>
 * Had to use priorities to win over LBG, this sucks.
 * An API probably should be made.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
@Mixin(value = ModelLoader.class, priority = 900)
public abstract class ModelLoaderMixin {
	@Shadow
	@Final
	private ResourceManager resourceManager;
	@Shadow
	@Final
	private ModelVariantMap.DeserializationContext variantMapDeserializationContext;

	@Unique
	private boolean aurorasdeco$firstRun = true;
	@Unique
	private final RestModelManager aurorasdeco$restModelManager = new RestModelManager((ModelLoader) (Object) this);
	@Unique
	private final Set<Identifier> aurorasdeco$visitedModels = new HashSet<>();

	@Shadow
	protected abstract void putModel(Identifier id, UnbakedModel unbakedModel);

	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> modelsToBake;

	@Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
	private void onPutModel(Identifier id, UnbakedModel unbakedModel, CallbackInfo ci) {
		if (id instanceof ModelIdentifier modelId
				&& !this.aurorasdeco$visitedModels.contains(id)) {
			if (!modelId.getVariant().equals("inventory")) {
				if (this.aurorasdeco$firstRun) {
					this.aurorasdeco$firstRun = false;
					this.aurorasdeco$restModelManager.init(this.resourceManager, this.variantMapDeserializationContext,
							(restModelId, model) -> {
								this.putModel(restModelId, model);
								this.modelsToBake.put(restModelId, model);
							});

					BlackboardPressBlockEntityRenderer.initModels(this.resourceManager, this.variantMapDeserializationContext,
							(pressModelId, model) -> {
								this.putModel(pressModelId, model);
								this.modelsToBake.put(pressModelId, model);
							});

					BlackboardBlockEntity.markAllMeshesDirty();
				}

				if (modelId.getNamespace().equals(AurorasDeco.NAMESPACE)) {
					if (modelId.getPath().startsWith("bench/")) {
						var model = new UnbakedBenchModel(unbakedModel, this.aurorasdeco$restModelManager);
						this.aurorasdeco$visitedModels.add(id);
						this.putModel(id, model);
						this.modelsToBake.put(id, model);
						ci.cancel();
					} else if (modelId.getPath().startsWith("big_flower_pot/")) {
						var potBlock = PottedPlantType.fromId(modelId.getPath().substring("big_flower_pot/".length())).getPot();
						if (potBlock.hasDynamicModel()) {
							this.aurorasdeco$visitedModels.add(id);
							this.putModel(id, new UnbakedForwardingModel(unbakedModel, BakedBigFlowerPotModel::new));
							ci.cancel();
						}
					} else if (modelId.getPath().startsWith("hanging_flower_pot")) {
						this.aurorasdeco$visitedModels.add(id);
						this.putModel(id, new UnbakedForwardingModel(unbakedModel, BakedHangingFlowerPotModel::new));
						ci.cancel();
					} else if (modelId.getPath().endsWith("board")) {
						this.aurorasdeco$visitedModels.add(id);
						this.putModel(id, UnbakedBlackboardModel.of(modelId, unbakedModel,
								this.resourceManager, this.variantMapDeserializationContext,
								(partId, model) -> {
									this.putModel(partId, model);
									this.modelsToBake.put(partId, model);
								}
						));
						ci.cancel();
					}
				}
			}
		}
	}
}
