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

package dev.lambdaurora.aurorasdeco.client.model;

import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigPottedSweetBerryBushBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.BlockRenderView;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BakedBigFlowerPotModel extends ForwardingBakedModel {
    private final MinecraftClient client = MinecraftClient.getInstance();

    public BakedBigFlowerPotModel(BakedModel baseModel) {
        this.wrapped = baseModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier,
                               RenderContext context) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        if (!(state.getBlock() instanceof BigFlowerPotBlock))
            return;

        BigFlowerPotBlock potBlock = (BigFlowerPotBlock) state.getBlock();
        BlockState plantState = potBlock.getPlantState(state);
        if (!plantState.isAir()) {
            float ratio = .65f;
            float offset = (1.f - ratio) / 2.f;

            if (!(potBlock instanceof BigPottedSweetBerryBushBlock))
                for (Property<?> property : plantState.getProperties()) {
                    if (property instanceof IntProperty && property.getName().equals("age")) {
                        IntProperty ageProperty = ((IntProperty) property);
                        Optional<Integer> max = ageProperty.getValues().stream().max(Integer::compareTo);
                        if (max.isPresent()) {
                            plantState = plantState.with(ageProperty, max.get());
                        }
                        break;
                    }
                }

            BakedModel model = client.getBakedModelManager().getBlockModels().getModel(plantState);
            if (model instanceof FabricBakedModel) {
                context.pushTransform(quad -> {
                    Vec3f vec = null;
                    for (int i = 0; i < 4; i++) {
                        vec = quad.copyPos(i, vec);
                        vec.multiplyComponentwise(ratio, ratio, ratio);
                        vec.add(offset, .8f, offset);
                        quad.pos(i, vec);
                    }
                    quad.material(RendererAccess.INSTANCE.getRenderer().materialFinder().disableAo(0, true).find());
                    return true;
                });
                ((FabricBakedModel) model).emitBlockQuads(blockView, state, pos, randomSupplier, context);
                context.popTransform();
            }

            if (plantState.getBlock() instanceof TallPlantBlock) {
                final BakedModel upModel = client.getBakedModelManager().getBlockModels().getModel(
                        plantState.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER)
                );
                if (upModel instanceof FabricBakedModel) {
                    context.pushTransform(quad -> {
                        Vec3f vec = null;
                        for (int i = 0; i < 4; i++) {
                            vec = quad.copyPos(i, vec);
                            vec.multiplyComponentwise(ratio, ratio, ratio);
                            vec.add(offset, .8f + ratio, offset);
                            quad.pos(i, vec);
                        }
                        quad.material(RendererAccess.INSTANCE.getRenderer().materialFinder().disableAo(0, true).find());
                        return true;
                    });
                    ((FabricBakedModel) upModel).emitBlockQuads(blockView, state, pos.up(), randomSupplier, context);
                    context.popTransform();
                }
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

    }
}
