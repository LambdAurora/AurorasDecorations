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

package dev.lambdaurora.aurorasdeco.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.aurorasdeco.blackboard.BlackboardColor;
import dev.lambdaurora.aurorasdeco.item.PainterPaletteItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.BundleTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Represents the painter's palette tooltip component.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.13
 * @since 1.0.0-beta.6
 */
@ClientOnly
public class PainterPaletteTooltipComponent implements TooltipComponent {
	private final PainterPaletteItem.PainterPaletteInventory inventory;
	private final Text selectedToolText;

	public PainterPaletteTooltipComponent(PainterPaletteItem.PainterPaletteInventory inventory) {
		this.inventory = inventory;
		var enabledFlags = MinecraftClient.getInstance().world.getEnabledFlags();
		this.selectedToolText = PainterPaletteItem.getSelectedToolMessage(inventory, enabledFlags).formatted(Formatting.GRAY);
	}

	@Override
	public int getHeight() {
		int height = 12;
		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return height;

		return height + 24;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		int width = textRenderer.getWidth(this.selectedToolText);

		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return width;

		return Math.max(width, 18 * 5 + 2);
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
		textRenderer.draw(
				this.selectedToolText, x, y, 0xffffffff, true, matrix4f, immediate, TextRenderer.TextLayerType.NORMAL,
				0, LightmapTextureManager.MAX_LIGHT_COORDINATE
		);
	}

	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer) {
		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return;

		ItemStack previousColorStack = this.inventory.getPreviousColorStack();
		ItemStack nextColorStack = this.inventory.getNextColorStack();

		matrices.push();
		y += 12;

		matrices.translate(x, y, 0);
		this.drawSlot(matrices, itemRenderer, textRenderer, previousColorStack, inventory.getSlotOf(previousColorStack), true, false);

		matrices.translate(18, 0, 0);
		this.drawSlot(matrices, itemRenderer, textRenderer, primaryColorStack, inventory.getSelectedColorSlot(), false, false);
		HandledScreen.drawSlotHighlight(matrices, 2, 2, 0);

		matrices.translate(18, 0, 0);
		this.drawSlot(matrices, itemRenderer, textRenderer, nextColorStack, inventory.getSlotOf(nextColorStack), false, true);

		matrices.pop();
	}

	private void drawSlot(
			MatrixStack matrices, ItemRenderer itemRenderer, TextRenderer textRenderer, ItemStack stack,
			int index, boolean start, boolean end
	) {
		this.drawSlotPart(matrices, 1, 1, 0, 0, 0, 18, 20);

		if (start) this.drawSlotPart(matrices, 0, 0, 0, 0, 20, 1, 1);
		if (end) this.drawSlotPart(matrices, 0, 0, 0, 0, 20, 1, 1);

		this.drawSlotPart(matrices, 1, 0, 0, 0, 20, 18, 1);
		this.drawSlotPart(matrices, 1, 20, 0, 0, 60, 18, 1);

		if (start) this.drawSlotPart(matrices, 0, 0, 0, 0, 18, 1, 20);
		if (end) this.drawSlotPart(matrices, 18 + 1, 0, 0, 0, 18, 1, 20);

		if (start) this.drawSlotPart(matrices, 0, 20, 0, 0, 60, 1, 1);
		if (end) this.drawSlotPart(matrices, 18 + 1, 20, 0, 0, 60, 1, 1);

		if (!stack.isEmpty()) {
			itemRenderer.method_32797(matrices, stack, 2, 2, index);
			itemRenderer.method_4025(matrices, textRenderer, stack, 2, 2);
			this.drawColorOverlay(matrices, stack);
		}
	}

	private void drawColorOverlay(MatrixStack matrices, ItemStack stack) {
		var color = BlackboardColor.fromItem(stack.getItem());
		if (color != null) {
			matrices.push();
			matrices.translate(14, 14, 210);
			DrawableHelper.fill(matrices, 0, 0, 4, 4, color.getColor());
			matrices.pop();
		}
	}

	private void drawSlotPart(MatrixStack matrices, int x, int y, int z, float u, float v, int width, int height) {
		RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);
		RenderSystem.setShaderTexture(0, BundleTooltipComponent.TEXTURE);
		DrawableHelper.drawTexture(matrices, x, y, 0, u, v, width, height, 128, 128);
	}
}
