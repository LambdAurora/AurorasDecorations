/*
 * Copyright (c) 2021-2022 LambdAurora <email@lambdaurora.dev>
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

import dev.lambdaurora.aurorasdeco.registry.AurorasDecoSounds;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

/**
 * Represents a brazier.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class BrazierBlock extends AuroraBlock implements Waterloggable {
	public static final BooleanProperty LIT = Properties.LIT;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	private static final VoxelShape COLLISION_SHAPE = VoxelShapes.union(
			createCuboidShape(2, 0, 2, 14, 6, 14),
			createCuboidShape(7, 0, 0, 9, 1, 2),
			createCuboidShape(7, 0, 14, 9, 1, 16),
			createCuboidShape(0, 0, 7, 2, 1, 9),
			createCuboidShape(14, 0, 7, 16, 1, 9)
	);
	private static final VoxelShape FLAME_SHAPE = createCuboidShape(2, 6, 2, 14, 16, 14);
	private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
			COLLISION_SHAPE,
			FLAME_SHAPE
	);
	private static final Box FLAME_BOX = FLAME_SHAPE.getBoundingBox();

	private final ParticleEffect particle;
	private final int fireDamage;

	public BrazierBlock(MapColor color, int fireDamage, int luminance, ParticleEffect particle) {
		this(QuiltBlockSettings.of(Material.DECORATION), color, fireDamage, luminance, particle);
	}

	public BrazierBlock(QuiltBlockSettings settings, MapColor color, int fireDamage, int luminance, ParticleEffect particle) {
		this(settings.mapColor(color)
						.strength(2.f)
						.nonOpaque()
						.luminance(state -> state.get(LIT) ? luminance : 0)
						.sounds(BlockSoundGroup.METAL),
				fireDamage, particle);
	}

	public BrazierBlock(QuiltBlockSettings settings, int fireDamage, ParticleEffect particle) {
		super(settings);

		this.fireDamage = fireDamage;
		this.particle = particle;

		this.setDefaultState(this.getDefaultState().with(LIT, false).with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LIT, WATERLOGGED);
	}

	/* Shapes */

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return COLLISION_SHAPE;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE_SHAPE;
	}

	/* Placement */

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var world = ctx.getWorld();
		var blockPos = ctx.getBlockPos();
		boolean water = world.getFluidState(blockPos).getFluid() == Fluids.WATER;

		return this.getDefaultState().with(WATERLOGGED, water).with(LIT, !water);
	}

	/* Interaction */

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		var box = FLAME_BOX.offset(pos);
		if (box.intersects(entity.getBoundingBox()) && !entity.isFireImmune() && state.get(LIT)
				&& entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
			entity.damage(DamageSource.IN_FIRE, (float) this.fireDamage);
		}

		super.onEntityCollision(state, world, pos, entity);
	}

	/* Updates */

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
	                                            WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	/* Entity Stuff */

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	/* Fluid */

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
			boolean lit = state.get(LIT);
			if (lit) {
				extinguish(null, world, pos, state);
			}

			world.setBlockState(pos, state.with(WATERLOGGED, true).with(LIT, false), Block.NOTIFY_ALL);
			world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
			return true;
		} else {
			return false;
		}
	}

	/* Client */

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, RandomGenerator random) {
		if (state.get(LIT)) {
			if (random.nextInt(10) == 0) {
				world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						AurorasDecoSounds.BRAZIER_CRACKLE_SOUND_EVENT, SoundCategory.BLOCKS,
						.5f + random.nextFloat(), random.nextFloat() * .7f + .6f, false);
			}

			world.addParticle(this.particle,
					pos.getX() + random.nextDouble() * 0.625 + 0.1875,
					pos.getY() + 0.375 + random.nextDouble() * 0.4,
					pos.getZ() + random.nextDouble() * 0.625 + 0.1875,
					0, 0, 0);
		}
	}

	public static void extinguish(@Nullable Entity entity, WorldAccess world, BlockPos pos, BlockState state) {
		if (!world.isClient()) {
			world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS,
					1.f, 1.f);
		} else {
			for (int i = 0; i < 20; ++i) {
				spawnSmokeParticle((World) world, pos);
			}
		}

		world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
	}

	public static void spawnSmokeParticle(World world, BlockPos pos) {
		var random = world.getRandom();
		world.addImportantParticle(ParticleTypes.SMOKE, true,
				pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
				pos.getY() + random.nextDouble() + random.nextDouble(),
				pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1),
				0.0, 0.07, 0.0);
	}

	public static boolean canBeLit(BlockState state) {
		return state.isInAndMatches(AurorasDecoTags.BRAZIERS, s -> s.contains(LIT) && !state.get(LIT));
	}

	public static boolean canBeUnlit(BlockState state) {
		return state.isInAndMatches(AurorasDecoTags.BRAZIERS, s -> s.contains(LIT) && state.get(LIT));
	}
}
