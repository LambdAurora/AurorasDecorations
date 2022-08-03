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

package dev.lambdaurora.aurorasdeco.registry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * Represents the registered sounds in Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoSounds {
	private AurorasDecoSounds() {
		throw new UnsupportedOperationException("Someone tried to instantiate a static-only class. How?");
	}

	public static final SoundEvent FLOWERING_SHEAR_SOUND_EVENT = register("block.flowering.shear");
	public static final SoundEvent BRAZIER_CRACKLE_SOUND_EVENT = register("block.brazier.crackle");
	public static final SoundEvent LANTERN_SWING_SOUND_EVENT = register("block.lantern.swing");
	public static final SoundEvent ARMOR_STAND_HIDE_BASE_PLATE_SOUND_EVENT = register("entity.armor_stand.hide_base_plate");
	public static final SoundEvent ITEM_FRAME_HIDE_BACKGROUND_SOUND_EVENT = register("entity.item_frame.hide_background");

	private static SoundEvent register(String path) {
		var id = id(path);
		return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
	}

	static void init() {
	}
}
