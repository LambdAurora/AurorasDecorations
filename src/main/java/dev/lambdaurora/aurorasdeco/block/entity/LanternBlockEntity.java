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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a Lantern Block Entity for the wall lanterns.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanternBlockEntity extends BlockEntity {
    public int swingTicks;
    public boolean swinging;
    public Direction lastSideHit;

    public Box lanternCollisionBoxX;
    public Box lanternCollisionBoxZ;

    private final Map<Entity, Direction.Axis> collisions = new Object2ObjectOpenHashMap<>();

    public LanternBlockEntity(BlockPos pos, BlockState state) {
        super(AurorasDecoRegistry.LANTERN_BLOCK_ENTITY_TYPE, pos, state);

        this.updateCollisionBoxes(state);
    }

    private void updateCollisionBoxes(BlockState state) {
        BlockPos pos = this.getPos();

        BlockState lanternState = state.get(WallLanternBlock.TYPE).getLantern().getDefaultState();
        Box box = lanternState.getOutlineShape(this.getWorld(), pos, ShapeContext.absent())
                .offset(0, 2.0 / 16.0, 0).getBoundingBox();

        this.lanternCollisionBoxX = box.expand(0.1, 0, 0).offset(pos);
        this.lanternCollisionBoxZ = box.expand(0, 0, 0.1).offset(pos);
    }

    /**
     * Returns the lantern collision box of the given axis.
     *
     * @param axis the axis which allows colliding
     * @return the collision box
     */
    public Box getLanternCollisionBox(Direction.Axis axis) {
        return axis == Direction.Axis.X ? this.lanternCollisionBoxX : this.lanternCollisionBoxZ;
    }

    /**
     * Returns whether this lantern is colliding with an entity or not.
     *
     * @return {@code true} if this lantern is colliding with an entity, else {@code false}
     */
    public boolean isColliding() {
        return this.getCachedState().get(WallLanternBlock.COLLISION);
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.lastSideHit = Direction.byId(data);
            if (!this.swinging || !this.isColliding()) {
                this.swingTicks = 0;
            }
            this.swinging = true;
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    private static void tick(LanternBlockEntity blockEntity) {
        if (blockEntity.swinging) {
            ++blockEntity.swingTicks;
        }

        if (blockEntity.swingTicks >= 4 && blockEntity.isColliding()) {
            blockEntity.swingTicks = 4;
        }

        float maxTicks = blockEntity.getCachedState().getFluidState().isEmpty() ? 50 : 100;
        if (blockEntity.swingTicks >= maxTicks) {
            blockEntity.swinging = false;
            blockEntity.swingTicks = 0;
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, LanternBlockEntity blockEntity) {
        tick(blockEntity);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, LanternBlockEntity blockEntity) {
        if (!blockEntity.collisions.isEmpty()) {
            List<Entity> toRemove = new ArrayList<>();
            boolean canTick = true;
            for (Map.Entry<Entity, Direction.Axis> entry : blockEntity.collisions.entrySet()) {
                if (entry.getKey().isRemoved()) {
                    toRemove.add(entry.getKey());
                } else {
                    if (blockEntity.getLanternCollisionBox(entry.getValue()).intersects(entry.getKey().getBoundingBox())) {
                        canTick = false;
                    } else {
                        toRemove.add(entry.getKey());
                    }
                }
            }

            toRemove.forEach(blockEntity.collisions::remove);

            if (!canTick)
                return;
        } else if (state.get(WallLanternBlock.COLLISION)) {
            world.setBlockState(pos, state.with(WallLanternBlock.COLLISION, false));
        }

        tick(blockEntity);
    }

    /**
     * Swings the lantern in a given direction.
     *
     * @param direction the direction to swing to
     */
    public void activate(Direction direction) {
        BlockPos blockPos = this.getPos();
        this.lastSideHit = direction;
        if (this.swinging) {
            if (this.isColliding())
                this.swingTicks = 4;
            else
                this.swingTicks = 0;
        } else {
            this.swinging = true;
        }

        this.getWorld().addSyncedBlockEvent(blockPos, this.getCachedState().getBlock(), 1, direction.getId());
    }

    /**
     * Swings the lantern in a given direction. Caused by an entity collision.
     *
     * @param direction the direction to swing to
     * @param entity the entity who made the lantern swing
     * @param lanternCollisionAxis the axis on which the lantern and the entity collides
     */
    public void activate(Direction direction, Entity entity, Direction.Axis lanternCollisionAxis) {
        this.collisions.put(entity, lanternCollisionAxis);

        if (!this.getCachedState().get(WallLanternBlock.COLLISION)) {
            this.getWorld().setBlockState(this.getPos(), this.getCachedState().with(WallLanternBlock.COLLISION, true));
        }
        this.activate(direction);
    }

    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.updateCollisionBoxes(state);
    }
}
