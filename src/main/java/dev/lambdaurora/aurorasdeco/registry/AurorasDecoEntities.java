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

package dev.lambdaurora.aurorasdeco.registry;

import dev.lambdaurora.aurorasdeco.entity.FakeLeashKnotEntity;
import dev.lambdaurora.aurorasdeco.entity.SeatEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * Contains the different entities definitions added in Aurora's Decorations.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasDecoEntities {
	private AurorasDecoEntities() {
		throw new UnsupportedOperationException("AurorasDecoEntities only contains static definitions.");
	}

	/* Entities */

	public static final EntityType<FakeLeashKnotEntity> FAKE_LEASH_KNOT_ENTITY_TYPE = Registry.register(
			Registries.ENTITY_TYPE,
			id("fake_leash_knot"),
			FabricEntityTypeBuilder.<FakeLeashKnotEntity>createMob()
					.entityFactory(FakeLeashKnotEntity::new)
					.dimensions(EntityDimensions.fixed(.375f, .5f))
					.defaultAttributes(MobEntity::createAttributes)
					.forceTrackedVelocityUpdates(false)
					.trackRangeChunks(10)
					.trackedUpdateRate(Integer.MAX_VALUE)
					.build()
	);
	public static final EntityType<SeatEntity> SEAT_ENTITY_TYPE = Registry.register(
			Registries.ENTITY_TYPE,
			id("seat"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, SeatEntity::new)
					.dimensions(EntityDimensions.fixed(0.f, 0.f))
					.disableSaving()
					.disableSummon()
					.trackRangeChunks(10)
					.build()
	);

	static void init() {}
}
