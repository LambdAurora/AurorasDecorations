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

import dev.lambdaurora.aurorasdeco.blackboard.Blackboard;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.function.Supplier;

@ClientOnly
public class BakedBlackboardModel extends ForwardingBakedModel {
	public BakedBlackboardModel(BakedModel baseModel) {
		this.wrapped = baseModel;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

		this.emitBlockMesh(blockView, pos, context);
	}

	protected void emitBlockMesh(BlockRenderView blockView, BlockPos pos, RenderContext context) {
		var attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
		if (attachment instanceof Mesh mesh) {
			context.meshConsumer().accept(mesh);
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
		super.emitItemQuads(stack, randomSupplier, context);

		var nbt = BlockItem.getBlockEntityNbtFromStack(stack);
		if (nbt != null && nbt.contains("pixels", NbtElement.BYTE_ARRAY_TYPE)) {
			var blackboard = Blackboard.fromNbt(nbt);
			context.meshConsumer().accept(blackboard.buildMesh(Direction.NORTH, blackboard.isLit() ? LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE : 0));
		}
	}
}
