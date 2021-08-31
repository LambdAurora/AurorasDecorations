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

package dev.lambdaurora.aurorasdeco.block.entity;

import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a Lantern Block Entity for the wall lanterns.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LanternBlockEntity extends SwayingBlockEntity {
    public int swingTicks;
    private boolean swinging;
    public Direction lastSideHit;

    private boolean colliding = false;

    private Box lanternCollisionBoxX;
    private Box lanternCollisionBoxZ;

    private final Map<Entity, Direction.Axis> collisions = new Object2ObjectOpenHashMap<>();

    public LanternBlockEntity(BlockPos pos, BlockState state) {
        super(AurorasDecoRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        this.updateCollisionBoxes();
    }

    /**
     * Returns the lantern of this wall lantern as block state.
     *
     * @return the lantern as block state
     */
    public BlockState getLanternState() {
        var cachedState = this.getCachedState();
        return ((WallLanternBlock) cachedState.getBlock()).getLanternState(cachedState);
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
     * Returns whether this lantern is swaying or not.
     *
     * @return {@code true} if this lantern is swaying, else {@code false}
     */
    public boolean isSwinging() {
        return this.swinging;
    }

    /**
     * Returns whether this lantern is colliding with an entity or not.
     *
     * @return {@code true} if this lantern is colliding with an entity, else {@code false}
     */
    public boolean isColliding() {
        return this.colliding;
    }

    /**
     * Returns the max swing ticks.
     *
     * @return the max swing ticks
     */
    public int getMaxSwingTicks() {
        return this.getCachedState().getFluidState().isEmpty() ? 60 : 100;
    }

    /**
     * Swings the lantern in a given direction.
     *
     * @param direction the direction to swing to
     */
    public void activate(Direction direction) {
        var blockPos = this.getPos();
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
        this.colliding = true;
        this.getWorld().addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 2, 1);
        this.getWorld().updateComparators(this.getPos(), this.getCachedState().getBlock());

        this.activate(direction);
    }

    private void updateCollisionBoxes() {
        if (this.getWorld() == null)
            return;

        var pos = this.getPos();

        var lanternState = this.getLanternState();
        var box = lanternState.getOutlineShape(this.getWorld(), pos, ShapeContext.absent())
                .offset(0, 2.0 / 16.0, 0).getBoundingBox();

        this.lanternCollisionBoxX = box.expand(0.1, 0, 0).offset(pos);
        this.lanternCollisionBoxZ = box.expand(0, 0, 0.1).offset(pos);
    }

    /* Syncing */

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == 1) {
            this.lastSideHit = Direction.byId(data);
            if (!this.swinging || !this.isColliding()) {
                this.swingTicks = 0;
            }
            this.swinging = true;
            return true;
        } else if (type == 2) {
            this.colliding = data != 0;
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    /* Ticking */

    private static void tick(LanternBlockEntity lantern) {
        if (lantern.swinging) {
            ++lantern.swingTicks;
        }

        if (lantern.swingTicks >= 4 && lantern.isColliding()) {
            lantern.swingTicks = 4;
        }

        if (lantern.swingTicks >= lantern.getMaxSwingTicks()) {
            lantern.swinging = false;
            lantern.swingTicks = 0;
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, LanternBlockEntity lantern) {
        lantern.tickClient(world);
        tick(lantern);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, LanternBlockEntity lantern) {
        boolean canTick = true;

        if (!lantern.collisions.isEmpty()) {
            var toRemove = new ArrayList<Entity>();
            for (var entry : lantern.collisions.entrySet()) {
                if (entry.getKey().isRemoved()) {
                    toRemove.add(entry.getKey());
                } else {
                    if (lantern.getLanternCollisionBox(entry.getValue()).intersects(entry.getKey().getBoundingBox())) {
                        canTick = false;
                    } else {
                        toRemove.add(entry.getKey());
                    }
                }
            }

            toRemove.forEach(lantern.collisions::remove);
        }
        if (lantern.collisions.isEmpty() && lantern.isColliding()) {
            lantern.colliding = false;
            world.addSyncedBlockEvent(pos, state.getBlock(), 2, 0);
            world.updateComparators(pos, state.getBlock());
        }

        if (canTick) {
            int oldSwingTicks = lantern.swingTicks;
            tick(lantern);
            if (oldSwingTicks != lantern.swingTicks) {
                world.updateComparators(pos, state.getBlock());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        this.updateCollisionBoxes();
    }
}
