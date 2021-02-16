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

package dev.lambdaurora.aurorasdeco.resource;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AurorasDecoPack implements ModResourcePack {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<String> NAMESPACES = Sets.newHashSet(AurorasDeco.NAMESPACE);

    private final Map<String, byte[]> resources = new Object2ObjectOpenHashMap<>();

    public AurorasDecoPack() {
    }

    public AurorasDecoPack rebuild(ResourceType type) {
        return type == ResourceType.CLIENT_RESOURCES ? this.rebuildClient() : this;
    }

    public AurorasDecoPack rebuildClient() {
        JsonObject baseBigFlowerPotJson = new JsonObject();

        {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", AurorasDeco.NAMESPACE + ":block/big_flower_pot/big_flower_pot");
            JsonObject variants = new JsonObject();
            variants.add("", variant);
            baseBigFlowerPotJson.add("variants", variants);
        }

        PottedPlantType.stream().filter(type -> !type.isEmpty() && type.getPot().hasDynamicModel())
                .forEach(type -> {
                    Identifier id = Registry.BLOCK.getId(type.getPot());
                    this.putJson("assets/" + id.getNamespace() + "/blockstates/" + id.getPath() + ".json",
                            baseBigFlowerPotJson);
                });

        return this;
    }

    public void putResource(String resource, byte[] data) {
        this.resources.put(resource, data);
    }

    public void putJson(String resource, JsonObject json) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        jsonWriter.setLenient(true);
        try {
            Streams.write(json, jsonWriter);
        } catch (IOException e) {
            LOGGER.error("Failed to write JSON at {}.", resource, e);
        }
        this.putResource(resource, stringWriter.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ModMetadata getFabricModMetadata() {
        return FabricLoader.getInstance().getModContainer(AurorasDeco.NAMESPACE).get().getMetadata();
    }

    @Override
    public @Nullable InputStream openRoot(String fileName) throws IOException {
        byte[] data;
        if ((data = this.resources.get(fileName)) != null) {
            return new ByteArrayInputStream(data);
        }
        throw new IOException("Generated resources pack has no data or alias for " + fileName);
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException {
        if (type == ResourceType.SERVER_DATA) throw new IOException("Reading server data from Aurora's Decorations client resource pack");
        return this.openRoot(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        if (type == ResourceType.SERVER_DATA) return Collections.emptyList();
        String start = "assets/" + namespace + "/" + prefix;
        return this.resources.keySet().stream()
                .filter(s -> s.startsWith(start) && pathFilter.test(s))
                .map(AurorasDecoPack::fromPath)
                .collect(Collectors.toList());
    }

    @Override
    public boolean contains(ResourceType type, Identifier id) {
        String path = type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath();
        return this.resources.containsKey(path);
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return NAMESPACES;
    }

    @Nullable
    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
        return null;
    }

    @Override
    public String getName() {
        return "Aurora's Decorations Virtual Pack";
    }

    @Override
    public void close() {
        this.resources.clear();
    }

    private static Identifier fromPath(String path) {
        String[] split = path.split("/", 2);
        return new Identifier(split[0], split[1]);
    }
}
