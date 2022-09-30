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

package dev.lambdaurora.aurorasdeco.item;

import dev.lambdaurora.aurorasdeco.blackboard.Blackboard;
import dev.lambdaurora.aurorasdeco.blackboard.BlackboardColor;
import dev.lambdaurora.aurorasdeco.blackboard.BlackboardDrawModifier;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoPackets;
import dev.lambdaurora.aurorasdeco.screen.NestedScreenHandler;
import dev.lambdaurora.aurorasdeco.screen.PainterPaletteScreenHandler;
import dev.lambdaurora.aurorasdeco.tooltip.PainterPaletteTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.Optional;

/**
 * Represents a painter's palette item which can be used for easier painting on blackboards.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class PainterPaletteItem extends Item {
	private static final int DEFAULT_BACKGROUND_COLOR = 0xff967441;

	public PainterPaletteItem(Settings settings) {
		super(settings);
	}

	public ItemStack getCurrentColorAsItem(ItemStack paletteStack) {
		var inventory = PainterPaletteInventory.fromNbt(paletteStack.getSubNbt("inventory"));

		return inventory.getSelectedColor();
	}

	public ItemStack getCurrentToolAsItem(ItemStack paletteStack) {
		var inventory = PainterPaletteInventory.fromNbt(paletteStack.getSubNbt("inventory"));
		if (inventory.selectedTool == -1) return ItemStack.EMPTY;

		return inventory.getSelectedTool();
	}

	public static MutableText getSelectedToolMessage(PainterPaletteInventory inventory) {
		Text toolName = Blackboard.DrawAction.ACTIONS.stream()
				.filter(drawAction -> {
					var offHandTool = drawAction.getOffHandTool();
					var selectedTool = inventory.getSelectedTool();

					return (offHandTool == null && selectedTool.isEmpty()) || selectedTool.isOf(offHandTool);
				}).findFirst()
				.map(Blackboard.DrawAction::getName).orElseGet(() -> {
					if (inventory.getSelectedTool().isOf(Items.STICK)) return Text.translatable("aurorasdeco.blackboard.tool.line");
					else throw new IllegalStateException("Could not get tool name.");
				});

		return Text.translatable("aurorasdeco.blackboard.change_tool", toolName);
	}

	@Override
	public boolean onClicked(ItemStack thisStack, ItemStack otherStack, Slot thisSlot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		if (clickType == ClickType.RIGHT && otherStack.isEmpty() && !(player.currentScreenHandler instanceof PainterPaletteScreenHandler)) {
			NestedScreenHandler.OriginType originType = null;
			if (thisSlot.inventory == player.getInventory()) {
				originType = NestedScreenHandler.OriginType.PLAYER;
			} else if (thisSlot.inventory == player.getEnderChestInventory()) {
				originType = NestedScreenHandler.OriginType.ENDER_CHEST;
			}

			if (originType != null && !player.getWorld().isClient()) {
				player.playerScreenHandler.enableSyncing();
				player.openHandledScreen(new PainterPaletteScreenHandler.Factory(thisStack, originType, thisSlot.getIndex()));
			}

			return true;
		}

		return super.onClicked(thisStack, otherStack, thisSlot, clickType, player, cursor);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (user.isSneaking()) {
			if (!user.getWorld().isClient()) {
				int index = user.getInventory().getSlotWithStack(stack);

				user.playerScreenHandler.enableSyncing();
				user.openHandledScreen(new PainterPaletteScreenHandler.Factory(stack, NestedScreenHandler.OriginType.PLAYER, index));
			}

			return TypedActionResult.consume(stack);
		}

		return super.use(world, user, hand);
	}

	public boolean onScroll(PlayerEntity player, ItemStack paletteStack, double scrollDelta, boolean toolModifier) {
		var inventory = PainterPaletteInventory.fromNbt(paletteStack.getSubNbt("inventory"));

		if (inventory.isEmpty()) {
			return false;
		}

		if (player.getWorld().isClient()) {
			var buffer = PacketByteBufs.create();
			buffer.writeDouble(scrollDelta);
			buffer.writeBoolean(toolModifier);

			ClientPlayNetworking.send(AurorasDecoPackets.PAINTER_PALETTE_SCROLL, buffer);
		} else {
			if (!toolModifier) {
				if (scrollDelta < 0) {
					byte nextColor = inventory.findFirstNextColor();

					if (nextColor != -1) {
						inventory.selectedColor = nextColor;
					}
				} else {
					byte previousColor = inventory.findFirstPreviousColor();

					if (previousColor != -1) {
						inventory.selectedColor = previousColor;
					}
				}

				var nbt = inventory.toNbt();
				if (nbt != null) paletteStack.setSubNbt("inventory", nbt);
				else paletteStack.removeSubNbt("inventory");
				player.playerScreenHandler.sendContentUpdates();

				var modifier = BlackboardDrawModifier.fromItem(inventory.getSelectedColor());

				if (!(modifier instanceof BlackboardColor) && modifier != null) {
					player.sendMessage(Text.translatable("aurorasdeco.blackboard.change_modifier", modifier.getName()), true);
				}
			} else {
				byte nextTool = inventory.scrollTool(scrollDelta < 0);

				if (inventory.selectedTool != nextTool) {
					inventory.selectedTool = nextTool;
					var nbt = inventory.toNbt();
					if (nbt != null) paletteStack.setSubNbt("inventory", nbt);
					else paletteStack.removeSubNbt("inventory");
					player.playerScreenHandler.sendContentUpdates();

					var message = getSelectedToolMessage(inventory);
					BlackboardColor primaryColor = BlackboardColor.fromItem(inventory.getSelectedColor().getItem());

					if (primaryColor != null && primaryColor != BlackboardColor.EMPTY) message.styled(style -> style.withColor(primaryColor.getColor()));

					player.sendMessage(message, true);
				}
			}
		}

		return true;
	}

	public int getColor(ItemStack paletteStack, int tintIndex) {
		var inventory = PainterPaletteInventory.fromNbt(paletteStack.getSubNbt("inventory"));

		var primaryColor = BlackboardDrawModifier.fromItem(inventory.getSelectedColor());
		BlackboardDrawModifier nextColor = inventory.getNextColor();
		BlackboardDrawModifier previousColor = inventory.getPreviousColor();

		if (primaryColor == BlackboardColor.EMPTY) primaryColor = null;
		if (previousColor == BlackboardColor.EMPTY) previousColor = null;
		if (nextColor == BlackboardColor.EMPTY) nextColor = null;

		return switch (tintIndex) {
			case 1 -> primaryColor == null ? DEFAULT_BACKGROUND_COLOR : primaryColor.getColor();
			case 2 -> primaryColor == null ? 0xffffffff : primaryColor.getColor();
			case 3 -> previousColor == null ? DEFAULT_BACKGROUND_COLOR : previousColor.getColor();
			case 4 -> nextColor == null ? DEFAULT_BACKGROUND_COLOR : nextColor.getColor();
			default -> 0xffffffff;
		};
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		var nbt = stack.getSubNbt("inventory");
		if (nbt != null) {
			return Optional.of(new PainterPaletteTooltipData(PainterPaletteInventory.fromNbt(nbt)));
		}
		return super.getTooltipData(stack);
	}

	public static class PainterPaletteInventory extends SimpleInventory {
		private static final int COLOR_SIZE = 27;
		private static final int TOOLS_SIZE = 4;
		private static final int SIZE = COLOR_SIZE + TOOLS_SIZE;
		private static final String SELECTED_COLOR_KEY = "selected_color";
		private static final String SELECTED_TOOL_KEY = "selected_tool";

		private byte selectedColor = 0;
		private byte selectedTool = -1;

		private final PropertyDelegate properties = new PropertyDelegate() {
			@Override
			public int get(int index) {
				return switch (index) {
					case 0 -> PainterPaletteInventory.this.selectedColor;
					case 1 -> PainterPaletteInventory.this.selectedTool;
					default -> 0;
				};
			}

			@Override
			public void set(int index, int value) {
				switch (index) {
					case 0 -> PainterPaletteInventory.this.selectedColor = (byte) value;
					case 1 -> PainterPaletteInventory.this.selectedTool = (byte) value;
				}
			}

			@Override
			public int size() {
				return 2;
			}
		};

		public PainterPaletteInventory() {
			super(SIZE);

			this.addListener(sender -> {
				if (this.getSelectedColor().isEmpty()) {
					byte previousSlot = this.findFirstPreviousColor();
					byte nextSlot = this.findFirstNextColor();

					byte oldColor = this.selectedColor;

					if (nextSlot == -1) this.selectedColor = previousSlot;
					else this.selectedColor = nextSlot;

					if (this.selectedColor == -1) {
						this.selectedColor = oldColor;
					}
				}

				if (this.selectedTool != -1 && this.getSelectedTool().isEmpty()) {
					this.selectedTool = this.scrollTool(true);
				}
			});
		}

		public byte getSelectedColorSlot() {
			return this.selectedColor;
		}

		public ItemStack getSelectedColor() {
			return this.getStack(this.selectedColor);
		}

		public void setSelectedColor(int color) {
			this.selectedColor = (byte) color;
		}

		public int getSelectedToolSlot() {
			return this.selectedTool + COLOR_SIZE;
		}

		public ItemStack getSelectedTool() {
			return this.getStack(this.getSelectedToolSlot());
		}

		public void setSelectedToolSlot(int slot) {
			this.selectedTool = (byte) (slot == -1 ? -1 : slot - COLOR_SIZE);
		}

		public PropertyDelegate getProperties() {
			return this.properties;
		}

		private byte findFirstNextColor() {
			byte i = (byte) ((this.selectedColor + 1) % COLOR_SIZE);
			while (i != this.selectedColor) {
				var stack = this.getStack(i);

				if (!stack.isEmpty()) {
					return i;
				}

				i = (byte) ((i + 1) % COLOR_SIZE);
			}

			return -1;
		}

		private byte findFirstPreviousColor() {
			byte i = (byte) (this.selectedColor - 1);
			if (i == -1) i = COLOR_SIZE - 1;

			while (i != this.selectedColor) {
				var stack = this.getStack(i);

				if (!stack.isEmpty()) {
					return i;
				}

				i--;
				if (i == -1) i = COLOR_SIZE - 1;
			}

			return -1;
		}

		public ItemStack getNextColorStack() {
			byte previousSlot = this.findFirstPreviousColor();
			byte nextSlot = this.findFirstNextColor();

			if (nextSlot == -1 || nextSlot == previousSlot) return ItemStack.EMPTY;
			else return this.getStack(nextSlot);
		}

		public @Nullable BlackboardDrawModifier getNextColor() {
			return BlackboardDrawModifier.fromItem(this.getNextColorStack());
		}

		public ItemStack getPreviousColorStack() {
			byte previousSlot = this.findFirstPreviousColor();

			if (previousSlot == -1) return ItemStack.EMPTY;
			else return this.getStack(previousSlot);
		}

		public @Nullable BlackboardDrawModifier getPreviousColor() {
			return BlackboardDrawModifier.fromItem(this.getPreviousColorStack());
		}

		public byte scrollTool(boolean next) {
			int localIndex = this.selectedTool + 1;

			do {
				if (next) localIndex++;
				else localIndex--;

				if (localIndex < 0) localIndex = (SIZE - COLOR_SIZE) + 1;
				else if (localIndex > SIZE - COLOR_SIZE + 1) localIndex = 0;
			} while (localIndex != 0 && this.getStack(COLOR_SIZE + localIndex - 1).isEmpty());

			return (byte) (localIndex - 1);
		}

		public int getSlotOf(ItemStack stack) {
			for (int i = 0; i < this.size(); i++) {
				if (this.getStack(i) == stack) {
					return i;
				}
			}

			return -1;
		}

		@Override
		public int getMaxCountPerStack() {
			return 1;
		}

		@Override
		public boolean canPlayerUse(PlayerEntity player) {
			return true;
		}

		public @Nullable NbtCompound toNbt() {
			if (this.isEmpty()) {
				return null;
			}

			var nbt = new NbtCompound();

			this.addInventoryPart(nbt, "colors", (byte) 0, (byte) COLOR_SIZE);
			this.addInventoryPart(nbt, "tools", (byte) COLOR_SIZE, (byte) (COLOR_SIZE + TOOLS_SIZE));

			if (!this.getStack(this.selectedColor).isEmpty()) {
				nbt.putByte(SELECTED_COLOR_KEY, this.selectedColor);
			}

			if (this.selectedTool != -1) {
				nbt.putByte(SELECTED_TOOL_KEY, this.selectedTool);
			}

			return nbt;
		}

		public void readNbt(NbtCompound nbt) {
			this.readInventoryPart(nbt.getList("colors", NbtElement.COMPOUND_TYPE), 0);
			this.readInventoryPart(nbt.getList("tools", NbtElement.COMPOUND_TYPE), COLOR_SIZE);

			if (nbt.contains(SELECTED_COLOR_KEY, NbtElement.BYTE_TYPE)) {
				this.selectedColor = nbt.getByte(SELECTED_COLOR_KEY);
			} else {
				this.selectedColor = 0;
			}

			if (nbt.contains(SELECTED_TOOL_KEY, NbtElement.BYTE_TYPE)) {
				this.selectedTool = nbt.getByte(SELECTED_TOOL_KEY);
			} else {
				this.selectedTool = -1;
			}
		}

		public static PainterPaletteInventory fromNbt(@Nullable NbtCompound nbt) {
			var inventory = new PainterPaletteInventory();

			if (nbt == null) {
				return inventory;
			}

			inventory.readNbt(nbt);
			return inventory;
		}

		private void addInventoryPart(NbtCompound nbt, String name, byte from, byte to) {
			var slots = new NbtList();

			for (byte slot = from; slot < to; slot++) {
				var stack = this.getStack(slot);

				if (!stack.isEmpty()) {
					var slotNbt = new NbtCompound();
					slotNbt.putByte("slot", (byte) (slot - from));
					slotNbt.put("item", stack.writeNbt(new NbtCompound()));
					slots.add(slotNbt);
				}
			}

			if (!slots.isEmpty()) {
				nbt.put(name, slots);
			}
		}

		private void readInventoryPart(NbtList nbtList, int from) {
			if (nbtList == null) return;

			for (var nbt : nbtList) {
				var slotNbt = (NbtCompound) nbt;
				int slot = slotNbt.getByte("slot") + from;
				var item = ItemStack.fromNbt(slotNbt.getCompound("item"));

				this.setStack(slot, item);
			}
		}
	}
}
