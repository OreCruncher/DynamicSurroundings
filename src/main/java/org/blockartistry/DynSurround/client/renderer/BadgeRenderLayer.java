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

package org.blockartistry.DynSurround.client.renderer;

import javax.annotation.Nonnull;

import org.blockartistry.lib.math.MathStuff;

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
	 * Used to provide logic for when a badges should be displayed or not.
	 * It is intended to provide a hook for things like configuration or
	 * keybind.  Per entity suppression should be handled using the
	 * IItemStackProvider.
	 */
	public static interface IShowBadge {
		boolean showBadge();
	}

	/**
	 * Method for the layer renderer to figure out what ItemStack to render
	 * as a badge.  If there is no stack to render then it should return
	 * ItemStack.EMPTY.
	 */
	public static interface IItemStackProvider {
		@Nonnull
		ItemStack getStackToDisplay(@Nonnull final EntityLivingBase e);
	}

	protected final IShowBadge displayCheck;
	protected final IItemStackProvider stackProvider;

	public BadgeRenderLayer(@Nonnull final IShowBadge check, @Nonnull final IItemStackProvider provider) {
		this.displayCheck = check;
		this.stackProvider = provider;
	}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		if (!this.displayCheck.showBadge())
			return;

		final ItemStack stackToRender = this.stackProvider.getStackToDisplay(entity);
		if (stackToRender.isEmpty())
			return;

		final float age = entity.ticksExisted + partialTicks;
		final float height = entity.height - 0.15F + (MathStuff.sin(age / 20F)) / 3F;
		final float s = 0.6F;

		GlStateManager.pushMatrix();
		GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.scale(s, s, s);
		GlStateManager.rotate(age * CONST, 0F, 1F, 0F);
		GlStateManager.translate(0, height, 0);
		Minecraft.getMinecraft().getRenderItem().renderItem(stackToRender, ItemCameraTransforms.TransformType.FIXED);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

}
