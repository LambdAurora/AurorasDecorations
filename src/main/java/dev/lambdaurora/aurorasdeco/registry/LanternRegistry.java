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

package dev.lambdaurora.aurorasdeco.registry;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.accessor.BlockEntityTypeAccessor;
import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class LanternRegistry {
    private static final Map<Identifier, WallLanternBlock> WALL_LANTERNS = new Object2ObjectOpenHashMap<>();
    private static final Map<LanternBlock, WallLanternBlock> WALL_LANTERN_BLOCK_MAP = new Object2ObjectOpenHashMap<>();

    public static void forEach(BiConsumer<Identifier, WallLanternBlock> consumer) {
        WALL_LANTERNS.forEach(consumer);
    }

    /**
     * Registers a wall lantern for the given lantern block.
     *
     * @param block the lantern block
     * @param lanternId the lantern block id
     * @return the wall lantern block
     */
    public static WallLanternBlock registerWallLantern(LanternBlock block, Identifier lanternId) {
        Identifier wallLanternId = getWallLanternId(lanternId);

        WallLanternBlock wallLanternBlock;
        if (WALL_LANTERNS.containsKey(wallLanternId))
            return WALL_LANTERNS.get(wallLanternId);
        else if (block == Blocks.LANTERN || block == Blocks.SOUL_LANTERN) {
            wallLanternBlock = (WallLanternBlock) Registry.BLOCK.get(wallLanternId);
        } else {
            wallLanternBlock = Registry.register(Registry.BLOCK, wallLanternId, new WallLanternBlock(block));
            ((BlockEntityTypeAccessor) AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE)
                    .aurorasdeco$addSupportedBlock(wallLanternBlock);
        }

        WALL_LANTERNS.put(wallLanternId, wallLanternBlock);
        WALL_LANTERN_BLOCK_MAP.put(block, wallLanternBlock);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            BlockRenderLayerMap.INSTANCE.putBlock(wallLanternBlock, RenderLayer.getCutout());
        }

        return wallLanternBlock;
    }

    public static WallLanternBlock registerWallLantern(LanternBlock block) {
        return registerWallLantern(block, Registry.BLOCK.getId(block));
    }

    public static void tryRegisterWallLantern(Block block, Identifier id) {
        if (block instanceof LanternBlock)
            registerWallLantern((LanternBlock) block, id);
    }

    private static Identifier getWallLanternId(Identifier lanternId) {
        String namespace = lanternId.getNamespace();
        String path = lanternId.getPath();
        String wallLanternPath = "wall_lantern";

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
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) item;
            if (blockItem.getBlock() instanceof LanternBlock) {
                LanternBlock lanternBlock = (LanternBlock) blockItem.getBlock();
                return WALL_LANTERN_BLOCK_MAP.get(lanternBlock);
            }
        }
        return null;
    }
}
