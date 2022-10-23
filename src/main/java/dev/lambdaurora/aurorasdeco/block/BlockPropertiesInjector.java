/*
 * Copyright (c) 2022 LambdAurora <email@lambdaurora.dev>
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

import net.minecraft.block.AbstractBlock;

/**
 * Represents a way to inject properties from other blocks into a specific block.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.9
 * @since 1.0.0-beta.9
 */
public final class BlockPropertiesInjector {
	private static final ThreadLocal<InjectData> DATA = new ThreadLocal<>();

	public static <S extends AbstractBlock.Settings> S inject(S settings, InjectData data) {
		DATA.set(data);
		return settings;
	}

	public static <D extends InjectData> D getInjectedData(Class<? extends D> dataClass) {
		return dataClass.cast(DATA.get());
	}

	public interface InjectData {
	}
}
