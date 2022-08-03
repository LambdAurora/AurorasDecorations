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

import dev.lambdaurora.aurorasdeco.item.SeatRestItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

import java.util.function.Supplier;

/**
 * Represents the baked bench model.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BakedBenchModel extends ForwardingBakedModel {
	private final RestModelManager restModelManager;

	public BakedBenchModel(BakedModel baseModel, RestModelManager restModelManager) {
		this.restModelManager = restModelManager;
		this.wrapped = baseModel;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

		var attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		if (attachment instanceof SeatRestItem seatRestItem) {
			var entry = this.restModelManager.get(seatRestItem.getWoodType());
			if (entry != null && entry.getBakedBenchRest() instanceof FabricBakedModel restModel) {
				restModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			}
		}
	}
}
