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
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StumpBlock extends Block implements SeatBlock, Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public static final Identifier LOG_STUMP_MODEL = AurorasDeco.id("block/template/log_stump");
    public static final Identifier LOG_STUMP_BROWN_MUSHROOM_MODEL = AurorasDeco.id("block/template/log_stump_brown_mushroom");
    public static final Identifier LOG_STUMP_RED_MUSHROOM_MODEL = AurorasDeco.id("block/template/log_stump_red_mushroom");
    public static final Identifier STEM_STUMP_MODEL = AurorasDeco.id("block/template/stem_stump");
    public static final Identifier STUMP_BETTERGRASS_DATA = AurorasDeco.id("bettergrass/data/stump");

    private static final List<StumpBlock> LOG_STUMPS = new ArrayList<>();

    protected static final VoxelShape SHAPE = createCuboidShape(3, 0, 3, 13, 10, 13);

    private final WoodType woodType;

    public StumpBlock(WoodType woodType) {
        super(settings(woodType));
        this.woodType = woodType;

        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));

        LOG_STUMPS.add(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    public static Stream<StumpBlock> streamLogStumps() {
        return LOG_STUMPS.stream();
    }

    public WoodType getWoodType() {
        return this.woodType;
    }

    @Override
    public float getSitYOffset() {
        return .4f;
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    /* Placement */

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var fluid = world.getFluidState(pos);

        return this.getDefaultState().with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
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

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
                              BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (this.sit(world, pos, state, player, stack))
            return ActionResult.success(world.isClient());
        return super.onUse(state, world, pos, player, hand, hit);
    }

    /* Fluid */

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    private static FabricBlockSettings settings(WoodType woodType) {
        var log = woodType.getComponent(WoodType.ComponentType.LOG);
        if (log == null) throw new IllegalStateException("StumpBlock attempted to be created while the wood type is invalid.");
        return FabricBlockSettings.copyOf(log.block())
                .mapColor(log.mapColor())
                .nonOpaque();
    }
}
