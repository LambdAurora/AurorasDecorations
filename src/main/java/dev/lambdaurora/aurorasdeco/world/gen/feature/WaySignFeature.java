/*
 * Copyright (c) 2023 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.world.gen.feature;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorasdeco.block.ExtensionType;
import dev.lambdaurora.aurorasdeco.block.SignPostBlock;
import dev.lambdaurora.aurorasdeco.block.WallLanternBlock;
import dev.lambdaurora.aurorasdeco.block.entity.SignPostBlockEntity;
import dev.lambdaurora.aurorasdeco.item.SignPostItem;
import dev.lambdaurora.aurorasdeco.mixin.world.ChunkGeneratorAccessor;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoTags;
import dev.lambdaurora.aurorasdeco.registry.LanternRegistry;
import dev.lambdaurora.aurorasdeco.world.gen.WorldGenUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.item.Items;
import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderSet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.*;

/**
 * Represents a feature that generates a lonely sign posts indicating directions
 * to nearby interesting landmarks with some parts of a road.
 *
 * @version 1.0.0-beta.12
 * @since 1.0.0-beta.12
 */
public class WaySignFeature extends Feature<WaySignFeature.Config> {

	public WaySignFeature(Codec<Config> configCodec) {
		super(configCodec);
	}

	@Override
	public boolean place(FeatureContext<Config> context) {
		var config = context.getConfig();
		var random = context.getRandom();
		var world = context.getWorld();
		var pos = context.getOrigin().mutableCopy();

		pos.move(Direction.DOWN);
		var downState = world.getBlockState(pos);
		if (!downState.isSolidBlock(world, pos)) {
			return false;
		}
		pos.move(Direction.UP);

		for (int i = 0; i < 3; i++) {
			if (!world.testBlockState(pos, state -> state.isAir() || !state.isSolidBlock(world, pos))) {
				return false;
			}

			pos.move(Direction.UP);
		}

		pos.set(context.getOrigin());

		this.setBlockState(world, pos, config.base().getBlockState(random, pos));
		pos.move(Direction.UP);
		this.setBlockState(world, pos, config.getSignPostState());

		var sign = world.getBlockEntity(pos, AurorasDecoRegistry.SIGN_POST_BLOCK_ENTITY_TYPE).orElseThrow();

		pos.move(Direction.UP);
		this.setBlockState(world, pos, config.fenceBlock().getDefaultState());

		var facing = Direction.byId(2 + random.nextInt(4));
		pos.move(facing);

		sign.setGenerationSettings(new SignPostBlockEntity.GenerationSettings(config.signPostItem(), facing));

		var lantern = LanternRegistry.fromItem(Items.LANTERN).getDefaultState();
		this.setBlockState(world, pos, lantern.with(WallLanternBlock.FACING, facing).with(WallLanternBlock.EXTENSION, ExtensionType.FENCE));

		pos.set(context.getOrigin());
		pos.move(Direction.DOWN);

		var path = config.path();
		this.setBlockState(world, pos, path.state().getBlockState(random, pos));
		WorldGenUtils.generateCircle(world, random, pos, path.radius().get(random), path.state(), path.additionFactor(), path.removalFactor(),
				start -> {
					var top = world.getTopPosition(Heightmap.Type.WORLD_SURFACE_WG, start);
					return top.mutableCopy().move(Direction.DOWN);
				}
		);

		return true;
	}

	/**
	 * Generates some directions to interesting landmarks.
	 * <p>
	 * This is triggered on the first tick of sign posts with the {@code generate_directions} property set to {@code true}
	 * (since during world generation we don't have access to all structures).
	 *
	 * @param world the world the sign post is in
	 * @param pos the block position of the sign post
	 * @param state the block state of the sign post
	 * @param signPost the sign post itself
	 */
	public static void generateDirections(World world, BlockPos pos, BlockState state, SignPostBlockEntity signPost) {
		var settings = signPost.getGenerationSettings();

		if (settings == null) {
			world.setBlockState(pos, ((SignPostBlock) state.getBlock()).getFenceState(state), Block.NOTIFY_LISTENERS);
			return;
		}

		world.setBlockState(pos, state.with(SignPostBlock.GENERATE_DIRECTIONS, false), Block.NOTIFY_LISTENERS);

		/* Search for structures */
		var serverWorld = (ServerWorld) world;
		var plausibleTag = serverWorld.getRegistryManager().get(RegistryKeys.STRUCTURE_FEATURE)
				.getTag(AurorasDecoTags.WAY_SIGN_DESTINATION_STRUCTURES);
		HolderSet<StructureFeature> tag;

		if (plausibleTag.isPresent()) {
			tag = plausibleTag.get();
		} else {
			tag = HolderSet.createDirect();
		}

		var places = findClosest(serverWorld, tag, pos, 100, false);

		if (places.isEmpty()) {
			signPost.putSignUp(settings.material(), Text.literal("N"), 180);
			signPost.putSignDown(settings.material(), Text.literal("S"), 0);
		} else {
			if (places.size() == 2) {
				var destination = places.get(1);
				int distance = (int) Math.sqrt(destination.distance);

				signPost.putSignDown(settings.material(), Text.literal(distance + "m"), 0);

				destination.makeSignTarget(signPost.getDown(), settings.facing(), world.getRandom());
			}

			var destination = places.get(0);
			int distance = (int) Math.sqrt(destination.distance);

			signPost.putSignUp(settings.material(), Text.literal(distance + "m"), 0);
			destination.makeSignTarget(signPost.getUp(), settings.facing(), world.getRandom());
		}
	}

	private static List<FoundFeatureEntry> findClosest(ServerWorld world, HolderSet<StructureFeature> structures, BlockPos origin,
			int range, boolean skipExistingChunks) {
		if (!world.getStructureManager().shouldGenerate()) {
			return Collections.emptyList();
		}

		var chunkGenerator = (ChunkGeneratorAccessor) world.getChunkManager().getChunkGenerator();
		ConcentricRingPlacementCalculator concentricRingPlacementCalculator = world.getChunkManager().method_46642();
		var structurePlacements = new Object2ObjectArrayMap<StructurePlacement, Set<Holder<StructureFeature>>>();

		for (var holder : structures) {
			for (StructurePlacement structurePlacement : concentricRingPlacementCalculator.getFeaturePlacements(holder)) {
				structurePlacements.computeIfAbsent(structurePlacement, sP -> new ObjectArraySet<>()).add(holder);
			}
		}

		if (structurePlacements.isEmpty()) {
			return Collections.emptyList();
		} else {
			var results = new ArrayList<FoundFeatureEntry>();

			StructureManager structureManager = world.getStructureManager();
			var list = new ArrayList<Map.Entry<StructurePlacement, Set<Holder<StructureFeature>>>>(structurePlacements.size());

			for (var entry : structurePlacements.entrySet()) {
				StructurePlacement structurePlacement2 = entry.getKey();
				if (structurePlacement2 instanceof ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
					Pair<BlockPos, Holder<StructureFeature>> foundStructure = chunkGenerator.invokeFindStructures(
							entry.getValue(), world, structureManager, origin, skipExistingChunks, concentricRingsStructurePlacement
					);

					if (foundStructure != null) {
						compareAndAdd(origin, foundStructure, results);
					}
				} else if (structurePlacement2 instanceof RandomSpreadStructurePlacement) {
					list.add(entry);
				}
			}

			if (!list.isEmpty()) {
				int chunkX = ChunkSectionPos.getSectionCoord(origin.getX());
				int chunkZ = ChunkSectionPos.getSectionCoord(origin.getZ());

				// Progressively explore from the closest to furthest.
				for (int chunkDist = 0; chunkDist <= range; ++chunkDist) {
					for (Map.Entry<StructurePlacement, Set<Holder<StructureFeature>>> entry2 : list) {
						RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement) entry2.getKey();
						var foundStructures = getNearestGeneratedStructures(
								entry2.getValue(), world, structureManager, origin, chunkX, chunkZ, chunkDist, skipExistingChunks,
								world.getSeed(), randomSpreadStructurePlacement, 2
						);

						if (!foundStructures.isEmpty()) {
							results.addAll(foundStructures);
							results.sort(Comparator.comparingDouble(FoundFeatureEntry::distance));

							if (results.size() > 2) return results.subList(0, 2);
							else if (results.size() == 2) return results;
						}
					}
				}
			}

			return results;
		}
	}

	private static List<FoundFeatureEntry> getNearestGeneratedStructures(
			Set<Holder<StructureFeature>> structures, WorldView world, StructureManager structureManager, BlockPos origin,
			int chunkX, int chunkZ, int chunkDist, boolean skipExistingChunks, long seed, RandomSpreadStructurePlacement placement,
			int limit
	) {
		var result = new ArrayList<FoundFeatureEntry>();
		int spacing = placement.getSpacing();

		for (int distX = -chunkDist; distX <= chunkDist; ++distX) {
			boolean edgeX = distX == -chunkDist || distX == chunkDist;

			for (int distZ = -chunkDist; distZ <= chunkDist; ++distZ) {
				boolean edgeZ = distZ == -chunkDist || distZ == chunkDist;

				if (edgeX || edgeZ) {
					int startChunkX = chunkX + spacing * distX;
					int startChunkZ = chunkZ + spacing * distZ;
					ChunkPos chunkPos = placement.getPotentialStartChunk(seed, startChunkX, startChunkZ);

					Pair<BlockPos, Holder<StructureFeature>> found = ChunkGeneratorAccessor.invokeMethod_41522(structures, world, structureManager,
							skipExistingChunks, placement, chunkPos);
					if (found != null) {
						BlockPos structurePos = found.getFirst();
						double xDist = origin.getX() - structurePos.getX();
						double zDist = origin.getZ() - structurePos.getZ();
						double distance = xDist * xDist + zDist * zDist;

						var feature = new FoundFeatureEntry(distance, structurePos, found.getSecond());
						result.add(feature);

						if (result.size() >= limit) {
							return result;
						}
					}
				}
			}
		}

		return result;
	}

	private static void compareAndAdd(BlockPos origin, Pair<BlockPos, Holder<StructureFeature>> foundStructure, List<FoundFeatureEntry> results) {
		BlockPos structurePos = foundStructure.getFirst();
		double xDist = origin.getX() - structurePos.getX();
		double zDist = origin.getZ() - structurePos.getZ();
		double distance = xDist * xDist + zDist * zDist;

		if (results.size() < 2) {
			var feature = new FoundFeatureEntry(distance, structurePos, foundStructure.getSecond());

			if (results.size() == 1) {
				if (distance < results.get(0).distance) {
					results.add(0, feature);
					return;
				}
			}

			results.add(feature);
		} else {
			var last = results.get(1);

			if (distance < last.distance) {
				results.remove(1);
				results.add(0, new FoundFeatureEntry(distance, structurePos, foundStructure.getSecond()));
			}
		}
	}

	private record FoundFeatureEntry(double distance, BlockPos pos, Holder<StructureFeature> feature) {
		public void makeSignTarget(SignPostBlockEntity.Sign sign, Direction facing, RandomGenerator random) {
			// I am honestly not sure *why* I need to invert the logic on the X axis, but it works so whatever...
			if (facing.getAxis() == Direction.Axis.X) facing = facing.getOpposite();

			// We want to know in which direction the sign is facing when it's pointed on its right.
			// Note: the returned yaw is where it points, not the facing!
			float rightYaw = sign.getPointTowardAngle(this.pos);
			Direction signFacing = Direction.fromRotation(rightYaw - 90);

			if (signFacing == facing) {
				// We're on the correct side! Let's set the yaw.
				sign.setYaw(rightYaw);
			} else if (signFacing.getOpposite() == facing) {
				// We're on the opposite side! Let's invert the sign direction and set the yaw.
				sign.setLeft(true);
				sign.pointToward(this.pos);
			} else {
				// We're on the other sides! Let's find out which side allows us to put the sign the closest to the "facing" direction.
				float rightYawFacing = MathHelper.wrapDegrees(rightYaw - 90);
				float leftYaw = MathHelper.wrapDegrees(rightYaw - 180);
				float leftYawFacing = leftYaw - 90;
				if (leftYawFacing < 0) leftYawFacing += 360;
				float facingYaw = facing.asRotation();

				// Let's see the deltas of each side.
				float rightDiff = 180 - Math.abs(MathHelper.floorMod(Math.abs(facingYaw - rightYawFacing), 360) - 180);
				float leftDiff = 180 - Math.abs(MathHelper.floorMod(Math.abs(facingYaw - leftYawFacing), 360) - 180);

				boolean left = false;

				if (rightDiff == leftDiff) { // Unlikely, but we can randomize the selection then.
					left = random.nextBoolean();
				} else if (leftDiff < rightDiff) { // Here the sign pointing to the left is closer to facing the lantern, do it!
					left = true;
				}

				if (left) {
					// The sign pointing to the left is closer to facing the lantern, make it point left and set its yaw.
					sign.setLeft(true);
					sign.setYaw(leftYaw);
				} else {
					// The sign pointing to the right is closer to facing the lantern, set its yaw.
					sign.setYaw(rightYaw);
				}
			}
		}
	}

	public record Config(
			FenceBlock fenceBlock,
			SignPostItem signPostItem,
			BlockStateProvider base,
			PathConfig path
	) implements FeatureConfig {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(
						Registries.BLOCK.getCodec().flatXmap(block -> {
							if (block instanceof FenceBlock fence) return DataResult.success(fence);
							else return DataResult.error(() -> "Given material isn't a fence block.");
						}, DataResult::success).fieldOf("material").forGetter(Config::fenceBlock),
						Registries.ITEM.getCodec().flatXmap(item -> {
							if (item instanceof SignPostItem sign) return DataResult.success(sign);
							else return DataResult.error(() -> "Given sign isn't a sign post item.");
						}, DataResult::success).fieldOf("board_material").forGetter(Config::signPostItem),
						BlockStateProvider.TYPE_CODEC.fieldOf("base").forGetter(Config::base),
						PathConfig.CODEC.fieldOf("path").forGetter(Config::path)
				)
				.apply(instance, Config::new)
		);

		public BlockState getSignPostState() {
			return SignPostBlock.byFence(this.fenceBlock).getDefaultState().with(SignPostBlock.GENERATE_DIRECTIONS, true);
		}
	}

	public record PathConfig(BlockStateProvider state, IntProvider radius, float additionFactor, float removalFactor) {
		public static final Codec<PathConfig> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(
						BlockStateProvider.TYPE_CODEC.fieldOf("state").forGetter(PathConfig::state),
						IntProvider.POSITIVE_CODEC.fieldOf("radius").forGetter(PathConfig::radius),
						Codec.FLOAT.fieldOf("addition_factor").forGetter(PathConfig::additionFactor),
						Codec.FLOAT.fieldOf("removal_factor").forGetter(PathConfig::removalFactor)
				)
				.apply(instance, PathConfig::new)
		);
	}
}
