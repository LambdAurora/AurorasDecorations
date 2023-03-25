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

package dev.lambdaurora.aurorasdeco.client;

import dev.lambdaurora.aurorasdeco.blackboard.Blackboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.Matrix4f;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a blackboard texture.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
public class BlackboardTexture {
	private static final BlackboardTextureLRUCache TEXTURE_CACHE = new BlackboardTextureLRUCache(64);
	private static final Deque<BlackboardTexture> UNUSED_TEXTURE_CACHE = new ArrayDeque<>();

	private final NativeImageBackedTexture texture = new NativeImageBackedTexture(16, 16, true);
	private final RenderLayer renderLayer;

	public BlackboardTexture() {
		var id = MinecraftClient.getInstance().getTextureManager()
				.registerDynamicTexture("aurorasdeco/blackboard", this.texture);
		this.renderLayer = RenderLayer.getText(id);
	}

	public static BlackboardTexture fromBlackboard(Blackboard blackboard) {
		return TEXTURE_CACHE.computeIfAbsent(blackboard, newBlackboard -> {
			var texture = getOrCreateTexture();
			texture.update(newBlackboard);
			return texture;
		});
	}

	public static BlackboardTexture getOrCreateTexture() {
		if (UNUSED_TEXTURE_CACHE.isEmpty()) {
			return new BlackboardTexture();
		} else {
			synchronized (UNUSED_TEXTURE_CACHE) {
				return UNUSED_TEXTURE_CACHE.pop();
			}
		}
	}

	public static void cacheTexture(BlackboardTexture texture) {
		synchronized (UNUSED_TEXTURE_CACHE) {
			UNUSED_TEXTURE_CACHE.push(texture);
		}
	}

	public void render(Matrix4f model, VertexConsumerProvider vertexConsumers, int light, boolean mirror) {
		var vertices = vertexConsumers.getBuffer(this.renderLayer);
		vertices.vertex(model, mirror ? 1.f : 0.f, 1.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 1.f : 0.f, 1.f).light(light).next();
		vertices.vertex(model, mirror ? 0.f : 1.f, 1.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 0.f : 1.f, 1.f).light(light).next();
		vertices.vertex(model, mirror ? 0.f : 1.f, 0.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 0.f : 1.f, 0.f).light(light).next();
		vertices.vertex(model, mirror ? 1.f : 0.f, 0.f, 0.f)
				.color(255, 255, 255, 255)
				.uv(mirror ? 1.f : 0.f, 0.f).light(light).next();
	}

	public void update(Blackboard blackboard) {
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				this.texture.getImage().setPixelColor(x, y, blackboard.getColor(x, y));
			}
		}
		this.texture.upload();
	}

	static class BlackboardTextureLRUCache extends LinkedHashMap<Blackboard, BlackboardTexture> {
		private final int capacity;

		public BlackboardTextureLRUCache(int capacity) {
			this.capacity = capacity;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<Blackboard, BlackboardTexture> eldest) {
			if (this.size() > capacity) {
				cacheTexture(eldest.getValue());
				return true;
			}
			return false;
		}
	}
}
