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

package dev.lambdaurora.aurorasdeco.registry;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.RedstoneLanternBlock;
import dev.lambdaurora.aurorasdeco.block.RedstoneWallLanternBlock;
import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class LanternRegistry {
	private static final Map<Identifier, WallLanternBlock<?>> WALL_LANTERNS = new Object2ObjectOpenHashMap<>();
	private static final Map<LanternBlock, WallLanternBlock<?>> WALL_LANTERN_BLOCK_MAP = new Object2ObjectOpenHashMap<>();

	public static Stream<Identifier> streamIds() {
		return WALL_LANTERNS.keySet().stream();
	}

	public static void forEach(BiConsumer<Identifier, WallLanternBlock<?>> consumer) {
		WALL_LANTERNS.forEach(consumer);
	}

	/**
	 * Registers a wall lantern for the given lantern block.
	 *
	 * @param block the lantern block
	 * @param lanternId the lantern block id
	 * @return the wall lantern block
	 */
	@SuppressWarnings("unchecked")
	public static <L extends LanternBlock> WallLanternBlock<L> registerWallLantern(Registry<Block> registry, L block, Identifier lanternId) {
		var wallLanternId = getWallLanternId(lanternId);

		WallLanternBlock<L> wallLanternBlock;
		if (WALL_LANTERNS.containsKey(wallLanternId))
			return (WallLanternBlock<L>) WALL_LANTERNS.get(wallLanternId);
		else if (block == Blocks.LANTERN || block == Blocks.SOUL_LANTERN) {
			wallLanternBlock = (WallLanternBlock<L>) Registries.BLOCK.get(wallLanternId);
		} else if (block instanceof RedstoneLanternBlock redstoneLanternBlock) {
			wallLanternBlock = (WallLanternBlock<L>) Registry.register(registry, wallLanternId, new RedstoneWallLanternBlock(redstoneLanternBlock));
		} else {
			wallLanternBlock = Registry.register(registry, wallLanternId, new WallLanternBlock<>(block));
			AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.addSupportedBlock(wallLanternBlock);
		}

		WALL_LANTERNS.put(wallLanternId, wallLanternBlock);
		WALL_LANTERN_BLOCK_MAP.put(block, wallLanternBlock);

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			BlockRenderLayerMap.put(RenderLayer.getCutout(), wallLanternBlock);
		}

		return wallLanternBlock;
	}

	public static <L extends LanternBlock> WallLanternBlock<L> registerWallLantern(L block) {
		return registerWallLantern(Registries.BLOCK, block, Registries.BLOCK.getId(block));
	}

	public static void tryRegisterWallLantern(Registry<Block> registry, Block block, Identifier id) {
		if (block instanceof LanternBlock)
			registerWallLantern(registry, (LanternBlock) block, id);
	}

	private static Identifier getWallLanternId(Identifier lanternId) {
		var namespace = lanternId.getNamespace();
		var path = lanternId.getPath();
		var wallLanternPath = "wall_lantern";

		if (!namespace.equals("minecraft") && !namespace.equals("aurorasdeco"))
			wallLanternPath += '/' + namespace + '/' + path.replace("_lantern_block", "")
					.replace("_lantern", "");
		else {
			if (!path.equals("lantern"))
				wallLanternPath += '/' + path.replace("_lantern", "");
		}

		return AurorasDeco.id(wallLanternPath);
	}

	public static @Nullable WallLanternBlock fromItem(Item item) {
		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof LanternBlock lanternBlock) {
			return WALL_LANTERN_BLOCK_MAP.get(lanternBlock);
		}
		return null;
	}
}
