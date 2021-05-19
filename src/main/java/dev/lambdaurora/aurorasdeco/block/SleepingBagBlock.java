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

import com.google.common.collect.ImmutableMap;
import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.accessor.PointOfInterestTypeAccessor;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a sleeping bag.
 * <p>
 * Living entities can sleep in them, but it won't set the respawn point of a player.
 * It doesn't explode in dimensions in which beds are forbidden.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SleepingBagBlock extends HorizontalFacingBlock {
    public static final EnumProperty<BedPart> PART = Properties.BED_PART;
    public static final BooleanProperty OCCUPIED = Properties.OCCUPIED;

    private static final VoxelShape FOOT_SHAPE = createCuboidShape(0, 0, 0, 16, 4, 16);
    private static final Map<Direction, VoxelShape> HEAD_SHAPES = new EnumMap<>(
            new ImmutableMap.Builder<Direction, VoxelShape>()
                    .put(Direction.NORTH, VoxelShapes.union(
                            createCuboidShape(0, 0, 0, 16, 3, 8),
                            createCuboidShape(0, 0, 8, 16, 4, 16)
                    ))
                    .put(Direction.EAST, VoxelShapes.union(
                            createCuboidShape(0, 0, 0, 8, 4, 16),
                            createCuboidShape(8, 0, 0, 16, 3, 16)
                    ))
                    .put(Direction.SOUTH, VoxelShapes.union(
                            createCuboidShape(0, 0, 0, 16, 4, 8),
                            createCuboidShape(0, 0, 8, 16, 3, 16)
                    ))
                    .put(Direction.WEST, VoxelShapes.union(
                            createCuboidShape(0, 0, 0, 8, 3, 16),
                            createCuboidShape(8, 0, 0, 16, 4, 16)
                    ))
                    .build()
    );

    private static final List<SleepingBagBlock> SLEEPING_BAGS = new ArrayList<>();
    private final DyeColor color;

    public SleepingBagBlock(DyeColor color) {
        super(FabricBlockSettings.of(Material.WOOL, color.getMapColor())
                .strength(.5f).breakByTool(FabricToolTags.SHEARS)
                .sounds(BlockSoundGroup.WOOL));

        this.color = color;
        this.setDefaultState(this.stateManager.getDefaultState().with(PART, BedPart.FOOT).with(OCCUPIED, false));
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED);
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(PART) == BedPart.FOOT) return FOOT_SHAPE;
        else return HEAD_SHAPES.get(state.get(FACING));
    }

    /* Placement */

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var direction = ctx.getPlayerFacing();
        var pos = ctx.getBlockPos();
        var headPos = pos.offset(direction);
        return ctx.getWorld().getBlockState(headPos).canReplace(ctx) ? this.getDefaultState().with(FACING, direction) : null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient()) {
            var headPos = pos.offset(state.get(FACING));
            world.setBlockState(headPos, state.with(PART, BedPart.HEAD), Block.NOTIFY_ALL);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, 0b11);
        }
    }

    /* Updates */

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
                                                WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (direction == getDirectionTowardsOtherPart(state.get(PART), state.get(FACING))) {
            return newState.isOf(this) && newState.get(PART) != state.get(PART)
                    ? state.with(OCCUPIED, newState.get(OCCUPIED))
                    : Blocks.AIR.getDefaultState();
        } else return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.CONSUME;
        } else {
            if (state.get(PART) != BedPart.HEAD) {
                pos = pos.offset(state.get(FACING));
                state = world.getBlockState(pos);
                if (!state.isOf(this)) {
                    return ActionResult.CONSUME;
                }
            }

            if (!BedBlock.isOverworld(world)) {
                var random = world.getRandom();
                for (int i = 0; i < 4; i++) {
                    double x = pos.getX() + random.nextFloat();
                    double y = pos.getY() + random.nextFloat();
                    double z = pos.getZ() + random.nextFloat();
                    world.addParticle(ParticleTypes.ANGRY_VILLAGER, x, y, z, 0.f, 0.f, 0.f);
                }

                player.sendMessage(new TranslatableText("lore.aurorasdeco.not_sleepy.place"), false);
                return ActionResult.SUCCESS;
            } else if (state.get(OCCUPIED)) {
                if (!this.isFree(world, pos)) {
                    player.sendMessage(new TranslatableText("block.minecraft.bed.occupied"), true);
                }

                return ActionResult.SUCCESS;
            } else {
                player.trySleep(pos).ifLeft((sleepFailureReason) -> {
                    if (sleepFailureReason != null) {
                        player.sendMessage(sleepFailureReason.toText(), true);
                    }

                });
                return ActionResult.SUCCESS;
            }
        }
    }

    private boolean isFree(World world, BlockPos pos) {
        var villagers = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
        if (villagers.isEmpty()) {
            return false;
        } else {
            villagers.get(0).wakeUp();
            return true;
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity playerEntity) {
        if (!world.isClient() && playerEntity.isCreative()) {
            var part = state.get(PART);
            if (part == BedPart.FOOT) {
                var otherPartPos = pos.offset(getDirectionTowardsOtherPart(part, state.get(FACING)));
                var otherPartState = world.getBlockState(otherPartPos);
                if (otherPartState.isOf(this) && otherPartState.get(PART) == BedPart.HEAD) {
                    world.setBlockState(otherPartPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
                    world.syncWorldEvent(playerEntity, 2001, otherPartPos, Block.getRawIdFromState(otherPartState));
                }
            }
        }

        super.onBreak(world, pos, state, playerEntity);
    }

    private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float distance) {
        super.onLandedUpon(world, state, pos, entity, distance * .5f);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (entity.bypassesLandingEffects()) {
            super.onEntityLand(world, entity);
        } else {
            this.bounceEntity(entity);
        }
    }

    private void bounceEntity(Entity entity) {
        var velocity = entity.getVelocity();
        if (velocity.y < 0) {
            double d = entity instanceof LivingEntity ? 0.85 : 0.65;
            entity.setVelocity(velocity.x, -velocity.y * 0.6600000262260437 * d, velocity.z);
        }
    }

    public static void forEach(Consumer<SleepingBagBlock> sleepingBagConsumer) {
        SLEEPING_BAGS.forEach(sleepingBagConsumer);
    }

    public static SleepingBagBlock register(DyeColor color) {
        var block = Registry.register(Registry.BLOCK,
                AurorasDeco.id("sleeping_bag/" + color.getName()),
                new SleepingBagBlock(color));

        SLEEPING_BAGS.add(block);

        return block;
    }

    public static void appendToPointOfInterest(PointOfInterestType poiType) {
        var states = ((PointOfInterestTypeAccessor) poiType).getBlockStates();
        SleepingBagBlock.forEach(sleepingBag -> {
            sleepingBag.getStateManager().getStates().stream()
                    .filter(state -> state.get(SleepingBagBlock.PART) == BedPart.HEAD)
                    .forEach(states::add);
        });
    }
}
