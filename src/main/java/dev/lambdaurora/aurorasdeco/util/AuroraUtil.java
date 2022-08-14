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

package dev.lambdaurora.aurorasdeco.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AuroraUtil {
	public static final List<Direction> DIRECTIONS = List.of(Direction.values());
	public static final List<Direction> HORIZONTAL_DIRECTIONS = DIRECTIONS.stream().filter(d -> d.getAxis().isHorizontal()).toList();

	private AuroraUtil() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}

	public static Identifier toResourcePackId(Identifier id, String prefix, String extension) {
		return new Identifier(id.getNamespace(), prefix + id.getPath() + '.' + extension);
	}

	public static Identifier toAbsoluteTexturesId(Identifier id) {
		return toResourcePackId(id, "textures/", "png");
	}

	public static <T> boolean contains(Collection<T> list, List<T> required) {
		int i = 0;
		var it = list.iterator();
		while (it.hasNext() && i < required.size()) {
			if (required.contains(it.next()))
				i++;
		}
		return i == required.size();
	}

	/**
	 * Returns whether the given id is equal to the given {@code namespace}:{@code path} id without allocation.
	 *
	 * @param id the identifier to compare
	 * @param namespace the namespace to compare to the identifier
	 * @param path the path to compare to the identifier
	 * @return {@code true} if both identifiers are equal, or {@code false} otherwise
	 */
	public static boolean idEqual(Identifier id, String namespace, String path) {
		return id.getNamespace().equals(namespace) && id.getPath().equals(path);
	}

	public static double posMod(double n, double d) {
		double v = n % d;
		if (v < 0) v = d + v;
		return v;
	}

	public static boolean isShapeEqual(Box s1, Box s2) {
		return s1.minX == s2.minX && s1.minY == s2.minY && s1.minZ == s2.minZ
				&& s1.maxX == s2.maxX && s1.maxY == s2.maxY && s1.maxZ == s2.maxZ;
	}

	public static Identifier appendWithNamespace(String prefix, Identifier id) {
		var path = id.getPath();
		if (!id.getNamespace().equals("minecraft") && !id.getNamespace().equals("aurorasdeco"))
			path = id.getNamespace() + '/' + path;
		return AurorasDeco.id(prefix + '/' + path);
	}

	public static String getIdPath(String prefix, Identifier originalId, String replacerRegex) {
		var namespace = originalId.getNamespace();
		namespace = switch (namespace) {
			case "minecraft", AurorasDeco.NAMESPACE -> "";
			default -> namespace + '/';
		};
		return prefix + '/' + namespace + originalId.getPath().replaceAll(replacerRegex, "");
	}

	/* NBT */

	public static void writeBlockEntityNbtToStack(ItemStack stack, BlockEntityType<?> type, NbtCompound nbt, boolean force) {
		boolean hasDummy = false;
		if (nbt.isEmpty() && force) {
			nbt.putBoolean("aurorasdeco$dummy", true);
			hasDummy = true;
		}

		BlockItem.writeBlockEntityNbtToStack(stack, type, nbt);
		nbt.remove("id");

		if (hasDummy) {
			nbt.remove("aurorasdeco$dummy");
		}
	}

	public static NbtCompound getOrCreateBlockEntityNbt(ItemStack stack, BlockEntityType<?> type) {
		var nbt = BlockItem.getBlockEntityNbtFromStack(stack);
		if (nbt == null) {
			/* setBlockEntityNbt only actually sets the nbt tag if it isn't empty.
			   We want to hit the code path to set the nbt tag. So we add a dummy boolean to our tag,
			    call the method to add it (which actually adds it because it's not empty),
			    and then remove the dummy boolean again.
			 */
			var nbt2 = new NbtCompound();
			writeBlockEntityNbtToStack(stack, type, nbt2, true);
			return nbt2;
		} else {
			return nbt;
		}
	}

	/* State Utils */

	public static BlockState remapBlockState(BlockState src, BlockState dst) {
		for (var property : src.getProperties()) {
			dst = remapProperty(src, property, dst);
		}
		return dst;
	}

	private static <T extends Comparable<T>> BlockState remapProperty(BlockState src, Property<T> property, BlockState dst) {
		if (dst.contains(property))
			dst = dst.with(property, src.get(property));
		return dst;
	}

	public static boolean isWaterLogged(BlockState state) {
		if (state.getProperties().contains(Properties.WATERLOGGED)) return state.get(Properties.WATERLOGGED);
		return false;
	}

	/* Shape Utils */

	public static VoxelShape resizeVoxelShape(VoxelShape shape, double factor) {
		var shapes = new ArrayList<VoxelShape>();
		shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
			shapes.add(VoxelShapes.cuboid(minX * factor, minY * factor, minZ * factor,
					maxX * factor, maxY * factor, maxZ * factor));
		});

		if (shapes.size() == 1)
			return shapes.get(0);
		return shapes.stream().collect(VoxelShapes::empty, VoxelShapes::union, VoxelShapes::union).simplify();
	}

	/* Json Utils */

	public static JsonArray jsonArray(Object... elements) {
		var array = new JsonArray();
		for (var element : elements) {
			if (element instanceof Number)
				array.add((Number) element);
			else if (element instanceof Boolean)
				array.add((Boolean) element);
			else if (element instanceof Character)
				array.add((Character) element);
			else if (element instanceof JsonElement)
				array.add((JsonElement) element);
			else
				array.add(String.valueOf(element));

		}
		return array;
	}
}
