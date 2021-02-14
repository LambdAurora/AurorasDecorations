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

package dev.lambdaurora.aurorasdeco;

import dev.lambdaurora.aurorasdeco.block.state.PlantProperty;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Represents the Aurora's Decorations mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AurorasDeco implements ModInitializer {
    public static final String NAMESPACE = "aurorasdeco";
    public static final Identifier BIG_FLOWER_POT_ID = id("big_flower_pot");

    @Override
    public void onInitialize() {
        AurorasDecoRegistry.init();

        RegistryEntryAddedCallback.event(Registry.BLOCK).register((rawId, id, object) -> {
            if (PlantProperty.isValidBlock(object)) {
                PlantProperty.registerValue(object);
            }
        });
    }

    public static Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }
}
