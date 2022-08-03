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

package dev.lambdaurora.aurorasdeco.block;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;

public final class AurorasDecoProperties {
	private AurorasDecoProperties() {
		throw new UnsupportedOperationException("Someone tried to instantiate a singleton. How?");
	}

	public static final EnumProperty<ExtensionType> EXTENSION = EnumProperty.of("extension", ExtensionType.class);
	public static final EnumProperty<PartType> PART_TYPE = EnumProperty.of("type", PartType.class);

	public static final Property.Value<PartType> PART_TYPE_BOTTOM = PART_TYPE.createValue(PartType.BOTTOM);
	public static final Property.Value<PartType> PART_TYPE_TOP = PART_TYPE.createValue(PartType.TOP);
	public static final Property.Value<PartType> PART_TYPE_DOUBLE = PART_TYPE.createValue(PartType.DOUBLE);
}
