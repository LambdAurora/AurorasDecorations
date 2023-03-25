/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.mixin.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.ConcentricRingsStructurePlacement;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacement;
import net.minecraft.util.Holder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.RandomState;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Set;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccessor {
	@Invoker
	List<StructurePlacement> invokeM_wozjtsiz(Holder<StructureFeature> feature, RandomState random);

	@Invoker
	@Nullable Pair<BlockPos, Holder<StructureFeature>> invokeFindStructures(
			Set<Holder<StructureFeature>> structures,
			ServerWorld world,
			StructureManager structureManager,
			BlockPos pos,
			boolean bl,
			ConcentricRingsStructurePlacement placement
	);

	@Invoker
	@Nullable
	static Pair<BlockPos, Holder<StructureFeature>> invokeM_gxxzcexz(
			Set<Holder<StructureFeature>> structures, WorldView world, StructureManager structureManager, boolean skipExistingChunks,
			StructurePlacement placement, ChunkPos pos
	) {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
