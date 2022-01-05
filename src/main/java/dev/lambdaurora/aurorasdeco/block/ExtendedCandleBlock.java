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

package dev.lambdaurora.aurorasdeco.block;

import net.minecraft.block.CandleBlock;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ExtendedCandleBlock extends CandleBlock {
	private static final List<ExtendedCandleBlock> EXTENDED_CANDLE_BLOCKS = new ArrayList<>();

	protected final CandleBlock parent;

	public ExtendedCandleBlock(CandleBlock candleBlock) {
		super(QuiltBlockSettings.copyOf(candleBlock)
				// Bump up a little bit the luminance,
				// especially since the candles extending this are not on the floor.
				.luminance((state) -> CandleBlock.STATE_TO_LUMINANCE.applyAsInt(state) + 2)
		);
		this.parent = candleBlock;

		EXTENDED_CANDLE_BLOCKS.add(this);
	}

	public static Stream<ExtendedCandleBlock> stream() {
		return EXTENDED_CANDLE_BLOCKS.stream();
	}

	public CandleBlock getParent() {
		return parent;
	}
}
