/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.entity.goal;

import net.minecraft.entity.passive.CatEntity;

public class CatSleepInPetBedGoal extends SleepInPetBedGoal {
    private final CatEntity cat;

    public CatSleepInPetBedGoal(CatEntity cat, double speed) {
        super(cat, speed);

        this.cat = cat;
    }

    @Override
    public void setInSleepingPosition(boolean value) {
        this.cat.setSleepingWithOwner(value);

        if (!value) {
            this.cat.setInSittingPose(this.cat.isSitting());
        }
    }
}
