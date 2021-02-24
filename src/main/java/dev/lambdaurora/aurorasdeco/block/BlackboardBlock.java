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

import com.google.common.collect.ImmutableMap;
import dev.lambdaurora.aurorasdeco.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorasdeco.item.BlackboardItem;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
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
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a blackboard that can be edited by players if not locked.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardBlock extends BlockWithEntity implements Waterloggable {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final Map<Direction, VoxelShape> SHAPES;

    private final boolean locked;

    public BlackboardBlock(Settings settings, boolean locked) {
        super(settings);
        this.locked = locked;

        this.setDefaultState(this.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIT, false)
                .with(WATERLOGGED, false)
        );
    }

    /**
     * Returns whether this blackboard block is locked or not.
     *
     * @return {@code true} if locked, else {@code false}
     */
    public boolean isLocked() {
        return this.locked;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, WATERLOGGED);
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    /* Placement */

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.offset(state.get(FACING).getOpposite())).getMaterial().isSolid();
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = this.getDefaultState();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        WorldView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        Direction[] directions = ctx.getPlacementDirections();

        CompoundTag nbt = ctx.getStack().getSubTag("BlockEntityTag");
        if (nbt != null && nbt.contains("lit")) {
            state = state.with(LIT, nbt.getBoolean("lit"));
        }

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction2 = direction.getOpposite();
                state = state.with(FACING, direction2);
                if (state.canPlaceAt(world, pos)) {
                    return state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
                }
            }
        }

        return null;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlackboardBlockEntity blackboard = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
        if (blackboard != null) {
            if (stack.hasCustomName()) {
                blackboard.setCustomName(stack.getName());
            }

            if (state.get(WATERLOGGED)) {
                if (!this.isLocked() && !blackboard.isEmpty())
                    blackboard.clear();
            }
        }
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
                                                WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction.getOpposite() == state.get(FACING) && !state.canPlaceAt(world, pos))
            return Blocks.AIR.getDefaultState();

        if (!this.isLocked()) {
            BlackboardBlockEntity blackboard = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
            if (blackboard != null && !world.isClient()) {
                if (state.get(WATERLOGGED) && !blackboard.isEmpty()) {
                    blackboard.clear();
                }
            }
        }

        return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        Direction facing = state.get(FACING);

        if (!this.isLocked() && hit.getSide() == facing) {
            BlackboardBlockEntity blackboard = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
            if (blackboard != null) {
                if (stack.isOf(Items.WATER_BUCKET) && tryClear(world, blackboard)) {
                    world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS,
                            2.f, 1.f);
                    return ActionResult.success(world.isClient());
                } else if (stack.isOf(Items.POTION) && PotionUtil.getPotion(stack) == Potions.WATER
                        && tryClear(world, blackboard)) {
                    player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);

                        if (stack.isEmpty()) {
                            player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
                        } else {
                            player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
                        }
                    }
                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS,
                            2.f, 1.f);
                    return ActionResult.success(world.isClient());
                } else if ((stack.isOf(Items.PAPER) || stack.getItem() instanceof DyeItem)
                        && !state.get(WATERLOGGED)) {
                    int x;
                    int y = (int) (posMod(hit.getPos().getY(), 1) * 16.0);
                    y = 15 - y;

                    if (facing.getAxis() == Direction.Axis.Z) {
                        x = (int) (posMod(hit.getPos().getX(), 1) * 16.0);
                    } else {
                        x = 15 - (int) (posMod(hit.getPos().getZ(), 1) * 16.0);
                    }
                    if (facing.getDirection() == Direction.AxisDirection.NEGATIVE) {
                        x = 15 - x;
                    }

                    player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

                    if (stack.isOf(Items.PAPER)) {
                        blackboard.clearPixel(x, y);
                    } else {
                        blackboard.setPixel(x, y, ((DyeItem) stack.getItem()).getColor());
                    }

                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos);
                    return ActionResult.success(world.isClient());
                } else if (stack.isOf(Items.GLOW_INK_SAC) || stack.isOf(Items.INK_SAC)) {
                    boolean lit = stack.isOf(Items.GLOW_INK_SAC);
                    if (lit != state.get(LIT)) {
                        if (lit) {
                            world.playSound(null, pos, SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.BLOCKS,
                                    1.f, 1.f);
                            world.setBlockState(pos, state.with(LIT, true));
                        } else {
                            world.playSound(null, pos, SoundEvents.ITEM_INK_SAC_USE, SoundCategory.BLOCKS,
                                    1.f, 1.f);
                            world.setBlockState(pos, state.with(LIT, false));
                        }

                        if (!player.isCreative()) {
                            stack.decrement(1);
                        }

                        return ActionResult.success(world.isClient());
                    }
                }
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    private static double posMod(double n, double d) {
        double v = n % d;
        if (v < 0) v = d + v;
        return v;
    }

    private boolean tryClear(World world, BlackboardBlockEntity blackboard) {
        if (!blackboard.isEmpty()) {
            if (!world.isClient())
                blackboard.clear();

            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blackboard.getPos());
            return true;
        }
        return false;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity playerEntity) {
        BlackboardBlockEntity blackboard = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
        if (blackboard != null) {
            if (!world.isClient() && playerEntity.isCreative()) {
                ItemStack stack = new ItemStack(this);
                if (!blackboard.isEmpty()) {
                    CompoundTag nbt = blackboard.writeBlackBoardNbt(new CompoundTag());
                    if (!nbt.isEmpty()) {
                        nbt.remove("custom_name");
                        nbt.putBoolean("lit", state.get(LIT));
                        stack.putSubTag("BlockEntityTag", nbt);
                    }
                }

                if (blackboard.hasCustomName()) {
                    stack.setCustomName(blackboard.getCustomName());
                }

                ItemEntity itemEntity = new ItemEntity(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        super.onBreak(world, pos, state, playerEntity);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(world, pos, state);
        BlackboardBlockEntity blackboard = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
        if (blackboard != null) {
            CompoundTag nbt = blackboard.writeBlackBoardNbt(new CompoundTag());
            if (!nbt.isEmpty()) {
                nbt.remove("custom_name");
                nbt.putBoolean("lit", state.get(LIT));
                stack.putSubTag("BlockEntityTag", nbt);
            }
        }

        return stack;
    }

    /* Loot table */

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        List<ItemStack> droppedStacks = super.getDroppedStacks(state, builder);
        if (!droppedStacks.isEmpty()) {
            droppedStacks.forEach(stack -> {
                if (stack.getItem() instanceof BlackboardItem) {
                    stack.getOrCreateSubTag("BlockEntityTag").putBoolean("lit", state.get(LIT));
                }
            });
        }
        return droppedStacks;
    }

    /* Piston */

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    /* Block Entity Stuff */

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.instantiate(pos, state);
    }

    /* Fluid */

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            boolean shouldEmitEvent = false;

            if (!world.isClient()) {
                world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), 3);
                world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));

                BlackboardBlockEntity blackboard = AurorasDecoRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE.get(world, pos);
                if (blackboard != null && !this.isLocked()) {
                    if (!blackboard.isEmpty()) {
                        blackboard.clear();
                        shouldEmitEvent = true;
                    }
                }
            }

            if (shouldEmitEvent) {
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos);
            }

            return true;
        } else {
            return false;
        }
    }

    static {
        ImmutableMap.Builder<Direction, VoxelShape> builder = ImmutableMap.builder();

        builder.put(Direction.NORTH, createCuboidShape(0.0, 0.0, 15.0, 16.0, 16.0, 16.0));
        builder.put(Direction.EAST, createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0));
        builder.put(Direction.SOUTH, createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 1.0));
        builder.put(Direction.WEST, createCuboidShape(15.0, 0.0, 0.0, 16.0, 16.0, 16.0));

        SHAPES = new EnumMap<>(builder.build());
    }
}
