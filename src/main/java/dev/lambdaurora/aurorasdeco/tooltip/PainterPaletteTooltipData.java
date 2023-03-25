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

package dev.lambdaurora.aurorasdeco.tooltip;

import dev.lambdaurora.aurorasdeco.client.tooltip.PainterPaletteTooltipComponent;
import dev.lambdaurora.aurorasdeco.item.PainterPaletteItem;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.tooltip.api.ConvertibleTooltipData;

/**
 * Represents the painter's palette tooltip data. Used to build the {@link PainterPaletteTooltipComponent} on the client.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.6
 * @since 1.0.0-beta.6
 */
public record PainterPaletteTooltipData(PainterPaletteItem.PainterPaletteInventory inventory) implements ConvertibleTooltipData {
	@ClientOnly
	public TooltipComponent toComponent() {
		return new PainterPaletteTooltipComponent(this.inventory());
	}
}
