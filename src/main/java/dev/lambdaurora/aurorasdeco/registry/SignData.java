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

package dev.lambdaurora.aurorasdeco.registry;

import com.terraformersmc.terraform.sign.block.TerraformHangingSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformWallHangingSignBlock;
import com.terraformersmc.terraform.sign.block.TerraformWallSignBlock;
import net.minecraft.block.Block;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.SignItem;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

import static dev.lambdaurora.aurorasdeco.AurorasDeco.id;

/**
 * Represents the sign data for a wood type.
 *
 * @author LambdAurora
 * @version 1.0.0-beta.13
 * @since 1.0.0-beta.13
 */
public final class SignData {
	private final TerraformSignBlock signBlock;
	private final Block wallSignBlock;
	private final Block hangingSignBlock;
	private final Block wallHangingSignBlock;
	private final SignItem signItem;
	private final HangingSignItem hangingSignItem;

	public SignData(String name, Block base) {
		Identifier signTexture = id("entity/signs/" + name);
		Identifier hangingSignTexture = id("entity/signs/hanging/" + name);
		Identifier hangingSignGuiTexture = id("textures/gui/hanging_sign/" + name);

		this.signBlock = AurorasDecoRegistry.registerBlock(name + "_sign",
				new TerraformSignBlock(signTexture, QuiltBlockSettings.copyOf(base).strength(1.f).noCollision())
		);
		this.wallSignBlock = AurorasDecoRegistry.registerBlock(name + "_wall_sign",
				new TerraformWallSignBlock(signTexture, QuiltBlockSettings.copyOf(base).dropsLike(this.signBlock))
		);
		this.hangingSignBlock = AurorasDecoRegistry.registerBlock(name + "_hanging_sign",
				new TerraformHangingSignBlock(
						hangingSignTexture, hangingSignGuiTexture,
						QuiltBlockSettings.copyOf(base).strength(1.f).requiredFlags(FeatureFlags.UPDATE_1_20)
				));
		this.wallHangingSignBlock = AurorasDecoRegistry.registerBlock(name + "_wall_hanging_sign",
				new TerraformWallHangingSignBlock(
						hangingSignTexture, hangingSignGuiTexture,
						QuiltBlockSettings.copyOf(base).strength(1.f).dropsLike(this.hangingSignBlock).requiredFlags(FeatureFlags.UPDATE_1_20)
				));

		this.signItem = AurorasDecoRegistry.registerItem(name + "_sign",
				new SignItem(new QuiltItemSettings(), this.signBlock, this.wallSignBlock)
		);
		this.hangingSignItem = AurorasDecoRegistry.registerItem(name + "_hanging_sign",
				new HangingSignItem(this.hangingSignBlock, this.wallHangingSignBlock, new QuiltItemSettings())
		);
	}

	public TerraformSignBlock signBlock() {
		return this.signBlock;
	}

	public SignItem signItem() {
		return this.signItem;
	}

	public HangingSignItem hangingSignItem() {
		return this.hangingSignItem;
	}
}
