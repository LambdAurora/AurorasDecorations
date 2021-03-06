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

package dev.lambdaurora.aurorasdeco;

import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigFlowerPotBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigPottedCactusBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * Represents the Aurora's Decorations mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AurorasDeco implements ModInitializer {
    public static final String NAMESPACE = "aurorasdeco";
    public static final AurorasDecoPack RESOURCE_PACK = new AurorasDecoPack(ResourceType.SERVER_DATA);

    @ApiStatus.Internal
    public static final Map<Identifier, Block> DELAYED_REGISTER_BLOCK = new Object2ObjectOpenHashMap<>();

    @Override
    public void onInitialize() {
        AurorasDecoRegistry.init(DELAYED_REGISTER_BLOCK);

        RegistryEntryAddedCallback.event(Registry.BLOCK).register((rawId, id, object) -> {
            if (PottedPlantType.isValidPlant(object)) {
                BigFlowerPotBlock potBlock = PottedPlantType.registerFromBlock(object);
                if (potBlock != null)
                    Registry.register(Registry.BLOCK, id("big_flower_pot/" + potBlock.getPlantType().getId()), potBlock);
            }
        });
        RegistryEntryAddedCallback.event(Registry.ITEM).register((rawId, id, object) -> {
            if (id.toString().equals("pockettools:pocket_cactus")) {
                Registry.register(Registry.BLOCK, id("big_flower_pot/pocket_cactus"),
                        PottedPlantType.register("pocket_cactus", Blocks.POTTED_CACTUS, object,
                                type -> new BigPottedCactusBlock(type, BigPottedCactusBlock.POCKET_CACTUS_SHAPE)));
            }

            Blackboard.Color.tryRegisterColorFromItem(id, object);
        });
    }

    public static Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }
}
