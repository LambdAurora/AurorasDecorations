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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a potted plant type for {@link BigFlowerPotBlock}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PottedPlantType {
    private static final Map<String, PottedPlantType> TYPES = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, BigFlowerPotBlock> ITEM_TO_FLOWER_POT = new Object2ObjectOpenHashMap<>();
    private static final Consumer<BigFlowerPotBlock> CLIENT_HANDLER;
    private final String id;
    private final Block plant;
    private final Item item;
    private BigFlowerPotBlock pot;

    private PottedPlantType(String id, Block plant, Item item) {
        this.id = id;
        this.plant = plant;
        this.item = item;
    }

    public static PottedPlantType fromId(String id) {
        return TYPES.getOrDefault(id, AurorasDecoRegistry.BIG_FLOWER_POT_BLOCK.getPlantType());
    }

    public static @Nullable BigFlowerPotBlock registerFromBlock(Block plant) {
        String id = Registry.BLOCK.getId(plant).toString();
        if (id.startsWith("minecraft:")) {
            id = id.substring("minecraft:".length());
        } else {
            id = id.replace(':', '/');
        }

        if (TYPES.containsKey(id))
            return null;

        return register(id, plant, plant.asItem());
    }

    public static BigFlowerPotBlock register(String id, Block plant, Item item) {
        return register(id, plant, item, BigFlowerPotBlock::new);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BigFlowerPotBlock> T register(String id, Block plant, Item item,
                                                           Function<PottedPlantType, T> flowerPotBlockFactory) {
        PottedPlantType type = new PottedPlantType(id, plant, item);
        TYPES.put(id, type);
        BigFlowerPotBlock potBlock = flowerPotBlockFactory.apply(type);
        type.pot = potBlock;
        if (!type.isEmpty())
            CLIENT_HANDLER.accept(potBlock);
        if (item != Items.AIR || type.isEmpty()) {
            ITEM_TO_FLOWER_POT.put(item, potBlock);
        }
        return (T) potBlock;
    }

    public static BigFlowerPotBlock getFlowerPotFromItem(Item item) {
        return ITEM_TO_FLOWER_POT.getOrDefault(item, AurorasDecoRegistry.BIG_FLOWER_POT_BLOCK);
    }

    public static Stream<PottedPlantType> stream() {
        return TYPES.values().stream();
    }

    public String getId() {
        return this.id;
    }

    public Block getPlant() {
        return this.plant;
    }

    public Item getItem() {
        return this.item;
    }

    /**
     * Returns the big flower pot block associated with this plant type.
     *
     * @return the big flower pot block
     */
    public BigFlowerPotBlock getPot() {
        return this.pot;
    }

    /**
     * Returns whether this plant type is empty or not.
     *
     * @return {@code true} if empty, else {@code false}
     */
    public boolean isEmpty() {
        return this.plant == Blocks.AIR && this.item == Items.AIR;
    }

    public static boolean isValidPlant(Block block) {
        if (block instanceof PlantBlock
                && !(block instanceof CropBlock)
                && !(block instanceof LilyPadBlock)
                && !(block instanceof StemBlock)
                && !(block instanceof AttachedStemBlock)) {
            return true;
        } else return block instanceof CactusBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        PottedPlantType that = (PottedPlantType) o;
        return this.getId().equals(that.getId()) && this.getPlant().equals(that.getPlant()) && Objects.equals(this.getItem(), that.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getPlant(), this.getItem());
    }

    static {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            CLIENT_HANDLER = block -> {
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutoutMipped());

                ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                                world != null && pos != null ? BiomeColors.getGrassColor(world, pos)
                                        : GrassColors.getColor(0.5D, 1.0D),
                        block);
            };
        } else {
            CLIENT_HANDLER = block -> {
            };
        }
    }
}
