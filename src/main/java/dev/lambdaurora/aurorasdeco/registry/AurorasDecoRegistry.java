/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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
import dev.lambdaurora.aurorasdeco.item.DerivedBlockItem;
import dev.lambdaurora.aurorasdeco.item.SeatRestItem;
import dev.lambdaurora.aurorasdeco.item.SignPostItem;
import dev.lambdaurora.aurorasdeco.mixin.SimpleRegistryAccessor;
import dev.lambdaurora.aurorasdeco.recipe.BlackboardCloneRecipe;
import dev.lambdaurora.aurorasdeco.recipe.ExplodingRecipe;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import dev.lambdaurora.aurorasdeco.screen.CopperHopperScreenHandler;
import dev.lambdaurora.aurorasdeco.screen.SawmillScreenHandler;
import dev.lambdaurora.aurorasdeco.screen.ShelfScreenHandler;
import dev.lambdaurora.aurorasdeco.util.Derivator;
import dev.lambdaurora.aurorasdeco.util.Registrar;
import dev.lambdaurora.aurorasdeco.util.RegistrationHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.StatFormatter;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;
import static dev.lambdaurora.aurorasdeco.registry.AurorasDecoParticles.COPPER_SULFATE_FLAME;
import static net.minecraft.stat.Stats.CUSTOM;

/**
 * Represents the Aurora's Decorations registry.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoRegistry {
	private AurorasDecoRegistry() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}

	/* Blocks & Items */

	public static final LanternBlock AMETHYST_LANTERN_BLOCK = registerWithItem("amethyst_lantern",
			new AmethystLanternBlock(), new FabricItemSettings().group(ItemGroup.DECORATIONS),
			DerivedBlockItem::lantern);

	public static final CopperHopperBlock COPPER_HOPPER_BLOCK = registerWithItem("copper_hopper",
			new CopperHopperBlock(QuiltBlockSettings.copyOf(Blocks.HOPPER).mapColor(MapColor.ORANGE)),
			new FabricItemSettings().group(ItemGroup.REDSTONE),
			DerivedBlockItem::hopper);

	public static final LanternBlock COPPER_SULFATE_LANTERN_BLOCK = registerWithItem("copper_sulfate_lantern",
			new LanternBlock(QuiltBlockSettings.copyOf(Blocks.LANTERN)),
			new FabricItemSettings().group(ItemGroup.DECORATIONS),
			DerivedBlockItem::lantern);
	public static final CopperSulfateCampfireBlock COPPER_SULFATE_CAMPFIRE_BLOCK = Registrar.register("copper_sulfate_campfire",
					new CopperSulfateCampfireBlock(QuiltBlockSettings.copyOf(Blocks.CAMPFIRE).ticksRandomly()))
			.withItem(new FabricItemSettings().group(ItemGroup.DECORATIONS), DerivedBlockItem::campfire)
			.addSelfTo(BlockEntityType.CAMPFIRE)
			.finish();
	public static final AuroraTorchBlock COPPER_SULFATE_TORCH_BLOCK = register("copper_sulfate_torch",
			new AuroraTorchBlock(QuiltBlockSettings.copyOf(Blocks.TORCH), COPPER_SULFATE_FLAME));
	public static final AuroraWallTorchBlock COPPER_SULFATE_WALL_TORCH_BLOCK = register("copper_sulfate_wall_torch",
			new AuroraWallTorchBlock(QuiltBlockSettings.copyOf(COPPER_SULFATE_TORCH_BLOCK)
					.dropsLike(COPPER_SULFATE_TORCH_BLOCK), COPPER_SULFATE_FLAME));
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
					QuiltBlockSettings.of(Material.AIR)
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
			new BlackboardBlock(QuiltBlockSettings.of(Material.DECORATION).strength(.2f)
					.nonOpaque()
					.sounds(BlockSoundGroup.WOOD),
					false),
			new FabricItemSettings().group(ItemGroup.DECORATIONS).equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);
	public static final BlackboardBlock WAXED_BLACKBOARD_BLOCK = registerWithItem("waxed_blackboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(BLACKBOARD_BLOCK), true),
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);

	public static final BookPileBlock BOOK_PILE_BLOCK = Registrar.register("book_pile",
					new BookPileBlock(QuiltBlockSettings.of(Material.DECORATION).strength(.2f)
							.nonOpaque()))
			.then(block -> {
				((ItemExtensions) Items.BOOK).makePlaceable(block);
				((ItemExtensions) Items.ENCHANTED_BOOK).makePlaceable(block);
			}).finish();

	public static final BurntVineBlock BURNT_VINE_BLOCK = register("burnt_vine", new BurntVineBlock());

	public static final BlackboardBlock CHALKBOARD_BLOCK = registerWithItem("chalkboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(BLACKBOARD_BLOCK), false),
			new FabricItemSettings().group(ItemGroup.DECORATIONS).equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);
	public static final BlackboardBlock WAXED_CHALKBOARD_BLOCK = registerWithItem("waxed_chalkboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(BLACKBOARD_BLOCK), true),
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);

	public static final Item COPPER_SULFATE_ITEM = register("copper_sulfate", new Item(new FabricItemSettings()
			.group(ItemGroup.MISC)));

	public static final FenceGateBlock NETHER_BRICK_FENCE_GATE = registerWithItem("nether_brick_fence_gate",
			new FenceGateBlock(QuiltBlockSettings.copyOf(Blocks.NETHER_BRICK_FENCE)),
			new FabricItemSettings().group(ItemGroup.REDSTONE));

	public static final PieBlock PUMPKIN_PIE_BLOCK = register("pumpkin_pie", PieBlock.fromPieItem(Items.PUMPKIN_PIE));

	public static final SawmillBlock SAWMILL_BLOCK = registerWithItem("sawmill", new SawmillBlock(),
			new FabricItemSettings().group(ItemGroup.DECORATIONS));

	public static final SturdyStoneBlock STURDY_STONE_BLOCK = registerWithItem("sturdy_stone",
			new SturdyStoneBlock(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(3.5f)),
			new FabricItemSettings().group(ItemGroup.REDSTONE));

	public static final WallLanternBlock WALL_LANTERN_BLOCK = register("wall_lantern",
			new WallLanternBlock((LanternBlock) Blocks.LANTERN));
	public static final WallLanternBlock WALL_SOUL_LANTERN_BLOCK = register("wall_lantern/soul",
			new WallLanternBlock((LanternBlock) Blocks.SOUL_LANTERN));
	public static final BlockEntityType<LanternBlockEntity> WALL_LANTERN_BLOCK_ENTITY_TYPE = Registry.register(
			Registry.BLOCK_ENTITY_TYPE,
			id("lantern"),
			FabricBlockEntityTypeBuilder.create(LanternBlockEntity::new, WALL_LANTERN_BLOCK, WALL_SOUL_LANTERN_BLOCK)
					.build()
	);

	public static final WallLanternBlock AMETHYST_WALL_LANTERN_BLOCK
			= LanternRegistry.registerWallLantern(AMETHYST_LANTERN_BLOCK);

	public static final WindChimeBlock WIND_CHIME_BLOCK = registerWithItem("wind_chime",
			new WindChimeBlock(QuiltBlockSettings.of(Material.DECORATION).nonOpaque()
					.sounds(BlockSoundGroup.AMETHYST_BLOCK)),
			new Item.Settings().group(ItemGroup.DECORATIONS));

	public static final BrazierBlock BRAZIER_BLOCK = registerWithItem("brazier",
			new BrazierBlock(MapColor.BRIGHT_RED, 1, 15, ParticleTypes.FLAME),
			new FabricItemSettings().group(ItemGroup.DECORATIONS));
	public static final BrazierBlock SOUL_BRAZIER_BLOCK = registerWithItem("soul_brazier",
			new BrazierBlock(MapColor.LIGHT_BLUE, 2, 10, ParticleTypes.SOUL),
			new FabricItemSettings().group(ItemGroup.DECORATIONS));
	public static final BrazierBlock COPPER_SULFATE_BRAZIER_BLOCK = registerWithItem("copper_sulfate_brazier",
			new CopperSulfateBrazierBlock(QuiltBlockSettings.copyOf(BRAZIER_BLOCK)
					.mapColor(MapColor.EMERALD_GREEN).luminance(14),
					2, COPPER_SULFATE_FLAME),
			new FabricItemSettings().group(ItemGroup.DECORATIONS));

	private static final Derivator CALCITE_DERIVATOR = new Derivator(Blocks.CALCITE.getDefaultState());
	public static final SlabBlock CALCITE_SLAB = CALCITE_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock CALCITE_STAIRS = CALCITE_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);

	public static final Block POLISHED_CALCITE = registerWithItem("polished_calcite",
			new Block(QuiltBlockSettings.copyOf(Blocks.CALCITE)),
			new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS),
			DerivedBlockItem.itemWithStrictPositionFactory(Items.CALCITE));
	private static final Derivator POLISHED_CALCITE_DERIVATOR = new Derivator(POLISHED_CALCITE.getDefaultState());
	public static final SlabBlock POLISHED_CALCITE_SLAB = POLISHED_CALCITE_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock POLISHED_CALCITE_STAIRS = POLISHED_CALCITE_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);
	public static final WallBlock POLISHED_CALCITE_WALL = POLISHED_CALCITE_DERIVATOR.wall();

	public static final Block CALCITE_BRICKS = registerWithItem("calcite_bricks",
			new Block(QuiltBlockSettings.copyOf(POLISHED_CALCITE)),
			new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS),
			DerivedBlockItem.itemWithStrictPositionFactory(Items.CHISELED_DEEPSLATE));
	private static final Derivator CALCITE_BRICKS_DERIVATOR = new Derivator(CALCITE_BRICKS.getDefaultState());
	public static final Block MOSSY_CALCITE_BRICKS = CALCITE_BRICKS_DERIVATOR.mossy();
	public static final Block CRACKED_CALCITE_BRICKS = CALCITE_BRICKS_DERIVATOR.cracked();
	public static final Block CHISELED_CALCITE_BRICKS = CALCITE_BRICKS_DERIVATOR.chiseled();
	public static final SlabBlock CALCITE_BRICK_SLAB = CALCITE_BRICKS_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock CALCITE_BRICK_STAIRS = CALCITE_BRICKS_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);
	public static final WallBlock CALCITE_BRICK_WALL = CALCITE_BRICKS_DERIVATOR.wall();
	private static final Derivator MOSSY_CALCITE_BRICKS_DERIVATOR = new Derivator(MOSSY_CALCITE_BRICKS.getDefaultState());
	public static final SlabBlock MOSSY_CALCITE_BRICK_SLAB = MOSSY_CALCITE_BRICKS_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock MOSSY_CALCITE_BRICK_STAIRS = MOSSY_CALCITE_BRICKS_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);
	public static final WallBlock MOSSY_CALCITE_BRICK_WALL = MOSSY_CALCITE_BRICKS_DERIVATOR.wall();

	private static final Derivator DEEPSLATE_BRICKS_DERIVATOR = new Derivator(Blocks.DEEPSLATE_BRICKS.getDefaultState());
	public static final Block MOSSY_DEEPSLATE_BRICKS = DEEPSLATE_BRICKS_DERIVATOR.mossy();
	private static final Derivator MOSSY_DEEPSLATE_BRICKS_DERIVATOR = new Derivator(MOSSY_DEEPSLATE_BRICKS.getDefaultState());
	public static final SlabBlock MOSSY_DEEPSLATE_BRICK_SLAB = MOSSY_DEEPSLATE_BRICKS_DERIVATOR.slab(Items.DEEPSLATE_BRICK_SLAB);
	public static final StairsBlock MOSSY_DEEPSLATE_BRICK_STAIRS = MOSSY_DEEPSLATE_BRICKS_DERIVATOR.stairs(Items.DEEPSLATE_BRICK_STAIRS);
	public static final WallBlock MOSSY_DEEPSLATE_BRICK_WALL = MOSSY_DEEPSLATE_BRICKS_DERIVATOR.wall();

	public static final HangingFlowerPotBlock HANGING_FLOWER_POT_BLOCK = register("hanging_flower_pot",
			new HangingFlowerPotBlock((FlowerPotBlock) Blocks.FLOWER_POT));

	public static final FenceLikeWallBlock POLISHED_BASALT_WALL = registerWithItem("polished_basalt_wall",
			new FenceLikeWallBlock(QuiltBlockSettings.copyOf(Blocks.POLISHED_BASALT)),
			new FabricItemSettings().group(ItemGroup.DECORATIONS), DerivedBlockItem::wall);

	private static final Derivator TUFF_DERIVATOR = new Derivator(Blocks.TUFF.getDefaultState());
	public static final SlabBlock TUFF_SLAB = TUFF_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock TUFF_STAIRS = TUFF_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);

	public static final Block POLISHED_TUFF = registerWithItem("polished_tuff",
			new Block(QuiltBlockSettings.copyOf(Blocks.TUFF)),
			new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS),
			DerivedBlockItem.itemWithStrictPositionFactory(Items.TUFF));
	private static final Derivator POLISHED_TUFF_DERIVATOR = new Derivator(POLISHED_TUFF.getDefaultState());
	public static final SlabBlock POLISHED_TUFF_SLAB = POLISHED_TUFF_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock POLISHED_TUFF_STAIRS = POLISHED_TUFF_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);
	public static final WallBlock POLISHED_TUFF_WALL = POLISHED_TUFF_DERIVATOR.wall();

	public static final Block TUFF_BRICKS = registerWithItem("tuff_bricks",
			new Block(QuiltBlockSettings.copyOf(POLISHED_TUFF)),
			new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS),
			DerivedBlockItem.itemWithStrictPositionFactory(CHISELED_CALCITE_BRICKS.asItem()));
	private static final Derivator TUFF_BRICKS_DERIVATOR = new Derivator(TUFF_BRICKS.getDefaultState());
	public static final Block MOSSY_TUFF_BRICKS = TUFF_BRICKS_DERIVATOR.mossy();
	public static final Block CRACKED_TUFF_BRICKS = TUFF_BRICKS_DERIVATOR.cracked();
	public static final Block CHISELED_TUFF_BRICKS = TUFF_BRICKS_DERIVATOR.chiseled();
	public static final SlabBlock TUFF_BRICK_SLAB = TUFF_BRICKS_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock TUFF_BRICK_STAIRS = TUFF_BRICKS_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);
	public static final WallBlock TUFF_BRICK_WALL = TUFF_BRICKS_DERIVATOR.wall();
	private static final Derivator MOSSY_TUFF_BRICKS_DERIVATOR = new Derivator(MOSSY_TUFF_BRICKS.getDefaultState());
	public static final SlabBlock MOSSY_TUFF_BRICK_SLAB = MOSSY_TUFF_BRICKS_DERIVATOR.slab(Items.DEEPSLATE_TILE_SLAB);
	public static final StairsBlock MOSSY_TUFF_BRICK_STAIRS = MOSSY_TUFF_BRICKS_DERIVATOR.stairs(Items.DEEPSLATE_TILE_STAIRS);
	public static final WallBlock MOSSY_TUFF_BRICK_WALL = MOSSY_TUFF_BRICKS_DERIVATOR.wall();

	/* Plants */

	public static final DaffodilBlock DAFFODIL = Registrar.register("daffodil", new DaffodilBlock())
			.withItem(new FabricItemSettings().group(ItemGroup.DECORATIONS), DerivedBlockItem::flower)
			.finish();

	/* Potted Plants */

	public static final FlowerPotBlock POTTED_DAFFODIL = register("potted/daffodil",
			new DirectionalFlowerPotBlock(DAFFODIL, QuiltBlockSettings.of(Material.DECORATION).nonOpaque().breakInstantly()));

	/* Block Entities */

	public static final BlockEntityType<BenchBlockEntity> BENCH_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"bench", BenchBlockEntity::new
	);
	public static final BlockEntityType<BlackboardBlockEntity> BLACKBOARD_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"blackboard",
			BlackboardBlockEntity::new,
			BLACKBOARD_BLOCK, CHALKBOARD_BLOCK, WAXED_BLACKBOARD_BLOCK, WAXED_CHALKBOARD_BLOCK
	);
	public static final BlockEntityType<BookPileBlockEntity> BOOK_PILE_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"book_pile", BookPileBlockEntity::new, BOOK_PILE_BLOCK
	);
	public static final BlockEntityType<CopperHopperBlockEntity> COPPER_HOPPER_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"copper_hopper", CopperHopperBlockEntity::new, COPPER_HOPPER_BLOCK
	);
	public static final BlockEntityType<ShelfBlockEntity> SHELF_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"shelf", ShelfBlockEntity::new
	);
	public static final BlockEntityType<SignPostBlockEntity> SIGN_POST_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"sign_post", SignPostBlockEntity::new
	);
	public static final BlockEntityType<WindChimeBlockEntity> WIND_CHIME_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"wind_chime", WindChimeBlockEntity::new, WIND_CHIME_BLOCK
	);

	/* Screen handlers */

	public static final ScreenHandlerType<CopperHopperScreenHandler> COPPER_HOPPER_SCREEN_HANDLER_TYPE =
			ScreenHandlerRegistry.registerSimple(id("copper_hopper"), CopperHopperScreenHandler::new);

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

	/* POI */

	public static final PointOfInterestType AMETHYST_LANTERN_POI = PointOfInterestHelper.register(
			id("amethyst_lantern"),
			0, 2,
			AMETHYST_LANTERN_BLOCK, AMETHYST_WALL_LANTERN_BLOCK
	);

	/* Advancement Criteria */

	public static final PetUsePetBedCriterion PET_USE_PET_BED_CRITERION = CriterionRegistry
			.register(new PetUsePetBedCriterion());

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

	private static PetBedBlock registerPetBed(DyeColor color) {
		return registerWithItem("pet_bed/" + color.getName(),
				new PetBedBlock(QuiltBlockSettings.of(Material.WOOL)
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

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name,
	                                                                              FabricBlockEntityTypeBuilder.Factory<T> factory,
	                                                                              Block... blocks) {
		return Registry.register(Registry.BLOCK_ENTITY_TYPE, id(name), FabricBlockEntityTypeBuilder.create(factory, blocks).build());
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

	private static Identifier register(String id, StatFormatter statFormatter) {
		var identifier = id(id);
		Registry.register(Registry.CUSTOM_STAT, id, identifier);
		CUSTOM.getOrCreateStat(identifier, statFormatter);
		return identifier;
	}

	@SuppressWarnings("unchecked")
	public static void init() {
		AurorasDecoSounds.init();

		OxidizableBlocksRegistry.registerWaxableBlockPair(BLACKBOARD_BLOCK, WAXED_BLACKBOARD_BLOCK);
		OxidizableBlocksRegistry.registerWaxableBlockPair(CHALKBOARD_BLOCK, WAXED_CHALKBOARD_BLOCK);

		((BlockItemAccessor) Items.FLOWER_POT).aurorasdeco$setCeilingBlock(HANGING_FLOWER_POT_BLOCK);
		Item.BLOCK_ITEMS.put(HANGING_FLOWER_POT_BLOCK, Items.FLOWER_POT);

		RegistrationHelper.BLOCK.addRegistrationCallback((helper, id, block) -> {
			if ((id.getNamespace().equals("betternether") || id.getNamespace().equals("betterend")) && (id.getPath().contains("stripped") || (id.getPath().contains("mushroom") && !id.getPath().contains("mushroom_fir")) || id.getPath().contains("amaranita")))
				return;
			if (id.getNamespace().equals("aurorasdeco") && id.getPath().contains("sign_post")) return;
			if (PottedPlantType.isValidPlant(block)) {
				var potBlock = PottedPlantType.registerFromBlock(block);
				if (potBlock != null)
					helper.register("big_flower_pot/" + potBlock.getPlantType().getId(), potBlock);
			} else if (block instanceof FlowerPotBlock flowerPotBlock) {
				if (block == Blocks.FLOWER_POT) return;

				RegistrationHelper.BLOCK.register(
						RegistrationHelper.getIdPath("hanging_flower_pot", id, "^potted_"),
						new HangingFlowerPotBlock(flowerPotBlock)
				);
			} else {
				WoodType.onBlockRegister(id, block);
				if (block instanceof FenceBlock fenceBlock) {
					var signPostBlock = RegistrationHelper.BLOCK.register(
							RegistrationHelper.getIdPath("sign_post", id, "_fence$"),
							new SignPostBlock(fenceBlock)
					);

					((BlockEntityTypeAccessor) SIGN_POST_BLOCK_ENTITY_TYPE).aurorasdeco$addSupportedBlock(signPostBlock);
				} else LanternRegistry.tryRegisterWallLantern(block, id);
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

		WoodType.registerWoodTypeModificationCallback(woodType -> Registrar.register("shelf/" + woodType.getPathName(), new ShelfBlock(woodType))
						.withItem(new FabricItemSettings().group(ItemGroup.DECORATIONS))
						.addSelfTo(SHELF_BLOCK_ENTITY_TYPE)
						.flammable(woodType.getComponent(WoodType.ComponentType.PLANKS).getFlammableEntry()),
				WoodType.ComponentType.PLANKS);

		WoodType.registerWoodTypeModificationCallback(woodType -> {
			register("seat_rest/" + woodType.getPathName(),
					new SeatRestItem(woodType, new FabricItemSettings().group(ItemGroup.MISC)));
			register("sign_post/" + woodType.getPathName(),
					new SignPostItem(woodType, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
		}, WoodType.ComponentType.PLANKS);

		WoodType.registerWoodTypeModificationCallback(woodType -> Registrar.register("bench/" + woodType.getPathName(), new BenchBlock(woodType))
						.withItem(new FabricItemSettings().group(ItemGroup.DECORATIONS))
						.addSelfTo(BENCH_BLOCK_ENTITY_TYPE)
						.flammable(woodType.getComponent(WoodType.ComponentType.PLANKS).getFlammableEntry()),
				WoodType.ComponentType.PLANKS);

		FlammableBlockRegistry.getDefaultInstance().add(AurorasDecoTags.PET_BEDS, 10, 30);
	}
}
