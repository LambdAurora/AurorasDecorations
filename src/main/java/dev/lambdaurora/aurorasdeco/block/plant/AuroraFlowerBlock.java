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

package dev.lambdaurora.aurorasdeco.block.plant;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.FlammableBlockEntry;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

/**
 * Represents an Aurora's Decorations flower block. This mod's flower blocks can be reproduced using bone meal.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AuroraFlowerBlock extends FlowerBlock implements Fertilizable {
	public AuroraFlowerBlock(StatusEffect statusEffect, int effectInStewDuration, Settings settings) {
		super(statusEffect, effectInStewDuration, settings);

		BlockContentRegistries.FLAMMABLE_BLOCK.put(this, new FlammableBlockEntry(60, 100));
	}

	public static QuiltBlockSettings defaultSettings() {
		return QuiltBlockSettings.of(Material.PLANT)
				.noCollision()
				.breakInstantly()
				.sounds(BlockSoundGroup.GRASS);
	}

	/* Fertilization */

	@Override
	public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
		return true;
	}

	@Override
	public boolean canGrow(World world, RandomGenerator random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, RandomGenerator random, BlockPos pos, BlockState state) {
		dropStack(world, pos, new ItemStack(this));
	}
}
