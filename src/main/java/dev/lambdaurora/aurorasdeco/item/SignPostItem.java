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

package dev.lambdaurora.aurorasdeco.item;

import dev.lambdaurora.aurorasdeco.AurorasDeco;
import dev.lambdaurora.aurorasdeco.block.SignPostBlock;
import dev.lambdaurora.aurorasdeco.block.entity.SignPostBlockEntity;
import dev.lambdaurora.aurorasdeco.registry.WoodType;
import dev.lambdaurora.aurorasdeco.util.KindSearcher;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * Represents a sign post item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SignPostItem extends Item {
	public static final Identifier SIGN_POST_MODEL = AurorasDeco.id("block/template/sign_post");
	public static final Identifier ABSOLUTE_OAK_SIGN_POST_TEXTURE = id("textures/special/sign_post/oak.png");

	private static final KindSearcher<ItemStack, Item> SIGN_POST_KIND_SEARCHER
			= KindSearcher.assignableSearcher(SignPostItem.class, ItemStack::getItem).build();
	private static final List<SignPostItem> SIGN_POSTS = new ArrayList<>();

	private final WoodType woodType;

	public SignPostItem(WoodType woodType, Settings settings) {
		super(settings);
		this.woodType = woodType;

		SIGN_POSTS.add(this);
	}

	public static SignPostItem fromWoodType(WoodType woodType) {
		for (var item : SIGN_POSTS) {
			if (item.getWoodType().equals(woodType))
				return item;
		}

		return SIGN_POSTS.get(0);
	}

	public static Stream<SignPostItem> stream() {
		return SIGN_POSTS.stream();
	}

	/**
	 * Gets the wood type of this rest item.
	 *
	 * @return the wood type
	 */
	public WoodType getWoodType() {
		return this.woodType;
	}

	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
		if (this.isInGroup(group) || group == ItemGroup.SEARCH) {
			stacks.add(SIGN_POST_KIND_SEARCHER.findLastOfGroup(stacks), new ItemStack(this));
		}
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		var player = context.getPlayer();
		if (player == null)
			return ActionResult.PASS;

		var world = context.getWorld();
		var pos = context.getBlockPos();
		var stack = context.getStack();

		var state = world.getBlockState(pos);
		boolean signPost = state.getBlock() instanceof SignPostBlock;

		if (state.getBlock() instanceof FenceBlock || signPost) {
			if (!signPost) {
				var signPostState = SignPostBlock.byFence((FenceBlock) state.getBlock())
						.getPlacementState(new ItemPlacementContext(context));
				if (signPostState == null)
					return ActionResult.FAIL;

				world.setBlockState(pos, signPostState, Block.NOTIFY_ALL);
			}

			boolean success = false;

			var blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof SignPostBlockEntity signPostBlockEntity) {
				int r = MathHelper.floor((double) ((180.f + context.getPlayerYaw()) * 16.f / 360.f) + 0.5) & 15;

				var text = stack.hasCustomName()
						? Text.Serializer.fromJson(stack.getSubNbt(ItemStack.DISPLAY_KEY).getString(ItemStack.NAME_KEY))
						: Text.empty();
				if (SignPostBlock.isUp(context.getHitPos().getY())) {
					if (signPostBlockEntity.getUp() == null) {
						signPostBlockEntity.putSignUp(this, text, 90 + r * -22.5f);
						success = true;
					}
				} else if (signPostBlockEntity.getDown() == null) {
					signPostBlockEntity.putSignDown(this, text, 90 + r * -22.5f);
					success = true;
				}
			}

			if (success) {
				if (world.isClient()) {
					var soundGroup = this.getWoodType().getComponent(WoodType.ComponentType.PLANKS).blockSoundGroup();
					world.playSound(player, pos, soundGroup.getPlaceSound(), SoundCategory.BLOCKS,
							(soundGroup.getVolume() + 1.f) / 2.f, soundGroup.getPitch() * 0.8f);
				}
				if (!context.getPlayer().isCreative()) stack.decrement(1);
				return ActionResult.success(world.isClient());
			}
		}
		return super.useOnBlock(context);
	}
}
