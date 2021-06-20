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
import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.accessor.BlockEntityTypeAccessor;
import dev.lambdaurora.aurorasdeco.accessor.BlockItemAccessor;
import dev.lambdaurora.aurorasdeco.accessor.ItemExtensions;
import dev.lambdaurora.aurorasdeco.advancement.PetUsePetBedCriterion;
import dev.lambdaurora.aurorasdeco.block.*;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.*;
import dev.lambdaurora.aurorasdeco.block.entity.*;
import dev.lambdaurora.aurorasdeco.entity.FakeLeashKnotEntity;
import dev.lambdaurora.aurorasdeco.entity.SeatEntity;
import dev.lambdaurora.aurorasdeco.item.BlackboardItem;
import dev.lambdaurora.aurorasdeco.item.SeatRestItem;
import dev.lambdaurora.aurorasdeco.mixin.SimpleRegistryAccessor;
import dev.lambdaurora.aurorasdeco.recipe.BlackboardCloneRecipe;
import dev.lambdaurora.aurorasdeco.recipe.ExplodingRecipe;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import dev.lambdaurora.aurorasdeco.screen.SawmillScreenHandler;
import dev.lambdaurora.aurorasdeco.screen.ShelfScreenHandler;
import dev.lambdaurora.aurorasdeco.util.RegistrationHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatFormatter;
import net.minecraft.tag.Tag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;
import static net.minecraft.stat.Stats.CUSTOM;

/**
 * Represents the Aurora's Decorations registry.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoRegistry {
    /* Particles */

    public static final DefaultParticleType AMETHYST_GLINT = registerParticle("amethyst_glint");
    public static final DefaultParticleType COPPER_SULFATE_FLAME = registerParticle("copper_sulfate_flame");
    public static final DefaultParticleType COPPER_SULFATE_LAVA = registerParticle("copper_sulfate_lava");

    /* Blocks & Items */

    public static final LanternBlock AMETHYST_LANTERN_BLOCK = registerWithItem("amethyst_lantern",
            new AmethystLanternBlock(), new FabricItemSettings().group(ItemGroup.DECORATIONS));

    public static final LanternBlock COPPER_SULFATE_LANTERN_BLOCK = registerWithItem("copper_sulfate_lantern",
            new LanternBlock(FabricBlockSettings.copyOf(Blocks.LANTERN)), new FabricItemSettings().group(ItemGroup.DECORATIONS));
    public static final CopperSulfateCampfireBlock COPPER_SULFATE_CAMPFIRE_BLOCK = registerWithItem("copper_sulfate_campfire",
            new CopperSulfateCampfireBlock(FabricBlockSettings.copyOf(Blocks.CAMPFIRE).ticksRandomly()),
            new FabricItemSettings().group(ItemGroup.DECORATIONS));
    public static final AuroraTorchBlock COPPER_SULFATE_TORCH_BLOCK = register("copper_sulfate_torch",
            new AuroraTorchBlock(FabricBlockSettings.copyOf(Blocks.TORCH), COPPER_SULFATE_FLAME));
    public static final AuroraWallTorchBlock COPPER_SULFATE_WALL_TORCH_BLOCK = register("copper_sulfate_wall_torch",
            new AuroraWallTorchBlock(FabricBlockSettings.copyOf(COPPER_SULFATE_TORCH_BLOCK).dropsLike(COPPER_SULFATE_TORCH_BLOCK), COPPER_SULFATE_FLAME));
    public static final WallStandingBlockItem COPPER_SULFATE_TORCH_ITEM = register("copper_sulfate_torch",
            new WallStandingBlockItem(COPPER_SULFATE_TORCH_BLOCK, COPPER_SULFATE_WALL_TORCH_BLOCK,
                    new FabricItemSettings().group(ItemGroup.DECORATIONS)));

    public static final BigFlowerPotBlock BIG_FLOWER_POT_BLOCK = registerWithItem(
            "big_flower_pot",
            PottedPlantType.register("none", Blocks.AIR, Items.AIR),
            new FabricItemSettings().group(ItemGroup.DECORATIONS)
    );
    public static final BigFlowerPotBlock.PlantAirBlock PLANT_AIR_BLOCK = register(
            "plant_air",
            new BigFlowerPotBlock.PlantAirBlock(
                    FabricBlockSettings.of(Material.AIR)
                            .nonOpaque()
                            .strength(-1.f, 3600000.f)
                            .dropsNothing()
                            .allowsSpawning((state, world, pos, type) -> false)
            )
    );
    public static final BigPottedCactusBlock BIG_POTTED_CACTUS_BLOCK = registerBigPotted("cactus",
            Blocks.CACTUS, Items.CACTUS,
            type -> new BigPottedCactusBlock(type, BigPottedCactusBlock.CACTUS_SHAPE));
    public static final BigStaticFlowerPotBlock BIG_POTTED_BAMBOO_BLOCK = registerBigPotted("bamboo",
            Blocks.AIR, Items.BAMBOO,
            type -> new BigStaticFlowerPotBlock(type, Block.createCuboidShape(
                    7.f, 14.f, 7.f,
                    9.f, 29.f, 9.f
            )));
    public static final BigPottedAzaleaBlock BIG_POTTED_AZALEA_BLOCK = registerBigPotted("azalea",
            Blocks.AZALEA, Items.AZALEA,
            BigPottedAzaleaBlock::new);
    public static final BigPottedAzaleaBlock BIG_POTTED_FLOWERING_AZALEA_BLOCK = registerBigPotted("flowering_azalea",
            Blocks.FLOWERING_AZALEA, Items.FLOWERING_AZALEA,
            BigPottedAzaleaBlock::new);
    public static final BigPottedSweetBerryBushBlock BIG_POTTED_SWEET_BERRY_BUSH_BLOCK =
            registerBigPotted("sweet_berry_bush",
                    Blocks.SWEET_BERRY_BUSH, Items.SWEET_BERRIES,
                    BigPottedSweetBerryBushBlock::new);
    public static final BigStaticFlowerPotBlock BIG_POTTED_TATER_BLOCK = registerBigPotted("tater",
            Blocks.AIR, Items.POTATO,
            type -> new BigStaticFlowerPotBlock(type, Block.createCuboidShape(
                    4.f, 14.f, 4.f,
                    12.f, 21.f, 12.f
            )));

    public static final BlackboardBlock BLACKBOARD_BLOCK = registerWithItem("blackboard",
            new BlackboardBlock(FabricBlockSettings.of(Material.DECORATION).strength(.2f)
                    .nonOpaque()
                    .sounds(BlockSoundGroup.WOOD),
                    false),
            new FabricItemSettings().group(ItemGroup.DECORATIONS).equipmentSlot(stack -> EquipmentSlot.HEAD),
            BlackboardItem::new);
    public static final BlackboardBlock WAXED_BLACKBOARD_BLOCK = registerWithItem("waxed_blackboard",
            new BlackboardBlock(FabricBlockSettings.copyOf(BLACKBOARD_BLOCK), true),
            new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
            BlackboardItem::new);

    public static final BookPileBlock BOOK_PILE_BLOCK = register("book_pile",
            new BookPileBlock(FabricBlockSettings.of(Material.DECORATION).strength(.2f)
                    .nonOpaque()));

    public static final BurntVineBlock BURNT_VINE_BLOCK = register("burnt_vine", new BurntVineBlock());

    public static final BlackboardBlock CHALKBOARD_BLOCK = registerWithItem("chalkboard",
            new BlackboardBlock(FabricBlockSettings.copyOf(BLACKBOARD_BLOCK), false),
            new FabricItemSettings().group(ItemGroup.DECORATIONS).equipmentSlot(stack -> EquipmentSlot.HEAD),
            BlackboardItem::new);
    public static final BlackboardBlock WAXED_CHALKBOARD_BLOCK = registerWithItem("waxed_chalkboard",
            new BlackboardBlock(FabricBlockSettings.copyOf(BLACKBOARD_BLOCK), true),
            new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
            BlackboardItem::new);

    public static final Item COPPER_SULFATE_ITEM = register("copper_sulfate", new Item(new FabricItemSettings().group(ItemGroup.MISC)));

    public static final FenceGateBlock NETHER_BRICK_FENCE_GATE = registerWithItem("nether_brick_fence_gate",
            new FenceGateBlock(FabricBlockSettings.copyOf(Blocks.NETHER_BRICK_FENCE)),
            new FabricItemSettings().group(ItemGroup.REDSTONE));

    public static final PieBlock PUMPKIN_PIE_BLOCK = register("pumpkin_pie", PieBlock.fromPieItem(Items.PUMPKIN_PIE));

    public static final SawmillBlock SAWMILL_BLOCK = registerWithItem("sawmill", new SawmillBlock(),
            new FabricItemSettings().group(ItemGroup.DECORATIONS));

    public static final SturdyStoneBlock STURDY_STONE_BLOCK = registerWithItem("sturdy_stone",
            new SturdyStoneBlock(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3.5f)),
            new FabricItemSettings().group(ItemGroup.REDSTONE));

    public static final WallLanternBlock WALL_LANTERN_BLOCK = register("wall_lantern",
            new WallLanternBlock((LanternBlock) Blocks.LANTERN));
    public static final WallLanternBlock WALL_SOUL_LANTERN_BLOCK = register("wall_lantern/soul",
            new WallLanternBlock((LanternBlock) Blocks.SOUL_LANTERN));
    public static final BlockEntityType<LanternBlockEntity> WALL_LANTERN_BLOCK_ENTITY_TYPE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            id("lantern"),
            FabricBlockEntityTypeBuilder.create(LanternBlockEntity::new, WALL_LANTERN_BLOCK, WALL_SOUL_LANTERN_BLOCK).build()
    );

    public static final WallLanternBlock AMETHYST_WALL_LANTERN_BLOCK = LanternRegistry.registerWallLantern(AMETHYST_LANTERN_BLOCK);

    public static final WindChimeBlock WIND_CHIME_BLOCK = registerWithItem("wind_chime",
            new WindChimeBlock(FabricBlockSettings.of(Material.DECORATION).nonOpaque()
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)),
            new Item.Settings().group(ItemGroup.DECORATIONS));

    public static final BrazierBlock BRAZIER_BLOCK = registerWithItem("brazier",
            new BrazierBlock(MapColor.BRIGHT_RED, 1, 15, ParticleTypes.FLAME),
            new FabricItemSettings().group(ItemGroup.DECORATIONS));
    public static final BrazierBlock SOUL_BRAZIER_BLOCK = registerWithItem("soul_brazier",
            new BrazierBlock(MapColor.LIGHT_BLUE, 2, 10, ParticleTypes.SOUL),
            new FabricItemSettings().group(ItemGroup.DECORATIONS));
    public static final BrazierBlock COPPER_SULFATE_BRAZIER_BLOCK = registerWithItem("copper_sulfate_brazier",
            new CopperSulfateBrazierBlock(FabricBlockSettings.copyOf(BRAZIER_BLOCK).mapColor(MapColor.EMERALD_GREEN).luminance(14),
                    2, COPPER_SULFATE_FLAME),
            new FabricItemSettings().group(ItemGroup.DECORATIONS));

    public static final HangingFlowerPotBlock HANGING_FLOWER_POT_BLOCK = register("hanging_flower_pot",
            new HangingFlowerPotBlock((FlowerPotBlock) Blocks.FLOWER_POT));

    public static final FenceLikeWallBlock POLISHED_BASALT_WALL = registerWithItem("polished_basalt_wall",
            new FenceLikeWallBlock(FabricBlockSettings.copyOf(Blocks.POLISHED_BASALT)),
            new FabricItemSettings().group(ItemGroup.DECORATIONS));

    /* Block Entities */

    public static final BlockEntityType<BenchBlockEntity> BENCH_BLOCK_ENTITY_TYPE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            id("bench"),
            FabricBlockEntityTypeBuilder.create(BenchBlockEntity::new).build()
    );
    public static final BlockEntityType<BlackboardBlockEntity> BLACKBOARD_BLOCK_ENTITY_TYPE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            id("blackboard"),
            FabricBlockEntityTypeBuilder.create(BlackboardBlockEntity::new,
                    BLACKBOARD_BLOCK, CHALKBOARD_BLOCK, WAXED_BLACKBOARD_BLOCK, WAXED_CHALKBOARD_BLOCK)
                    .build()
    );
    public static final BlockEntityType<BookPileBlockEntity> BOOK_PILE_BLOCK_ENTITY_TYPE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            id("book_pile"),
            FabricBlockEntityTypeBuilder.create(BookPileBlockEntity::new, BOOK_PILE_BLOCK).build()
    );
    public static final BlockEntityType<ShelfBlockEntity> SHELF_BLOCK_ENTITY_TYPE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            id("shelf"),
            FabricBlockEntityTypeBuilder.create(ShelfBlockEntity::new).build()
    );
    public static final BlockEntityType<WindChimeBlockEntity> WIND_CHIME_BLOCK_ENTITY_TYPE = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            id("wind_chime"),
            FabricBlockEntityTypeBuilder.create(WindChimeBlockEntity::new, WIND_CHIME_BLOCK).build()
    );

    public static final ScreenHandlerType<SawmillScreenHandler> SAWMILL_SCREEN_HANDLER_TYPE =
            ScreenHandlerRegistry.registerSimple(id("sawmill"), SawmillScreenHandler::new);

    public static final ScreenHandlerType<ShelfScreenHandler> SHELF_SCREEN_HANDLER_TYPE =
            ScreenHandlerRegistry.registerExtended(id("shelf"), ShelfScreenHandler::new);

    /* Entities */

    public static final EntityType<FakeLeashKnotEntity> FAKE_LEASH_KNOT_ENTITY_TYPE = Registry.register(
            Registry.ENTITY_TYPE,
            id("fake_leash_knot"),
            FabricEntityTypeBuilder.<FakeLeashKnotEntity>createMob()
                    .entityFactory(FakeLeashKnotEntity::new)
                    .dimensions(EntityDimensions.fixed(.375f, .5f))
                    .defaultAttributes(MobEntity::createMobAttributes)
                    .forceTrackedVelocityUpdates(false)
                    .trackRangeChunks(10)
                    .trackedUpdateRate(Integer.MAX_VALUE)
                    .build()
    );
    public static final EntityType<SeatEntity> SEAT_ENTITY_TYPE = Registry.register(
            Registry.ENTITY_TYPE,
            id("seat"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, SeatEntity::new)
                    .dimensions(EntityDimensions.fixed(0.f, 0.f))
                    .disableSaving()
                    .disableSummon()
                    .trackRangeChunks(10)
                    .build()
    );

    /* Stats */

    public static final Identifier INTERACT_WITH_SAWMILL = register("interact_with_sawmill", StatFormatter.DEFAULT);

    /* Sounds */

    public static final SoundEvent BRAZIER_CRACKLE_SOUND_EVENT = registerSound("block.brazier.crackle");
    public static final SoundEvent LANTERN_SWING_SOUND_EVENT = registerSound("block.lantern.swing");
    public static final SoundEvent ARMOR_STAND_HIDE_BASE_PLATE_SOUND_EVENT = registerSound("entity.armor_stand.hide_base_plate");
    public static final SoundEvent ITEM_FRAME_HIDE_BACKGROUND_EVENT = registerSound("entity.item_frame.hide_background");

    /* Recipes */

    public static final SpecialRecipeSerializer<BlackboardCloneRecipe> BLACKBOARD_CLONE_RECIPE_SERIALIZER
            = register("crafting_special_blackboard_clone",
            new SpecialRecipeSerializer<>(BlackboardCloneRecipe::new));

    public static final Identifier EXPLODING_RECIPE_ID = id("exploding");
    public static final RecipeType<ExplodingRecipe> EXPLODING_RECIPE_TYPE = registerRecipeType("exploding");
    public static final RecipeSerializer<ExplodingRecipe> EXPLODING_RECIPE_SERIALIZER
            = register("exploding", ExplodingRecipe.SERIALIZER);

    public static final Identifier WOODCUTTING_RECIPE_ID = id("woodcutting");
    public static final RecipeType<WoodcuttingRecipe> WOODCUTTING_RECIPE_TYPE = registerRecipeType("woodcutting");
    public static final RecipeSerializer<WoodcuttingRecipe> WOODCUTTING_RECIPE_SERIALIZER
            = register("woodcutting", WoodcuttingRecipe.SERIALIZER);

    /* Tags */

    public static final Tag<Item> BLACKBOARD_ITEMS = TagRegistry.item(id("blackboards"));
    public static final Tag<Block> BRAZIERS = TagRegistry.block(id("braziers"));
    public static final Tag<Block> COPPER_SULFATE_DECOMPOSABLE = TagRegistry.block(id("copper_sulfate_decomposable"));
    public static final Tag<Block> PET_BEDS = TagRegistry.block(id("pet_beds"));
    public static final Tag<Block> SHELVES = TagRegistry.block(id("shelves"));
    public static final Tag<Block> SMALL_LOG_PILES = TagRegistry.block(id("small_log_piles"));
    public static final Tag<Block> STUMPS = TagRegistry.block(id("stumps"));

    /* POI */

    public static final PointOfInterestType AMETHYST_LANTERN_POI = PointOfInterestHelper.register(
            id("amethyst_lantern"),
            0, 2,
            AMETHYST_LANTERN_BLOCK, AMETHYST_WALL_LANTERN_BLOCK
    );

    /* Advancement Criteria */

    public static final PetUsePetBedCriterion PET_USE_PET_BED_CRITERION = CriterionRegistry.register(new PetUsePetBedCriterion());

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(Registry.BLOCK, id(name), block);
    }

    private static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings) {
        return registerWithItem(name, block, settings, BlockItem::new);
    }

    private static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings,
                                                        BiFunction<T, Item.Settings, BlockItem> factory) {
        register(name, factory.apply(register(name, block), settings));
        return block;
    }

    private static DefaultParticleType registerParticle(String name) {
        return Registry.register(Registry.PARTICLE_TYPE, AurorasDeco.id(name), FabricParticleTypes.simple());
    }

    private static PetBedBlock registerPetBed(DyeColor color) {
        return registerWithItem("pet_bed/" + color.getName(),
                new PetBedBlock(FabricBlockSettings.of(Material.WOOL)
                        .mapColor(color).sounds(BlockSoundGroup.WOOD).strength(.2f)),
                new FabricItemSettings().group(ItemGroup.DECORATIONS));
    }

    private static <T extends BigFlowerPotBlock> T registerBigPotted(String name, Block plant, Item item,
                                                                     Function<PottedPlantType, T> block) {
        return register("big_flower_pot/" + name, PottedPlantType.register(name, plant, item, block));
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registry.ITEM, id(name), item);
    }

    private static <R extends Recipe<?>, T extends RecipeSerializer<R>> T register(String name, T recipe) {
        return Registry.register(Registry.RECIPE_SERIALIZER, id(name), recipe);
    }

    private static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String id) {
        return Registry.register(Registry.RECIPE_TYPE, id(id), new RecipeType<T>() {
            public String toString() {
                return id;
            }
        });
    }

    private static SoundEvent registerSound(String path) {
        var id = id(path);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    private static Identifier register(String id, StatFormatter statFormatter) {
        var identifier = id(id);
        Registry.register(Registry.CUSTOM_STAT, id, identifier);
        CUSTOM.getOrCreateStat(identifier, statFormatter);
        return identifier;
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        ((BlockEntityTypeAccessor) BlockEntityType.CAMPFIRE).aurorasdeco$addSupportedBlock(COPPER_SULFATE_CAMPFIRE_BLOCK);
        ((BlockItemAccessor) Items.FLOWER_POT).aurorasdeco$setCeilingBlock(HANGING_FLOWER_POT_BLOCK);
        Item.BLOCK_ITEMS.put(HANGING_FLOWER_POT_BLOCK, Items.FLOWER_POT);

        RegistrationHelper.BLOCK.addRegistrationCallback((helper, id, block) -> {
            if (PottedPlantType.isValidPlant(block)) {
                var potBlock = PottedPlantType.registerFromBlock(block);
                if (potBlock != null)
                    helper.register("big_flower_pot/" + potBlock.getPlantType().getId(), potBlock);
            } else if (block instanceof FlowerPotBlock flowerPotBlock) {
                if (block == Blocks.FLOWER_POT) return;

                RegistrationHelper.BLOCK.register(RegistrationHelper.getIdPath("hanging_flower_pot", id, "^potted_"),
                        new HangingFlowerPotBlock(flowerPotBlock));
            } else {
                WoodType.onBlockRegister(id, block);
                LanternRegistry.tryRegisterWallLantern(block, id);
            }
        });

        RegistrationHelper.BLOCK.init();

        ((SimpleRegistryAccessor<Item>) Registry.ITEM).getIdToEntry()
                .forEach(Blackboard.Color::tryRegisterColorFromItem);

        Registry.ITEM.getOrEmpty(new Identifier("pockettools", "pocket_cactus"))
                .ifPresent(pocketCactus -> registerBigPotted("pocket_cactus", Blocks.POTTED_CACTUS, pocketCactus,
                        type -> new BigPottedCactusBlock(type, BigPottedCactusBlock.POCKET_CACTUS_SHAPE)));

        var colors = DyeColor.values();
        for (var color : colors) {
            registerPetBed(color);
        }
        for (var color : colors) {
            var block = SleepingBagBlock.register(color);
            register("sleeping_bag/" + block.getColor().getName(),
                    new BlockItem(block, new FabricItemSettings().maxCount(1).group(ItemGroup.DECORATIONS)));
        }
        SleepingBagBlock.appendToPointOfInterest(PointOfInterestType.HOME);

        WoodType.registerWoodTypeModificationCallback(woodType -> {
            var block = registerWithItem("stump/" + woodType.getPathName(),
                    new StumpBlock(woodType),
                    new FabricItemSettings().group(ItemGroup.DECORATIONS));

            var entry = woodType.getComponent(WoodType.ComponentType.LOG).getFlammableEntry();
            if (entry != null && entry.getBurnChance() != 0 && entry.getSpreadChance() != 0)
                FlammableBlockRegistry.getDefaultInstance().add(block, entry.getBurnChance(), entry.getSpreadChance());
        }, WoodType.ComponentType.LOG);

        WoodType.registerWoodTypeModificationCallback(woodType -> {
            var block = registerWithItem("small_log_pile/" + woodType.getPathName(),
                    new SmallLogPileBlock(woodType),
                    new FabricItemSettings().group(ItemGroup.DECORATIONS));

            var entry = woodType.getComponent(WoodType.ComponentType.LOG).getFlammableEntry();
            if (entry != null && entry.getBurnChance() != 0 && entry.getSpreadChance() != 0)
                FlammableBlockRegistry.getDefaultInstance().add(block, entry.getBurnChance(), entry.getSpreadChance());
        }, WoodType.ComponentType.LOG);

        WoodType.registerWoodTypeModificationCallback(woodType -> {
            var block = registerWithItem("shelf/" + woodType.getPathName(),
                    new ShelfBlock(woodType),
                    new FabricItemSettings().group(ItemGroup.DECORATIONS));

            ((BlockEntityTypeAccessor) AurorasDecoRegistry.SHELF_BLOCK_ENTITY_TYPE)
                    .aurorasdeco$addSupportedBlock(block);

            var entry = woodType.getComponent(WoodType.ComponentType.PLANKS).getFlammableEntry();
            if (entry != null && entry.getBurnChance() != 0 && entry.getSpreadChance() != 0)
                FlammableBlockRegistry.getDefaultInstance().add(block, entry.getBurnChance(), entry.getSpreadChance());
        }, WoodType.ComponentType.PLANKS);

        WoodType.registerWoodTypeModificationCallback(woodType -> {
            register("seat_rest/" + woodType.getPathName(),
                    new SeatRestItem(woodType, new FabricItemSettings().group(ItemGroup.MISC)));
        }, WoodType.ComponentType.PLANKS);

        WoodType.registerWoodTypeModificationCallback(woodType -> {
            var block = registerWithItem("bench/" + woodType.getPathName(),
                    new BenchBlock(woodType),
                    new FabricItemSettings().group(ItemGroup.DECORATIONS));

            ((BlockEntityTypeAccessor) AurorasDecoRegistry.BENCH_BLOCK_ENTITY_TYPE)
                    .aurorasdeco$addSupportedBlock(block);

            var entry = woodType.getComponent(WoodType.ComponentType.PLANKS).getFlammableEntry();
            if (entry != null && entry.getBurnChance() != 0 && entry.getSpreadChance() != 0)
                FlammableBlockRegistry.getDefaultInstance().add(block, entry.getBurnChance(), entry.getSpreadChance());
        }, WoodType.ComponentType.PLANKS);

        FlammableBlockRegistry.getDefaultInstance().add(PET_BEDS, 10, 30);

        ((ItemExtensions) Items.BOOK).makePlaceable(BOOK_PILE_BLOCK);
        ((ItemExtensions) Items.ENCHANTED_BOOK).makePlaceable(BOOK_PILE_BLOCK);
    }
}
