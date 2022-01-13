/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigPottedCactusBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPackets;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import dev.lambdaurora.aurorasdeco.util.RegistrationHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the Aurora's Decorations mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AurorasDeco implements ModInitializer {
	public static final String NAMESPACE = "aurorasdeco";
	public static final Logger LOGGER = LogManager.getLogger();
	public static final AurorasDecoPack RESOURCE_PACK = new AurorasDecoPack(ResourceType.SERVER_DATA);

	@Override
	public void onInitialize() {
		AurorasDecoRegistry.init();

		RegistrationHelper.ITEM.addRegistrationCallback((helper, id, object) -> {
			if (id.toString().equals("pockettools:pocket_cactus")) {
				Registry.register(Registry.BLOCK, id("big_flower_pot/pocket_cactus"),
						PottedPlantType.register("pocket_cactus", Blocks.POTTED_CACTUS, object,
								type -> new BigPottedCactusBlock(type, BigPottedCactusBlock.POCKET_CACTUS_SHAPE)));
			} else if (PottedPlantType.isValidPlant(object)) {
				var potBlock = PottedPlantType.registerFromItem(object);
				if (potBlock != null)
					Registry.register(Registry.BLOCK, id("big_flower_pot/" + potBlock.getPlantType().getId()), potBlock);
			}

			Blackboard.Color.tryRegisterColorFromItem(id, object);
		});
		RegistrationHelper.ITEM.init();

		ServerPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.SIGN_POST_OPEN_GUI_FAIL, AurorasDecoPackets::handleSignPostOpenGuiFailPacket);
		ServerPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.SIGN_POST_SET_TEXT, AurorasDecoPackets::handleSignPostSetTextPacket);
	}

	public static boolean isDevMode() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static void log(String message) {
		if (isDevMode())
			LOGGER.info("\033[32m" + message + "\033[0m");
		else
			LOGGER.info("[AurorasDeco] " + message);
	}

	public static void warn(String message, Object... params) {
		if (isDevMode())
			LOGGER.warn("\033[33m" + message + "\033[0m", params);
		else
			LOGGER.warn("[AurorasDeco] " + message, params);
	}

	public static void error(String message, Object... params) {
		if (isDevMode())
			LOGGER.error("\033[31;1m" + message + "\033[0m", params);
		else
			LOGGER.error("[AurorasDeco] " + message, params);
	}

	public static void debug(String message, Object... params) {
		if (isDevMode()) {
			LOGGER.info("\033[38;5;214m[Debug] \033[32;1m" + message + "\033[0m", params);
		}
	}

	public static void debugWarn(String message, Object... params) {
		if (isDevMode()) {
			LOGGER.info("\033[38;5;214m[Debug] \033[31;1m" + message + "\033[0m", params);
		}
	}

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}
}
