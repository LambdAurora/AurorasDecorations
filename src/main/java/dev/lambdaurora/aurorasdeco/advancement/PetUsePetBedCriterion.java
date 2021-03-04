/*
 * Copyright (c) 2021 LambdAurora <aurora42lambda@gmail.com>
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

package dev.lambdaurora.aurorasdeco.advancement;

import com.google.gson.JsonObject;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.mixin.FoxEntityAccessor;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public class PetUsePetBedCriterion extends AbstractCriterion<PetUsePetBedCriterion.Conditions> {
    private static final Identifier ID = AurorasDeco.id("pet_use_pet_bed");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        BlockPredicate blockPredicate = BlockPredicate.fromJson(obj.get("block"));
        return new Conditions(playerPredicate, blockPredicate);
    }

    public void trigger(PathAwareEntity entity, ServerWorld world, BlockPos pos) {
        if (entity instanceof TameableEntity) {
            LivingEntity player = ((TameableEntity) entity).getOwner();
            if (player != null)
                this.trigger((ServerPlayerEntity) player, world, pos);
        } else if (entity instanceof FoxEntityAccessor) { // Foxes <3
            List<UUID> trusted = ((FoxEntityAccessor) entity).aurorasdeco$getTrustedUuids();
            if (!trusted.isEmpty()) {
                PlayerEntity player = world.getPlayerByUuid(trusted.get(0));
                if (player != null)
                    this.trigger((ServerPlayerEntity) player, world, pos);
            }
        }
    }

    public void trigger(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        this.test(player, (conditions) -> conditions.matches(world, pos));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final BlockPredicate blockPredicate;

        public Conditions(EntityPredicate.Extended playerPredicate, BlockPredicate blockPredicate) {
            super(ID, playerPredicate);
            this.blockPredicate = blockPredicate;
        }

        public boolean matches(ServerWorld world, BlockPos pos) {
            return this.blockPredicate.test(world, pos);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("block", this.blockPredicate.toJson());
            return jsonObject;
        }
    }
}
