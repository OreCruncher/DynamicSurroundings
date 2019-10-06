/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.client.renderer;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BadgeRenderLayer implements LayerRenderer<EntityLivingBase> {

	private final static float CONST = (180F / MathStuff.PI_F) / 20.0F;

	/**
	 * Used to provide logic for when a badges should be displayed or not. It is
	 * intended to provide a hook for things like configuration or keybind. Per
	 * entity suppression should be handled using the IItemStackProvider.
	 */
	public static interface IBadgeDisplayCheck {
		boolean showBadge();
	}

	/**
	 * Method for the layer renderer to figure out what ItemStack to render as a
	 * badge. If there is no stack to render then it should return ItemStack.EMPTY.
	 */
	public static interface IItemStackProvider {
		/**
		 * Get the ItemStack to render as a badge
		 * @param e Entity that is being rendered
		 * @return ItemStack to render as a badge
		 */
		@Nonnull
		ItemStack getStackToDisplay(@Nonnull final EntityLivingBase e);

		/**
		 * Mircro adjustment of vertical position of the badge
		 * @param e Entity that is being rendered
		 * @return Vertical adjustment to the icon position
		 */
		default float adjustY(@Nonnull final EntityLivingBase e) {
			return 0F;
		}

		/**
		 * Scale factor to apply to the badge when rendering
		 * @param e Entity that is being rendered
		 * @return Scale factor to apply to icon render
		 */
		default float scale(@Nonnull final EntityLivingBase e) {
			return 0.6F;
		}
	}

	protected final IBadgeDisplayCheck displayCheck;
	protected final IItemStackProvider stackProvider;

	public BadgeRenderLayer(@Nonnull final IBadgeDisplayCheck check, @Nonnull final IItemStackProvider provider) {
		this.displayCheck = check;
		this.stackProvider = provider;
	}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		if (!this.displayCheck.showBadge())
			return;

		// Only render if in range
		final double distSq = ModOptions.effects.specialEffectRange * ModOptions.effects.specialEffectRange;
		if (entity.getDistanceSq(EnvironState.getPlayer()) > distSq)
			return;

		// Only render if there is a stack to display
		final ItemStack stackToRender = this.stackProvider.getStackToDisplay(entity);
		if (stackToRender.isEmpty())
			return;

		final float age = entity.ticksExisted + partialTicks;
		final float s = this.stackProvider.scale(entity);
		final float dY = this.stackProvider.adjustY(entity) + entity.height - 0.15F + (MathStuff.sin(age / 20F)) / 3F;

		GlStateManager.pushMatrix();
		GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.scale(s, s, s);
		GlStateManager.rotate(age * CONST, 0F, 1F, 0F);
		GlStateManager.translate(0, dY, 0);
		Minecraft.getMinecraft().getRenderItem().renderItem(stackToRender, ItemCameraTransforms.TransformType.FIXED);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

}
