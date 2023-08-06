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

package dev.lambdaurora.aurorasdeco.client.screen;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.screen.PainterPaletteScreenHandler;
import dev.lambdaurora.aurorasdeco.screen.slot.BlackboardToolSlot;
import dev.lambdaurora.aurorasdeco.screen.slot.ColorSlot;
import dev.lambdaurora.aurorasdeco.screen.slot.LockedSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Represents the painter's palette container screen.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.13
 * @since 1.0.0-beta.6
 */
@ClientOnly
public class PainterPaletteScreen extends HandledScreen<PainterPaletteScreenHandler> {
	private static final Identifier TEXTURE = AurorasDeco.id("textures/gui/container/painter_palette.png");
	private static final Identifier LOCK_TEXTURE = new Identifier("textures/gui/container/cartography_table.png");

	public PainterPaletteScreen(PainterPaletteScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundHeight += 2;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	public int getBackgroundX() {
		return (this.width - this.backgroundWidth) / 2 - 24;
	}

	public int getBackgroundY() {
		return (this.height - this.backgroundHeight) / 2;
	}

	@Override
	protected void drawBackground(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		graphics.setShaderColor(1.f, 1.f, 1.f, 1.f);
		graphics.drawTexture(TEXTURE,
				this.getBackgroundX(), this.getBackgroundY(), 0, 0, this.backgroundWidth + 24, this.backgroundHeight
		);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(graphics);

		for (var slot : this.handler.slots) {
			if (slot instanceof BlackboardToolSlot) {
				int x = this.getBackgroundX() + slot.x + 24 - 1;
				int y = this.getBackgroundY() + slot.y - 1;

				if (slot.getStack().isEmpty()) {
					graphics.drawTexture(TEXTURE, x, y, this.backgroundWidth + 26, 24, 18, 18, 256, 256);
				} else {
					graphics.drawTexture(TEXTURE, x, y, this.backgroundWidth + 26, 42, 18, 18, 256, 256);
				}
			}
		}

		super.render(graphics, mouseX, mouseY, delta);

		MatrixStack matrices = graphics.getMatrices();
		matrices.push();
		matrices.translate(this.getBackgroundX(), this.getBackgroundY(), 275);
		for (var slot : this.handler.slots) {
			if (slot instanceof LockedSlot) {
				matrices.push();
				matrices.translate(slot.x + 24 + 4, slot.y + 4, 0);
				matrices.scale(.75f, .75f, 1);
				graphics.drawTexture(LOCK_TEXTURE, 0, 0, 46, 212, 16, 16, 256, 256);
				matrices.pop();
			} else if ((slot instanceof ColorSlot && slot.getIndex() == this.handler.getInventory().getSelectedColorSlot())
					|| (slot instanceof BlackboardToolSlot && slot.getIndex() == this.handler.getInventory().getSelectedToolSlot())) {
				matrices.push();
				matrices.translate(slot.x + 24, slot.y, 0);
				this.drawSelectedIndicator(graphics);
				matrices.pop();
			}
		}
		matrices.pop();

		this.drawMouseoverTooltip(graphics, mouseX, mouseY);
	}

	private void drawSelectedIndicator(GuiGraphics graphics) {
		graphics.drawTexture(TEXTURE, -3, -3, this.backgroundWidth + 24, 0, 22, 22, 256, 256);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 2) {
			if (this.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, button)) {
				if (this.handler.onButtonClick(this.client.player, this.handler.getInventory().size())) {
					this.client.interactionManager.clickButton(this.handler.syncId, this.handler.getInventory().size());
					return true;
				}
			} else {
				int slot = this.getSlotAt(mouseX, mouseY);

				if (slot != -1 && this.handler.onButtonClick(this.client.player, slot)) {
					this.client.interactionManager.clickButton(this.handler.syncId, slot);
					return true;
				}
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	private int getSlotAt(double x, double y) {
		for (int i = 0; i < this.handler.slots.size(); ++i) {
			Slot slot = this.handler.slots.get(i);
			if (this.isPointOverSlot(slot, x, y) && slot.isEnabled()) {
				return i;
			}
		}

		return -1;
	}

	private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
		return this.isPointWithinBounds(slot.x, slot.y, 16, 16, pointX, pointY);
	}

	@Override
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
		return super.isClickOutsideBounds(mouseX, mouseY, left, top, button)
				&& (mouseX < this.getBackgroundX() || mouseY < this.getBackgroundY() || mouseY > this.getBackgroundY() + 86 || mouseY > this.getBackgroundX() + 10);
	}
}
