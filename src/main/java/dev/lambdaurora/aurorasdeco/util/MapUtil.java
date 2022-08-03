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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Represents a util class for Map manipulations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class MapUtil {
	private MapUtil() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}

	public static <K, I, O> Map<K, O> map(Map<K, I> map, Function<I, O> mapper) {
		var out = new Object2ObjectOpenHashMap<K, O>();
		map.forEach((key, input) -> out.put(key, mapper.apply(input)));
		return out;
	}

	public static <K, I, O> Map<K, O> mapWithKey(Map<K, I> map, BiFunction<K, I, O> mapper) {
		var out = new Object2ObjectOpenHashMap<K, O>();
		map.forEach((key, input) -> out.put(key, mapper.apply(key, input)));
		return out;
	}

	public static <K, I> Map<K, I> filterWithKey(Map<K, I> map, BiPredicate<K, I> filter) {
		var out = new Object2ObjectOpenHashMap<K, I>();
		map.forEach((key, input) -> {
			if (filter.test(key, input))
				out.put(key, input);
		});
		return out;
	}

	public static <K extends Enum<K>, I, O> Map<K, O> mapWithEnumKey(Map<K, I> map, BiFunction<K, I, O> mapper) {
		var out = new ImmutableMap.Builder<K, O>();
		map.forEach((key, input) -> out.put(key, mapper.apply(key, input)));
		return Maps.newEnumMap(out.build());
	}

	public static <I, O> Int2ObjectMap<O> map(Int2ObjectMap<I> map, Function<I, O> mapper) {
		var out = new Int2ObjectOpenHashMap<O>();
		map.int2ObjectEntrySet().forEach(entry -> out.put(entry.getIntKey(), mapper.apply(entry.getValue())));
		return out;
	}

	public static <I, O> Int2ObjectMap<O> mapWithKey(Int2ObjectMap<I> map, FunctionWithIntKey<I, O> mapper) {
		var out = new Int2ObjectOpenHashMap<O>();
		map.int2ObjectEntrySet().forEach(entry ->
				out.put(entry.getIntKey(),
						mapper.apply(entry.getIntKey(), entry.getValue()))
		);
		return out;
	}

	@FunctionalInterface
	public interface FunctionWithIntKey<I, O> {
		O apply(int key, I input);
	}
}
