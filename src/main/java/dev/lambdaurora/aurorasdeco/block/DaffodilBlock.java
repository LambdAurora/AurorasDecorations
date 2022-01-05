/*
 * Copyright (c) 2021 - 2022 LambdAurora <aurora42lambda@gmail.com>
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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import java.util.List;

/**
 * Represents the daffodil flower.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class DaffodilBlock extends FlowerBlock {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public DaffodilBlock() {
		super(StatusEffects.NAUSEA, 8,
				QuiltBlockSettings.of(Material.PLANT)
						.noCollision()
						.breakInstantly()
						.sounds(BlockSoundGroup.GRASS)
		);

		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/* Placement */

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		var state = super.getPlacementState(ctx);
		if (state != null)
			return state.with(FACING, ctx.getPlayerFacing().getOpposite());
		return null;
	}

	/* Tooltip */

	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
		super.appendTooltip(stack, world, tooltip, options);
		tooltip.add(new LiteralText("Narcissus pseudonarcissus").formatted(Formatting.GOLD, Formatting.ITALIC));
	}
}
