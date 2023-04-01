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

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.aurorasdeco.screen.SawmillScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Represents the sawmill screen.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
public class SawmillScreen extends HandledScreen<SawmillScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("textures/gui/container/stonecutter.png");
	private float scrollAmount;
	private boolean mouseClicked;
	private int scrollOffset;
	private boolean canCraft;

	public SawmillScreen(SawmillScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		handler.setContentsChangedListener(this::onInventoryChange);
		--this.titleY;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		this.renderBackground(matrices);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
		RenderSystem.setShaderTexture(0, TEXTURE);
		drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		int scrollAmount = (int) (41.f * this.scrollAmount);
		drawTexture(matrices, this.x + 119, this.y + 15 + scrollAmount, 176 + (this.shouldScroll() ? 0 : 12), 0,
				12, 15);
		int recipesX = this.x + 52;
		int recipesY = this.y + 14;
		int recipesScrollOffset = this.scrollOffset + 12;
		this.renderRecipeBackground(matrices, mouseX, mouseY, recipesX, recipesY, recipesScrollOffset);
		this.renderRecipeIcons(matrices, recipesX, recipesY, recipesScrollOffset);
	}

	@Override
	protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
		super.drawMouseoverTooltip(matrices, x, y);
		if (this.canCraft) {
			int i = this.x + 52;
			int j = this.y + 14;
			int k = this.scrollOffset + 12;
			var list = this.handler.getAvailableRecipes();

			for (int l = this.scrollOffset; l < k && l < this.handler.getAvailableRecipeCount(); ++l) {
				int m = l - this.scrollOffset;
				int n = i + m % 4 * 16;
				int o = j + m / 4 * 18 + 2;
				if (x >= n && x < n + 16 && y >= o && y < o + 18) {
					this.renderTooltip(matrices, list.get(l).getResult(this.client.world.getRegistryManager()), x, y);
				}
			}
		}
	}

	private void renderRecipeBackground(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int scrollOffset) {
		for (int i = this.scrollOffset; i < scrollOffset && i < this.handler.getAvailableRecipeCount(); ++i) {
			int offset = i - this.scrollOffset;
			int recipeX = x + offset % 4 * 16;
			int line = offset / 4;
			int recipeY = y + line * 18 + 2;
			int v = this.backgroundHeight;
			if (i == this.handler.getSelectedRecipe()) {
				v += 18;
			} else if (mouseX >= recipeX && mouseY >= recipeY && mouseX < recipeX + 16 && mouseY < recipeY + 18) {
				v += 36;
			}

			drawTexture(matrices, recipeX, recipeY - 1, 0, v, 16, 18);
		}
	}

	private void renderRecipeIcons(MatrixStack matrices, int x, int y, int scrollOffset) {
		var list = this.handler.getAvailableRecipes();

		for (int i = this.scrollOffset; i < scrollOffset && i < this.handler.getAvailableRecipeCount(); ++i) {
			int offset = i - this.scrollOffset;
			int recipeX = x + offset % 4 * 16;
			int line = offset / 4;
			int recipeY = y + line * 18 + 2;
			this.client.getItemRenderer().renderItemInGui(matrices, list.get(i).getResult(this.client.world.getRegistryManager()), recipeX, recipeY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.mouseClicked = false;
		if (this.canCraft) {
			int i = this.x + 52;
			int j = this.y + 14;
			int k = this.scrollOffset + 12;

			for (int slot = this.scrollOffset; slot < k; ++slot) {
				int m = slot - this.scrollOffset;
				double recipeX = mouseX - (double) (i + m % 4 * 16);
				double recipeY = mouseY - (double) (j + m / 4 * 18);
				if (recipeX >= 0 && recipeY >= 0 && recipeX < 16 && recipeY < 18
						&& this.handler.onButtonClick(this.client.player, slot)) {
					this.client.getSoundManager()
							.play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.f));
					this.client.interactionManager.clickButton(this.handler.syncId, slot);
					return true;
				}
			}

			i = this.x + 119;
			j = this.y + 9;
			if (mouseX >= (double) i && mouseX < (double) (i + 12) && mouseY >= (double) j && mouseY < (double) (j + 54)) {
				this.mouseClicked = true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.mouseClicked && this.shouldScroll()) {
			int y = this.y + 14;
			int j = y + 54;
			this.scrollAmount = ((float) mouseY - (float) y - 7.5f) / ((float) (j - y) - 15.f);
			this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.f, 1.f);
			this.scrollOffset = (int) (this.scrollAmount * this.getMaxScroll() + 0.5) * 4;
			return true;
		} else {
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (this.shouldScroll()) {
			int maxScroll = this.getMaxScroll();
			this.scrollAmount = (float) (this.scrollAmount - amount / maxScroll);
			this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
			this.scrollOffset = (int) (this.scrollAmount * maxScroll + 0.5D) * 4;
		}

		return true;
	}

	private boolean shouldScroll() {
		return this.canCraft && this.handler.getAvailableRecipeCount() > 12;
	}

	protected int getMaxScroll() {
		return (this.handler.getAvailableRecipeCount() + 4 - 1) / 4 - 3;
	}

	private void onInventoryChange() {
		this.canCraft = this.handler.canCraft();
		if (!this.canCraft) {
			this.scrollAmount = 0.0F;
			this.scrollOffset = 0;
		}
	}
}
