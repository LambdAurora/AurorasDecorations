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

package dev.lambdaurora.aurorasdeco.block.behavior;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

/**
 * Contains common behavior with copper-sulfate-based blocks.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CopperSulfateBehavior {
    private CopperSulfateBehavior() {
        throw new UnsupportedOperationException("Someone tried to instantiate a class only containing static definitions. How?");
    }

    public static void attemptToDecompose(BlockState state, ServerWorld world, BlockPos pos, Random random, int radius) {
        var currentPos = pos.mutableCopy();
        for (int y = 0; y < radius; y++) {
            currentPos.move(0, 1, 0);

            var currentState = world.getBlockState(currentPos);
            if (currentState.isIn(AurorasDecoTags.COPPER_SULFATE_DECOMPOSABLE)) {
                if (currentState.isIn(BlockTags.LEAVES))
                    Block.dropStacks(currentState, world, currentPos, world.getBlockEntity(pos));
                else {
                    var virtualTool = new ItemStack(Items.NETHERITE_PICKAXE);
                    virtualTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
                    Block.dropStacks(currentState, world, currentPos, world.getBlockEntity(pos), null, virtualTool);
                }

                world.setBlockState(currentPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
                break;
            }
        }
    }
}
