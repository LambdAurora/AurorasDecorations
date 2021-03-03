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

package dev.lambdaurora.aurorasdeco.resource;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.AbstractBlockAccessor;
import dev.lambdaurora.aurorasdeco.recipe.RecipeSerializerExtended;
import dev.lambdaurora.aurorasdeco.recipe.WoodcuttingRecipe;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceType;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static dev.lambdaurora.aurorasdeco.util.AuroraUtil.jsonArray;

/**
 * Represents the Aurora's Decorations data generator.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class Datagen {
    public static final Logger LOGGER = LogManager.getLogger("aurorasdeco:datagen");

    private static final Pattern PLANKS_TO_BASE_ID = Pattern.compile("[_/]planks$");
    private static final Pattern PLANKS_SEPARATOR_DETECTOR = Pattern.compile("[/]planks$");
    private static final Pattern LOG_TO_BASE_ID = Pattern.compile("[_/]log$");
    private static final Pattern LOG_SEPARATOR_DETECTOR = Pattern.compile("[/]log$");
    private static final Pattern STEM_TO_BASE_ID = Pattern.compile("[_/]stem$");
    private static final Pattern STEM_SEPARATOR_DETECTOR = Pattern.compile("[/]stem$");

    private static final Map<RecipeType<?>, List<Recipe<?>>> RECIPES = new Object2ObjectOpenHashMap<>();
    private static final Map<Recipe<?>, String> RECIPES_CATEGORIES = new Object2ObjectOpenHashMap<>();

    public static void applyRecipes(Map<Identifier, JsonElement> map,
                                    Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> builderMap) {
        int[] recipeCount = new int[]{0};
        RECIPES.forEach((key, recipes) -> {
            ImmutableMap.Builder<Identifier, Recipe<?>> recipeBuilder =
                    builderMap.computeIfAbsent(key, o -> ImmutableMap.builder());

            recipes.forEach(recipe -> {
                if (!map.containsKey(recipe.getId())) {
                    recipeBuilder.put(recipe.getId(), recipe);
                    recipeCount[0]++;
                }
            });
        });

        LOGGER.info("Loaded {} additional recipes", recipeCount[0]);
    }

    public static void registerRecipe(Recipe<?> recipe, String category) {
        RECIPES.computeIfAbsent(recipe.getType(),
                recipeType -> new ArrayList<>()).add(recipe);
        RECIPES_CATEGORIES.put(recipe, category);
    }

    public static JsonObject inventoryChangedCriteria(String type, Identifier item) {
        JsonObject root = new JsonObject();
        root.addProperty("trigger", "minecraft:inventory_changed");
        JsonObject conditions = new JsonObject();
        JsonArray items = new JsonArray();
        JsonObject child = new JsonObject();
        child.addProperty(type, item.toString());
        conditions.add("items", items);
        root.add("conditions", conditions);
        return root;
    }

    public static JsonObject inventoryChangedCriteria(Ingredient item) {
        JsonObject root = new JsonObject();
        root.addProperty("trigger", "minecraft:inventory_changed");
        JsonObject conditions = new JsonObject();
        JsonArray items = new JsonArray();
        items.add(item.toJson());
        conditions.add("items", items);
        root.add("conditions", conditions);
        return root;
    }

    public static JsonObject recipeUnlockedCriteria(Identifier recipe) {
        JsonObject root = new JsonObject();
        root.addProperty("trigger", "minecraft:recipe_unlocked");
        JsonObject conditions = new JsonObject();
        conditions.addProperty("recipe", recipe.toString());
        root.add("conditions", conditions);
        return root;
    }

    public static JsonObject simpleRecipeUnlock(Identifier recipe, List<Ingredient> ingredients, Ingredient output) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:recipes/root");
        JsonObject rewards = new JsonObject();
        rewards.add("recipes", AuroraUtil.jsonArray(new Object[]{recipe}));
        root.add("rewards", rewards);

        JsonObject criteria = new JsonObject();
        JsonArray requirements = new JsonArray();
        int i = 0;
        for (Ingredient ingredient : ingredients) {
            criteria.add("has_" + i, inventoryChangedCriteria(ingredient));
            requirements.add("has_" + i);
            i++;
        }

        criteria.add("has_self", inventoryChangedCriteria(output));
        criteria.add("has_the_recipe", recipeUnlockedCriteria(recipe));
        root.add("criteria", criteria);

        requirements.add("has_self");
        requirements.add("has_the_recipe");
        root.add("requirements", requirements);
        return root;
    }

    public static JsonObject simpleRecipeUnlock(Recipe<?> recipe) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:recipes/root");
        JsonObject rewards = new JsonObject();
        rewards.add("recipes", AuroraUtil.jsonArray(new Object[]{recipe.getId()}));
        root.add("rewards", rewards);

        JsonObject criteria = new JsonObject();
        JsonArray requirements = new JsonArray();
        int i = 0;
        for (Ingredient ingredient : recipe.getPreviewInputs()) {
            criteria.add("has_" + i, inventoryChangedCriteria(ingredient));
            requirements.add("has_" + i);
            i++;
        }

        criteria.add("has_self", inventoryChangedCriteria(Ingredient.ofStacks(recipe.getOutput())));
        criteria.add("has_the_recipe", recipeUnlockedCriteria(recipe.getId()));
        root.add("criteria", criteria);

        requirements.add("has_self");
        requirements.add("has_the_recipe");
        root.add("requirements", jsonArray(new Object[]{requirements}));
        return root;
    }

    public static void registerSimpleRecipesUnlock() {
        RECIPES_CATEGORIES.forEach((recipe, category) -> {
            JsonObject json = simpleRecipeUnlock(recipe);

            Identifier id = new Identifier(recipe.getId().getNamespace(),
                    "advancements/recipes/" + category + "/" + recipe.getId().getPath());

            AurorasDeco.RESOURCE_PACK.putJson(ResourceType.SERVER_DATA, id, json);
        });
    }

    public static JsonObject simpleBlockLootTable(Identifier id, boolean copyName) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        JsonArray pools = new JsonArray();
        {
            JsonObject pool = new JsonObject();
            pool.addProperty("rolls", 1.0);
            pool.addProperty("bonus_rolls", 0.0);

            JsonArray entries = new JsonArray();

            JsonObject entry = new JsonObject();
            entry.addProperty("type", "minecraft:item");
            entry.addProperty("name", id.toString());

            if (copyName) {
                JsonObject function = new JsonObject();
                function.addProperty("function", "minecraft:copy_name");
                function.addProperty("source", "block_entity");
                entry.add("functions", jsonArray(new JsonObject[]{function}));
            }

            entries.add(entry);

            pool.add("entries", entries);

            pools.add(pool);
        }

        root.add("pools", pools);

        return root;
    }

    public static void registerSimpleBlockLootTable(Block block) {
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

    public static JsonObject recipeRoot(Identifier id) {
        return recipeRoot(id.toString());
    }

    public static JsonObject recipeRoot(String type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        return json;
    }

    @SuppressWarnings("unchecked")
    public static JsonObject recipe(Recipe<?> recipe) {
        if (!(recipe.getSerializer() instanceof RecipeSerializerExtended))
            throw new UnsupportedOperationException("Cannot serialize recipe " + recipe);

        return ((RecipeSerializerExtended<Recipe<?>>) recipe.getSerializer()).toJson(recipe);
    }

    public static void registerWoodcuttingRecipesForBlockVariants(Block block) {
        if (!(((AbstractBlockAccessor) block).getMaterial() == Material.WOOD
                || ((AbstractBlockAccessor) block).getMaterial() == Material.NETHER_WOOD))
            return;

        Identifier blockId = Registry.BLOCK.getId(block);
        if (blockId.getPath().endsWith("planks")) {
            char separator = '_';
            String basePath = PLANKS_TO_BASE_ID.matcher(blockId.getPath()).replaceAll("");
            if (PLANKS_SEPARATOR_DETECTOR.matcher(blockId.getPath()).matches()) separator = '/';
            basePath += separator;

            tryRegisterWoodcuttingRecipeFor(block, basePath, "slab", 2, "building_blocks");
            tryRegisterWoodcuttingRecipeFor(block, basePath, "stairs", 1, "building_blocks");
            tryRegisterWoodcuttingRecipeFor(block, basePath, "button", 1, "redstone");
            tryRegisterWoodcuttingRecipeFor(block, basePath, "pressure_plate", 1,
                    "redstone");
            tryRegisterWoodcuttingRecipeFor(block, basePath, "trapdoor", 1, "redstone");
            tryRegisterWoodcuttingRecipeFor(block, basePath, "door", 1, "redstone");
            tryRegisterWoodcuttingRecipeFor(block, basePath, "fence", 2, "decorations");
            tryRegisterWoodcuttingRecipeFor(block, basePath, "fence_gate", 1, "redstone");
        } else if (blockId.getPath().endsWith("log")) {
            char separator = '_';
            String basePath = LOG_TO_BASE_ID.matcher(blockId.getPath()).replaceAll("");
            if (LOG_SEPARATOR_DETECTOR.matcher(blockId.getPath()).matches()) separator = '/';
            basePath += separator;

            tryRegisterWoodcuttingRecipeFor(block, basePath, "wood", 1, "building_blocks");

            if (FabricLoader.getInstance().isModLoaded("blockus")) {
                tryRegisterWoodcuttingRecipeFor(block, "blockus", basePath,
                        "small_logs", 1, "building_blocks");
            }
        } else if (blockId.getPath().endsWith("stem")) {
            char separator = '_';
            String basePath = STEM_TO_BASE_ID.matcher(blockId.getPath()).replaceAll("");
            if (STEM_SEPARATOR_DETECTOR.matcher(blockId.getPath()).matches()) separator = '/';
            basePath += separator;

            tryRegisterWoodcuttingRecipeFor(block, basePath, "hyphae", 1, "building_blocks");
            if (FabricLoader.getInstance().isModLoaded("blockus")) {
                tryRegisterWoodcuttingRecipeFor(block, "blockus", basePath,
                        "small_stems", 1, "building_blocks");
            }
        }
    }

    public static void registerDefaultWoodcuttingRecipes() {
        registerRecipe(new WoodcuttingRecipe(new Identifier("woodcutting/stick"), "",
                Ingredient.fromTag(ItemTags.PLANKS),
                new ItemStack(Items.STICK, 2)), "misc");

        Registry.BLOCK.stream().filter(block -> ((AbstractBlockAccessor) block).getMaterial() == Material.WOOD
                || ((AbstractBlockAccessor) block).getMaterial() == Material.NETHER_WOOD)
                .forEach(Datagen::registerWoodcuttingRecipesForBlockVariants);
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
        Identifier id = new Identifier(namespace, basePath + type);
        Item item = Registry.ITEM.get(id);
        if (item != Items.AIR) {
            WoodcuttingRecipe recipe = new WoodcuttingRecipe(
                    new Identifier(namespace, "woodcutting/" + basePath + type),
                    "", Ingredient.ofItems(planks),
                    new ItemStack(item, count));
            registerRecipe(recipe, category);
        }
    }

    public static String toPath(Identifier id, ResourceType type) {
        return type.getDirectory() + '/' + id.getNamespace() + '/' + id.getPath();
    }
}
