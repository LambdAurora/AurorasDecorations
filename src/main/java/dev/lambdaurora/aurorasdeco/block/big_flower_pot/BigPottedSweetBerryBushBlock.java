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

package dev.lambdaurora.aurorasdeco.block.big_flower_pot;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Represents a potted sweet berry bush.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BigPottedSweetBerryBushBlock extends BigFlowerPotBlock implements Fertilizable {
    public static final IntProperty AGE = SweetBerryBushBlock.AGE;

    private static final VoxelShape SMALL_SHAPE = VoxelShapes.union(BIG_FLOWER_POT_SHAPE, createCuboidShape(
            4.f, 14.f, 4.f,
            12.f, 18.f, 12.f
    ));
    private static final VoxelShape LARGE_SHAPE;
    private static final Box SWEET_BERRY_BUSH_BOX;

    public BigPottedSweetBerryBushBlock(PottedPlantType type) {
        super(type);

        this.setDefaultState(this.getDefaultState().with(AGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AGE);
    }

    @Override
    public BlockState getPlantState(BlockState potState) {
        return super.getPlantState(potState).with(AGE, potState.get(AGE));
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(AGE) == 0) {
            return SMALL_SHAPE;
        } else {
            return state.get(AGE) < 3 ? LARGE_SHAPE : BIG_FLOWER_POT_POTTED_FULL_CUBE_SHAPE;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BIG_FLOWER_POT_SHAPE;
    }

    /* Ticking */

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(AGE) < 3;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int i = state.get(AGE);
        if (i < 3 && random.nextInt(5) == 0 && world.getBaseLightLevel(pos.up(), 0) >= 9) {
            world.setBlockState(pos, state.with(AGE, i + 1), 0b10);
        }
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        int age = state.get(AGE);
        boolean mature = age == 3;
        if (!mature && player.getStackInHand(hand).isOf(Items.BONE_MEAL)) {
            return ActionResult.PASS;
        } else if (age > 1) {
            int count = 1 + world.random.nextInt(2);
            dropStack(world, pos, new ItemStack(this.getPlant().asItem(), count + (mature ? 1 : 0)));
            world.playSound(null, pos, SoundEvents.ITEM_SWEET_BERRIES_PICK_FROM_BUSH, SoundCategory.BLOCKS,
                    1.f, .8f + world.random.nextFloat() * .4f);
            world.setBlockState(pos, state.with(AGE, 1), 0b10);
            return ActionResult.success(world.isClient());
        } else {
            return super.onUse(state, world, pos, player, hand, hit);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        Box selfBox = SWEET_BERRY_BUSH_BOX.offset(pos);
        if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE
                && selfBox.intersects(entity.getBoundingBox())) {
            entity.slowMovement(state, new Vec3d(0.800000011920929, 0.75, 0.800000011920929));
            if (!world.isClient() && state.get(AGE) > 0 && (entity.lastRenderX != entity.getX() || entity.lastRenderZ != entity.getZ())) {
                double d = Math.abs(entity.getX() - entity.lastRenderX);
                double e = Math.abs(entity.getZ() - entity.lastRenderZ);
                if (d >= 0.003000000026077032 || e >= 0.003000000026077032) {
                    entity.damage(DamageSource.SWEET_BERRY_BUSH, 1.f);
                }
            }
        }
    }

    /* Loot table */

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        Identifier identifier = this.getLootTableId();
        List<ItemStack> stacks = super.getDroppedStacks(state, builder);

        if (identifier == LootTables.EMPTY) {
            return stacks;
        } else {
            LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
            ServerWorld serverWorld = lootContext.getWorld();
            LootTable lootTable = serverWorld.getServer().getLootManager().getTable(identifier);
            List<ItemStack> loot = lootTable.generateLoot(lootContext);
            loot.addAll(stacks);
            return loot;
        }
    }

    /* Fertilizable stuff */

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        return state.get(AGE) < 3;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int age = Math.min(3, state.get(AGE) + 1);
        world.setBlockState(pos, state.with(AGE, age), 0b10);
    }

    static {
        VoxelShape largeSweetBerryBushShape = createCuboidShape(
                3.f, 14.f, 3.f,
                13.f, 23.5f, 13.f
        );
        LARGE_SHAPE = VoxelShapes.union(BIG_FLOWER_POT_SHAPE, largeSweetBerryBushShape);
        SWEET_BERRY_BUSH_BOX = largeSweetBerryBushShape.getBoundingBox();
    }
}
