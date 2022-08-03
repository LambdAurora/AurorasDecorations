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

package dev.lambdaurora.aurorasdeco.mixin.client;

import dev.lambdaurora.aurorasdeco.client.AurorasDecoClient;
import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin {
	@Shadow
	@Final
	private ResourceType type;

	@ModifyArg(
			method = "reload",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/MultiPackResourceManager;<init>(Lnet/minecraft/resource/ResourceType;Ljava/util/List;)V"
			),
			index = 1
	)
	private List<ResourcePack> onBeginMonitoredReload(List<ResourcePack> packs) {
		if (this.type == ResourceType.CLIENT_RESOURCES) {
			packs = new ArrayList<>(packs);
			var mirror = new MultiPackResourceManager(ResourceType.CLIENT_RESOURCES, packs);
			packs.add(0, AurorasDecoClient.RESOURCE_PACK.rebuild(this.type, mirror));
		}

		return packs;
	}
}
