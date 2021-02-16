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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.stat.Stats;
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
    public static final VoxelShape BIG_FLOWER_POT_SHAPE = createCuboidShape(
            1.f, 0.f, 1.f,
            15.f, 14.f, 15.f
    );
    public static final VoxelShape PLANT_FULL_CUBE = createCuboidShape(
            2.8f, 14.f, 2.8f,
            13.2f, 23.1f, 13.2f
    );

    public static final VoxelShape BIG_FLOWER_POT_POTTED_FULL_CUBE_SHAPE = VoxelShapes.union(BIG_FLOWER_POT_SHAPE, PLANT_FULL_CUBE);

    protected final PottedPlantType type;

    public BigFlowerPotBlock(PottedPlantType type) {
        super(FabricBlockSettings.of(Material.DECORATION).strength(.1f).nonOpaque());

        this.type = type;
    }

    public PottedPlantType getPlantType() {
        return this.type;
    }

    public Block getPlant() {
        return this.getPlantType().getPlant();
    }

    public BlockState getPlantState(BlockState potState) {
        return this.getPlant().getDefaultState();
    }

    public boolean isEmpty() {
        return this.getPlantType().isEmpty();
    }

    public boolean hasDynamicModel() {
        return !this.isEmpty();
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BIG_FLOWER_POT_SHAPE;
    }

    /* Interaction */

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        if (this.getPlantType().getItem() == Items.AIR)
            return super.getPickStack(world, pos, state);

        return new ItemStack(this.getPlantType().getItem());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);
        BigFlowerPotBlock toPlace = PottedPlantType.getFlowerPotFromItem(handStack.getItem());
        boolean empty = this.isEmpty();
        boolean toPlaceEmpty = toPlace.isEmpty();
        if (empty != toPlaceEmpty) {
            BlockPos up = pos.up();
            if (empty) {
                if (!world.getBlockState(up).isAir())
                    return ActionResult.PASS;

                world.setBlockState(pos, toPlace.getDefaultState(), 3);
                player.incrementStat(Stats.POT_FLOWER);
                if (!player.getAbilities().creativeMode) {
                    handStack.decrement(1);
                }

                world.setBlockState(up, AurorasDecoRegistry.PLANT_AIR_BLOCK.getDefaultState());
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            } else {
                this.removePlant(world, pos, player, hand, true);
            }

            return ActionResult.success(world.isClient());
        } else {
            return toPlaceEmpty ? ActionResult.PASS : ActionResult.CONSUME;
        }
    }

    private void removePlant(World world, BlockPos pos, @Nullable PlayerEntity player, @Nullable Hand hand, boolean removeUp) {
        ItemStack droppedStack = new ItemStack(this.getPlantType().getItem());

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

        world.setBlockState(pos, AurorasDecoRegistry.BIG_FLOWER_POT_BLOCK.getDefaultState(), 3);

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
        if (this.isEmpty())
            return super.getDroppedStacks(state, builder);

        Item item = this.getPlantType().getItem();

        List<ItemStack> stacks = AurorasDecoRegistry.BIG_FLOWER_POT_BLOCK.getDroppedStacks(state, builder);
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

        /* Shapes */

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
                BigFlowerPotBlock block = (BigFlowerPotBlock) downState.getBlock();
                if (!block.isEmpty()) {
                    block.removePlant(world, downPos, null, null, false);
                }
            }
        }

        @Override
        public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
            BlockPos downPos = pos.down();
            BlockState downState = world.getBlockState(downPos);
            if (downState.getBlock() instanceof BigFlowerPotBlock) {
                downState.onEntityCollision(world, downPos, entity);
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
                if (!((BigFlowerPotBlock) downState.getBlock()).isEmpty())
                    return state;
            }

            return Blocks.AIR.getDefaultState();
        }
    }
}
