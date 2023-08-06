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
import dev.lambdaurora.aurorasdeco.block.PartType;
import dev.lambdaurora.aurorasdeco.screen.ShelfScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Represents the shelf container screen.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
public class ShelfScreen extends HandledScreen<ShelfScreenHandler> {
	private static final Identifier TEXTURE = AurorasDeco.id("textures/gui/container/shelf.png");

	public ShelfScreen(ShelfScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundHeight = 148;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	@Override
	protected void drawBackground(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		graphics.setShaderColor(1.f, 1.f, 1.f, 1.f);
		int x = (this.width - this.backgroundWidth) / 2;
		int y = (this.height - this.backgroundHeight) / 2;
		graphics.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

		if (this.handler.getPartType() != PartType.DOUBLE) {
			x += 52;
			y += 16;

			graphics.drawTexture(TEXTURE, x, y, 184, 0, 256 - 184, 18 * 2);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(graphics, mouseX, mouseY);
	}
}
