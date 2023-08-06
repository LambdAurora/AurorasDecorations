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

package dev.lambdaurora.aurorasdeco.client.model;

import dev.lambdaurora.aurorasdeco.block.SignPostBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.BlockStateResolver;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.function.Supplier;

/**
 * Represents the baked model of the sign post block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
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
			this.wrapped.emitBlockQuads(blockView, signPostBlock.getFenceState(state), pos, randomSupplier, context);
		}
	}

	public record Provider(SignPostBlock signPostBlock) implements BlockStateResolver {
		@Override
		public void resolveBlockStates(Context context) {
			var states = this.signPostBlock.getStateManager().getStates();
			for (var state : states) {
				var fenceState = this.signPostBlock.getFenceState(state);
				var fenceModel = context.getOrLoadModel(BlockModels.getModelId(fenceState));
				context.setModel(state, new UnbakedForwardingModel(fenceModel, BakedSignPostModel::new));
			}
		}
	}
}
