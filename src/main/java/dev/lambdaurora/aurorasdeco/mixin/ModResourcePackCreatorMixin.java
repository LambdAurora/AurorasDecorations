/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * This is cursed, don't look, don't even think about it.
 * <p>
 * It's just me not having actually worked on the virtual resource pack API in Fabric API so I have to do stupid stuff like this.
 */
@Mixin(ModResourcePackCreator.class)
public class ModResourcePackCreatorMixin {
    @Shadow(remap = false)
    @Final
    private ResourceType type;

    @Inject(
            method = "register(Ljava/util/function/Consumer;Lnet/minecraft/resource/ResourcePackProfile$Factory;)V",
            at = @At("RETURN")
    )
    private void onRegister(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory, CallbackInfo ci) {
        if (this.type == ResourceType.SERVER_DATA)
            new AurorasDecoPackCreator().register(consumer, factory);
    }
}
