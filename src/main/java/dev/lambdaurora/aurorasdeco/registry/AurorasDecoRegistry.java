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

import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.*;
import dev.lambdaurora.aurorasdeco.block.entity.LanternBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * Represents the Aurora's Decorations registry.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoRegistry {
    public static final BigFlowerPotBlock BIG_FLOWER_POT_BLOCK = registerWithItem(
            "big_flower_pot",
            PottedPlantType.register("none", Blocks.AIR, Items.AIR),
            new Item.Settings().group(ItemGroup.DECORATIONS)
    );
    public static final BigFlowerPotBlock.PlantAir PLANT_AIR_BLOCK = register(
            "plant_air",
            new BigFlowerPotBlock.PlantAir(
                    FabricBlockSettings.of(Material.AIR)
                            .nonOpaque()
                            .strength(-1.f, 3600000.f)
                            .dropsNothing()
                            .allowsSpawning((state, world, pos, type) -> false)
            )
    );
    public static final BigPottedCactusBlock BIG_POTTED_CACTUS_BLOCK = registerBigPotted("cactus", Blocks.CACTUS, Items.CACTUS,
            type -> new BigPottedCactusBlock(type, BigPottedCactusBlock.CACTUS_SHAPE));
    public static final BigStaticFlowerPot BIG_POTTED_BAMBOO_BLOCK = registerBigPotted("bamboo", Blocks.AIR, Items.BAMBOO,
            type -> new BigStaticFlowerPot(type, Block.createCuboidShape(
                    7.f, 14.f, 7.f,
                    9.f, 29.f, 9.f
            )));
    public static final BigPottedAzaleaBlock BIG_POTTED_AZALEA_BLOCK = registerBigPotted("azalea", Blocks.AZALEA, Items.AZALEA,
            BigPottedAzaleaBlock::new);
    public static final BigPottedAzaleaBlock BIG_POTTED_FLOWERING_AZALEA_BLOCK = registerBigPotted("flowering_azalea",
            Blocks.FLOWERING_AZALEA, Items.FLOWERING_AZALEA,
            BigPottedAzaleaBlock::new);
    public static final BigPottedSweetBerryBushBlock BIG_POTTED_SWEET_BERRY_BUSH_BLOCK = registerBigPotted("sweet_berry_bush",
            Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES,
            BigPottedSweetBerryBushBlock::new);
    public static final BigStaticFlowerPot BIG_POTTED_TATER_BLOCK = registerBigPotted("tater", Blocks.AIR, Items.POTATO,
            type -> new BigStaticFlowerPot(type, Block.createCuboidShape(
                    4.f, 14.f, 4.f,
                    12.f, 21.f, 12.f
            )));

    public static final WallLanternBlock WALL_LANTERN_BLOCK = register("wall_lantern", new WallLanternBlock());

    public static final BlockEntityType<LanternBlockEntity> LANTERN_BLOCK_ENTITY_TYPE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            id("lantern"),
            FabricBlockEntityTypeBuilder.create(LanternBlockEntity::new, WALL_LANTERN_BLOCK).build()
    );

    public static final Identifier LANTERN_SWING_SOUND_ID = id("block.lantern.swing");
    public static final SoundEvent LANTERN_SWING_SOUND_EVENT = Registry.register(Registry.SOUND_EVENT, LANTERN_SWING_SOUND_ID,
            new SoundEvent(LANTERN_SWING_SOUND_ID)
    );

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(Registry.BLOCK, id(name), block);
    }

    private static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings) {
        register(name, new BlockItem(register(name, block), settings));
        return block;
    }

    private static <T extends BigFlowerPotBlock> T registerBigPotted(String name, Block plant, Item item, Function<PottedPlantType, T> block) {
        return register("big_flower_pot/" + name, PottedPlantType.register(name, plant, item, block));
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registry.ITEM, id(name), item);
    }

    public static void init() {
        List<BigFlowerPotBlock> plants = new ArrayList<>();
        Registry.BLOCK.forEach(block -> {
            if (PottedPlantType.isValidPlant(block)) {
                BigFlowerPotBlock potBlock = PottedPlantType.registerFromBlock(block);
                if (potBlock != null)
                    plants.add(potBlock);
            }
        });

        plants.forEach(pot -> register("big_flower_pot/" + pot.getPlantType().getId(), pot));

        Registry.ITEM.getOrEmpty(new Identifier("pockettools", "pocket_cactus"))
                .ifPresent(pocketCactus -> registerBigPotted("pocket_cactus", Blocks.POTTED_CACTUS, pocketCactus,
                        type -> new BigPottedCactusBlock(type, BigPottedCactusBlock.POCKET_CACTUS_SHAPE)));
    }
}
