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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.block.entity.WindChimeBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class WindChimeBlock extends BlockWithEntity {
    public static final VoxelShape SHAPE;
    public static final Box COLLISION_BOX;

    private static final VoxelShape HOLDER_SHAPE = createCuboidShape(6.0, 0.0, 6.0, 10.0, 1.0, 10.0);

    public WindChimeBlock(Settings settings) {
        super(settings);
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    /* Placement */

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos upPos = pos.up();
        BlockState upState = world.getBlockState(upPos);

        if (upState.isIn(BlockTags.LEAVES))
            return true;

        if (upState.isOf(state.getBlock()))
            return false;

        return !VoxelShapes.matchesAnywhere(upState.getSidesShape(world, upPos).getFace(Direction.DOWN),
                HOLDER_SHAPE, BooleanBiFunction.ONLY_SECOND);
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
                                                BlockPos pos, BlockPos posFrom) {
        state = super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
        return direction == Direction.UP && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : state;
    }

    /* Interaction */

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient())
            return;

        WindChimeBlockEntity windChime = AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE.get(world, pos);
        if (windChime == null)
            return;

        if (windChime.getCollisionBox().intersects(entity.getBoundingBox()))
            windChime.startColliding(entity);
    }

    /* Block Entity Stuff */

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE.instantiate(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state,
                                                                            BlockEntityType<T> type) {
        return checkType(type, AurorasDecoRegistry.WIND_CHIME_BLOCK_ENTITY_TYPE,
                world.isClient() ? WindChimeBlockEntity::clientTick : null);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    static {
        VoxelShape up = createCuboidShape(4.0, 13.0, 4.0, 12.0, 16.0, 12.0);
        VoxelShape wood = createCuboidShape(3.0, 12.0, 3.0, 13.0, 13.0, 13.0);
        VoxelShape chimes = createCuboidShape(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);
        SHAPE = VoxelShapes.union(up, wood, chimes);
        COLLISION_BOX = chimes.getBoundingBox().expand(0.1);
    }
}
