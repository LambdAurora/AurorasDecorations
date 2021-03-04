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

import dev.lambdaurora.aurorasdeco.mixin.BlockAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents a potted sweet berry bush.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BigPottedSweetBerryBushBlock extends BigFlowerPotBlock implements Fertilizable {
    private static final Box SWEET_BERRY_BUSH_BOX;

    private final Map<BlockState, VoxelShape> shapeCache = new Object2ObjectOpenHashMap<>();

    public BigPottedSweetBerryBushBlock(PottedPlantType type) {
        super(type);

        StateManager.Builder<Block, BlockState> builder = new StateManager.Builder<>(this);
        this.appendProperties(builder);
        ((BlockAccessor) this.getPlant()).aurorasdeco$appendProperties(builder);
        ((BlockAccessor) this).setStateManager(builder.build(Block::getDefaultState, BlockState::new));

        this.setDefaultState(remap(type.getPlant().getDefaultState(), this.stateManager.getDefaultState()));
    }

    @Override
    public BlockState getPlantState(BlockState potState) {
        return remap(potState, super.getPlantState(potState));
    }

    /* Shapes */

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeCache.computeIfAbsent(state, s -> shape(s, world, pos));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BIG_FLOWER_POT_SHAPE;
    }

    /* Ticking */

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return this.getPlant().hasRandomTicks(state);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.getPlant().randomTick(state, world, pos, random);
    }

    /* Interaction */

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result = this.getPlant().onUse(state, world, pos, player, hand, hit);
        if (result.isAccepted())
            return result;
        else if (player.getStackInHand(hand).isOf(Items.BONE_MEAL))
            return ActionResult.PASS;
        else
            return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        Box selfBox = SWEET_BERRY_BUSH_BOX.offset(pos);
        if (selfBox.intersects(entity.getBoundingBox())) {
            this.getPlant().onEntityCollision(state, world, pos, entity);
        }
    }

    /* Loot table */

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(super.getDroppedStacks(state, builder));
        drops.addAll(this.getPlantState(state).getDroppedStacks(builder));
        return drops;
    }

    /* Fertilizable stuff */

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        return ((Fertilizable) this.getPlant()).isFertilizable(world, pos, state, isClient);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return ((Fertilizable) this.getPlant()).canGrow(world, random, pos, state);
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        ((Fertilizable) this.getPlant()).grow(world, random, pos, state);
    }

    private static BlockState remap(BlockState src, BlockState dst) {
        for (Property<?> property : src.getProperties()) {
            dst = remapProperty(src, property, dst);
        }
        return dst;
    }

    private static <T extends Comparable<T>> BlockState remapProperty(BlockState src, Property<T> property, BlockState dst) {
        if (dst.contains(property))
            dst = dst.with(property, src.get(property));
        return dst;
    }

    private VoxelShape shape(BlockState state, BlockView world, BlockPos pos) {
        VoxelShape plantShape = this.getPlant().getOutlineShape(state, world, pos, ShapeContext.absent());
        float ratio = .65f;
        float offset = (1.f - ratio) / 2.f;
        return VoxelShapes.union(BIG_FLOWER_POT_SHAPE, resize(plantShape, ratio).offset(offset, .8f, offset));
    }

    private static VoxelShape resize(VoxelShape shape, double factor) {
        List<VoxelShape> shapes = new ArrayList<>();
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            shapes.add(VoxelShapes.cuboid(minX * factor, minY * factor, minZ * factor,
                    maxX * factor, maxY * factor, maxZ * factor));
        });

        if (shapes.size() == 1)
            return shapes.get(0);
        return shapes.stream().collect(VoxelShapes::empty, VoxelShapes::union, VoxelShapes::union).simplify();
    }

    static {
        VoxelShape largeSweetBerryBushShape = createCuboidShape(
                3.f, 14.f, 3.f,
                13.f, 23.5f, 13.f
        );
        SWEET_BERRY_BUSH_BOX = largeSweetBerryBushShape.getBoundingBox();
    }
}
