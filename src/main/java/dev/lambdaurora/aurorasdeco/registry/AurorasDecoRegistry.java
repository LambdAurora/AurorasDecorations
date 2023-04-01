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

import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;
import com.terraformersmc.terraform.boat.api.item.TerraformBoatItemHelper;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.accessor.BlockItemAccessor;
import dev.lambdaurora.aurorasdeco.accessor.ItemExtensions;
import dev.lambdaurora.aurorasdeco.advancement.PetUsePetBedCriterion;
import dev.lambdaurora.aurorasdeco.block.*;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigPottedCactusBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigStaticFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.block.entity.*;
import dev.lambdaurora.aurorasdeco.item.*;
import dev.lambdaurora.aurorasdeco.item.group.ItemTree;
import dev.lambdaurora.aurorasdeco.recipe.BlackboardCloneRecipe;
import dev.lambdaurora.aurorasdeco.recipe.ExplodingRecipe;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import dev.lambdaurora.aurorasdeco.util.Derivator;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.StatFormatter;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.SignType;
import net.minecraft.util.math.Direction;
import net.minecraft.world.poi.PointOfInterestType;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.quiltmc.qsl.poi.api.PointOfInterestHelper;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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
@SuppressWarnings("unused")
public final class AurorasDecoRegistry {
	private AurorasDecoRegistry() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}

	/* Blocks & Items */

	public static final AmethystLanternBlock AMETHYST_LANTERN_BLOCK = registerWithItem("amethyst_lantern",
			new AmethystLanternBlock(), new QuiltItemSettings());

	//region Azalea
	public static final PillarBlock AZALEA_LOG_BLOCK = registerWithItem("azalea_log",
			createLogBlock(MapColor.DULL_PINK, MapColor.DARK_DULL_PINK),
			new QuiltItemSettings());
	public static final PillarBlock STRIPPED_AZALEA_LOG_BLOCK = registerWithItem("stripped_azalea_log",
			new PillarBlock(QuiltBlockSettings.copyOf(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.DARK_DULL_PINK)),
			new QuiltItemSettings());
	public static final PillarBlock STRIPPED_AZALEA_WOOD_BLOCK = registerWithItem("stripped_azalea_wood",
			new PillarBlock(QuiltBlockSettings.copyOf(STRIPPED_AZALEA_LOG_BLOCK)),
			new QuiltItemSettings());
	public static final PillarBlock AZALEA_WOOD_BLOCK = registerWithItem("azalea_wood",
			createLogBlock(MapColor.DARK_DULL_PINK, MapColor.DARK_DULL_PINK),
			new QuiltItemSettings());
	public static final PillarBlock FLOWERING_AZALEA_LOG_BLOCK = registerWithItem("flowering_azalea_log",
			createFloweringLogBlock(() -> AZALEA_LOG_BLOCK, MapColor.DULL_PINK, MapColor.DARK_DULL_PINK),
			new QuiltItemSettings());
	public static final PillarBlock FLOWERING_AZALEA_WOOD_BLOCK = registerWithItem("flowering_azalea_wood",
			createFloweringLogBlock(() -> AZALEA_WOOD_BLOCK, MapColor.DULL_PINK, MapColor.DARK_DULL_PINK),
			new QuiltItemSettings());
	public static final Block AZALEA_PLANKS_BLOCK = registerWithItem("azalea_planks",
			new Block(QuiltBlockSettings.copyOf(Blocks.OAK_PLANKS).mapColor(MapColor.DULL_PINK)),
			new QuiltItemSettings());
	public static final Block AZALEA_SLAB_BLOCK = registerWithItem("azalea_slab",
			new SlabBlock(QuiltBlockSettings.copyOf(AZALEA_PLANKS_BLOCK)), new QuiltItemSettings()
	);
	public static final Block AZALEA_STAIRS_BLOCK = registerWithItem("azalea_stairs",
			new StairsBlock(AZALEA_PLANKS_BLOCK.getDefaultState(), QuiltBlockSettings.copyOf(AZALEA_PLANKS_BLOCK)),
			new QuiltItemSettings()
	);
	public static final Block AZALEA_BUTTON_BLOCK = registerWithItem("azalea_button",
			new AbstractButtonBlock(QuiltBlockSettings.copyOf(Blocks.OAK_BUTTON), BlockSetType.OAK, 30, true),
			new QuiltItemSettings()
	);
	public static final DoorBlock AZALEA_DOOR = registerWithItem("azalea_door",
			new DoorBlock(QuiltBlockSettings.copyOf(Blocks.OAK_DOOR).mapColor(AZALEA_PLANKS_BLOCK.getDefaultMapColor()), BlockSetType.OAK),
			new QuiltItemSettings()
	);
	public static final FenceBlock AZALEA_FENCE_BLOCK = registerWithItem("azalea_fence",
			new FenceBlock(QuiltBlockSettings.copyOf(AZALEA_PLANKS_BLOCK)),
			new QuiltItemSettings());
	public static final FenceGateBlock AZALEA_FENCE_GATE_BLOCK = registerWithItem("azalea_fence_gate",
			new FenceGateBlock(QuiltBlockSettings.copyOf(AZALEA_PLANKS_BLOCK), SignType.OAK), new QuiltItemSettings());
	public static final Block AZALEA_PRESSURE_PLATE_BLOCK = registerWithItem("azalea_pressure_plate",
			new PressurePlateBlock(
					PressurePlateBlock.ActivationRule.EVERYTHING,
					QuiltBlockSettings.copyOf(Blocks.OAK_PRESSURE_PLATE).mapColor(AZALEA_PLANKS_BLOCK.getDefaultMapColor()),
					BlockSetType.OAK
			),
			new QuiltItemSettings()
	);
	public static final TrapdoorBlock AZALEA_TRAPDOOR = registerWithItem("azalea_trapdoor",
			new TrapdoorBlock(QuiltBlockSettings.copyOf(Blocks.OAK_TRAPDOOR).mapColor(AZALEA_PLANKS_BLOCK.getDefaultMapColor()), BlockSetType.OAK),
			new QuiltItemSettings()
	);

	public static final RegistryKey<TerraformBoatType> AZALEA_BOAT_TYPE_KEY = TerraformBoatTypeRegistry.createKey(id("azalea"));
	public static final Item AZALEA_BOAT_ITEM = TerraformBoatItemHelper.registerBoatItem(
			id("azalea_boat"), AZALEA_BOAT_TYPE_KEY, false
	);
	public static final Item AZALEA_CHEST_BOAT_ITEM = TerraformBoatItemHelper.registerBoatItem(
			id("azalea_chest_boat"), AZALEA_BOAT_TYPE_KEY, true
	);
	public static final TerraformBoatType AZALEA_BOAT_TYPE = Registry.register(TerraformBoatTypeRegistry.INSTANCE, AZALEA_BOAT_TYPE_KEY,
			new TerraformBoatType.Builder().item(AZALEA_BOAT_ITEM).chestItem(AZALEA_CHEST_BOAT_ITEM).build()
	);

	public static final SignData AZALEA_SIGNS = new SignData("azalea", AZALEA_PLANKS_BLOCK);
	//endregion

	//region Jacaranda
	public static final PillarBlock JACARANDA_LOG_BLOCK = registerWithItem("jacaranda_log",
			createLogBlock(MapColor.PALE_PURPLE, MapColor.TERRACOTTA_PURPLE),
			new QuiltItemSettings());
	public static final PillarBlock STRIPPED_JACARANDA_LOG_BLOCK = registerWithItem("stripped_jacaranda_log",
			new PillarBlock(QuiltBlockSettings.copyOf(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.TERRACOTTA_PURPLE)),
			new QuiltItemSettings());
	public static final PillarBlock STRIPPED_JACARANDA_WOOD_BLOCK = registerWithItem("stripped_jacaranda_wood",
			new PillarBlock(QuiltBlockSettings.copyOf(STRIPPED_JACARANDA_LOG_BLOCK)),
			new QuiltItemSettings());
	public static final PillarBlock JACARANDA_WOOD_BLOCK = registerWithItem("jacaranda_wood",
			createLogBlock(MapColor.TERRACOTTA_PURPLE, MapColor.TERRACOTTA_PURPLE),
			new QuiltItemSettings());
	public static final Block JACARANDA_PLANKS_BLOCK = registerWithItem("jacaranda_planks",
			new Block(QuiltBlockSettings.copyOf(AZALEA_PLANKS_BLOCK).mapColor(MapColor.PALE_PURPLE)),
			new QuiltItemSettings());
	public static final Block JACARANDA_SLAB_BLOCK = registerWithItem("jacaranda_slab",
			new SlabBlock(QuiltBlockSettings.copyOf(JACARANDA_PLANKS_BLOCK)), new QuiltItemSettings()
	);
	public static final Block JACARANDA_STAIRS_BLOCK = registerWithItem("jacaranda_stairs",
			new StairsBlock(JACARANDA_PLANKS_BLOCK.getDefaultState(), QuiltBlockSettings.copyOf(JACARANDA_PLANKS_BLOCK)),
			new QuiltItemSettings()
	);
	public static final Block JACARANDA_BUTTON_BLOCK = registerWithItem("jacaranda_button",
			new AbstractButtonBlock(QuiltBlockSettings.copyOf(Blocks.OAK_BUTTON), BlockSetType.OAK, 30, true),
			new QuiltItemSettings()
	);
	public static final DoorBlock JACARANDA_DOOR = registerWithItem("jacaranda_door",
			new DoorBlock(QuiltBlockSettings.copyOf(Blocks.OAK_DOOR).mapColor(JACARANDA_PLANKS_BLOCK.getDefaultMapColor()), BlockSetType.OAK),
			new QuiltItemSettings()
	);
	public static final FenceBlock JACARANDA_FENCE_BLOCK = registerWithItem("jacaranda_fence",
			new FenceBlock(QuiltBlockSettings.copyOf(JACARANDA_PLANKS_BLOCK)),
			new QuiltItemSettings());
	public static final FenceGateBlock JACARANDA_FENCE_GATE_BLOCK = registerWithItem("jacaranda_fence_gate",
			new FenceGateBlock(QuiltBlockSettings.copyOf(JACARANDA_PLANKS_BLOCK), SignType.OAK), new QuiltItemSettings());
	public static final Block JACARANDA_PRESSURE_PLATE_BLOCK = registerWithItem("jacaranda_pressure_plate",
			new PressurePlateBlock(
					PressurePlateBlock.ActivationRule.EVERYTHING,
					QuiltBlockSettings.copyOf(Blocks.OAK_PRESSURE_PLATE).mapColor(JACARANDA_PLANKS_BLOCK.getDefaultMapColor()),
					BlockSetType.OAK
			),
			new QuiltItemSettings()
	);
	public static final TrapdoorBlock JACARANDA_TRAPDOOR = registerWithItem("jacaranda_trapdoor",
			new TrapdoorBlock(QuiltBlockSettings.copyOf(Blocks.OAK_TRAPDOOR).mapColor(JACARANDA_PLANKS_BLOCK.getDefaultMapColor()), BlockSetType.OAK),
			new QuiltItemSettings()
	);

	public static final RegistryKey<TerraformBoatType> JACARANDA_BOAT_TYPE_KEY = TerraformBoatTypeRegistry.createKey(id("jacaranda"));
	public static final Item JACARANDA_BOAT_ITEM = TerraformBoatItemHelper.registerBoatItem(
			id("jacaranda_boat"), JACARANDA_BOAT_TYPE_KEY, false
	);
	public static final Item JACARANDA_CHEST_BOAT_ITEM = TerraformBoatItemHelper.registerBoatItem(
			id("jacaranda_chest_boat"), JACARANDA_BOAT_TYPE_KEY, true
	);
	public static final TerraformBoatType JACARANDA_BOAT_TYPE = Registry.register(TerraformBoatTypeRegistry.INSTANCE, JACARANDA_BOAT_TYPE_KEY,
			new TerraformBoatType.Builder().item(JACARANDA_BOAT_ITEM).chestItem(JACARANDA_BOAT_ITEM).build()
	);

	public static final SignData JACARANDA_SIGNS = new SignData("jacaranda", JACARANDA_PLANKS_BLOCK);
	//endregion

	//region Big Flower Pot
	public static final BigFlowerPotBlock BIG_FLOWER_POT_BLOCK = registerWithItem(
			"big_flower_pot",
			PottedPlantType.register("none", Blocks.AIR, Items.AIR),
			new QuiltItemSettings()
	);
	public static final BigFlowerPotBlock.PlantAirBlock PLANT_AIR_BLOCK = registerBlock(
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
	public static final BigStaticFlowerPotBlock BIG_POTTED_TATER_BLOCK = registerBigPotted("tater",
			Blocks.AIR, Items.POTATO,
			type -> new BigStaticFlowerPotBlock(type, Block.createCuboidShape(
					4.f, 14.f, 4.f,
					12.f, 21.f, 12.f
			)));
	//endregion

	//region Blackboards
	public static final BlackboardBlock BLACKBOARD_BLOCK = registerWithItem("blackboard",
			new BlackboardBlock(QuiltBlockSettings.of(Material.DECORATION).strength(.2f)
					.nonOpaque()
					.sounds(BlockSoundGroup.WOOD),
					false),
			new QuiltItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);
	public static final BlackboardBlock WAXED_BLACKBOARD_BLOCK = registerWithItem("waxed_blackboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(BLACKBOARD_BLOCK), true),
			new QuiltItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);

	public static final BlackboardBlock CHALKBOARD_BLOCK = registerWithItem("chalkboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(BLACKBOARD_BLOCK), false),
			new QuiltItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);
	public static final BlackboardBlock WAXED_CHALKBOARD_BLOCK = registerWithItem("waxed_chalkboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(CHALKBOARD_BLOCK), true),
			new QuiltItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);

	public static final BlackboardBlock GLASSBOARD_BLOCK = registerWithItem("glassboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(BLACKBOARD_BLOCK).sounds(BlockSoundGroup.GLASS), false),
			new QuiltItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);
	public static final BlackboardBlock WAXED_GLASSBOARD_BLOCK = registerWithItem("waxed_glassboard",
			new BlackboardBlock(QuiltBlockSettings.copyOf(GLASSBOARD_BLOCK), true),
			new QuiltItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);

	public static final BlackboardPressBlock BLACKBOARD_PRESS_BLOCK = registerWithItem("blackboard_press",
			new BlackboardPressBlock(QuiltBlockSettings.of(Material.METAL)),
			new QuiltItemSettings()
	);

	public static final PainterPaletteItem PAINTER_PALETTE_ITEM = registerItem("painter_palette",
			new PainterPaletteItem(new QuiltItemSettings().maxCount(1))
	);
	//endregion

	//region Copper Sulfate
	public static final Item COPPER_SULFATE_ITEM = registerItem("copper_sulfate", new Item(new QuiltItemSettings()));
	public static final LanternBlock COPPER_SULFATE_LANTERN_BLOCK = registerWithItem("copper_sulfate_lantern",
			new LanternBlock(QuiltBlockSettings.copyOf(Blocks.LANTERN)), new QuiltItemSettings());
	public static final CopperSulfateCampfireBlock COPPER_SULFATE_CAMPFIRE_BLOCK = Registrar.register("copper_sulfate_campfire",
					new CopperSulfateCampfireBlock(QuiltBlockSettings.copyOf(Blocks.CAMPFIRE).ticksRandomly()))
			.withItem(new QuiltItemSettings())
			.addSelfTo(BlockEntityType.CAMPFIRE)
			.finish().block();
	public static final TorchBlock COPPER_SULFATE_TORCH_BLOCK = registerBlock("copper_sulfate_torch",
			new TorchBlock(QuiltBlockSettings.copyOf(Blocks.TORCH), COPPER_SULFATE_FLAME));
	public static final WallTorchBlock COPPER_SULFATE_WALL_TORCH_BLOCK = registerBlock("copper_sulfate_wall_torch",
			new WallTorchBlock(QuiltBlockSettings.copyOf(COPPER_SULFATE_TORCH_BLOCK)
					.dropsLike(COPPER_SULFATE_TORCH_BLOCK), COPPER_SULFATE_FLAME));
	public static final WallStandingBlockItem COPPER_SULFATE_TORCH_ITEM = registerItem("copper_sulfate_torch",
					new WallStandingBlockItem(COPPER_SULFATE_TORCH_BLOCK, COPPER_SULFATE_WALL_TORCH_BLOCK, new QuiltItemSettings(), Direction.DOWN)
	);
	//endregion

	//region Redstone
	public static final RedstoneLanternBlock REDSTONE_LANTERN_BLOCK = registerWithItem("redstone_lantern",
			new RedstoneLanternBlock(), new QuiltItemSettings());
	public static final CopperHopperBlock COPPER_HOPPER_BLOCK = registerWithItem("copper_hopper",
			new CopperHopperBlock(QuiltBlockSettings.copyOf(Blocks.HOPPER).mapColor(MapColor.ORANGE)),
			new QuiltItemSettings());
	public static final SturdyStoneBlock STURDY_STONE_BLOCK = registerWithItem("sturdy_stone",
			new SturdyStoneBlock(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(3.5f)),
			new QuiltItemSettings());
	public static final FenceGateBlock NETHER_BRICK_FENCE_GATE = registerWithItem("nether_brick_fence_gate",
			new FenceGateBlock(QuiltBlockSettings.copyOf(Blocks.NETHER_BRICK_FENCE), SignType.OAK),
			new QuiltItemSettings());
	//endregion

	public static final BookPileBlock BOOK_PILE_BLOCK = Registrar.register("book_pile",
					new BookPileBlock(QuiltBlockSettings.of(Material.DECORATION).strength(.2f)
							.nonOpaque()))
			.then(block -> {
				((ItemExtensions) Items.BOOK).makePlaceable(block, false);
				((ItemExtensions) Items.ENCHANTED_BOOK).makePlaceable(block, false);
			}).finish().block();

	public static final PieBlock PUMPKIN_PIE_BLOCK = registerBlock("pumpkin_pie", PieBlock.fromPieItem(Items.PUMPKIN_PIE));

	public static final SawmillBlock SAWMILL_BLOCK = registerWithItem("sawmill", new SawmillBlock(),
			new QuiltItemSettings());

	//region Wall lanterns
	public static final WallLanternBlock<LanternBlock> WALL_LANTERN_BLOCK = registerBlock("wall_lantern",
			new WallLanternBlock<>((LanternBlock) Blocks.LANTERN));
	public static final WallLanternBlock<LanternBlock> SOUL_WALL_LANTERN_BLOCK = registerBlock("wall_lantern/soul",
			new WallLanternBlock<>((LanternBlock) Blocks.SOUL_LANTERN));
	public static final WallLanternBlock<RedstoneLanternBlock> REDSTONE_WALL_LANTERN_BLOCK = LanternRegistry.registerWallLantern(REDSTONE_LANTERN_BLOCK);
	public static final BlockEntityType<LanternBlockEntity> WALL_LANTERN_BLOCK_ENTITY_TYPE = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			id("lantern"),
			QuiltBlockEntityTypeBuilder.create(LanternBlockEntity::new, WALL_LANTERN_BLOCK, SOUL_WALL_LANTERN_BLOCK, REDSTONE_WALL_LANTERN_BLOCK)
					.build()
	);
	public static final WallLanternBlock<AmethystLanternBlock> AMETHYST_WALL_LANTERN_BLOCK = LanternRegistry.registerWallLantern(AMETHYST_LANTERN_BLOCK);
	//endregion

	public static final WindChimeBlock WIND_CHIME_BLOCK = registerWithItem("wind_chime",
			new WindChimeBlock(QuiltBlockSettings.of(Material.DECORATION).nonOpaque()
					.sounds(BlockSoundGroup.AMETHYST_BLOCK)),
			new QuiltItemSettings());

	//region Braziers
	public static final BrazierBlock BRAZIER_BLOCK = registerWithItem("brazier",
			new BrazierBlock(MapColor.BRIGHT_RED, 1, 15, ParticleTypes.FLAME),
			new QuiltItemSettings());
	public static final BrazierBlock SOUL_BRAZIER_BLOCK = registerWithItem("soul_brazier",
			new BrazierBlock(MapColor.LIGHT_BLUE, 2, 10, ParticleTypes.SOUL),
			new QuiltItemSettings());
	public static final BrazierBlock COPPER_SULFATE_BRAZIER_BLOCK = registerWithItem("copper_sulfate_brazier",
			new CopperSulfateBrazierBlock(MapColor.EMERALD_GREEN, 2, 14, COPPER_SULFATE_FLAME),
			new QuiltItemSettings());
	//endregion

	//region Calcite
	public static final Derivator CALCITE_DERIVATOR = new Derivator(Blocks.CALCITE.getDefaultState());
	public static final StairsBlock CALCITE_STAIRS = CALCITE_DERIVATOR.stairs();
	public static final SlabBlock CALCITE_SLAB = CALCITE_DERIVATOR.slab();

	public static final Block POLISHED_CALCITE = registerWithItem("polished_calcite",
			new Block(QuiltBlockSettings.copyOf(Blocks.CALCITE)),
			new QuiltItemSettings());
	private static final Derivator POLISHED_CALCITE_DERIVATOR = new Derivator(POLISHED_CALCITE.getDefaultState(), CALCITE_DERIVATOR);
	public static final StairsBlock POLISHED_CALCITE_STAIRS = POLISHED_CALCITE_DERIVATOR.stairs();
	public static final SlabBlock POLISHED_CALCITE_SLAB = POLISHED_CALCITE_DERIVATOR.slab();
	public static final WallBlock POLISHED_CALCITE_WALL = POLISHED_CALCITE_DERIVATOR.wall();

	public static final Block CALCITE_BRICKS = registerWithItem("calcite_bricks",
			new Block(QuiltBlockSettings.copyOf(POLISHED_CALCITE)),
			new QuiltItemSettings());
	private static final Derivator CALCITE_BRICKS_DERIVATOR = new Derivator(CALCITE_BRICKS.getDefaultState(), POLISHED_CALCITE_DERIVATOR);
	public static final Block CRACKED_CALCITE_BRICKS = CALCITE_BRICKS_DERIVATOR.cracked();
	public static final StairsBlock CALCITE_BRICK_STAIRS = CALCITE_BRICKS_DERIVATOR.stairs();
	public static final SlabBlock CALCITE_BRICK_SLAB = CALCITE_BRICKS_DERIVATOR.slab();
	public static final WallBlock CALCITE_BRICK_WALL = CALCITE_BRICKS_DERIVATOR.wall();
	public static final Block CHISELED_CALCITE_BRICKS = CALCITE_BRICKS_DERIVATOR.chiseled();

	public static final Block MOSSY_CALCITE_BRICKS = CALCITE_BRICKS_DERIVATOR.mossy();
	private static final Derivator MOSSY_CALCITE_BRICKS_DERIVATOR = new Derivator(MOSSY_CALCITE_BRICKS.getDefaultState(), CALCITE_BRICKS_DERIVATOR);
	public static final StairsBlock MOSSY_CALCITE_BRICK_STAIRS = MOSSY_CALCITE_BRICKS_DERIVATOR.stairs();
	public static final SlabBlock MOSSY_CALCITE_BRICK_SLAB = MOSSY_CALCITE_BRICKS_DERIVATOR.slab();
	public static final WallBlock MOSSY_CALCITE_BRICK_WALL = MOSSY_CALCITE_BRICKS_DERIVATOR.wall();
	//endregion

	//region Tuff
	public static final Derivator TUFF_DERIVATOR = new Derivator(Blocks.TUFF.getDefaultState());
	public static final StairsBlock TUFF_STAIRS = TUFF_DERIVATOR.stairs();
	public static final SlabBlock TUFF_SLAB = TUFF_DERIVATOR.slab();

	public static final Block POLISHED_TUFF = registerWithItem("polished_tuff",
			new Block(QuiltBlockSettings.copyOf(Blocks.TUFF)),
			new QuiltItemSettings());
	private static final Derivator POLISHED_TUFF_DERIVATOR = new Derivator(POLISHED_TUFF.getDefaultState(), TUFF_DERIVATOR);
	public static final StairsBlock POLISHED_TUFF_STAIRS = POLISHED_TUFF_DERIVATOR.stairs();
	public static final SlabBlock POLISHED_TUFF_SLAB = POLISHED_TUFF_DERIVATOR.slab();
	public static final WallBlock POLISHED_TUFF_WALL = POLISHED_TUFF_DERIVATOR.wall();

	public static final Block TUFF_BRICKS = registerWithItem("tuff_bricks",
			new Block(QuiltBlockSettings.copyOf(POLISHED_TUFF)),
			new QuiltItemSettings());
	private static final Derivator TUFF_BRICKS_DERIVATOR = new Derivator(TUFF_BRICKS.getDefaultState(), POLISHED_TUFF_DERIVATOR);
	public static final Block CRACKED_TUFF_BRICKS = TUFF_BRICKS_DERIVATOR.cracked();
	public static final StairsBlock TUFF_BRICK_STAIRS = TUFF_BRICKS_DERIVATOR.stairs();
	public static final SlabBlock TUFF_BRICK_SLAB = TUFF_BRICKS_DERIVATOR.slab();
	public static final WallBlock TUFF_BRICK_WALL = TUFF_BRICKS_DERIVATOR.wall();
	public static final Block CHISELED_TUFF_BRICKS = TUFF_BRICKS_DERIVATOR.chiseled();

	public static final Block MOSSY_TUFF_BRICKS = TUFF_BRICKS_DERIVATOR.mossy();
	private static final Derivator MOSSY_TUFF_BRICKS_DERIVATOR = new Derivator(MOSSY_TUFF_BRICKS.getDefaultState(), TUFF_BRICKS_DERIVATOR);
	public static final StairsBlock MOSSY_TUFF_BRICK_STAIRS = MOSSY_TUFF_BRICKS_DERIVATOR.stairs();
	public static final SlabBlock MOSSY_TUFF_BRICK_SLAB = MOSSY_TUFF_BRICKS_DERIVATOR.slab();
	public static final WallBlock MOSSY_TUFF_BRICK_WALL = MOSSY_TUFF_BRICKS_DERIVATOR.wall();
	//endregion

	//region Deepslate
	private static final Derivator DEEPSLATE_BRICKS_DERIVATOR = new Derivator(Blocks.DEEPSLATE_BRICKS.getDefaultState());
	public static final Block MOSSY_DEEPSLATE_BRICKS = DEEPSLATE_BRICKS_DERIVATOR.mossy();
	public static final Derivator MOSSY_DEEPSLATE_BRICKS_DERIVATOR = new Derivator(MOSSY_DEEPSLATE_BRICKS.getDefaultState());
	public static final StairsBlock MOSSY_DEEPSLATE_BRICK_STAIRS = MOSSY_DEEPSLATE_BRICKS_DERIVATOR.stairs();
	public static final SlabBlock MOSSY_DEEPSLATE_BRICK_SLAB = MOSSY_DEEPSLATE_BRICKS_DERIVATOR.slab();
	public static final WallBlock MOSSY_DEEPSLATE_BRICK_WALL = MOSSY_DEEPSLATE_BRICKS_DERIVATOR.wall();
	//endregion

	public static final HangingFlowerPotBlock HANGING_FLOWER_POT_BLOCK = HangingFlowerPotBlock.initEmpty();

	public static final FenceLikeWallBlock POLISHED_BASALT_WALL = registerWithItem("polished_basalt_wall",
			new FenceLikeWallBlock(QuiltBlockSettings.copyOf(Blocks.POLISHED_BASALT)),
			new QuiltItemSettings());

	/* Block Entities */

	public static final BlockEntityType<BenchBlockEntity> BENCH_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"bench", BenchBlockEntity::new
	);
	public static final BlockEntityType<BlackboardBlockEntity> BLACKBOARD_BLOCK_ENTITY_TYPE = registerBlockEntity(
			"blackboard",
			BlackboardBlockEntity::new,
			BLACKBOARD_BLOCK, CHALKBOARD_BLOCK, GLASSBOARD_BLOCK,
			WAXED_BLACKBOARD_BLOCK, WAXED_CHALKBOARD_BLOCK, WAXED_GLASSBOARD_BLOCK
	);
	public static final BlockEntityType<BlackboardPressBlockEntity> BLACKBOARD_PRESS_BLOCK_ENTITY = registerBlockEntity(
			"blackboard_press", BlackboardPressBlockEntity::new, BLACKBOARD_PRESS_BLOCK
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

	public static final RegistryKey<PointOfInterestType> AMETHYST_LANTERN_POI = PointOfInterestHelper.register(
			id("amethyst_lantern"),
			0, 2,
			AMETHYST_LANTERN_BLOCK, AMETHYST_WALL_LANTERN_BLOCK
	);

	/* Advancement Criteria */

	public static final PetUsePetBedCriterion PET_USE_PET_BED_CRITERION = Criteria.register(new PetUsePetBedCriterion());

	static <T extends Block> T registerBlock(String name, T block) {
		return Registry.register(Registries.BLOCK, id(name), block);
	}

	private static PillarBlock createFloweringLogBlock(Supplier<Block> normal, MapColor topMapColor, MapColor sideMapColor) {
		return new FloweringAzaleaLogBlock(
				normal,
				QuiltBlockSettings.copyOf(Blocks.OAK_LOG)
						.mapColorProvider(state -> state.get(PillarBlock.AXIS).isVertical() ? topMapColor : sideMapColor)
		);
	}

	private static PillarBlock createLogBlock(MapColor topMapColor, MapColor sideMapColor) {
		return new PillarBlock(
				QuiltBlockSettings.copyOf(Blocks.OAK_LOG)
						.mapColorProvider(state -> state.get(PillarBlock.AXIS).isVertical() ? topMapColor : sideMapColor)
		);
	}

	static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings) {
		return registerWithItem(name, block, settings, BlockItem::new);
	}

	static <T extends Block> T registerWithItem(String name, T block, Item.Settings settings,
			BiFunction<T, Item.Settings, BlockItem> factory) {
		registerItem(name, factory.apply(registerBlock(name, block), settings));
		return block;
	}

	private static <T extends BigFlowerPotBlock> T registerBigPotted(String name, Block plant, Item item,
			Function<PottedPlantType, T> block) {
		return registerBlock("big_flower_pot/" + name, PottedPlantType.register(name, plant, item, block));
	}

	public static <T extends Item> T registerItem(String name, T item) {
		return Registry.register(Registries.ITEM, id(name), item);
	}

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name,
			BlockEntityType.BlockEntityFactory<T> factory,
			Block... blocks) {
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, id(name), QuiltBlockEntityTypeBuilder.create(factory, blocks).build());
	}

	private static <R extends Recipe<?>, T extends RecipeSerializer<R>> T register(String name, T recipe) {
		return Registry.register(Registries.RECIPE_SERIALIZER, id(name), recipe);
	}

	private static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String id) {
		return Registry.register(Registries.RECIPE_TYPE, id(id), new RecipeType<T>() {
			public String toString() {
				return id;
			}
		});
	}

	private static Identifier register(String id, StatFormatter statFormatter) {
		var identifier = id(id);
		Registry.register(Registries.CUSTOM_STAT, id, identifier);
		CUSTOM.getOrCreateStat(identifier, statFormatter);
		return identifier;
	}

	public static void init() {
		AurorasDecoPlants.init();
		AurorasDecoBiomes.init();
		AurorasDecoEntities.init();
		AurorasDecoScreenHandlers.init();
		AurorasDecoSounds.init();

		RegistryMonitor.create(Registries.BLOCK)
				.filter(context -> {
					var id = context.id();

					if ((id.getNamespace().equals("betternether") || id.getNamespace().equals("betterend")) && (id.getPath().contains("stripped") || (id.getPath().contains("mushroom") && !id.getPath().contains("mushroom_fir")) || id.getPath().contains("amaranita")))
						return false;
					return !id.getNamespace().equals("aurorasdeco") || !id.getPath().contains("sign_post");
				})
				.forAll(context -> {
					if (context.value() instanceof FlowerPotBlock flowerPotBlock) {
						if (flowerPotBlock == Blocks.FLOWER_POT) return;

						context.register(
								AurorasDeco.id(AuroraUtil.getIdPath("hanging_flower_pot", context.id(), "^potted[_/]")),
								new HangingFlowerPotBlock(flowerPotBlock)
						);
					} else {
						WoodType.onBlockRegister(context.id(), context.value());
						if (context.value() instanceof FenceBlock fenceBlock) {
							var signPostBlock = Registry.register(
									context.registry(),
									AurorasDeco.id(AuroraUtil.getIdPath("sign_post", context.id(), "_fence$")),
									new SignPostBlock(fenceBlock)
							);

							SIGN_POST_BLOCK_ENTITY_TYPE.addSupportedBlock(signPostBlock);
						} else LanternRegistry.tryRegisterWallLantern(context.registry(), context.value(), context.id());
					}
				});

		RegistryMonitor.create(Registries.ITEM).filter(context -> context.value() instanceof BlockItem item)
				.forAll(context -> {
					var accessor = (BlockItemAccessor) context.value();
					var item = (BlockItem) context.value();

					if (item.getBlock() instanceof LanternBlock) {
						var lanternBlock = LanternRegistry.fromItem(item);
						if (lanternBlock != null)
							accessor.aurorasdeco$setWallBlock(lanternBlock);
						Item.BLOCK_ITEMS.put(lanternBlock, item);
					} else if (item.getBlock() instanceof CandleBlock candleBlock && context.id().getNamespace().equals("minecraft")) {
						var wall = registerBlock(
								"wall_" + context.id().getPath(),
								new WallCandleBlock(candleBlock)
						);
						var chandelier = registerBlock(
								"chandelier/" + context.id().getPath().replace("_candle", ""),
								new ChandelierBlock(candleBlock)
						);
						accessor.aurorasdeco$setWallBlock(wall);
						accessor.aurorasdeco$setCeilingBlock(chandelier);

						Item.BLOCK_ITEMS.put(wall, context.value());
						Item.BLOCK_ITEMS.put(chandelier, context.value());
					}
				});

		var colors = DyeColor.values();

		SleepingBagBlock.register();
		PetBedBlock.register();

		WoodType.registerWoodTypeModificationCallback(woodType -> {
			if (woodType == WoodType.BAMBOO) return;

			var block = registerWithItem("stump/" + woodType.getPathName(),
					new StumpBlock(woodType),
					new QuiltItemSettings());

			ItemTree.STUMPS.add(block);
			woodType.getComponent(WoodType.ComponentType.LOG).syncFlammabilityWith(block);
		}, WoodType.ComponentType.LOG);

		WoodType.registerWoodTypeModificationCallback(woodType -> {
			var block = registerWithItem("small_log_pile/" + woodType.getPathName(),
					new SmallLogPileBlock(woodType),
					new QuiltItemSettings());

			ItemTree.SMALL_LOG_PILES.add(block);
			woodType.getComponent(WoodType.ComponentType.LOG).syncFlammabilityWith(block);
		}, WoodType.ComponentType.LOG);

		WoodType.registerWoodTypeModificationCallback(woodType -> Registrar.register("shelf/" + woodType.getPathName(), new ShelfBlock(woodType))
						.withItem(new QuiltItemSettings())
						.addToGroup(ItemTree.SHELVES)
						.addSelfTo(SHELF_BLOCK_ENTITY_TYPE),
				WoodType.ComponentType.PLANKS);

		WoodType.registerWoodTypeModificationCallback(woodType -> {
			var seatRest = registerItem("seat_rest/" + woodType.getPathName(),
					new SeatRestItem(woodType, new QuiltItemSettings()));
			ItemTree.SEAT_RESTS.add(seatRest);
			registerItem("sign_post/" + woodType.getPathName(),
					new SignPostItem(woodType, new QuiltItemSettings()));
		}, WoodType.ComponentType.PLANKS);

		WoodType.registerWoodTypeModificationCallback(woodType -> Registrar.register("bench/" + woodType.getPathName(), new BenchBlock(woodType))
						.withItem(new QuiltItemSettings())
						.addToGroup(ItemTree.BENCHES)
						.addSelfTo(BENCH_BLOCK_ENTITY_TYPE)
						.syncFlammabilityWith(woodType.getComponent(WoodType.ComponentType.PLANKS)),
				WoodType.ComponentType.PLANKS);
	}
}
