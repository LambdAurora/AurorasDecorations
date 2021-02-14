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

package dev.lambdaurora.aurorasdeco.block;

import dev.lambdaurora.aurorasdeco.block.state.PlantProperty;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a big flower pot.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BigFlowerPotBlock extends Block {
    public static final PlantProperty PLANT = new PlantProperty("plant");
    public static final VoxelShape BIG_FLOWER_POT_SHAPE = createCuboidShape(
            1.f, 0.f, 1.f,
            15.f, 14.f, 15.f
    );
    public static final VoxelShape AZALEA_SHAPE = createCuboidShape(
            2.8f, 14.f, 2.8f,
            13.2f, 23.2f, 13.2f
    );
    public static final VoxelShape CACTUS_SHAPE = createCuboidShape(
            3.f, 14.f, 3.f,
            13.f, 23.1f, 13.f
    );
    public static final VoxelShape POTTED_AZALEA_SHAPE = VoxelShapes.union(BIG_FLOWER_POT_SHAPE, AZALEA_SHAPE);
    public static final VoxelShape POTTED_CACTUS_SHAPE = VoxelShapes.union(BIG_FLOWER_POT_SHAPE, CACTUS_SHAPE);

    public BigFlowerPotBlock() {
        super(FabricBlockSettings.of(Material.DECORATION).strength(.1f).nonOpaque());
        this.setDefaultState(this.getStateManager().getDefaultState().with(PLANT, PlantProperty.NONE));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(PLANT);
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        PlantProperty.Value value = state.get(PLANT);
        if (value.getPlant() instanceof AzaleaBlock) {
            return POTTED_AZALEA_SHAPE;
        } else if (value.getPlant() instanceof CactusBlock) {
            return POTTED_CACTUS_SHAPE;
        }

        return BIG_FLOWER_POT_SHAPE;
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);
        PlantProperty.Value value = state.get(PLANT);
        PlantProperty.Value toPlace = PlantProperty.fromItem(handStack.getItem());
        boolean empty = value.isEmpty();
        boolean toPlaceEmpty = toPlace == null || toPlace.isEmpty();
        if (empty != toPlaceEmpty) {
            BlockPos up = pos.up();
            if (empty) {
                if (!world.getBlockState(up).isAir())
                    return ActionResult.PASS;

                world.setBlockState(pos, this.getDefaultState().with(PLANT, toPlace), 3);
                player.incrementStat(Stats.POT_FLOWER);
                if (!player.getAbilities().creativeMode) {
                    handStack.decrement(1);
                }

                world.setBlockState(up, AurorasDecoRegistry.PLANT_AIR_BLOCK.getDefaultState());
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            } else {
                this.removePlant(state, world, pos, player, hand, true);
            }

            return ActionResult.success(world.isClient());
        } else {
            return toPlaceEmpty ? ActionResult.PASS : ActionResult.CONSUME;
        }
    }

    private void removePlant(BlockState state, World world, BlockPos pos, @Nullable PlayerEntity player, @Nullable Hand hand, boolean removeUp) {
        PlantProperty.Value value = state.get(PLANT);
        ItemStack droppedStack = new ItemStack(value.getPlant());

        if (!droppedStack.isEmpty()) {
            if (player != null) {
                ItemStack handStack = player.getStackInHand(hand);
                if (handStack.isEmpty()) {
                    player.setStackInHand(hand, droppedStack);
                } else if (!player.giveItemStack(droppedStack)) {
                    player.dropItem(droppedStack, false);
                }
            } else {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + 1.5f, pos.getZ() + .5f, droppedStack);
                float speed = world.random.nextFloat() * .5f;
                float angle = world.random.nextFloat() * 6.2831855f;
                itemEntity.setVelocity(-MathHelper.sin(angle) * speed, 0.20000000298023224D, MathHelper.cos(angle) * speed);
                world.spawnEntity(itemEntity);
            }
        }

        world.setBlockState(pos, this.getDefaultState(), 3);

        if (removeUp) {
            BlockPos up = pos.up();
            if (world.getBlockState(up).isOf(AurorasDecoRegistry.PLANT_AIR_BLOCK)) {
                world.setBlockState(up, Blocks.AIR.getDefaultState());
            }
        }

        world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }

    /* Loot table */

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        PlantProperty.Value value = state.get(PLANT);
        Item item = value.getItem();

        List<ItemStack> stacks = super.getDroppedStacks(state, builder);
        if (item != null && !stacks.isEmpty()) {
            stacks.add(new ItemStack(item));
        }

        return stacks;
    }

    public static class PlantAir extends Block {
        public PlantAir(Settings settings) {
            super(settings);
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
            return BlockRenderType.INVISIBLE;
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return VoxelShapes.empty();
        }

        /* Interaction */

        @Override
        public boolean canReplace(BlockState state, ItemPlacementContext context) {
            return false;
        }

        @Override
        public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
            super.onStateReplaced(state, world, pos, newState, moved);

            BlockPos downPos = pos.down();
            BlockState downState = world.getBlockState(downPos);
            if (downState.getBlock() instanceof BigFlowerPotBlock) {
                if (!downState.get(PLANT).isEmpty()) {
                    ((BigFlowerPotBlock) downState.getBlock()).removePlant(downState, world, downPos, null, null, false);
                }
            }
        }

        /* Piston */

        @Override
        public PistonBehavior getPistonBehavior(BlockState state) {
            return PistonBehavior.DESTROY;
        }

        /* Updates */

        @Override
        public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world,
                                                    BlockPos pos, BlockPos posFrom) {
            BlockState downState = world.getBlockState(pos.down());
            if (downState.getBlock() instanceof BigFlowerPotBlock) {
                if (!downState.get(PLANT).isEmpty())
                    return state;
            }

            return Blocks.AIR.getDefaultState();
        }
    }
}
