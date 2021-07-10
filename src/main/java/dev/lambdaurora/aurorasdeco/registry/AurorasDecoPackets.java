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

package dev.lambdaurora.aurorasdeco.registry;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.client.screen.SignPostEditScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Contains the different packet definitions used in Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoPackets {
    private AurorasDecoPackets() {
        throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
    }

    public static final Identifier SIGN_POST_OPEN_GUI = AurorasDeco.id("sign_post/open_gui");
    public static final Identifier SIGN_POST_OPEN_GUI_FAIL = AurorasDeco.id("sign_post/open_gui/fail");
    public static final Identifier SIGN_POST_SET_TEXT = AurorasDeco.id("sign_post/set_text");

    public static void handleSignPostOpenGuiFailPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                                       PacketByteBuf buf, PacketSender responseSender) {
        var pos = buf.readBlockPos();

        server.execute(() -> {
            var signPost = AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE.get(player.getEntityWorld(), pos);
            if (signPost == null)
                return; // Sign Post is not here.

            signPost.cancelEditing(player);
        });
    }

    public static void handleSignPostSetTextPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                                   PacketByteBuf buf, PacketSender responseSender) {
        var pos = buf.readBlockPos();
        var mode = buf.readByte();
        String upText = ((mode & 1) == 1) ? buf.readString() : null;
        String downText = ((mode & 2) == 2) ? buf.readString() : null;

        server.execute(() -> {
            if (!player.getAbilities().allowModifyWorld)
                return; // Avoid griefing.

            var signPost = AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE.get(player.getEntityWorld(), pos);
            if (signPost == null)
                return; // Sign Post is not here.

            if (player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) >= 16) {
                signPost.finishEditing(player, null, null);
                return; // Be close.
            }

            signPost.finishEditing(player, upText, downText);
        });
    }

    @Environment(EnvType.CLIENT)
    public static final class Client {
        private Client() {
            throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
        }

        public static void handleSignPostOpenGuiPacket(MinecraftClient client, ClientPlayNetworkHandler handler,
                                                       PacketByteBuf buf, PacketSender responseSender) {
            var pos = buf.readBlockPos();

            client.execute(() -> {
                var signPost = AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE.get(client.world, pos);
                if (signPost == null) {
                    var buffer = PacketByteBufs.create();
                    buffer.writeBlockPos(pos);
                    ClientPlayNetworking.send(SIGN_POST_OPEN_GUI_FAIL, buffer);
                    return;
                }

                client.setScreen(new SignPostEditScreen(signPost));
            });
        }
    }
}
