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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;

import java.util.function.Consumer;

public class AurorasDecoPackCreator implements ResourcePackProvider {
    @Override
    public void register(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory) {
        consumer.accept(ResourcePackProfile.of("aurorasdeco:pack/runtime",
                true,
                () -> AurorasDeco.RESOURCE_PACK.rebuild(ResourceType.SERVER_DATA),
                factory, ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.onlyName()));
    }
}
