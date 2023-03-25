/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.item.SignPostItem;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPackets;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.UUID;

/**
 * Represents the sign post block entity.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.12
 * @since 1.0.0-beta.1
 */
public class SignPostBlockEntity extends BasicBlockEntity {
	private Sign up;
	private Sign down;
	private GenerationSettings generationSettings;
	@Nullable
	private UUID editor;

	public SignPostBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE, pos, state);
	}

	/* Signs */

	public Sign getUp() {
		return this.up;
	}

	public void putSignUp(SignPostItem item, Text text, float yaw) {
		this.up = new Sign(item, text, DyeColor.BLACK, false, yaw, false);

		this.attemptToSync();
	}

	public Sign getDown() {
		return this.down;
	}

	public void putSignDown(SignPostItem item, Text text, float yaw) {
		this.down = new Sign(item, text, DyeColor.BLACK, false, yaw, false);

		this.attemptToSync();
	}

	public @Nullable Sign getSign(boolean up) {
		return up ? this.up : this.down;
	}

	/* Generation settings */

	public GenerationSettings getGenerationSettings() {
		return this.generationSettings;
	}

	public void setGenerationSettings(GenerationSettings generationSettings) {
		this.generationSettings = generationSettings;
		this.markDirty();
	}

	/* Edition */

	public void startEdit(ServerPlayerEntity player) {
		this.editor = player.getUuid();

		var buffer = PacketByteBufs.create();
		buffer.writeBlockPos(this.getPos());
		ServerPlayNetworking.send(player, AurorasDecoPackets.SIGN_POST_OPEN_GUI, buffer);
	}

	public void cancelEditing(ServerPlayerEntity player) {
		if (this.editor != null && this.editor.equals(player.getUuid()))
			this.editor = null;
	}

	public boolean hasEditor() {
		if (this.world == null)
			return false;

		if (this.editor != null) {
			var player = this.world.getPlayerByUuid(this.editor);
			if (player == null)
				this.editor = null;
			else
				return true;
		}

		return false;
	}

	public void finishEditing(PlayerEntity player, String upText, String downText) {
		if (this.editor != null) {
			if (player.getServer().getPlayerManager().getPlayer(this.editor) == null) {
				this.editor = player.getUuid();
			}

			if (player.getUuid().equals(this.editor)) {
				if (upText != null) {
					var sign = this.getUp();
					if (sign != null)
						sign.setText(Text.literal(upText));
				}

				if (downText != null) {
					var sign = this.getDown();
					if (sign != null)
						sign.setText(Text.literal(downText));
				}

				this.editor = null;
			} else {
				AurorasDeco.warn("Player {} just tried to change non-editable sign", player.getName().getString());
			}
		}
	}

	/* Sync */

	private void attemptToSync() {
		if (this.world != null && !this.world.isClient()) {
			this.markDirty();
			this.sync();
		}
	}

	/* Serialization */

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.readSignPostNbt(nbt);

		if (nbt.contains("generation_settings", NbtElement.COMPOUND_TYPE)) {
			this.generationSettings = GenerationSettings.fromNbt(nbt.getCompound("generation_settings"));
		}
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		this.writeSignPostNbt(nbt);

		if (this.generationSettings != null) {
			nbt.put("generation_settings", this.generationSettings.toNbt());
		}
	}

	private void readSignPostNbt(NbtCompound nbt) {
		if (nbt.contains("up_sign", NbtElement.COMPOUND_TYPE)) {
			this.up = this.getSignFromNbt(nbt.getCompound("up_sign"));
		} else this.up = null;

		if (nbt.contains("down_sign", NbtElement.COMPOUND_TYPE)) {
			this.down = this.getSignFromNbt(nbt.getCompound("down_sign"));
		} else this.down = null;
	}

	private NbtCompound writeSignPostNbt(NbtCompound nbt) {
		if (this.up != null)
			nbt.put("up_sign", this.up.toNbt());
		if (this.down != null)
			nbt.put("down_sign", this.down.toNbt());

		return nbt;
	}

	private static Text unparsedTextFromJson(String json) {
		try {
			var text = Text.Serializer.fromJson(json);
			if (text != null) {
				return text;
			}
		} catch (Exception ignored) {
		}

		return Text.empty();
	}

	private Sign getSignFromNbt(NbtCompound nbt) {
		var woodId = Identifier.tryParse(nbt.getString("wood_type"));

		var woodType = WoodType.OAK;
		if (woodId != null) {
			var identifiedWood = WoodType.fromId(woodId);
			if (identifiedWood != null)
				woodType = identifiedWood;
		}

		var text = unparsedTextFromJson(nbt.getString("text"));
		var textColor = DyeColor.byName(nbt.getString("color"), DyeColor.BLACK);
		boolean glowing = nbt.getBoolean("glowing_text");

		float yaw = nbt.getFloat("yaw");
		boolean left = nbt.getBoolean("left");

		return new Sign(SignPostItem.fromWoodType(woodType), text, textColor, glowing, yaw, left);
	}

	public class Sign {
		private final SignPostItem sign;
		private Text text;
		private DyeColor color;
		private boolean glowing;
		private float yaw;
		private boolean left;

		public Sign(SignPostItem sign, Text text, DyeColor color, boolean glowing, float yaw, boolean left) {
			this.sign = sign;
			this.text = text;
			this.color = color;
			this.glowing = glowing;
			this.yaw = yaw;
			this.left = left;
		}

		public SignPostItem getSign() {
			return this.sign;
		}

		public Text getText() {
			return this.text;
		}

		public void setText(Text text) {
			this.text = text;

			SignPostBlockEntity.this.attemptToSync();
		}

		public DyeColor getColor() {
			return this.color;
		}

		public boolean setColor(DyeColor color) {
			if (this.color == color) return false;

			this.color = color;

			SignPostBlockEntity.this.attemptToSync();
			return true;
		}

		public boolean isGlowing() {
			return this.glowing;
		}

		public boolean setGlowing(boolean glowing) {
			if (this.glowing == glowing) return false;

			this.glowing = glowing;

			SignPostBlockEntity.this.attemptToSync();
			return true;
		}

		public float getYaw() {
			return this.yaw;
		}

		public boolean setYaw(float yaw) {
			if (this.yaw == yaw) return false;
			this.yaw = yaw;

			SignPostBlockEntity.this.attemptToSync();
			return true;
		}

		/**
		 * {@return the angle in degrees to make this sign point towards the target position}
		 *
		 * @param targetPos the target position
		 */
		public float getPointTowardAngle(BlockPos targetPos) {
			var pos = SignPostBlockEntity.this.getPos();
			float yaw = (float) (Math.atan2(targetPos.getX() - pos.getX(), targetPos.getZ() - pos.getZ()) * 180 / Math.PI);
			return MathHelper.wrapDegrees(yaw - (this.isLeft() ? 180 : 0));
		}

		public boolean pointToward(BlockPos targetPos) {
			float yaw = this.getPointTowardAngle(targetPos);
			return this.setYaw(yaw);
		}

		public boolean isLeft() {
			return this.left;
		}

		public void setLeft(boolean left) {
			this.left = left;

			SignPostBlockEntity.this.attemptToSync();
		}

		public NbtCompound toNbt() {
			var nbt = new NbtCompound();

			nbt.putString("wood_type", this.sign.getWoodType().getId().toString());
			nbt.putString("text", Text.Serializer.toJson(this.text));
			nbt.putString("color", this.color.getName());
			nbt.putBoolean("glowing_text", this.glowing);

			nbt.putFloat("yaw", this.yaw);
			nbt.putBoolean("left", this.left);

			return nbt;
		}
	}

	public record GenerationSettings(SignPostItem material, Direction facing) {
		public static @Nullable GenerationSettings fromNbt(NbtCompound nbt) {
			var materialId = Identifier.tryParse(nbt.getString("material"));

			if (materialId == null) {
				return null;
			}

			var material = Registry.ITEM.get(materialId);
			var facing = Direction.byName(nbt.getString("facing"));

			if (material instanceof SignPostItem actualMaterial && facing != null) {
				return new GenerationSettings(actualMaterial, facing);
			}

			return null;
		}

		public NbtCompound toNbt() {
			var nbt = new NbtCompound();

			nbt.putString("material", Registry.ITEM.getId(this.material).toString());
			nbt.putString("facing", facing.getName());

			return nbt;
		}
	}
}
