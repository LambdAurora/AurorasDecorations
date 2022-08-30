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

package dev.lambdaurora.aurorasdeco.client.model;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.SignPostBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Represents the baked model of the sign post block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BakedSignPostModel extends ForwardingBakedModel {
	public BakedSignPostModel(BakedModel fenceModel) {
		this.wrapped = fenceModel;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier,
			RenderContext context) {
		if (state.getBlock() instanceof SignPostBlock signPostBlock) {
			((FabricBakedModel) this.wrapped).emitBlockQuads(blockView, signPostBlock.getFenceState(state), pos, randomSupplier, context);
		}
	}

	public static class Provider implements ModelVariantProvider {
		@Override
		public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) {
			if (modelId.getNamespace().equals(AurorasDeco.NAMESPACE) && modelId.getPath().startsWith("sign_post/") &&
					!modelId.getVariant().equals("inventory")) {
				if (Registry.BLOCK.get(new Identifier(modelId.getNamespace(), modelId.getPath())) instanceof SignPostBlock signPostBlock) {
					var states = signPostBlock.getStateManager().getStates();
					for (var state : states) {
						if (modelId.equals(BlockModels.getModelId(state))) {
							var fenceState = signPostBlock.getFenceState(state);
							var fenceModel = context.loadModel(BlockModels.getModelId(fenceState));
							return new UnbakedForwardingModel(fenceModel, BakedSignPostModel::new);
						}
					}
				}
			}
			return null;
		}
	}
}
