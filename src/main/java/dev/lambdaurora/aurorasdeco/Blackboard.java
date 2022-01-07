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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPlants;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

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
	public static final Int2ObjectMap<Color> COLORS = new Int2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<Item, Color> ITEM_TO_COLOR = new Object2ObjectOpenHashMap<>();
	@Environment(EnvType.CLIENT)
	private static Sprite WHITE_SPRITE;

	private final byte[] pixels = new byte[256];
	private boolean lit;

	public Blackboard() {
	}

	/**
	 * Gets the pixels of the blackboard.
	 *
	 * @return the pixels
	 */
	public byte[] getPixels() {
		return this.pixels;
	}

	@Override
	public byte getPixel(int x, int y) {
		return this.pixels[y * 16 + x];
	}

	public int getColor(int x, int y) {
		int id = this.getPixel(x, y);
		return getColor(id / 4).getRenderColor(id & 3);
	}

	@Override
	public boolean setPixel(int x, int y, Color color, int shade) {
		byte id = color.toRawId(shade);
		if (this.pixels[y * 16 + x] != id) {
			this.pixels[y * 16 + x] = id;
			return true;
		}
		return false;
	}

	@Override
	public boolean brush(int x, int y, Color color, int shade) {
		byte id = color.toRawId(shade);

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
	public boolean replace(int x, int y, Color color, int shade) {
		byte id = this.getPixel(x, y);
		byte repl = color.toRawId(shade);

		for (int i = 0; i < this.pixels.length; i++) {
			if (this.pixels[i] == id) {
				this.pixels[i] = repl;
			}
		}
		return true;
	}

	@Override
	public boolean line(int x1, int y1, int x2, int y2, Color color, int shade) {
		byte id = color.toRawId(shade);

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
				this.pixels[y * 16 + x] = id;
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
				this.pixels[y * 16 + x] = id;
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
	public boolean fill(int x, int y, Color color, int shade) {
		byte replacement = color.toRawId(shade);
		byte target = this.getPixel(x, y);
		if (target != replacement) {
			this.flood(x, y, target, replacement);
		}
		return true;
	}

	private void flood(int x, int y, byte target, byte replacement) {
		byte pixel = this.getPixel(x, y);
		if (pixel == target) {
			this.pixels[y * 16 + x] = replacement;
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
	 * Clears the pixel at the specified coordinates.
	 *
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	public boolean clearPixel(int x, int y) {
		if (this.pixels[y * 16 + x] != 0) {
			this.pixels[y * 16 + x] = 0;
			return true;
		}
		return false;
	}

	/**
	 * Clears the blackboard.
	 */
	public void clear() {
		Arrays.fill(this.pixels, (byte) 0);
	}

	/**
	 * Returns whether this blackboard is empty or not.
	 *
	 * @return {@code true} if empty, else {@code false}
	 */
	public boolean isEmpty() {
		for (byte b : this.pixels) {
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

	@Environment(EnvType.CLIENT)
	public static void setWhiteSprite(Sprite whiteSprite) {
		WHITE_SPRITE = whiteSprite;
	}

	@Environment(EnvType.CLIENT)
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
		if (pixels.length == 256) {
			System.arraycopy(pixels, 0, this.pixels, 0, 256);
		}

		this.lit = nbt.getBoolean("lit");

		if (!nbt.contains("version", NbtType.INT)) {
			convert01(this);
		}
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putByteArray("pixels", this.pixels);
		nbt.putBoolean("lit", this.isLit());
		nbt.putInt("version", 1);
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

	private static void convert01(Blackboard blackboard) {
		for (int i = 0; i < blackboard.pixels.length; i++) {
			blackboard.pixels[i] *= 4;
		}
	}

	public static Color getColor(int color) {
		return COLORS.getOrDefault(color, Color.EMPTY);
	}

	public static @Nullable Color getColorFromItem(Item item) {
		return ITEM_TO_COLOR.get(item);
	}

	/**
	 * Represents a blackboard color.
	 */
	public static class Color {
		public static final Color EMPTY = new Color(0, 0x00000000, Items.PAPER);
		public static final byte FREE_COLOR_SPACE = (byte) (DyeColor.values().length + 1);
		public static final Color SWEET_BERRIES = new Color(FREE_COLOR_SPACE, 0xffbb0000, Items.SWEET_BERRIES);
		public static final Color GLOW_BERRIES = new Color(FREE_COLOR_SPACE + 1, 0xffff9737, Items.GLOW_BERRIES);
		public static final Color LAVENDER = new Color(FREE_COLOR_SPACE + 3, 0xffb886db, AurorasDecoPlants.LAVENDER.asItem());

		public static final int BLUEBERRIES_COLOR = 0xff006ac6;

		private final byte id;
		private final int color;
		private final Item item;

		private Color(int id, int color, Item item) {
			this.id = (byte) id;
			this.color = color;
			this.item = item;

			COLORS.put(id, this);
			ITEM_TO_COLOR.put(item, this);
		}

		public byte getId() {
			return this.id;
		}

		/**
		 * Returns the raw id with shading of this color.
		 *
		 * @param shade the shade
		 * @return the raw id
		 */
		public byte toRawId(int shade) {
			if (this == EMPTY) return 0;
			return (byte) (this.getId() * 4 + shade);
		}

		/**
		 * Returns the color in the ABGR format.
		 *
		 * @return the color in the ABGR format
		 */
		public int getColor() {
			return this.color;
		}

		/**
		 * Returns the render color in the ABGR format.
		 *
		 * @param shade the shade
		 * @return the render color in the ABGR format
		 */
		public int getRenderColor(int shade) {
			if (this.getId() == 0)
				return this.getColor();

			int factor = 220;
			if (shade == 3) {
				factor = 135;
			}

			if (shade == 2) {
				factor = 180;
			}

			if (shade == 1) {
				factor = 220;
			}

			if (shade == 0) {
				factor = 255;
			}

			int color = this.getColor();
			int red = (color >> 16 & 255) * factor / 255;
			int green = (color >> 8 & 255) * factor / 255;
			int blue = (color & 255) * factor / 255;
			return 0xff000000 | blue << 16 | green << 8 | red;
		}

		public Item getItem() {
			return this.item;
		}

		public static Color fromDye(DyeItem dyeItem) {
			var color = dyeItem.getColor();

			if (COLORS.containsKey(color.getId() + 1)) {
				return COLORS.get(color.getId() + 1);
			}

			int red = (int) (color.getColorComponents()[0] * 255.f);
			int green = (int) (color.getColorComponents()[1] * 255.f);
			int blue = (int) (color.getColorComponents()[2] * 255.f);
			return new Color(color.getId() + 1, 0xff000000 | (red << 16) | (green << 8) | blue, dyeItem);
		}

		public static void tryRegisterColorFromItem(Identifier id, Item item) {
			if (item instanceof DyeItem dyeItem) {
				fromDye(dyeItem);
			} else if (id.getNamespace().equals("ecotones") && id.getPath().equals("blueberries")) {
				new Color(FREE_COLOR_SPACE + 2, BLUEBERRIES_COLOR, item);
			}
		}
	}

	public enum DrawAction {
		DEFAULT(null) {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, @Nullable Color color, boolean isBoneMeal, boolean isCoal) {
				byte colorData = blackboard.getPixel(x, y);
				int shade = colorData & 3;
				if (color != null) {
					return blackboard.setPixel(x, y, color, 0);
				} else if (isBoneMeal) {
					if (shade > 0)
						return blackboard.setPixel(x, y, Blackboard.getColor(colorData / 4), shade - 1);
				} else if (isCoal) {
					if (shade < 3)
						return blackboard.setPixel(x, y, Blackboard.getColor(colorData / 4), shade + 1);
				}
				return false;
			}
		},
		BRUSH(Items.WHITE_WOOL) {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, @Nullable Color color, boolean isBoneMeal, boolean isCoal) {
				byte colorData = blackboard.getPixel(x, y);
				int shade = colorData & 3;
				if (color != null) {
					return blackboard.brush(x, y, color, 0);
				} else if (isBoneMeal) {
					if (shade > 0)
						return blackboard.brush(x, y, Blackboard.getColor(colorData / 4), shade - 1);
				} else if (isCoal) {
					if (shade < 3)
						return blackboard.brush(x, y, Blackboard.getColor(colorData / 4), shade + 1);
				}
				return false;
			}
		},
		FILL(Items.BUCKET) {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, @Nullable Color color, boolean isBoneMeal, boolean isCoal) {
				byte colorData = blackboard.getPixel(x, y);
				int shade = colorData & 3;
				if (color != null) {
					return blackboard.fill(x, y, color, 0);
				} else if (isBoneMeal) {
					if (shade > 0)
						return blackboard.fill(x, y, Blackboard.getColor(colorData / 4), shade - 1);
				} else if (isCoal) {
					if (shade < 3)
						return blackboard.fill(x, y, Blackboard.getColor(colorData / 4), shade + 1);
				}
				return false;
			}
		},
		REPLACE(Items.ENDER_PEARL) {
			@Override
			public boolean execute(BlackboardHandler blackboard, int x, int y, @Nullable Color color, boolean isBoneMeal, boolean isCoal) {
				byte colorData = blackboard.getPixel(x, y);
				int shade = colorData & 3;
				if (color != null) {
					return blackboard.replace(x, y, color, 0);
				} else if (isBoneMeal) {
					if (shade > 0)
						return blackboard.replace(x, y, Blackboard.getColor(colorData / 4), shade - 1);
				} else if (isCoal) {
					if (shade < 3)
						return blackboard.replace(x, y, Blackboard.getColor(colorData / 4), shade + 1);
				}
				return false;
			}
		};

		public static final List<DrawAction> ACTIONS = List.of(values());

		private final Item offhandTool;

		DrawAction(@Nullable Item offhandTool) {
			this.offhandTool = offhandTool;
		}

		public @Nullable Item getOffhandTool() {
			return this.offhandTool;
		}

		public abstract boolean execute(BlackboardHandler blackboard, int x, int y, @Nullable Color color, boolean isBoneMeal, boolean isCoal);
	}
}
