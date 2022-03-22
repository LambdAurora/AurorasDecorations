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

package dev.lambdaurora.aurorasdeco.tooltip;

import dev.lambdaurora.aurorasdeco.Blackboard;
import dev.lambdaurora.aurorasdeco.client.tooltip.BlackboardTooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import org.quiltmc.qsl.tooltip.api.ConvertibleTooltipData;

/**
 * Represents the blackboard tooltip data. Used to build the {@link BlackboardTooltipComponent} on the client.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public record BlackboardTooltipData(String background, Blackboard blackboard, boolean locked) implements ConvertibleTooltipData {
	@Environment(EnvType.CLIENT)
	public TooltipComponent toComponent() {
		return new BlackboardTooltipComponent(this.background(), this.blackboard(), this.locked());
	}
}
