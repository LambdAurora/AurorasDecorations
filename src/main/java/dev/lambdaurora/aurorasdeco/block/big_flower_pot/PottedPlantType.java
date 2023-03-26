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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.plant.AuroraPlantBlock;
import dev.lambdaurora.aurorasdeco.block.plant.DaffodilBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private final String id;
	private final Block plant;
	private final Item item;
	private final Category category;
	private BigFlowerPotBlock pot;

	private PottedPlantType(String id, Block plant, Item item) {
		this.id = id;
		this.plant = plant;
		this.item = item;

		this.category = Arrays.stream(Category.values())
				.filter(category -> category.filter(id, plant, item))
				.findFirst().orElse(Category.UNKNOWN);
	}

	public static PottedPlantType fromId(String id) {
		return TYPES.getOrDefault(id, AurorasDecoRegistry.BIG_FLOWER_POT_BLOCK.getPlantType());
	}

	private static String getIdFromPlant(Identifier plantId) {
		var id = plantId.toString();
		if (id.startsWith("minecraft:")) {
			id = id.substring("minecraft:".length());
		} else if (id.startsWith(AurorasDeco.NAMESPACE + ':')) {
			id = id.substring(AurorasDeco.NAMESPACE.length() + 1);
		} else {
			id = id.replace(':', '/');
		}

		return id;
	}

	public static @Nullable BigFlowerPotBlock registerFromItem(Item item) {
		if (item instanceof BlockItem blockItem) {
			var block = blockItem.getBlock();
			var id = getIdFromPlant(Registries.BLOCK.getId(block));

			if (TYPES.containsKey(id))
				return null;

			return register(id, block, item);
		}
		return null;
	}

	public static BigFlowerPotBlock register(String id, Block plant, Item item) {
		for (var category : Category.CATEGORIES) {
			if (category.filter(id, plant, item)) {
				if (category.getFactory() != null) {
					return register(id, plant, item, category.getFactory());
				}

				break;
			}
		}
		if (plant instanceof DaffodilBlock)
			return register(id, plant, item, BigPottedDaffodilBlock::new);
		else if (id.startsWith("floral_flair/"))
			return register(id, plant, item, BigPottedProxyBlock::new);
		//else if (plant instanceof SeaPickleBlock)
		//    return register(id, plant, item, BigPottedSeaPickleBlock::new);
		return register(id, plant, item, BigFlowerPotBlock::new);
	}

	public static <T extends BigFlowerPotBlock> T register(String id, Block plant, Item item,
			Function<PottedPlantType, T> flowerPotBlockFactory) {
		var type = new PottedPlantType(id, plant, item);
		TYPES.put(id, type);
		var potBlock = flowerPotBlockFactory.apply(type);
		type.pot = potBlock;
		if (item != Items.AIR || type.isEmpty()) {
			ITEM_TO_FLOWER_POT.put(item, potBlock);
		}
		return potBlock;
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
	 * Returns the category of this plant type.
	 *
	 * @return the category
	 */
	public Category getCategory() {
		return this.category;
	}

	/**
	 * Returns whether this plant type is tall or not.
	 *
	 * @return {@code true} if this plant is tall, or {@code false} otherwise
	 */
	public boolean isTall() {
		return this.getPlant() instanceof TallPlantBlock;
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
	 * @return {@code true} if empty, or {@code false} otherwise
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
			if (block instanceof AuroraPlantBlock special) {
				return special.canBePotted();
			}

			return true;
		} else return block instanceof CactusBlock;
	}

	public static boolean isValidPlant(Item item) {
		if (item instanceof BlockItem) {
			return isValidPlant(((BlockItem) item).getBlock());
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || this.getClass() != o.getClass()) return false;
		var that = (PottedPlantType) o;
		return this.getId().equals(that.getId()) && this.getPlant().equals(that.getPlant()) && Objects.equals(this.getItem(), that.getItem());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getId(), this.getPlant(), this.getItem());
	}

	/**
	 * Represents a plant category.
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public enum Category {
		AZALEA((id, plant, item) -> plant instanceof AzaleaBlock, false, BigPottedAzaleaBlock::new),
		BAMBOO((id, plant, item) -> item == Items.BAMBOO, false, null),
		CACTUS((id, plant, item) -> plant instanceof CactusBlock || id.equals("pocket_cactus"), false, null),
		FLOWER((id, plant, item) -> plant instanceof FlowerBlock, true, null),
		MASCOT((id, plant, item) -> item == Items.POTATO || item == Items.PUMPKIN, false, null),
		MUSHROOM((id, plant, item) -> plant instanceof MushroomPlantBlock || plant instanceof FungusBlock, true, null),
		NETHER_WART((id, plant, item) -> plant instanceof NetherWartBlock, true, BigPottedNetherWartBlock::new),
		SAPLING((id, plant, item) -> plant instanceof SaplingBlock, true, BigFlowerPotBlock::new),
		SWEET_BERRY_BUSH((id, plant, item) -> plant instanceof SweetBerryBushBlock || id.equals("ecotones/blueberry_bush"), false,
				BigPottedSweetBerryBushBlock::new),
		UNKNOWN((id, plant, item) -> true, true, null);

		public static final List<Category> CATEGORIES = List.of(values());

		private final CategoryFilter filter;
		private final boolean allowBlocksOnTop;
		private final Function<PottedPlantType, BigFlowerPotBlock> factory;

		Category(CategoryFilter filter, boolean allowBlocksOnTop, Function<PottedPlantType, BigFlowerPotBlock> factory) {
			this.filter = filter;
			this.allowBlocksOnTop = allowBlocksOnTop;
			this.factory = factory;
		}

		public boolean allowBlocksOnTop() {
			return this.allowBlocksOnTop;
		}

		public @Nullable Function<PottedPlantType, BigFlowerPotBlock> getFactory() {
			return this.factory;
		}

		private boolean filter(String id, Block plant, Item item) {
			return this.filter.filter(id, plant, item);
		}
	}

	/**
	 * Represents a category filter.
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	private interface CategoryFilter {
		boolean filter(String id, Block plant, Item item);
	}
}
