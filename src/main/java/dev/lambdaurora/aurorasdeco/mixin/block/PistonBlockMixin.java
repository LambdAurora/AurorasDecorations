/*
 * Copyright (c) 2021 - 2022 LambdAurora <email@lambdaurora.dev>
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

package dev.lambdaurora.aurorasdeco.mixin.block;

import dev.lambdaurora.aurorasdeco.block.SeatBlock;
import dev.lambdaurora.aurorasdeco.entity.SeatEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin {
	@Shadow
	protected abstract boolean move(World world, BlockPos pos, Direction dir, boolean retract);

	@Inject(
			method = "move",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onMove(World world, BlockPos pos, Direction dir, boolean retract,
	                    CallbackInfoReturnable<Boolean> cir,
	                    BlockPos pistonHeadPos,
	                    PistonHandler handler, Map<BlockPos, BlockState> map,
	                    List<BlockPos> movedBlocks, List<BlockState> list2, List<BlockPos> brokenBlocks,
	                    BlockState[] affectedStates,
	                    Direction moveDir, int j, int l,
	                    BlockPos currentPos, BlockState currentState) {
		if (currentState.getBlock() instanceof SeatBlock && !world.isClient()) {
			var seats = world.getEntitiesByClass(SeatEntity.class,
					new Box(
							currentPos.getX() - moveDir.getOffsetX(),
							currentPos.getY() - moveDir.getOffsetY(),
							currentPos.getZ() - moveDir.getOffsetZ(),
							currentPos.getX() + 1 - moveDir.getOffsetX(),
							currentPos.getY() + 1 - moveDir.getOffsetY(),
							currentPos.getZ() + 1 - moveDir.getOffsetZ()),
					Entity::hasPassengers
			);

			for (var seat : seats) {
				seat.refreshPositionAndAngles(
						seat.getX() + moveDir.getOffsetX(), seat.getY() + moveDir.getOffsetY(), seat.getZ() + moveDir.getOffsetZ(),
						seat.getYaw(), seat.getPitch()
				);
				seat.setTimeout(true);
			}
		}
	}
}
