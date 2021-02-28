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

package dev.lambdaurora.aurorasdeco.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a render rule.
 * <p>
 * Render rules can be used to change the default rendering of an item in a specific context like shelves.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class RenderRule {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Identifier, RenderRule> ITEM_RULES = new Object2ObjectOpenHashMap<>();
    private static final Map<Tag<Item>, RenderRule> TAG_RULES = new Object2ObjectOpenHashMap<>();
    private static final JsonParser PARSER = new JsonParser();

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final List<ModelIdentifier> models;

    public RenderRule(List<ModelIdentifier> models) {
        this.models = models;
    }

    public ModelIdentifier getModelId(ItemStack stack) {
        if (this.models.size() == 1) {
            return this.models.get(0);
        } else {
            int i = Math.abs(Objects.hash(stack.getCount(), stack.getName().asString()) % this.models.size());
            return this.models.get(i);
        }
    }

    public BakedModel getModel(ItemStack stack) {
        return this.client.getBakedModelManager().getModel(this.getModelId(stack));
    }

    public static @Nullable RenderRule getRenderRule(ItemStack stack) {
        Identifier itemId = Registry.ITEM.getId(stack.getItem());

        RenderRule rule = ITEM_RULES.get(itemId);
        if (rule != null) {
            return rule;
        }

        for (Map.Entry<Tag<Item>, RenderRule> entry : TAG_RULES.entrySet()) {
            if (stack.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static void reload(ResourceManager manager, Consumer<Identifier> out) {
        ITEM_RULES.clear();
        TAG_RULES.clear();

        manager.findResources("aurorasdeco_render_rules/", path -> path.endsWith(".json")).forEach(id -> {
            try {
                Resource resource = manager.getResource(id);
                JsonElement element = PARSER.parse(new InputStreamReader(resource.getInputStream()));
                if (element.isJsonObject()) {
                    JsonObject root = element.getAsJsonObject();

                    List<ModelIdentifier> modelsId = new ArrayList<>();
                    JsonArray models = root.getAsJsonArray("models");
                    models.forEach(modelElement -> {
                        if (modelElement.isJsonPrimitive()) {
                            modelsId.add(new ModelIdentifier(new Identifier(modelElement.getAsString()), "inventory"));
                        }
                    });

                    if (modelsId.isEmpty())
                        return;

                    RenderRule renderRule = new RenderRule(modelsId);

                    JsonObject match = root.getAsJsonObject("match");
                    boolean success = false;
                    if (match.has("item")) {
                        ITEM_RULES.put(Identifier.tryParse(match.get("item").getAsString()), renderRule);
                        success = true;
                    } else if (match.has("items")) {
                        JsonArray array = match.getAsJsonArray("items");
                        for (JsonElement item : array) {
                            ITEM_RULES.put(Identifier.tryParse(item.getAsString()), renderRule);
                        }
                        success = true;
                    } else if (match.has("tag")) {
                        Identifier tagId = Identifier.tryParse(match.get("tag").getAsString());
                        Tag<Item> tag = TagRegistry.item(tagId);
                        TAG_RULES.put(tag, renderRule);
                        success = true;
                    }

                    if (success)
                        modelsId.forEach(out);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read render rule {}. {}", id, e);
            }
        });
    }
}
