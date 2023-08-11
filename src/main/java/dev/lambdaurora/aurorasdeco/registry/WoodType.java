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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.resource.ModTagReader;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.FlammableBlockEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a wood type.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class WoodType {
	public static final WoodType OAK;
	public static final WoodType BAMBOO;

	private static final List<ModificationCallbackEntry> CALLBACKS = new ArrayList<>();
	private static final List<WoodType> TYPES;
	private final Map<ComponentType, Component> components = new Reference2ObjectOpenHashMap<>();
	private final List<ModificationCallbackEntry> toTrigger = new ArrayList<>();
	private final Identifier id;
	private final String pathName;
	private final String absoluteLangPath;
	private final String langPath;

	public WoodType(Identifier id) {
		this.id = id;
		this.pathName = getPathName(this.id);
		this.absoluteLangPath = this.pathName.replaceAll("/", ".");
		this.langPath = getLangPath(this.id);

		this.toTrigger.addAll(CALLBACKS);
	}

	static {
		TYPES = new ArrayList<>(List.of(
				OAK = new WoodType(new Identifier("oak")),
				new WoodType(new Identifier("spruce")),
				new WoodType(new Identifier("birch")),
				new WoodType(new Identifier("jungle")),
				new WoodType(new Identifier("acacia")),
				new WoodType(new Identifier("dark_oak")),
				new WoodType(new Identifier("mangrove")),
				new WoodType(new Identifier("cherry")),
				new WoodType(AurorasDeco.id("azalea")),
				new WoodType(AurorasDeco.id("flowering_azalea")),
				new WoodType(AurorasDeco.id("jacaranda")),
				BAMBOO = new WoodType(new Identifier("bamboo")),
				new WoodType(new Identifier("mushroom")),
				new WoodType(new Identifier("crimson")),
				new WoodType(new Identifier("warped"))
		));
	}

	/**
	 * Gets the identifier of the wood type.
	 *
	 * @return the identifier of the wood type
	 */
	public Identifier getId() {
		return this.id;
	}

	public String getPathName() {
		return this.pathName;
	}

	public String getAbsoluteLangPath() {
		return this.absoluteLangPath;
	}

	public String getLangPath() {
		return this.langPath;
	}

	public String getFullLangPath() {
		return "aurorasdeco.wood_type." + this.langPath;
	}

	public boolean hasLog() {
		return this.getComponent(ComponentType.LOG) != null;
	}

	public String getLogType() {
		if (AuroraUtil.idEqual(this.id, Identifier.DEFAULT_NAMESPACE, "bamboo")) {
			return "log";
		}

		var component = this.getComponent(ComponentType.LOG);
		if (component == null) return "none";

		return component.id().getPath().substring(this.id.getPath().length() + 1);
	}

	public @Nullable Block getLog() {
		var component = this.getComponent(ComponentType.LOG);
		if (component == null) return null;
		return component.block();
	}

	/**
	 * {@return the planks texture path}
	 */
	public Identifier getPlanksTexture(ResourceManager resourceManager) {
		return ComponentType.PLANKS.getTexture(resourceManager, this.getComponent(ComponentType.PLANKS));
	}

	/**
	 * {@return the log side texture if a log component is associated, otherwise the planks texture}
	 */
	public Identifier getLogSideTexture(ResourceManager resourceManager) {
		var log = this.getComponent(ComponentType.LOG);
		if (log == null) return this.getComponent(ComponentType.PLANKS).texture();
		return ComponentType.LOG.getTexture(resourceManager, log);
	}

	/**
	 * {@return the log top texture if a log component is associated, otherwise the planks texture}
	 */
	public Identifier getLogTopTexture(ResourceManager resourceManager) {
		var log = this.getComponent(ComponentType.LOG);
		if (log == null) return this.getComponent(ComponentType.PLANKS).texture();
		return ComponentType.LOG.getTopTexture(resourceManager, log);
	}

	/**
	 * {@return the leaves texture path}
	 */
	public Identifier getLeavesTexture(ResourceManager resourceManager) {
		return ComponentType.LEAVES.getTexture(resourceManager, this.getComponent(ComponentType.LEAVES));
	}

	/**
	 * Returns the component associated to the given component type.
	 *
	 * @param type the component type
	 * @return the component if associated to the given component type, or {@code null} otherwise
	 */
	public Component getComponent(ComponentType type) {
		return this.components.get(type);
	}

	private void addComponent(ComponentType type, Component component) {
		this.components.put(type, component);

		this.onWoodTypeModified();
	}

	private void onWoodTypeModified() {
		var it = this.toTrigger.iterator();
		while (it.hasNext()) {
			var entry = it.next();

			if (AuroraUtil.contains(this.components.keySet(), entry.requiredComponents())) {
				entry.callback().accept(this);
				it.remove();
			}
		}
	}

	private void tryTriggerCallback(ModificationCallbackEntry callbackEntry) {
		if (AuroraUtil.contains(this.components.keySet(), callbackEntry.requiredComponents())) {
			callbackEntry.callback().accept(this);
			this.toTrigger.remove(callbackEntry);
		}
	}

	@Override
	public String toString() {
		return "WoodType{" +
				"id=" + this.id +
				", pathName='" + this.pathName + '\'' +
				", remaining_registry_callbacks=" + this.toTrigger.size() +
				", components=" + this.components.keySet() +
				'}';
	}

	public static void registerWoodTypeModificationCallback(Consumer<WoodType> callback, ComponentType... requiredComponents) {
		var entry = new ModificationCallbackEntry(callback, Arrays.asList(requiredComponents));
		CALLBACKS.add(entry);

		for (var woodType : TYPES) {
			woodType.toTrigger.add(entry);
			woodType.tryTriggerCallback(entry);
		}
	}

	public static void onBlockRegister(Identifier id, Block block) {
		if (id.getNamespace().equals("mossywood")) return; // Mossywood is too much of a pain to support.

		for (var componentType : ComponentType.types()) {
			var woodName = componentType.filter(id, block);
			if (woodName == null) continue;

			Identifier woodId;
			if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && woodName.equals("azalea")) {
				woodId = AurorasDeco.id("azalea");
			} else {
				woodId = new Identifier(id.getNamespace(), woodName);
			}

			var woodType = TYPES.stream().filter(type -> type.getId().equals(woodId)).findFirst()
					.orElseGet(() -> {
						var newWoodType = new WoodType(woodId);
						TYPES.add(newWoodType);
						return newWoodType;
					});
			woodType.addComponent(componentType, new Component(woodType, block));
			break;
		}
	}

	/**
	 * Returns the wood type of the specified identifier.
	 *
	 * @param id the identifier of the wood type
	 * @return the wood type if it exists, or {@code null} otherwise
	 */
	public static @Nullable WoodType fromId(Identifier id) {
		for (var type : TYPES) {
			if (type.getId().equals(id))
				return type;
		}

		return null;
	}

	public static void forEach(Consumer<WoodType> consumer) {
		TYPES.forEach(consumer);
	}

	private static String getPathName(Identifier id) {
		var path = id.getPath();
		var namespace = id.getNamespace();
		if (!namespace.equals(Identifier.DEFAULT_NAMESPACE) && !namespace.equals(AurorasDeco.NAMESPACE))
			path = namespace + '/' + path;
		return path;
	}

	private static String getLangPath(Identifier id) {
		return switch (id.getPath()) {
			case "azalea" -> "azalea"; // Common
			case "redwood" -> "redwood";
			default -> getPathName(id).replaceAll("/", ".");
		};
	}

	public record Component(WoodType woodType, Block block) {
		public Identifier id() {
			return Registries.BLOCK.getId(this.block());
		}

		public MapColor mapColor() {
			return this.block().getDefaultMapColor();
		}

		public BlockSoundGroup blockSoundGroup() {
			return this.block().getSoundGroup(this.block().getDefaultState());
		}

		public Item item() {
			return this.block().asItem();
		}

		public boolean hasItem() {
			return this.item() != Items.AIR;
		}

		public Identifier getItemId() {
			return Registries.ITEM.getId(this.item());
		}

		public Identifier texture() {
			var id = this.id();
			return new Identifier(id.getNamespace(), "block/" + id.getPath());
		}

		public Identifier topTexture() {
			if (this.block() == Blocks.MUSHROOM_STEM) return new Identifier("block/mushroom_block_inside");
			var id = this.id();
			return new Identifier(id.getNamespace(), "block/" + id.getPath() + "_top");
		}

		public @Nullable FlammableBlockEntry getFlammableEntry() {
			return BlockContentRegistries.FLAMMABLE.getNullable(this.block());
		}

		public void syncFlammabilityWith(Block other) {
			BlockContentRegistries.FLAMMABLE.valueAddedEvent().register((entry, value) -> {
				if (entry == this.block) {
					BlockContentRegistries.FLAMMABLE.put(other, value);
				}
			});
		}

		@ClientOnly
		public BlockColorProvider getBlockColorProvider() {
			return ColorProviderRegistry.BLOCK.get(this.block());
		}

		@ClientOnly
		public ItemColorProvider getItemColorProvider() {
			return ColorProviderRegistry.ITEM.get(this.block());
		}
	}

	// This can't be good
	public enum ComponentType {
		PLANKS((id, block) -> {
			if (!id.getPath().endsWith("_planks")) return null;
			if (!ModTagReader.INSTANCE.getValues(BlockTags.PLANKS).contains(id)) return null;
			return id.getPath().substring(0, id.getPath().length() - "_planks".length());
		}, (resourceManager, component) -> {
			Identifier texture = component.texture();

			if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(texture)).isEmpty()) {
				// For mods that don't use standard texture paths but logical.
				var alternate = new Identifier(component.id().getNamespace(), "block/" + component.woodType().getId().getPath() + "/planks");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(alternate)).isPresent())
					return alternate;
			}

			return texture;
		}),
		LOG((id, block) -> {
			if (block == Blocks.BAMBOO_BLOCK) return "bamboo";
			else if (block == Blocks.MUSHROOM_STEM) return "mushroom";

			String logType;
			if (id.getPath().startsWith("stripped_")) return null;
			else if (id.getPath().startsWith("striped_")) return null;
			else if (id.getPath().startsWith("attached_")) return null;
			else if (id.getPath().endsWith("_log")) logType = "_log";
			else if (id.getPath().endsWith("_stem")) logType = "_stem";
			else return null;

			if (id.getPath().endsWith("_table" + logType)) return null;

			if (!ModTagReader.INSTANCE.getValues(BlockTags.LOGS).contains(id)) return null;

			return id.getPath().substring(0, id.getPath().length() - logType.length());
		}, (resourceManager, component) -> {
			if (component.block() == Blocks.BAMBOO_BLOCK) {
				return new Identifier(Identifier.DEFAULT_NAMESPACE, "block/bamboo_block");
			}

			var componentId = component.id();
			var texture = getBetterNetherEndPaths(component.texture(), false);
			if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(texture)).isPresent())
				return texture;
			else {
				// For mods that don't use standard texture paths but logical.
				var sideId = new Identifier(componentId.getNamespace(), "block/" + component.woodType().getId().getPath() + "/log");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(sideId)).isPresent())
					return sideId;

				// For mods that don't use standard texture paths but logically evil.
				sideId = new Identifier(
						componentId.getNamespace(),
						"block/" + component.woodType().getId().getPath() + "/" + component.woodType().getLogType() + "/side"
				);
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(sideId)).isPresent())
					return sideId;

				// For mods similar to how Yttr does it.
				sideId = new Identifier(componentId.getNamespace(), "block/" + componentId.getPath() + "_side");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(sideId)).isPresent())
					return sideId;

				// For mods similar to how Promenade did it.
				sideId = new Identifier(componentId.getNamespace(), "block/" + componentId.getPath() + "/side");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(sideId)).isPresent()) {
					return sideId;
				}
			}
			return texture;
		}, (resourceManager, component) -> {
			if (component.block() == Blocks.BAMBOO_BLOCK) {
				return new Identifier(Identifier.DEFAULT_NAMESPACE, "block/bamboo_block_top");
			}

			var componentId = component.id();
			var texture = getBetterNetherEndPaths(component.topTexture(), true);
			if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(texture)).isPresent())
				return texture;
			else {
				// For mods that don't use standard texture paths but logical.
				var topId = new Identifier(componentId.getNamespace(), "block/" + component.woodType().getId().getPath() + "/log_top");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(topId)).isPresent())
					return topId;

				// For mods that don't use standard texture paths but logically evil.
				topId = new Identifier(
						componentId.getNamespace(),
						"block/" + component.woodType().getId().getPath() + "/" + component.woodType().getLogType() + "/top"
				);
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(topId)).isPresent())
					return topId;

				// For mods similar to how Promenade did it.
				topId = new Identifier(componentId.getNamespace(), "block/" + componentId.getPath() + "/top");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(topId)).isPresent())
					return topId;
			}
			return texture;
		}),
		SLAB(simpleWoodFilter("slab", BlockTags.WOODEN_SLABS)),
		STAIRS(simpleWoodFilter("stairs", BlockTags.WOODEN_STAIRS)),
		LEAVES((id, block) -> {
			String leavesType;
			if (AuroraUtil.idEqual(id, Identifier.DEFAULT_NAMESPACE, "nether_wart_block"))
				return "crimson"; // Thanks Minecraft.
			else if (id.getPath().endsWith("_leaves")) leavesType = "_leaves";
			else if (id.getPath().endsWith("_wart_block")) leavesType = "_wart_block";
			else return null;

			if (id.getPath().startsWith("flowering")) return null;

			if (!ModTagReader.INSTANCE.getValues(BlockTags.LEAVES).contains(id)) return null;

			return id.getPath().substring(0, id.getPath().length() - leavesType.length());
		}, (resourceManager, component) -> {
			Identifier texture = component.texture();

			if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(texture)).isEmpty()) {
				// For mods that don't use standard texture paths but logical.
				var alternate = new Identifier(component.id().getNamespace(), "block/" + component.woodType().getId().getPath() + "/leaves");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(alternate)).isPresent())
					return alternate;

				alternate = new Identifier(component.id().getNamespace(), "block/" + component.woodType().getId().getPath() + "/wart_block");
				if (resourceManager.getResource(AuroraUtil.toAbsoluteTexturesId(alternate)).isPresent())
					return alternate;
			}

			return texture;
		}),
		PRESSURE_PLATE(simpleWoodFilter("pressure_plate", BlockTags.WOODEN_PRESSURE_PLATES)),
		TRAPDOOR(simpleWoodFilter("trapdoor", BlockTags.WOODEN_TRAPDOORS)),
		DOOR(simpleWoodFilter("door", BlockTags.WOODEN_DOORS)),
		FENCE(simpleWoodFilter("fence", BlockTags.WOODEN_FENCES)),
		FENCE_GATE(simpleWoodFilter("fence_gate", BlockTags.FENCE_GATES)),
		LADDER(simpleWoodFilter("ladder", null));

		private static final List<ComponentType> COMPONENT_TYPES = List.of(values());
		private final Filter filter;
		private final TextureProvider textureProvider;
		private final TextureProvider topTextureProvider;

		ComponentType(Filter filter, TextureProvider textureProvider, TextureProvider topTextureProvider) {
			this.filter = filter;
			this.textureProvider = textureProvider;
			this.topTextureProvider = topTextureProvider;
		}

		ComponentType(Filter filter, TextureProvider textureProvider) {
			this(filter, textureProvider, textureProvider);
		}

		ComponentType(Filter filter) {
			this(filter, BASIC_TEXTURE_PROVIDER);
		}

		public @Nullable String filter(Identifier id, Block block) {
			return this.filter.filter(id, block);
		}

		public Identifier getTexture(ResourceManager resourceManager, Component component) {
			return this.textureProvider.searchTexture(resourceManager, component);
		}

		public Identifier getTopTexture(ResourceManager resourceManager, Component component) {
			return this.topTextureProvider.searchTexture(resourceManager, component);
		}

		public static List<ComponentType> types() {
			return COMPONENT_TYPES;
		}
	}

	public interface Filter {
		@Nullable String filter(Identifier id, Block block);
	}

	private static Filter simpleWoodFilter(String suffix, TagKey<Block> tagKey) {
		return (id, block) -> {
			if (!id.getPath().endsWith('_' + suffix)) return null;
			if (tagKey != null && !ModTagReader.INSTANCE.getValues(tagKey).contains(id)) return null;

			String path = id.getPath().substring(0, id.getPath().length() - (suffix.length() + 1));
			if (WoodType.fromId(new Identifier(id.getNamespace(), path)) == null) return null;
			return path;
		};
	}

	public static final TextureProvider BASIC_TEXTURE_PROVIDER = (resourceManager, component) -> component.texture();

	public interface TextureProvider {
		Identifier searchTexture(ResourceManager resourceManager, Component component);
	}

	public static Identifier getBetterNetherEndPaths(Identifier texture, boolean top) {
		if (top) {
			if (texture.getPath().contains("stalagnate")) {
				String newPath = texture.getPath().substring(0, texture.getPath().length() - 8) + "_bark_top";
				return new Identifier(texture.getNamespace(), newPath);
			}
			return texture;
		}

		if (texture.getNamespace().equals("betternether") || texture.getNamespace().equals("betterend")) {
			String newPath = texture.getPath().substring(0, texture.getPath().length() - 4) + "_bark";
			boolean logSides = texture.getNamespace().equals("betterend") || texture.getPath().contains("rubeus")
					|| texture.getPath().contains("nether_sakura") || texture.getPath().contains("anchor_tree");
			if (logSides) newPath = texture.getPath() + "_side";
			if (texture.getPath().contains("stalagnate")) newPath += "_side";
			return new Identifier(texture.getNamespace(), newPath);
		}

		return texture;
	}

	private record ModificationCallbackEntry(Consumer<WoodType> callback, List<ComponentType> requiredComponents) {
	}
}
