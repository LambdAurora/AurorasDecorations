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

package dev.lambdaurora.aurorasdeco.entity;

import dev.lambdaurora.aurorasdeco.mixin.entity.MobEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Represents a fake leash knot entity that can be leashed to real leash knot.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class FakeLeashKnotEntity extends MobEntity {
    public FakeLeashKnotEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);

        this.setAiDisabled(true);
    }

    /* Serialization */

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        if (tag.contains("Leash", 10)) {
            ((MobEntityAccessor) this).setLeashTag(tag.getCompound("Leash"));
        }
        this.setAiDisabled(true);
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        CompoundTag leashNbt = ((MobEntityAccessor) this).getLeashTag();

        if (this.getHoldingEntity() != null) {
            CompoundTag nbt = new CompoundTag();
            if (this.getHoldingEntity() instanceof LivingEntity) {
                UUID uuid = this.getHoldingEntity().getUuid();
                nbt.putUuid("UUID", uuid);
            } else if (this.getHoldingEntity() instanceof AbstractDecorationEntity) {
                BlockPos pos = ((AbstractDecorationEntity) this.getHoldingEntity()).getDecorationBlockPos();
                nbt.putInt("X", pos.getX());
                nbt.putInt("Y", pos.getY());
                nbt.putInt("Z", pos.getZ());
            }

            tag.put("Leash", nbt);
        } else if (leashNbt != null) {
            tag.put("Leash", leashNbt.copy());
        }
    }

    /* Ticking */

    @Override
    public void tick() {
        super.tick();

        Vec3d pos = getPos();
        double decimal = pos.y - (int) pos.y;
        double target = 0.375;
        if (decimal != target) {
            double diff = target - decimal;
            this.setPosition(pos.x, pos.y + diff, pos.z);
        }

        if (!this.canStayAttached()) {
            this.breakAndDiscard(true);
        } else {
            Entity holding = this.getHoldingEntity();
            if (holding == null || !holding.isAlive())
                this.breakAndDiscard(true);
        }
    }

    /* Interaction */

    @Override
    public boolean damage(DamageSource source, float amount) {
        this.breakAndDiscard(!source.isSourceCreativePlayer());
        return true;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        return ActionResult.success(this.world.isClient());
    }

    private void breakAndDiscard(boolean drop) {
        this.world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.BLOCKS,
                1.f, 1.f);
        if (this.isAlive() && this.getHoldingEntity() != null && drop && !this.world.isClient())
            this.dropItem(Items.LEAD, 1);
        this.discard();

        Entity holding = this.getHoldingEntity();
        if (holding instanceof LeashKnotEntity)
            holding.discard();
    }

    public boolean canStayAttached() {
        return this.world.getBlockState(this.getBlockPos()).isIn(BlockTags.FENCES);
    }
}
