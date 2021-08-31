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

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a pile of small logs.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class SmallLogPileBlock extends Block implements Waterloggable {
    public static final EnumProperty<PartType> TYPE = AurorasDecoProperties.PART_TYPE;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    protected static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
    protected static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0.0, 9.0, 0.0, 16.0, 16.0, 16.0);

    public static final Identifier BOTTOM_MODEL = AurorasDeco.id("block/template/small_log_pile");
    public static final Identifier TOP_MODEL = AurorasDeco.id("block/template/small_log_pile_top");
    public static final Identifier DOUBLE_MODEL = AurorasDeco.id("block/template/small_log_pile_double");
    public static final Identifier BETTERGRASS_DATA = AurorasDeco.id("bettergrass/data/small_log_pile");

    private static final List<SmallLogPileBlock> SMALL_LOG_PILES = new ArrayList<>();

    private final WoodType woodType;

    public SmallLogPileBlock(WoodType woodType) {
        super(settings(woodType));

        this.woodType = woodType;

        this.setDefaultState(this.getDefaultState()
                .with(TYPE, PartType.BOTTOM)
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false)
        );

        SMALL_LOG_PILES.add(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE, FACING, WATERLOGGED);
    }

    public static Stream<SmallLogPileBlock> stream() {
        return SMALL_LOG_PILES.stream();
    }

    public WoodType getWoodType() {
        return this.woodType;
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(TYPE)) {
            case BOTTOM -> BOTTOM_SHAPE;
            case TOP -> TOP_SHAPE;
            case DOUBLE -> VoxelShapes.fullCube();
        };
    }

    /* Placement */

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var placedState = world.getBlockState(pos);

        if (placedState.isOf(this)) {
            return placedState.with(TYPE, PartType.DOUBLE);
        } else {
            var fluid = world.getFluidState(pos);
            var state = this.getDefaultState().with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);

            var side = ctx.getSide();
            if (side == Direction.DOWN) {
                state = state.with(TYPE, PartType.TOP);
            } else if (side == Direction.UP) {
                state = state.with(TYPE, PartType.BOTTOM);
            } else {
                if (AuroraUtil.posMod(ctx.getHitPos().getY(), 1) > 0.5)
                    state = state.with(TYPE, PartType.TOP);
                else
                    state = state.with(TYPE, PartType.BOTTOM);
            }

            return state.with(FACING, ctx.getPlayerFacing());
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        var stack = context.getStack();
        var type = state.get(TYPE);
        if (type != PartType.DOUBLE && stack.isOf(this.asItem())) {
            return !context.shouldCancelInteraction()
                    && this.canPlaceAt(state.with(TYPE, PartType.DOUBLE), context.getWorld(), context.getBlockPos());
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
                                                WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    /* Fluid */

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    private static Settings settings(WoodType woodType) {
        var log = woodType.getComponent(WoodType.ComponentType.LOG);
        return FabricBlockSettings.of(log.material(), log.mapColor())
                .sounds(log.blockSoundGroup())
                .strength(2.f)
                .nonOpaque();
    }
}
