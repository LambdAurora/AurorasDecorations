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

import dev.lambdaurora.aurorasdeco.mixin.AbstractBlockAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.ItemConvertible;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

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
public final class WoodType {
    private static final List<WoodType> WOOD_TYPES = new ArrayList<>();

    private final Identifier id;
    private final String logType;
    private final boolean flammable;
    private final Identifier planksTexture;
    private final Identifier leavesId;
    private final Identifier leavesTexture;
    public final Material material;
    private final MapColor mapColor;
    public final BlockSoundGroup logSoundGroup;

    public WoodType(Identifier id, String logType, boolean flammable, Identifier planksTexture, Identifier leavesId,
                    Material material, MapColor color,
                    BlockSoundGroup logSoundGroup) {
        this.id = id;
        this.logType = logType;
        this.flammable = flammable;
        this.planksTexture = planksTexture;
        this.leavesId = leavesId;
        this.leavesTexture = new Identifier(leavesId.getNamespace(), "block/" + leavesId.getPath());
        this.material = material;
        this.mapColor = color;
        this.logSoundGroup = logSoundGroup;
    }

    public Identifier getId() {
        return this.id;
    }

    public String getPathName() {
        var path = this.id.getPath();
        var namespace = this.id.getNamespace();
        if (!namespace.equals("minecraft"))
            path = namespace + '/' + path;
        return path;
    }

    public boolean hasLog() {
        return !this.logType.equals("none");
    }

    public String getLogType() {
        return this.logType;
    }

    public boolean isFlammable() {
        return this.flammable;
    }

    public Identifier getPlanksTexture() {
        return this.planksTexture;
    }

    public Identifier getLogSideTexture() {
        if (!this.hasLog())
            return this.getPlanksTexture();
        return new Identifier(this.id.getNamespace(), "block/" + this.id.getPath() + '_' + this.logType);
    }

    public Identifier getLogTopTexture() {
        if (!this.hasLog())
            return this.getPlanksTexture();
        return new Identifier(this.id.getNamespace(), "block/" + this.id.getPath() + '_' + this.logType + "_top");
    }

    public Identifier getLogId() {
        return new Identifier(this.id.getNamespace(), this.id.getPath() + '_' + this.logType);
    }

    public @Nullable Block getLog() {
        if (!this.hasLog())
            return null;
        return Registry.BLOCK.get(this.getLogId());
    }

    public @Nullable Block getLeaves() {
        if (!this.hasLog())
            return null;
        return Registry.BLOCK.get(this.leavesId);
    }

    public Identifier getPlanksId() {
        return new Identifier(this.id.getNamespace(), this.getId().getPath() + "_planks");
    }

    public Identifier getSlabId() {
        return new Identifier(this.id.getNamespace(), this.getId().getPath() + "_slab");
    }

    public Identifier getLeavesTexture() {
        return this.leavesTexture;
    }

    public MapColor getMapColor() {
        return this.mapColor;
    }

    @Environment(EnvType.CLIENT)
    public BlockColorProvider getLeavesColorProvider() {
        return ColorProviderRegistry.BLOCK.get(this.getLeaves());
    }

    @Environment(EnvType.CLIENT)
    public ItemColorProvider getLeavesItemColorProvider() {
        return ColorProviderRegistry.ITEM.get(this.getLeaves());
    }

    public static @Nullable WoodType getFromPlanks(ItemConvertible block) {
        var id = Registry.ITEM.getId(block.asItem());

        for (var type : WOOD_TYPES) {
            if (type.getPlanksId().equals(id))
                return type;
        }

        return null;
    }

    public static @Nullable WoodType getFromLog(ItemConvertible block) {
        var id = Registry.ITEM.getId(block.asItem());

        for (var type : WOOD_TYPES) {
            if (type.getLogId().equals(id))
                return type;
        }

        return null;
    }

    public static WoodType register(WoodType woodType) {
        WOOD_TYPES.add(woodType);
        return woodType;
    }

    public static Stream<WoodType> stream() {
        return WOOD_TYPES.stream();
    }

    static {
        builder(Blocks.OAK_PLANKS).register();
        builder(Blocks.SPRUCE_PLANKS).register();
        builder(Blocks.BIRCH_PLANKS).register();
        builder(Blocks.JUNGLE_PLANKS).register();
        builder(Blocks.ACACIA_PLANKS).register();
        builder(Blocks.DARK_OAK_PLANKS).register();
        builder(Blocks.CRIMSON_PLANKS).leavesId(new Identifier("nether_wart_block"))
                .logSoundGroup(BlockSoundGroup.NETHER_STEM)
                .register();
        builder(Blocks.WARPED_PLANKS).logSoundGroup(BlockSoundGroup.NETHER_STEM).register();

        if (FabricLoader.getInstance().isModLoaded("blockus")) {
            new Builder(new Identifier("blockus", "bamboo"))
                    .logType("none")
                    .mapColor(MapColor.PALE_YELLOW)
                    .register();
            new Builder(new Identifier("blockus", "charred"))
                    .logType("none")
                    .mapColor(MapColor.TERRACOTTA_GRAY)
                    .register();
            new Builder(new Identifier("blockus", "white_oak"))
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .register();
        }
    }

    public static Builder builder(Block block) {
        var blockId = Registry.BLOCK.getId(block);
        var id = new Identifier(blockId.getNamespace(), blockId.getPath().replace("_planks", ""));
        var material = ((AbstractBlockAccessor) block).getMaterial();
        var builder = new Builder(id)
                .material(material)
                .mapColor(block.getDefaultMapColor())
                .logSoundGroup(block.getSoundGroup(block.getDefaultState()));
        if (material == Material.NETHER_WOOD)
            builder.logType("stem").flammable(false);
        return builder;
    }

    private static class Builder {
        final Identifier id;
        boolean flammable = true;
        Identifier planksTexture;
        Identifier leavesId;
        boolean customLeavesId = false;
        String logType = "log";
        Material material = Material.WOOD;
        MapColor mapColor = Material.WOOD.getColor();
        BlockSoundGroup logSoundGroup = BlockSoundGroup.WOOD;

        public Builder(Identifier id) {
            this.id = id;
            this.planksTexture = new Identifier(id.getNamespace(), "block/" + id.getPath() + "_planks");
            this.leavesId = new Identifier(this.id.getNamespace(), this.id.getPath() + "_leaves");
        }

        public Builder planksTexture(Identifier id) {
            this.planksTexture = id;
            return this;
        }

        public Builder logType(String type) {
            this.logType = type;
            if (!this.customLeavesId && type.equals("stem")) {
                this.leavesId = new Identifier(this.id.getNamespace(), this.id.getPath() + "_wart_block");
            }
            return this;
        }

        public Builder flammable(boolean flammable) {
            this.flammable = flammable;
            return this;
        }

        public Builder leavesId(Identifier id) {
            this.leavesId = id;
            this.customLeavesId = true;
            return this;
        }

        public Builder material(Material material) {
            this.material = material;
            return this;
        }

        public Builder mapColor(MapColor color) {
            this.mapColor = color;
            return this;
        }

        public Builder logSoundGroup(BlockSoundGroup soundGroup) {
            this.logSoundGroup = soundGroup;
            return this;
        }

        public WoodType build() {
            return new WoodType(
                    this.id, this.logType, this.flammable, this.planksTexture, this.leavesId,
                    this.material, this.mapColor, this.logSoundGroup
            );
        }

        public void register() {
            WoodType.register(this.build());
        }
    }
}
