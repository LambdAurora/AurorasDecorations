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

package dev.lambdaurora.aurorasdeco.client.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a baked model which is using a variant map to adapt to depending on block states.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BakedVariantModel extends ForwardingBakedModel {
	private final Map<BlockState, BakedModel> variantMap;

	public BakedVariantModel(Map<BlockState, BakedModel> variantMap) {
		this.variantMap = variantMap;
		this.wrapped = variantMap.entrySet().iterator().next().getValue();
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		var model = this.variantMap.get(state);
		if (model == null) return;

		((FabricBakedModel) model).emitBlockQuads(blockView, state, pos, randomSupplier, context);
	}
}
