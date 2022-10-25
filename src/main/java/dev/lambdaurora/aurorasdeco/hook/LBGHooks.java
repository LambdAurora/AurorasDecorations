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

package dev.lambdaurora.aurorasdeco.hook;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Represents hooks for LambdaBetterGrass.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@ClientOnly
public final class LBGHooks {
	private LBGHooks() {
		throw new UnsupportedOperationException("Someone tried to instantiate a class only containing static definitions. How?");
	}

	private static final MethodHandle PUSH_DISABLE_BETTER_LAYER_METHOD;
	private static final MethodHandle POP_DISABLE_BETTER_LAYER_METHOD;

	/**
	 * Pushes the force-disable of the better layer feature.
	 */
	public static void pushDisableBetterLayer() {
		if (PUSH_DISABLE_BETTER_LAYER_METHOD != null) {
			try {
				PUSH_DISABLE_BETTER_LAYER_METHOD.invoke();
			} catch (Throwable ignored) {
			}
		}
	}

	/**
	 * Pops the force-disable of the better layer feature.
	 */
	public static void popDisableBetterLayer() {
		if (POP_DISABLE_BETTER_LAYER_METHOD != null) {
			try {
				POP_DISABLE_BETTER_LAYER_METHOD.invoke();
			} catch (Throwable ignored) {
			}
		}
	}

	static {
		MethodHandle push = null, pop = null;

		try {
			var lbgClass = Class.forName("dev.lambdaurora.lambdabettergrass.LambdaBetterGrass");
			push = MethodHandles.lookup().findStatic(lbgClass, "pushDisableBetterLayer", MethodType.methodType(void.class));
			pop = MethodHandles.lookup().findStatic(lbgClass, "popDisableBetterLayer", MethodType.methodType(void.class));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {
		}

		PUSH_DISABLE_BETTER_LAYER_METHOD = push;
		POP_DISABLE_BETTER_LAYER_METHOD = pop;
	}
}
