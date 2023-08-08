/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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

import com.mojang.logging.LogUtils;
import dev.lambdaurora.aurorasdeco.blackboard.BlackboardColor;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.BigPottedCactusBlock;
import dev.lambdaurora.aurorasdeco.block.big_flower_pot.PottedPlantType;
import dev.lambdaurora.aurorasdeco.item.group.ItemTree;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPackets;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.resource.AurorasDecoPack;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import dev.lambdaurora.aurorasdeco.world.gen.DynamicWorldGen;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.slf4j.Logger;

/**
 * Represents the Aurora's Decorations mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class AurorasDeco implements ModInitializer {
	public static final String NAMESPACE = "aurorasdeco";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final AurorasDecoPack RESOURCE_PACK = new AurorasDecoPack(ResourceType.SERVER_DATA);

	@Override
	public void onInitialize(ModContainer mod) {
		AurorasDecoRegistry.init();

		RegistryMonitor.create(Registries.ITEM).forAll(context -> {
			if (AuroraUtil.idEqual(context.id(), "pockettools", "pocket_cactus")) {
				Registry.register(Registries.BLOCK, id("big_flower_pot/pocket_cactus"),
						PottedPlantType.register("pocket_cactus", Blocks.POTTED_CACTUS, context.value(),
								type -> new BigPottedCactusBlock(type, BigPottedCactusBlock.POCKET_CACTUS_SHAPE)));
			} else if (PottedPlantType.isValidPlant(context.value())) {
				var potBlock = PottedPlantType.registerFromItem(context.value());
				if (potBlock != null)
					Registry.register(Registries.BLOCK, id("big_flower_pot/" + potBlock.getPlantType().getId()), potBlock);
			}

			BlackboardColor.tryRegisterColorFromItem(context.id(), context.value());
		});

		ItemTree.init();

		ServerPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.SIGN_POST_OPEN_GUI_FAIL, AurorasDecoPackets::handleSignPostOpenGuiFailPacket);
		ServerPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.SIGN_POST_SET_TEXT, AurorasDecoPackets::handleSignPostSetTextPacket);
		ServerPlayNetworking.registerGlobalReceiver(AurorasDecoPackets.PAINTER_PALETTE_SCROLL, AurorasDecoPackets::handlePainterPaletteScroll);

		DynamicWorldGen.init();

		ResourceLoader.registerBuiltinResourcePack(id("azalea_tree"), ResourcePackActivationType.DEFAULT_ENABLED,
				Text.literal("Aurora's Deco").formatted(Formatting.GOLD)
						.append(Text.literal(" - ").formatted(Formatting.GRAY))
						.append(Text.translatable("resourcepack.aurorasdeco.azalea_tree.name").formatted(Formatting.LIGHT_PURPLE))
		);
		ResourceLoader.registerBuiltinResourcePack(id("swamp_worldgen"), ResourcePackActivationType.NORMAL,
				Text.literal("Aurora's Deco").formatted(Formatting.GOLD)
						.append(Text.literal(" - ").formatted(Formatting.GRAY))
						.append(Text.translatable("resourcepack.aurorasdeco.swamp_tweaks.name").formatted(Formatting.DARK_GREEN))
		);
		ResourceLoader.get(ResourceType.SERVER_DATA).getRegisterDefaultResourcePackEvent().register(context -> {
			context.addResourcePack(RESOURCE_PACK.rebuild(ResourceType.SERVER_DATA, null));
		});
	}

	public static boolean isDevMode() {
		return QuiltLoader.isDevelopmentEnvironment() || TriState.fromProperty("aurorasdeco.debug").toBooleanOrElse(false);
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
