/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.texture.NativeImage;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.*;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.client.AurorasDecoClient;
import dev.lambdaurora.aurorasdeco.item.SeatRestItem;
import dev.lambdaurora.aurorasdeco.item.SignPostItem;
import dev.lambdaurora.aurorasdeco.mixin.block.AbstractBlockAccessor;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.LanternRegistry;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import dev.lambdaurora.aurorasdeco.resource.datagen.*;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import dev.lambdaurora.aurorasdeco.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.recipe.api.builder.VanillaRecipeBuilders;
import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.regex.Pattern;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;
import static dev.lambdaurora.aurorasdeco.resource.datagen.RecipeDatagen.registerRecipe;
import static dev.lambdaurora.aurorasdeco.util.AuroraUtil.jsonArray;

/**
 * Represents the Aurora's Decorations data generator.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Datagen {
	public static final Logger LOGGER = LogManager.getLogger("aurorasdeco:datagen");

	private static final Identifier WALL_LANTERN_ATTACHMENT = id("block/wall_lantern_attachment");
	private static final Identifier WALL_LANTERN_ATTACHMENT_EXTENDED1 = id("block/wall_lantern_attachment_extended1");
	private static final Identifier WALL_LANTERN_ATTACHMENT_EXTENDED2 = id("block/wall_lantern_attachment_extended2");

	private static final Identifier TEMPLATE_LANTERN_MODEL = new Identifier("block/template_lantern");
	private static final Identifier TEMPLATE_HANGING_LANTERN_MODEL = new Identifier("block/template_hanging_lantern");
	private static final Identifier TEMPLATE_SLEEPING_BAG_FOOT_MODEL = id("block/template/sleeping_bag_foot");
	private static final Identifier TEMPLATE_SLEEPING_BAG_HEAD_MODEL = id("block/template/sleeping_bag_head");
	private static final Identifier TEMPLATE_SLEEPING_BAG_ITEM_MODEL = id("item/template/sleeping_bag");
	private static final Identifier TEMPLATE_SEAT_REST_ITEM_MODEL = id("item/template/seat_rest");

	private static final Identifier BIG_FLOWER_POT_MODEL = id("block/big_flower_pot/big_flower_pot");
	private static final Identifier BIG_FLOWER_POT_WITH_MYCELIUM_MODEL = id("block/big_flower_pot/mycelium");
	private static final Identifier LOG_STUMP_LEAF_TEXTURE = id("block/log_stump_leaf");

	static final Identifier SHELF_BETTERGRASS_DATA = id("bettergrass/data/shelf");

	private static final Pattern PLANKS_TO_BASE_ID = Pattern.compile("[_/]planks$");
	private static final Pattern PLANKS_SEPARATOR_DETECTOR = Pattern.compile("[/]planks$");
	private static final Pattern LOG_TO_BASE_ID = Pattern.compile("[_/]log$");
	private static final Pattern LOG_SEPARATOR_DETECTOR = Pattern.compile("[/]log$");
	private static final Pattern STEM_TO_BASE_ID = Pattern.compile("[_/]stem$");
	private static final Pattern STEM_SEPARATOR_DETECTOR = Pattern.compile("[/]stem$");

	private Datagen() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}

	public static void registerBetterGrassLayer(Identifier blockId, Identifier data) {
		var json = new JsonObject();
		json.addProperty("type", "layer");
		json.addProperty("data", data.toString());
		AurorasDecoClient.RESOURCE_PACK.putJson(
				ResourceType.CLIENT_RESOURCES,
				new Identifier(blockId.getNamespace(), "bettergrass/states/" + blockId.getPath()),
				json
		);
	}

	public static void registerBetterGrassLayer(Block block, Identifier data) {
		registerBetterGrassLayer(Registry.BLOCK.getId(block), data);
	}

	private static JsonObject generateBlockLootTableSimplePool(Identifier id, boolean copyName) {
		var pool = new JsonObject();
		pool.addProperty("rolls", 1.0);
		pool.addProperty("bonus_rolls", 0.0);

		var entries = new JsonArray();

		var entry = new JsonObject();
		entry.addProperty("type", "minecraft:item");
		entry.addProperty("name", id.toString());

		if (copyName) {
			var function = new JsonObject();
			function.addProperty("function", "minecraft:copy_name");
			function.addProperty("source", "block_entity");
			entry.add("functions", jsonArray(function));
		}

		entries.add(entry);

		pool.add("entries", entries);

		var survivesExplosion = new JsonObject();
		survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
		pool.add("conditions", jsonArray(survivesExplosion));

		return pool;
	}

	public static JsonObject simpleBlockLootTable(Identifier id, boolean copyName) {
		var root = new JsonObject();
		root.addProperty("type", "minecraft:block");
		var pools = new JsonArray();
		pools.add(generateBlockLootTableSimplePool(id, copyName));

		root.add("pools", pools);

		return root;
	}

	private static JsonObject benchBlockLootTable(Identifier id) {
		var root = new JsonObject();
		root.addProperty("type", "minecraft:block");
		var pools = new JsonArray();
		pools.add(generateBlockLootTableSimplePool(id, true));

		{
			var restPool = new JsonObject();
			pools.add(restPool);
			restPool.addProperty("rolls", 1.0);
			var entries = new JsonArray();
			restPool.add("entries", entries);
			var entry = new JsonObject();
			entries.add(entry);
			entry.addProperty("type", "minecraft:dynamic");
			entry.addProperty("name", "aurorasdeco:seat_rest");
		}

		root.add("pools", pools);

		return root;
	}

	public static void registerBenchBlockLootTable(Block block) {
		var id = Registry.BLOCK.getId(block);
		AurorasDeco.RESOURCE_PACK.putJson(
				ResourceType.SERVER_DATA,
				new Identifier(id.getNamespace(), "loot_tables/blocks/" + id.getPath()),
				benchBlockLootTable(id)
		);
	}

	private static JsonObject doubleBlockLootTable(Identifier id) {
		var root = new JsonObject();
		root.addProperty("type", "minecraft:block");
		var pools = new JsonArray();
		pools.add(generateBlockLootTableSimplePool(id, true));

		{
			var pool = generateBlockLootTableSimplePool(id, true);
			pools.add(pool);

			var conditions = pool.getAsJsonArray("conditions");
			var condition = new JsonObject();
			conditions.add(condition);
			condition.addProperty("condition", "minecraft:block_state_property");
			condition.addProperty("block", id.toString());
			{
				var properties = new JsonObject();
				properties.addProperty("type", "double");
				condition.add("properties", properties);
			}
		}

		root.add("pools", pools);

		return root;
	}

	public static void registerDoubleBlockLootTable(Block block) {
		var id = Registry.BLOCK.getId(block);
		AurorasDeco.RESOURCE_PACK.putJson(
				ResourceType.SERVER_DATA,
				new Identifier(id.getNamespace(), "loot_tables/blocks/" + id.getPath()),
				doubleBlockLootTable(id)
		);
	}

	private static String candleLikeBlockLootTable(Identifier blockId, Identifier itemId) {
		return """
				{
				  "type": "minecraft:block",
				  "pools": [
				    {
				      "rolls": 1.0,
				      "bonus_rolls": 0.0,
				      "entries": [
				        {
				          "type": "minecraft:item",
				          "functions": [
				            {
				              "function": "minecraft:set_count",
				              "conditions": [
				                {
				                  "condition": "minecraft:block_state_property",
				                  "block": "${block}",
				                  "properties": {
				                    "candles": "2"
				                  }
				                }
				              ],
				              "count": 2.0,
				              "add": false
				            },
				            {
				              "function": "minecraft:set_count",
				              "conditions": [
				                {
				                  "condition": "minecraft:block_state_property",
				                  "block": "${block}",
				                  "properties": {
				                    "candles": "3"
				                  }
				                }
				              ],
				              "count": 3.0,
				              "add": false
				            },
				            {
				              "function": "minecraft:set_count",
				              "conditions": [
				                {
				                  "condition": "minecraft:block_state_property",
				                  "block": "${block}",
				                  "properties": {
				                    "candles": "4"
				                  }
				                }
				              ],
				              "count": 4.0,
				              "add": false
				            },
				            {
				              "function": "minecraft:explosion_decay"
				            }
				          ],
				          "name": "${item}"
				        }
				      ]
				    }
				  ]
				}
				"""
				.replace("${block}", blockId.toString())
				.replace("${item}", itemId.toString());
	}

	public static void registerCandleLikeBlockLootTable(ExtendedCandleBlock block) {
		var blockId = Registry.BLOCK.getId(block);
		AurorasDeco.RESOURCE_PACK.putJsonText(
				ResourceType.SERVER_DATA,
				new Identifier(blockId.getNamespace(), "loot_tables/blocks/" + blockId.getPath()),
				candleLikeBlockLootTable(blockId, Registry.ITEM.getId(block.getParent().asItem()))
		);
	}

	public static void dropsSelf(Block block) {
		registerSimpleBlockLootTable(Registry.BLOCK.getId(block), Registry.ITEM.getId(block.asItem()),
				block instanceof BlockWithEntity);
	}

	public static void registerSimpleBlockLootTable(Identifier blockId, Identifier itemId, boolean copyName) {
		AurorasDeco.RESOURCE_PACK.putJson(
				ResourceType.SERVER_DATA,
				new Identifier(blockId.getNamespace(), "loot_tables/blocks/" + blockId.getPath()),
				simpleBlockLootTable(itemId, copyName)
		);
	}

	@SuppressWarnings("unchecked")
	public static JsonObject recipe(Recipe<?> recipe) {
		if (!(recipe.getSerializer() instanceof QuiltRecipeSerializer<?>))
			throw new UnsupportedOperationException("Cannot serialize recipe " + recipe);

		return ((QuiltRecipeSerializer<Recipe<?>>) recipe.getSerializer()).toJson(recipe);
	}

	public static void registerWoodcuttingRecipesForBlockVariants(Block block) {
		var blockId = Registry.BLOCK.getId(block);
		if (blockId.getPath().endsWith("log")) {
			char separator = '_';
			var basePath = LOG_TO_BASE_ID.matcher(blockId.getPath()).replaceAll("");
			if (LOG_SEPARATOR_DETECTOR.matcher(blockId.getPath()).matches()) separator = '/';
			basePath += separator;

			tryRegisterWoodcuttingRecipeFor(block, basePath, "wood", 1, "building_blocks");

			if (QuiltLoader.isModLoaded("blockus")) {
				tryRegisterWoodcuttingRecipeFor(block, "blockus", basePath,
						"small_logs", 1, "building_blocks");
			}
		} else if (blockId.getPath().endsWith("stem")) {
			char separator = '_';
			var basePath = STEM_TO_BASE_ID.matcher(blockId.getPath()).replaceAll("");
			if (STEM_SEPARATOR_DETECTOR.matcher(blockId.getPath()).matches()) separator = '/';
			basePath += separator;

			tryRegisterWoodcuttingRecipeFor(block, basePath, "hyphae", 1, "building_blocks");
			if (QuiltLoader.isModLoaded("blockus")) {
				tryRegisterWoodcuttingRecipeFor(block, "blockus", basePath,
						"small_stems", 1, "building_blocks");
			}
		}
	}

	public static void registerDefaultRecipes() {
		{
			var sulfurItem = Registry.ITEM.get(new Identifier("sulfurpotassiummod", "sulfur"));
			if (sulfurItem != Items.AIR) {
				registerRecipe(VanillaRecipeBuilders.shapelessRecipe(new ItemStack(AurorasDecoRegistry.COPPER_SULFATE_ITEM))
						.ingredient(sulfurItem)
						.ingredient(Items.RAW_COPPER)
						.build(id("copper_sulfate_from_sulfurpotassiummod"), ""), "misc");
			}
		}
	}

	public static void registerDefaultWoodcuttingRecipes() {
		WoodType.forEach(type -> {
			var log = type.getComponent(WoodType.ComponentType.LOG);
			var planks = type.getComponent(WoodType.ComponentType.PLANKS);

			if (planks != null) {
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.SLAB, 2, "building_blocks");
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.STAIRS, 1, "building_blocks");
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.PRESSURE_PLATE, 1, "redstone");
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.TRAPDOOR, 1, "redstone");
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.DOOR, 1, "redstone");
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.FENCE, 2, "decorations");
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.FENCE_GATE, 1, "redstone");
				// Not in vanilla by default, but some mods do add it.
				tryRegisterWoodcuttingRecipeFor(type, planks, WoodType.ComponentType.LADDER, 4, "misc");

				char separator = '_';
				var basePath = type.getId().getPath();
				if (PLANKS_SEPARATOR_DETECTOR.matcher(planks.id().getPath()).matches()) separator = '/';
				basePath += separator;

				tryRegisterWoodcuttingRecipeFor(planks.block(), basePath, "button", 1, "misc");
				tryRegisterWoodcuttingRecipeFor(planks.block(), basePath, "boat", 1, "misc");

				if (log == null) return;
				var planksId = planks.getItemId();
				registerRecipe(new WoodcuttingRecipe(AuroraUtil.appendWithNamespace("woodcutting", planksId),
								"planks",
								Ingredient.ofTag(TagKey.of(
										Registry.ITEM_KEY,
										new Identifier(log.id().getNamespace(), log.id().getPath() + "s")
								)),
								new ItemStack(planks.item(), 4)),
						"building_blocks");
			}
		});

		Registry.BLOCK.stream().filter(block -> ((AbstractBlockAccessor) block).getMaterial() == Material.WOOD
						|| ((AbstractBlockAccessor) block).getMaterial() == Material.NETHER_WOOD)
				.forEach(Datagen::registerWoodcuttingRecipesForBlockVariants);

		BenchBlock.streamBenches().forEach(block -> {
			var planks = block.getWoodType().getComponent(WoodType.ComponentType.PLANKS).item();
			var recipe = new WoodcuttingRecipe(
					id("woodcutting/bench/" + block.getWoodType().getPathName()),
					"bench", Ingredient.ofItems(planks),
					new ItemStack(block));
			registerRecipe(recipe, "decorations");

			var slabComponent = block.getWoodType().getComponent(WoodType.ComponentType.SLAB);
			if (slabComponent != null) {
				var slab = Ingredient.ofItems(slabComponent.item());
				var stick = Ingredient.ofItems(Items.STICK);
				registerRecipe(VanillaRecipeBuilders.shapedRecipe("---", "S S")
						.ingredient('-', slab)
						.ingredient('S', stick)
						.output(new ItemStack(block, 2))
						.build(id("bench/" + block.getWoodType().getPathName()), "bench"), "decorations");
			}
		});

		SeatRestItem.streamSeatRests().forEach(item -> {
			var planks = item.getWoodType().getComponent(WoodType.ComponentType.PLANKS).item();
			var recipe = new WoodcuttingRecipe(
					id("woodcutting/seat_rest/" + item.getWoodType().getPathName()),
					"seat_rests", Ingredient.ofItems(planks),
					new ItemStack(item));
			registerRecipe(recipe, "misc");
		});

		SignPostItem.stream().forEach(item -> {
			var planks = item.getWoodType().getComponent(WoodType.ComponentType.PLANKS).item();
			var recipe = new WoodcuttingRecipe(
					id("woodcutting/sign_post/" + item.getWoodType().getPathName()),
					"sign_posts", Ingredient.ofItems(planks),
					new ItemStack(item));
			registerRecipe(recipe, "misc");
		});

		ShelfBlock.streamShelves().forEach(block -> {
			var planks = block.getWoodType().getComponent(WoodType.ComponentType.PLANKS).item();
			var recipe = new WoodcuttingRecipe(
					id("woodcutting/shelf/" + block.getWoodType().getPathName()),
					"shelf", Ingredient.ofItems(planks),
					new ItemStack(block));
			registerRecipe(recipe, "decorations");

			var slabComponent = block.getWoodType().getComponent(WoodType.ComponentType.SLAB);
			if (slabComponent != null) {
				registerRecipe(VanillaRecipeBuilders.shapedRecipe("SS")
						.ingredient('S', slabComponent.item())
						.output(new ItemStack(block, 2))
						.build(id("shelf/" + block.getWoodType().getPathName()), "shelf"), "decorations");
			}
		});

		SmallLogPileBlock.stream().forEach(block -> {
			if (block.getWoodType().getLog() == null)
				return;
			var recipe = new WoodcuttingRecipe(
					id("woodcutting/small_log_pile/" + block.getWoodType().getPathName()),
					"small_log_pile", Ingredient.ofItems(block.getWoodType().getLog()),
					new ItemStack(block, 2));
			registerRecipe(recipe, "decorations");
		});

		StumpBlock.streamLogStumps().forEach(block -> {
			if (block.getWoodType().getLog() == null)
				return;
			var recipe = new WoodcuttingRecipe(
					id("woodcutting/stump/" + block.getWoodType().getPathName()),
					"log_stump", Ingredient.ofItems(block.getWoodType().getLog()),
					new ItemStack(block));
			registerRecipe(recipe, "decorations");
		});
	}

	private static void tryRegisterWoodcuttingRecipeFor(ItemConvertible planks, String basePath, String type, int count,
	                                                    String category) {
		tryRegisterWoodcuttingRecipeFor(planks, Registry.ITEM.getId(planks.asItem()).getNamespace(), basePath, type, count,
				category);
	}

	public static void tryRegisterWoodcuttingRecipeFor(ItemConvertible planks, String namespace, String basePath,
	                                                   String type, int count, String category) {
		if (planks.asItem() == Items.AIR)
			return;
		var id = new Identifier(namespace, basePath + type);
		var item = Registry.ITEM.get(id);
		if (item != Items.AIR) {
			var recipe = new WoodcuttingRecipe(
					new Identifier(namespace, "woodcutting/" + basePath + type),
					"", Ingredient.ofItems(planks),
					new ItemStack(item, count));
			registerRecipe(recipe, category);
		}
	}

	public static void tryRegisterWoodcuttingRecipeFor(WoodType woodType, WoodType.Component planks, WoodType.ComponentType target, int count, String category) {
		var component = woodType.getComponent(target);
		if (component != null && component.hasItem()) {
			var output = component.getItemId();
			registerRecipe(new WoodcuttingRecipe(AuroraUtil.appendWithNamespace("woodcutting", output),
							target.name().toLowerCase(Locale.ROOT),
							Ingredient.ofItems(planks.item()), new ItemStack(component.item(), count)),
					category);
		}
	}

	public static void generateClientData(ResourceManager resourceManager) {
		generateBenchesClientData(resourceManager);
		generateDirectionalSignsClientData(resourceManager);
		generateShelvesClientData(resourceManager);
		generateSmallLogPilesClientData(resourceManager);
		generateStumpsClientData(resourceManager);

		PottedPlantType.stream().filter(type -> !type.isEmpty() && type.getPot().hasDynamicModel())
				.forEach(type -> {
					var id = Registry.BLOCK.getId(type.getPot());
					var builder = blockStateBuilder(type.getPot());
					if (id.getPath().endsWith("mushroom")) builder.addToVariant("", BIG_FLOWER_POT_WITH_MYCELIUM_MODEL);
					else builder.addToVariant("", BIG_FLOWER_POT_MODEL);
					builder.register();

					Datagen.registerBetterGrassLayer(id, BigFlowerPotBlock.POT_BETTERGRASS_DATA);
				});
		HangingFlowerPotBlock.stream().forEach(block -> {
			if (block == AurorasDecoRegistry.HANGING_FLOWER_POT_BLOCK) return;

			var id = Registry.BLOCK.getId(block);
			blockStateBuilder(block)
					.addToVariant("", HangingFlowerPotBlock.HANGING_FLOWER_POT_ATTACHMENT_MODEL)
					.register();
			Datagen.registerBetterGrassLayer(id, HangingFlowerPotBlock.BETTER_GRASS_DATA);
		});

		SleepingBagBlock.stream().forEach(sleepingBag -> {
			var color = sleepingBag.getColor();
			var builder = blockStateBuilder(sleepingBag);

			var footSideTexture = id("block/sleeping_bag/" + color.getName() + "/foot_side");
			var footTopTexture = id("block/sleeping_bag/" + color.getName() + "/foot_top");
			var footModel = modelBuilder(TEMPLATE_SLEEPING_BAG_FOOT_MODEL)
					.texture("side", footSideTexture)
					.texture("top", footTopTexture)
					.register(id("block/sleeping_bag/" + color.getName() + "/foot"));

			var headBottomTexture = id("block/sleeping_bag/" + color.getName() + "/head_bottom");
			var headSideTexture = id("block/sleeping_bag/" + color.getName() + "/head_side");
			var headTopTexture = id("block/sleeping_bag/" + color.getName() + "/head_top");
			var headModel = modelBuilder(TEMPLATE_SLEEPING_BAG_HEAD_MODEL)
					.texture("bottom", headBottomTexture)
					.texture("side", headSideTexture)
					.texture("top", headTopTexture)
					.register(id("block/sleeping_bag/" + color.getName() + "/head"));

			for (var direction : AuroraUtil.DIRECTIONS) {
				if (direction.getAxis().isHorizontal()) {
					builder.addToVariant("part=foot,facing=" + direction.getName(),
							footModel,
							((int) direction.asRotation() + 180) % 360);
					builder.addToVariant("part=head,facing=" + direction.getName(),
							headModel,
							((int) direction.asRotation() + 180) % 360);
				}
			}

			builder.register();

			modelBuilder(TEMPLATE_SLEEPING_BAG_ITEM_MODEL)
					.texture("foot_side", footSideTexture)
					.texture("foot_top", footTopTexture)
					.texture("head_bottom", headBottomTexture)
					.texture("head_side", headSideTexture)
					.texture("head_top", headTopTexture)
					.register(id("item/" + Registry.ITEM.getId(sleepingBag.asItem()).getPath()));
		});

		LanternRegistry.forEach((lanternId, wallLantern) -> {
			var builder = blockStateBuilder(wallLantern);
			for (var direction : AuroraUtil.DIRECTIONS) {
				if (direction.getAxis().isHorizontal()) {
					int rotation = (int) (direction.getOpposite().asRotation() + 90) % 360;
					builder.addToVariant("facing=" + direction.getName() + ",extension=none",
							WALL_LANTERN_ATTACHMENT, rotation);
					builder.addToVariant("facing=" + direction.getName() + ",extension=wall",
							WALL_LANTERN_ATTACHMENT_EXTENDED1, rotation);
					builder.addToVariant("facing=" + direction.getName() + ",extension=fence",
							WALL_LANTERN_ATTACHMENT_EXTENDED2, rotation);
				}
			}
			builder.register();

			registerBetterGrassLayer(wallLantern, WallLanternBlock.LANTERN_BETTERGRASS_DATA);
		});
	}

	private static void generateBenchesClientData(ResourceManager resourceManager) {
		BenchBlock.streamBenches().forEach(block -> {
			var builder = multipartBlockStateBuilder(block);
			var restBuilder = new MultipartBlockStateBuilder(AurorasDeco.id(Registry.BLOCK.getId(block).getPath() + "_rest"));

			var pathName = block.getWoodType().getPathName();
			var blockPathName = "block/bench/" + pathName;
			var planksTexture = block.getWoodType().getPlanksTexture(resourceManager);
			var logSideTexture = block.getWoodType().getLogSideTexture(resourceManager);
			var seatModel = modelBuilder(BenchBlock.BENCH_SEAT_MODEL)
					.texture("planks", planksTexture)
					.register(block);
			var restPlankModel = modelBuilder(BenchBlock.BENCH_REST_PLANK_MODEL)
					.texture("planks", planksTexture)
					.register(AurorasDeco.id(blockPathName + "_rest_plank"));
			var restLeftModel = modelBuilder(BenchBlock.BENCH_REST_LEFT_MODEL)
					.texture("log", logSideTexture)
					.register(AurorasDeco.id(blockPathName + "_rest_left"));
			var restRightModel = modelBuilder(BenchBlock.BENCH_REST_RIGHT_MODEL)
					.texture("log", logSideTexture)
					.register(AurorasDeco.id(blockPathName + "_rest_right"));
			var legsModel = modelBuilder(BenchBlock.BENCH_LEGS_MODEL)
					.texture("log", logSideTexture)
					.register(AurorasDeco.id(blockPathName + "_legs"));

			var withLeftLegs = BenchBlock.LEFT_LEGS.createValue(true);
			var withRightLegs = BenchBlock.RIGHT_LEGS.createValue(true);
			for (var facing : AuroraUtil.DIRECTIONS) {
				if (facing.getAxis().isVertical()) continue;

				var facingValue = BenchBlock.FACING.createValue(facing);

				var rotation = ((facing.getHorizontal() + 2) & 3) * 90;
				builder.addWhen(new StateModel(seatModel, rotation), facingValue);
				builder.addWhen(new StateModel(legsModel, rotation), facingValue, withRightLegs);
				builder.addWhen(new StateModel(legsModel, (rotation + 180) % 360), facingValue, withLeftLegs);
				restBuilder.addWhen(new StateModel(restLeftModel, rotation), facingValue, withLeftLegs);
				restBuilder.addWhen(new StateModel(restRightModel, rotation), facingValue, withRightLegs);
				restBuilder.addWhen(new StateModel(restPlankModel, rotation), facingValue);
			}

			modelBuilder(BenchBlock.BENCH_FULL_MODEL)
					.texture("log", logSideTexture)
					.texture("planks", planksTexture)
					.register(id("item/bench/" + pathName));
			modelBuilder(TEMPLATE_SEAT_REST_ITEM_MODEL)
					.texture("log", logSideTexture)
					.texture("planks", planksTexture)
					.register(id("item/seat_rest/" + pathName));

			builder.register();
			restBuilder.register();

			registerBetterGrassLayer(block, BenchBlock.BENCH_BETTERGRASS_DATA);

			DynamicLang.registerWooded("item.aurorasdeco.seat_rest", block.getWoodType());
			DynamicLang.registerWooded("block.aurorasdeco.bench", block.getWoodType());
		});
	}

	private static void generateDirectionalSignsClientData(ResourceManager resourceManager) {
		final NativeImage defaultTexture = resourceManager.getResource(SignPostItem.ABSOLUTE_OAK_SIGN_POST_TEXTURE)
				.map(resource1 -> {
					try (InputStream is = resource1.open()) {
						return NativeImage.read(is);
					} catch (IOException e1) {
						LOGGER.error("Cannot read the default texture of the directional sign.", e1);
						return null;
					}
				}).orElse(null);
		final var defaultPalette = ColorUtil.getPaletteFromImage(defaultTexture);

		SignPostItem.stream().forEach(item -> {
			var planks = item.getWoodType().getComponent(WoodType.ComponentType.PLANKS);

			var textureId = id("special/sign_post/" + item.getWoodType().getPathName());

			modelBuilder(SignPostItem.SIGN_POST_MODEL)
					.texture("sign", textureId)
					.register(item);

			DynamicLang.registerWooded("item.aurorasdeco.sign_post", item.getWoodType());

			if (planks.block() == Blocks.OAK_PLANKS || defaultTexture == null)
				return;

			var planksTextureId = item.getWoodType().getPlanksTexture(resourceManager);
			var texturePath = new Identifier(planksTextureId.getNamespace(), "textures/" + planksTextureId.getPath() + ".png");
			var resource = resourceManager.getResource(texturePath);

			if (resource.isPresent()) {
				try (InputStream is = resource.get().open()) {
					var image = NativeImage.read(is);

					var woodPalette = ColorUtil.getPaletteFromImage(image, 8);
					var outputImage = new NativeImage(defaultTexture.getWidth(), defaultTexture.getHeight(), true);

					for (int y = 0; y < defaultTexture.getHeight(); y++) {
						for (int x = 0; x < defaultTexture.getWidth(); x++) {
							var paletteIndex = defaultPalette.indexOf(defaultTexture.getPixelColor(x, y));

							if (paletteIndex < 0)
								continue;
							else if (paletteIndex >= woodPalette.size())
								paletteIndex = woodPalette.size() - 1;

							outputImage.setPixelColor(x, y, woodPalette.getInt(paletteIndex));
						}
					}

					image.close();
					AurorasDecoClient.RESOURCE_PACK.putImage(textureId, outputImage);
					outputImage.close();
				} catch (IOException e) {
					LOGGER.error("Cannot read the planks texture to generate the directional sign texture.", e);
				}
			} else {
				LOGGER.error("Cannot read the planks texture to generate the directional sign texture: could not find the texture.");
			}
		});

		defaultTexture.close();
	}

	private static void generateShelvesClientData(ResourceManager resourceManager) {
		ShelfBlock.streamShelves().forEach(block -> {
			var woodPathName = block.getWoodType().getPathName();
			var builder = blockStateBuilder(block);

			var planksTexture = block.getWoodType().getPlanksTexture(resourceManager);
			var logTexture = block.getWoodType().getLogSideTexture(resourceManager);
			Identifier bottomModel = null;
			for (var partType : PartType.getValues()) {
				var model = modelBuilder(id("block/template/shelf_" + partType.asString()))
						.texture("planks", planksTexture)
						.texture("log", logTexture)
						.register(id("block/shelf/" + woodPathName + '/' + partType.asString()));
				if (partType == PartType.BOTTOM)
					bottomModel = model;

				for (var direction : AuroraUtil.DIRECTIONS) {
					if (direction.getAxis().isVertical())
						continue;

					builder.addToVariant("type=" + partType.asString() + ",facing=" + direction.asString(),
							new StateModel(model, switch (direction) {
								default -> 0;
								case EAST -> 90;
								case SOUTH -> 180;
								case WEST -> 270;
							}));
				}
			}

			modelBuilder(bottomModel).register(id("item/shelf/" + woodPathName));
			builder.register();

			Datagen.registerBetterGrassLayer(AurorasDeco.id("shelf/" + woodPathName), Datagen.SHELF_BETTERGRASS_DATA);

			DynamicLang.registerWooded("block.aurorasdeco.shelf", block.getWoodType());
		});
	}

	private static void generateSmallLogPilesClientData(ResourceManager resourceManager) {
		SmallLogPileBlock.stream().forEach(block -> {
			if (AuroraUtil.idEqual(block.getWoodType().getId(), "minecraft", "oak"))
				return;
			var woodPathName = block.getWoodType().getPathName();

			var builder = new MultipartBlockStateBuilder(block);

			var partTypes = PartType.getValues();
			var models = new Identifier[partTypes.size()];
			for (int i = 0; i < partTypes.size(); i++) {
				var partType = partTypes.get(i);
				models[i] = modelBuilder(switch (partType) {
					case BOTTOM -> SmallLogPileBlock.BOTTOM_MODEL;
					case TOP -> SmallLogPileBlock.TOP_MODEL;
					case DOUBLE -> SmallLogPileBlock.DOUBLE_MODEL;
				})
						.texture("log", block.getWoodType().getLogSideTexture(resourceManager))
						.texture("log_top", block.getWoodType().getLogTopTexture(resourceManager))
						.register(id("block/small_log_pile/" + woodPathName + '/' + partType.asString()));
			}

			for (var direction : AuroraUtil.DIRECTIONS) {
				if (direction.getAxis().isVertical())
					continue;

				var facingValue = SmallLogPileBlock.FACING.createValue(direction);

				builder.addWhenOr(new StateModel(models[0], (int) direction.asRotation()),
						new MultipartOr(facingValue, AurorasDecoProperties.PART_TYPE_BOTTOM),
						new MultipartOr(facingValue, AurorasDecoProperties.PART_TYPE_DOUBLE));
				builder.addWhenOr(new StateModel(models[1], (int) direction.asRotation()),
						new MultipartOr(facingValue, AurorasDecoProperties.PART_TYPE_TOP),
						new MultipartOr(facingValue, AurorasDecoProperties.PART_TYPE_DOUBLE));
				builder.addWhen(new StateModel(models[2], (int) direction.asRotation()), facingValue, AurorasDecoProperties.PART_TYPE_DOUBLE);
			}

			modelBuilder(models[0]).register(id("item/small_log_pile/" + block.getWoodType().getPathName()));

			builder.register();

			registerBetterGrassLayer(block, SmallLogPileBlock.BETTERGRASS_DATA);

			DynamicLang.registerProvider(
					"block.aurorasdeco.small_log_pile." + block.getWoodType().getAbsoluteLangPath(),
					entry -> entries -> entries.get("block.aurorasdeco.small_" + block.getWoodType().getLogType() + "_pile")
							.formatted(entries.get(block.getWoodType().getFullLangPath()))
			);
		});
	}

	private static void generateStumpsClientData(ResourceManager resourceManager) {
		StumpBlock.streamLogStumps().forEach(block -> {
			var builder = blockStateBuilder(block);

			Identifier model;
			var logSideTexture = block.getWoodType().getLogSideTexture(resourceManager);
			var logTopTexture = block.getWoodType().getLogTopTexture(resourceManager);
			if (block.getWoodType().getLogType().equals("stem")) {
				Identifier leavesTexture;
				var component = block.getWoodType().getComponent(WoodType.ComponentType.LEAVES);
				if (component == null) leavesTexture = new Identifier("block/red_mushroom_block");
				else leavesTexture = block.getWoodType().getLeavesTexture(resourceManager);
				model = modelBuilder(StumpBlock.STEM_STUMP_MODEL)
						.texture("log_side", logSideTexture)
						.texture("log_top", logTopTexture)
						.texture("mushroom", leavesTexture)
						.register(block);
				for (var direction : AuroraUtil.DIRECTIONS) {
					if (direction.getAxis().isHorizontal())
						builder.addToVariant("", model, (int) direction.asRotation());
				}
			} else {
				model = new ModelBuilder(StumpBlock.LOG_STUMP_MODEL)
						.texture("log_side", logSideTexture)
						.texture("log_top", logTopTexture)
						.texture("leaf", LOG_STUMP_LEAF_TEXTURE)
						.register(block);
				var brownMushroomModel = modelBuilder(StumpBlock.LOG_STUMP_BROWN_MUSHROOM_MODEL)
						.texture("log_side", logSideTexture)
						.texture("log_top", logTopTexture)
						.texture("leaf", LOG_STUMP_LEAF_TEXTURE)
						.texture("mushroom", new Identifier("block/brown_mushroom_block"))
						.register(id("block/stump/"
								+ block.getWoodType().getPathName() + "_brown_mushroom"));
				var redMushroomModel = modelBuilder(StumpBlock.LOG_STUMP_RED_MUSHROOM_MODEL)
						.texture("log_side", logSideTexture)
						.texture("log_top", logTopTexture)
						.texture("leaf", LOG_STUMP_LEAF_TEXTURE)
						.texture("mushroom", new Identifier("block/red_mushroom_block"))
						.register(id("block/stump/"
								+ block.getWoodType().getPathName() + "_red_mushroom"));

				for (var direction : AuroraUtil.DIRECTIONS) {
					if (direction.getAxis().isHorizontal()) {
						int rotation = (int) direction.asRotation();
						builder.addToVariant("", model, rotation);
						builder.addToVariant("", brownMushroomModel, rotation);
						builder.addToVariant("", redMushroomModel, rotation);
					}
				}
			}

			modelBuilder(model).register(id("item/stump/" + block.getWoodType().getPathName()));

			builder.register();

			registerBetterGrassLayer(block, StumpBlock.STUMP_BETTERGRASS_DATA);

			DynamicLang.registerWooded("block.aurorasdeco.stump", block.getWoodType());
		});
	}

	private static void generateSimpleItemModel(Item item) {
		var itemId = Registry.ITEM.getId(item);
		generateSimpleItemModel(new Identifier(itemId.getNamespace(), "item/" + itemId.getPath()));
	}

	private static void generateSimpleItemModel(Identifier id) {
		modelBuilder(new Identifier("item/generated"))
				.texture("layer0", id)
				.register(id);
	}

	public static String toPath(Identifier id, ResourceType type) {
		return toPath(id, type, "");
	}

	public static String toPath(Identifier id, ResourceType type, String prefix) {
		return type.getDirectory() + '/' + id.getNamespace() + '/' + prefix + id.getPath();
	}

	public static BlockStateBuilder blockStateBuilder(Block block) {
		return new BlockStateBuilder(block);
	}

	public static MultipartBlockStateBuilder multipartBlockStateBuilder(Block block) {
		return new MultipartBlockStateBuilder(block);
	}

	public static ModelBuilder modelBuilder(Identifier parent) {
		return new ModelBuilder(parent);
	}
}
