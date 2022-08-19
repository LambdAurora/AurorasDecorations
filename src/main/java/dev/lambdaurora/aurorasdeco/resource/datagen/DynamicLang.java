/*
 * Copyright (c) 2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.resource.datagen;

import dev.lambdaurora.aurorasdeco.registry.WoodType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.function.Function;

/**
 * Gives the ability to dynamically generate lang entries.
 * <p>
 * It might not generate stuff perfectly, but in the context of a lot of dynamically generated blocks it's the best effort that could be done.
 *
 * @author LambdAurora
 */
public final class DynamicLang {
	private static final Map<String, EntryProvider> PROVIDERS = new Object2ObjectOpenHashMap<>();

	public static void registerProvider(String entry, Function<String, EntryProvider> provider) {
		PROVIDERS.computeIfAbsent(entry, provider);
	}

	public static void registerWooded(String baseName, WoodType woodType) {
		registerProvider(
				baseName + '.' + woodType.getAbsoluteLangPath(),
				entry -> context -> context.getFormatted(baseName, context.getOrKey(woodType.getFullLangPath()))
		);
	}

	/**
	 * Applies the dynamic language entries.
	 *
	 * @param entries the existing language entries
	 */
	@ApiStatus.Internal
	public static void apply(Map<String, String> entries) {
		var context = new Context(entries);

		var oakPlanks = WoodType.OAK.getComponent(WoodType.ComponentType.PLANKS);
		var oakLog = WoodType.OAK.getComponent(WoodType.ComponentType.LOG);
		var oakSlab = WoodType.OAK.getComponent(WoodType.ComponentType.SLAB);

		var oakPlanksName = context.get(oakPlanks.block().getTranslationKey());
		var oakLogName = context.get(oakLog.block().getTranslationKey());
		var oakSlabName = context.get(oakSlab.block().getTranslationKey());
		assert oakLogName != null;
		var common = extractCommonPart(oakPlanksName, oakLogName);

		if (common != null) {
			{
				assert oakSlabName != null;
				var refined = extractCommonPart(common, oakSlabName);

				if (refined != null) {
					common = refined;
				}
			}

			var planksName = oakPlanksName.replace(common, "").strip();
			var logName = oakLogName.replace(common, "").strip();

			WoodType.forEach(woodType -> {
				if (entries.containsKey(woodType.getFullLangPath())) return;

				String woodName = null;
				var component = woodType.getComponent(WoodType.ComponentType.PLANKS);

				if (component == null) {
					component = woodType.getComponent(WoodType.ComponentType.LOG);
				} else {
					var compName = context.get(component.block().getTranslationKey());

					if (compName != null)
						woodName = compName.replace(planksName, "").strip();
				}

				if (component == null) return;
				else if (woodName == null) {
					var compName = context.get(component.block().getTranslationKey());

					if (compName != null)
						woodName = compName.replace(logName, "").strip();
				}

				if (woodName == null) return; // Unlikely

				entries.put(woodType.getFullLangPath(), woodName);
			});
		}

		PROVIDERS.forEach((entry, provider) -> {
			entries.computeIfAbsent(entry, s -> provider.provideEntry(context));
		});
	}

	private static String extractCommonPart(String first, String second) {
		if (second.startsWith(first)) {
			return first;
		} else if (second.endsWith(first)) {
			return first;
		}

		for (int i = 0; i < first.length() && i < second.length(); i++) {
			if (first.charAt(i) != second.charAt(i)) {
				if (i != 0) {
					return first.substring(0, i).strip();
				}

				break;
			}
		}

		String reversedFirst = new StringBuilder(first).reverse().toString();
		String reversedSecond = new StringBuilder(second).reverse().toString();

		for (int i = 0; i < reversedFirst.length() && i < reversedSecond.length(); i++) {
			if (reversedFirst.charAt(i) != reversedSecond.charAt(i)) {
				if (i != 0) {
					return new StringBuilder(reversedFirst.substring(0, i)).reverse().toString().strip();
				}

				break;
			}
		}

		return null;
	}

	@FunctionalInterface
	public interface EntryProvider {
		String provideEntry(Context context);
	}

	public record Context(@UnmodifiableView Map<String, String> entries) {
		public @Nullable String get(String key) {
			return this.entries.get(key);
		}

		@Contract(value = "!null -> !null", pure = true)
		public String getOrKey(String key) {
			return this.entries.getOrDefault(key, key);
		}

		public String getFormatted(String key, Object... args) {
			var value = this.get(key);

			if (value == null) {
				return key;
			}

			return value.formatted(args);
		}
	}
}
