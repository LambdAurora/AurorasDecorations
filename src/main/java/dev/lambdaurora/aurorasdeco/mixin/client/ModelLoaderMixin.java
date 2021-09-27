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

package dev.lambdaurora.aurorasdeco.mixin.client;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorasdeco.client.model.*;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Injects the big flower pot dynamic models.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Mixin(ModelLoader.class)
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

	@Shadow
	protected abstract void putModel(Identifier id, UnbakedModel unbakedModel);

	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> modelsToBake;

	@Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
	private void onPutModel(Identifier id, UnbakedModel unbakedModel, CallbackInfo ci) {
		if (id instanceof ModelIdentifier modelId
				&& !(unbakedModel instanceof AuroraUnbakedModel)) {
			if (!modelId.getVariant().equals("inventory")) {
				if (this.aurorasdeco$firstRun) {
					this.aurorasdeco$firstRun = false;
					this.aurorasdeco$restModelManager.init(this.resourceManager, this.variantMapDeserializationContext,
							(restModelId, model) -> {
								this.putModel(restModelId, model);
								this.modelsToBake.put(restModelId, model);
							});
					BlackboardBlockEntity.markAllMeshesDirty();
				}

				if (modelId.getNamespace().equals(AurorasDeco.NAMESPACE)) {
					if (modelId.getPath().startsWith("bench/")) {
						this.putModel(id, new UnbakedBenchModel(unbakedModel, this.aurorasdeco$restModelManager));
						ci.cancel();
					} else if (modelId.getPath().startsWith("big_flower_pot/")) {
						var potBlock = PottedPlantType.fromId(modelId.getPath().substring("big_flower_pot/".length())).getPot();
						if (potBlock.hasDynamicModel()) {
							this.putModel(id, new UnbakedForwardingModel(unbakedModel, BakedBigFlowerPotModel::new));
							ci.cancel();
						}
					} else if (modelId.getPath().startsWith("hanging_flower_pot")) {
						this.putModel(id, new UnbakedForwardingModel(unbakedModel, BakedHangingFlowerPotModel::new));
						ci.cancel();
					} else if (modelId.getPath().endsWith("board")) {
						this.putModel(id, new UnbakedBlackboardModel(unbakedModel));
						ci.cancel();
					}
				}
			}
		}
	}
}
