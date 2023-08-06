/*
 * Copyright (c) 2021 LambdAurora <email@lambdaurora.dev>
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
import dev.lambdaurora.aurorasdeco.item.group.ItemTreeGroupNode;
import dev.lambdaurora.aurorasdeco.registry.AurorasDecoRegistry;
import dev.lambdaurora.aurorasdeco.util.AuroraUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a bed block for pets.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class PetBedBlock extends Block {
	private static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	private static final Map<Direction, VoxelShape> COLLISION_SHAPES;
	private static final Map<Direction, VoxelShape> OUTLINE_SHAPES;
	public static final ItemTreeGroupNode PET_BEDS_ITEM_GROUP_NODE = new ItemTreeGroupNode(AurorasDeco.id("pet_bed"));

	public PetBedBlock(Settings settings) {
		super(settings);

		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	/* Shapes */

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return COLLISION_SHAPES.get(state.get(FACING));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return OUTLINE_SHAPES.get(state.get(FACING));
	}

	/* Placement */

	@Override
	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		var direction = ctx.getPlayerFacing();
		return this.getDefaultState().with(FACING, direction.getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	public static void register() {
		for (var dye : AuroraUtil.DYE_COLORS) {
			registerPetBed(dye);
		}
	}

	private static void registerPetBed(DyeColor color) {
		var block = Registry.register(Registries.BLOCK,
				AurorasDeco.id("pet_bed/" + color.getName()),
				new PetBedBlock(QuiltBlockSettings.create()
						.mapColor(color).sounds(BlockSoundGroup.WOOD).strength(.2f)));
		var item = AurorasDecoRegistry.registerItem("pet_bed/" + color.getName(), new BlockItem(block, new QuiltItemSettings()));
		PET_BEDS_ITEM_GROUP_NODE.add(item);
	}

	static {
		var base = createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

		var shapes = ImmutableMap.<Direction, VoxelShape>builder();

		shapes.put(Direction.NORTH, VoxelShapes.union(base,
				createCuboidShape(0.0, 2.0, 1.0, 16.0, 4.0, 16.0)
		));
		shapes.put(Direction.SOUTH, VoxelShapes.union(base,
				createCuboidShape(0.0, 2.0, 0.0, 16.0, 4.0, 15.0)
		));
		shapes.put(Direction.EAST, VoxelShapes.union(base,
				createCuboidShape(0.0, 2.0, 0.0, 15.0, 4.0, 16.0)
		));
		shapes.put(Direction.WEST, VoxelShapes.union(base,
				createCuboidShape(1.0, 2.0, 0.0, 16.0, 4.0, 16.0)
		));

		COLLISION_SHAPES = new EnumMap<>(shapes.build());

		base = VoxelShapes.union(
				base,
				createCuboidShape(1.0, 2.0, 1.0, 15.0, 4.0, 15.0)
		);

		shapes = ImmutableMap.builder();

		var sidesX = VoxelShapes.union(
				createCuboidShape(1.0, 2.0, 0.0, 15.0, 8.0, 1.0),
				createCuboidShape(1.0, 2.0, 15.0, 15.0, 8.0, 16.0)
		);
		var sidesZ = VoxelShapes.union(
				createCuboidShape(0.0, 2.0, 1.0, 1.0, 8.0, 15.0),
				createCuboidShape(15.0, 2.0, 1.0, 16.0, 8.0, 15.0)
		);

		shapes.put(Direction.NORTH, VoxelShapes.union(base, sidesZ,
				createCuboidShape(0.0, 2.0, 15.0, 16.0, 8.0, 16.0)
		));
		shapes.put(Direction.SOUTH, VoxelShapes.union(base, sidesZ,
				createCuboidShape(0.0, 2.0, 0.0, 16.0, 8.0, 1.0)
		));
		shapes.put(Direction.EAST, VoxelShapes.union(base, sidesX,
				createCuboidShape(0.0, 2.0, 0.0, 1.0, 8.0, 16.0)
		));
		shapes.put(Direction.WEST, VoxelShapes.union(base, sidesX,
				createCuboidShape(15.0, 2.0, 0.0, 16.0, 8.0, 16.0)
		));

		OUTLINE_SHAPES = new EnumMap<>(shapes.build());
	}
}
