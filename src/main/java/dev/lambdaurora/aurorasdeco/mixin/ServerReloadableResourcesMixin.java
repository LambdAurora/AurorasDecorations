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

package dev.lambdaurora.aurorasdeco.mixin;

import dev.lambdaurora.aurorasdeco.world.gen.DynamicWorldGen;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.ServerReloadableResources;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ServerReloadableResources.class)
public class ServerReloadableResourcesMixin {
	@Inject(method = "loadResources", at = @At("HEAD"))
	private static void onLoad(ResourceManager resources, DynamicRegistryManager.Frozen registry,
			CommandManager.RegistrationEnvironment environment,
			int level, Executor prepareExecutor, Executor applyExecutor,
			CallbackInfoReturnable<CompletableFuture<ServerReloadableResources>> cir) {
		DynamicWorldGen.setupRegistries(registry);
	}
}
