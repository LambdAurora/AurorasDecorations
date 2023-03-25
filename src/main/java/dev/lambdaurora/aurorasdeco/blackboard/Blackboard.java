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

package dev.lambdaurora.aurorasdeco.blackboard;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a blackboard drawing.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class Blackboard implements BlackboardHandler {
	@ClientOnly
	private static Sprite WHITE_SPRITE;

	private final short[] pixels = new short[256];
	private boolean lit;

	public Blackboard() {}

	/**
	 * Gets the pixels of the blackboard.
	 *
	 * @return the pixels
	 */
	public short[] getPixels() {
		return this.pixels;
	}

	@Override
	public short getPixel(int x, int y) {
		return this.pixels[y * 16 + x];
	}

	public int getColor(int x, int y) {
		int id = this.getPixel(x, y);
		return BlackboardColor.getRenderColor(id);
	}

	@Override
	public boolean setPixel(int x, int y, int color) {
		if ((color & BlackboardColor.COLOR_MASK) == 0) color = 0; // There's no color, make sure to erase any extra metadata.

		short id = (short) color;
		if (this.pixels[y * 16 + x] != id) {
			this.pixels[y * 16 + x] = id;
			return true;
		}
		return false;
	}

	@Override
	public boolean brush(int x, int y, int color) {
		short id = (short) color;

		x = x - 1;
		y = y - 1;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (x < 16 && x >= 0 && y < 16 && y >= 0)
					this.pixels[y * 16 + x] = id;
				x++;
			}
			x = x - 3;
			y++;
		}
		return true;
	}

	@Override
	public boolean replace(int x, int y, int color) {
		short id = this.getPixel(x, y);
		short repl = (short) color;

		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i] == id) {
				this.pixels[i] = repl;
			}
		}
		return true;
	}

	@Override
	public boolean line(int x1, int y1, int x2, int y2, BlackboardDrawModifier modifier) {
		int d = 0;

		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);

		int dx2 = 2 * dx;
		int dy2 = 2 * dy;

		int ix = x1 < x2 ? 1 : -1; // increment direction
		int iy = y1 < y2 ? 1 : -1;

		int x = x1;
		int y = y1;

		if (dx >= dy) {
			while (true) {
				this.pixels[y * 16 + x] = modifier.apply(this.getPixel(x, y));
				if (x == x2)
					break;
				x += ix;
				d += dy2;
				if (d > dx) {
					y += iy;
					d -= dx2;
				}
			}
		} else {
			while (true) {
				this.pixels[y * 16 + x] = modifier.apply(this.getPixel(x, y));
				if (y == y2)
					break;
				y += iy;
				d += dx2;
				if (d > dy) {
					x += ix;
					d -= dy2;
				}
			}
		}
		return true;
	}

	@Override
	public boolean fill(int x, int y, int color) {
		int replacement = (short) color;
		int target = this.getPixel(x, y);
		if (target != replacement) {
			this.flood(x, y, target, replacement);
		}
		return true;
	}

	private void flood(int x, int y, int target, int replacement) {
		short pixel = this.getPixel(x, y);
		if (pixel == target) {
			this.pixels[y * 16 + x] = (short) replacement;
			this.flood((x <= 0 ? x : x - 1), y, target, replacement);
			this.flood((x >= 15 ? x : x + 1), y, target, replacement);
			this.flood(x, (y <= 0 ? y : y - 1), target, replacement);
			this.flood(x, (y >= 15 ? y : y + 1), target, replacement);
		}
	}

	/**
	 * Copies the blackboard data to this blackboard.
	 *
	 * @param source the blackboard to copy
	 */
	public void copy(Blackboard source) {
		System.arraycopy(source.pixels, 0, this.pixels, 0, this.pixels.length);
		this.setLit(source.isLit());
	}

	/**
	 * Clears the blackboard.
	 */
	public void clear() {
		Arrays.fill(this.pixels, (short) 0);
	}

	/**
	 * Returns whether this blackboard is empty or not.
	 *
	 * @return {@code true} if empty, or {@code false} otherwise
	 */
	public boolean isEmpty() {
		for (short b : this.pixels) {
			if (b != 0)
				return false;
		}
		return true;
	}

	public boolean isLit() {
		return this.lit;
	}

	public void setLit(boolean lit) {
		this.lit = lit;
	}

	/* Rendering */

	@ClientOnly
	public static void setWhiteSprite(Sprite whiteSprite) {
		WHITE_SPRITE = whiteSprite;
	}

	@ClientOnly
	public Mesh buildMesh(Direction facing, int light) {
		var meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
		var emitter = meshBuilder.getEmitter();

		var lit = light != 0;

		var material = RendererAccess.INSTANCE.getRenderer().materialFinder()
				.disableDiffuse(0, lit)
				.disableAo(0, lit)
				.find();
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int color = this.getColor(x, y);
				if (color != 0) {
					{
						int red = color & 255;
						int green = (color >> 8) & 255;
						int blue = (color >> 16) & 255;
						color = 0xff000000 | (red << 16) | (green << 8) | blue;
					}

					int squareY = 15 - y;
					emitter.square(facing, x / 16.f, squareY / 16.f,
									(x + 1) / 16.f, (squareY + 1) / 16.f, 0.928f)
							.spriteBake(0, WHITE_SPRITE, MutableQuadView.BAKE_LOCK_UV)
							.spriteColor(0, color, color, color, color)
							.material(material);
					if (light != 0)
						emitter.lightmap(light, light, light, light);
					emitter.emit();
				}
			}
		}

		return meshBuilder.build();
	}

	/* Serialization */

	public void readNbt(NbtCompound nbt) {
		byte[] pixels = nbt.getByteArray("pixels");

		if (!nbt.contains("version", NbtElement.INT_TYPE)) {
			convert01(pixels);
		} else {
			switch (nbt.getInt("version")) {
				case 1 -> pixels = convert02(pixels);
				default -> {
				}
			}
		}

		int boardIndex = 0;
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == 0) {
				this.pixels[boardIndex] = 0;
			} else {
				this.pixels[boardIndex] = (short) (pixels[i] << 8 | pixels[++i] & 0xff);
			}

			boardIndex++;
			if (boardIndex >= this.pixels.length) break;
		}

		this.lit = nbt.getBoolean("lit");
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		if (!this.isEmpty()) {
			int length = 0;
			for (short pixel : this.pixels) {
				if (pixel == 0) length++;
				else length += 2;
			}

			var pixels = new byte[length];

			int rawIndex = 0;
			for (short pixel : this.pixels) {
				if (pixel == 0) {
					pixels[rawIndex++] = 0;
				} else {
					pixels[rawIndex] = (byte) (pixel >> 8);
					pixels[rawIndex + 1] = (byte) (pixel & 0xff);
					rawIndex += 2;
				}
			}

			nbt.putByteArray("pixels", pixels);
		}

		nbt.putBoolean("lit", this.isLit());
		nbt.putInt("version", 2);
		return nbt;
	}

	public static Blackboard fromNbt(NbtCompound nbt) {
		var blackboard = new Blackboard();
		blackboard.readNbt(nbt);
		return blackboard;
	}

	public static boolean shouldConvert(NbtCompound nbt) {
		return !nbt.contains("version", NbtElement.INT_TYPE);
	}

	/**
	 * Converts the raw pixel data from version 0 to version 1.
	 *
	 * @param pixels the raw pixel data
	 */
	private static void convert01(byte[] pixels) {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] *= 4;
		}
	}

	/**
	 * Converts the raw pixel data from version 1 to version 2.
	 *
	 * @param pixels the raw pixel data
	 * @return the converted raw pixel data
	 */
	private static byte[] convert02(byte[] pixels) {
		var converted = new byte[256 * 2];

		int newIndex = 0;
		for (byte pixel : pixels) {
			if (pixel == 0) {
				converted[newIndex] = 0;
				newIndex++;
			} else {
				converted[newIndex] = (byte) (pixel / 4);
				converted[newIndex + 1] = (byte) ((pixel & 3) << 4);
				newIndex += 2;
			}
		}

		return converted;
	}

	public enum DrawAction {
		DEFAULT(null, "aurorasdeco.blackboard.tool.pixel") {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, BlackboardDrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.setPixel(x, y, modifier.apply(colorData));
			}
		},
		BRUSH(Items.WHITE_WOOL, "aurorasdeco.blackboard.tool.brush") {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, BlackboardDrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.brush(x, y, modifier.apply(colorData));
			}
		},
		FILL(Items.BUCKET, "aurorasdeco.blackboard.tool.fill") {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, BlackboardDrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.fill(x, y, modifier.apply(colorData));
			}
		},
		REPLACE(Items.ENDER_PEARL, "aurorasdeco.blackboard.tool.replace") {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, BlackboardDrawModifier modifier) {
				short colorData = blackboard.getPixel(x, y);
				return blackboard.replace(x, y, modifier.apply(colorData));
			}
		};

		public static final List<DrawAction> ACTIONS = List.of(values());

		private final Item offhandTool;
		private final String translationKey;

		DrawAction(@Nullable Item offhandTool, @NotNull String translationKey) {
			this.offhandTool = offhandTool;
			this.translationKey = translationKey;
		}

		public @Nullable Item getOffHandTool() {
			return this.offhandTool;
		}

		public Text getName() {
			return Text.translatable(this.translationKey);
		}

		public abstract boolean execute(BlackboardHandler blackboard, int x, int y, BlackboardDrawModifier modifier);
	}
}
