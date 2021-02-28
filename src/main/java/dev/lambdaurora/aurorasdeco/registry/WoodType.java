/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a wood type.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class WoodType {
    private static final List<WoodType> WOOD_TYPES = new ArrayList<>();

    private final Identifier id;
    private final Identifier planksTexture;
    private final Identifier logTexture;
    private final MapColor mapColor;

    public WoodType(Identifier id, Identifier planksTexture, Identifier logTexture, MapColor color) {
        this.id = id;
        this.planksTexture = planksTexture;
        this.logTexture = logTexture;
        this.mapColor = color;
    }

    public Identifier getId() {
        return this.id;
    }

    public String getPathName() {
        String path = this.id.getPath();
        String namespace = this.id.getNamespace();
        if (!namespace.equals("minecraft"))
            path = namespace + '/' + path;
        return path;
    }

    public Identifier getPlanksTexture() {
        return this.planksTexture;
    }

    public Identifier getLogTexture() {
        return this.logTexture;
    }

    public Identifier getSlabId() {
        return new Identifier(this.id.getNamespace(), this.getId().getPath() + "_slab");
    }

    public MapColor getMapColor() {
        return this.mapColor;
    }

    public static WoodType register(WoodType woodType) {
        WOOD_TYPES.add(woodType);
        return woodType;
    }

    public static WoodType registerFromPlanks(Block block) {
        Identifier blockId = Registry.BLOCK.getId(block);
        Identifier id = new Identifier(blockId.getNamespace(), blockId.getPath().replace("_planks", ""));
        return register(id,
                new Identifier(id.getNamespace(), "block/" + id.getPath() + "_planks"),
                new Identifier(id.getNamespace(), "block/" + id.getPath() + "_log"),
                block.getDefaultMapColor()
        );
    }

    public static WoodType register(Identifier id, MapColor mapColor) {
        return register(id,
                new Identifier(id.getNamespace(), "block/" + id.getPath() + "_planks"),
                new Identifier(id.getNamespace(), "block/" + id.getPath() + "_log"),
                mapColor
        );
    }

    public static WoodType register(Identifier id, Identifier planksTexture, Identifier logTexture, MapColor mapColor) {
        return register(new WoodType(id, planksTexture, logTexture, mapColor));
    }

    public static Stream<WoodType> stream() {
        return WOOD_TYPES.stream();
    }

    static {
        registerFromPlanks(Blocks.OAK_PLANKS);
        registerFromPlanks(Blocks.SPRUCE_PLANKS);
        registerFromPlanks(Blocks.BIRCH_PLANKS);
        registerFromPlanks(Blocks.JUNGLE_PLANKS);
        registerFromPlanks(Blocks.ACACIA_PLANKS);
        registerFromPlanks(Blocks.DARK_OAK_PLANKS);
        register(new Identifier("crimson"),
                new Identifier("block/crimson_planks"),
                new Identifier("block/crimson_stem"),
                Blocks.CRIMSON_PLANKS.getDefaultMapColor());
        register(new Identifier("warped"),
                new Identifier("block/warped_planks"),
                new Identifier("block/warped_stem"),
                Blocks.WARPED_PLANKS.getDefaultMapColor());

        if (FabricLoader.getInstance().isModLoaded("blockus")) {
            register(new Identifier("blockus", "bamboo"),
                    new Identifier("blockus", "block/bamboo_planks"),
                    new Identifier("blockus", "block/bamboo_planks"), MapColor.PALE_YELLOW);
            register(new Identifier("blockus", "charred"),
                    new Identifier("blockus", "block/charred_planks"),
                    new Identifier("blockus", "block/charred_planks"), MapColor.TERRACOTTA_GRAY);
            register(new Identifier("blockus", "white_oak"), MapColor.TERRACOTTA_WHITE);
        }
    }
}
