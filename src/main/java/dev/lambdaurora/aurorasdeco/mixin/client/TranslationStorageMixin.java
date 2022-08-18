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

package dev.lambdaurora.aurorasdeco.mixin.client;

import dev.lambdaurora.aurorasdeco.resource.datagen.DynamicLang;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {
	@Inject(
			method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resource/language/TranslationStorage;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void onLoad(ResourceManager resourceManager, List<LanguageDefinition> definitions, CallbackInfoReturnable<TranslationStorage> cir,
	                           Map<String, String> map) {
		DynamicLang.apply(map);
	}
}
