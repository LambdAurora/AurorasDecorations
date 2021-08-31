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

import dev.lambdaurora.aurorasdeco.accessor.ItemExtensions;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

/**
 * Represents a pie block.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class PieBlock extends Block {
    public static final IntProperty BITES = IntProperty.of("bites", 0, 3);
    public static final VoxelShape FULL_SHAPE = createCuboidShape(
            1.0, 0.0, 1.0,
            15.0, 4.0, 15.0
    );
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            FULL_SHAPE,
            FULL_SHAPE,
            createCuboidShape(1.0, 0.0, 1.0, 8.0, 4.0, 15.0),
            createCuboidShape(1.0, 0.0, 1.0, 8.0, 4.0, 8.0)
    };

    private final FoodComponent foodComponent;

    public PieBlock(FoodComponent foodComponent) {
        this(FabricBlockSettings.of(Material.CAKE).strength(0.5f).sounds(BlockSoundGroup.WOOL), foodComponent);
    }

    public PieBlock(Settings settings, FoodComponent foodComponent) {
        super(settings);

        this.foodComponent = foodComponent;

        this.setDefaultState(this.getDefaultState().with(BITES, 0));
    }

    /**
     * Returns a pie block created from an already existing pie item.
     *
     * @param item the pie item
     * @return the corresponding pie block
     */
    public static PieBlock fromPieItem(Item item) {
        var block = new PieBlock(item.getFoodComponent());

        Item.BLOCK_ITEMS.put(block, item);

        ((ItemExtensions) item).makePlaceable(block);

        return block;
    }

    /**
     * Represents the total food component when the pie is fully eaten.
     *
     * @return the food component
     */
    public FoodComponent getFoodComponent() {
        return this.foodComponent;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[state.get(BITES)];
    }

    /* Placement */

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).getMaterial().isSolid();
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
                                                WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return direction == Direction.DOWN && !state.canPlaceAt(world, pos)
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            if (tryEat(world, pos, state, player, this.getFoodComponent()).isAccepted()) {
                return ActionResult.SUCCESS;
            }
        }

        return tryEat(world, pos, state, player, this.getFoodComponent());
    }

    protected static ActionResult tryEat(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player,
                                         FoodComponent foodComponent) {
        if (!player.canConsume(false)) {
            return ActionResult.PASS;
        } else {
            player.incrementStat(Stats.EAT_CAKE_SLICE);
            player.getHungerManager().add(foodComponent.getHunger() / 4, foodComponent.getSaturationModifier());
            int bites = state.get(BITES);
            world.emitGameEvent(player, GameEvent.EAT, pos);
            if (bites < 3) {
                world.setBlockState(pos, state.with(BITES, bites + 1), Block.NOTIFY_ALL);
            } else {
                world.removeBlock(pos, false);
                world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            }

            return ActionResult.SUCCESS;
        }
    }

    /* Entity Stuff */

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    /* Redstone */

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return getComparatorOutput(state.get(BITES));
    }

    public static int getComparatorOutput(int bites) {
        return (4 - bites) * 2;
    }
}
