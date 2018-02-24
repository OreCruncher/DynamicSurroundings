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

package org.blockartistry.DynSurround.client.fx.particle;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleItemStack extends ParticleAsset {

	protected static RenderItem itemRenderer;

	protected final ItemStack prototype;
	protected final IBakedModel model;

	public ParticleItemStack(@Nonnull final ItemStack stack, @Nonnull final World world, final double x, final double y,
			final double z) {
		this(stack, world, x, y, z, 0, 0, 0);
	}

	public ParticleItemStack(@Nonnull final ItemStack stack, @Nonnull final World world, final double x, final double y,
			final double z, final double dX, final double dY, final double dZ) {
		super(world, x, y, z, dX, dY, dZ);

		if (itemRenderer == null) {
			itemRenderer = Minecraft.getMinecraft().getRenderItem();
		}

		this.prototype = stack.copy();
		this.model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(this.prototype, null,
				(EntityLivingBase) null);
		setScale(1.0F);
	}

	@Override
	protected void doModelTranslate() {
		GlStateManager.translate(0, -0.16F, 0);
	}

	@Override
	protected void handleRender(final float partialTicks) {
		final IBakedModel baked = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(this.model,
				ItemCameraTransforms.TransformType.GROUND, false);
		itemRenderer.renderItem(this.prototype, baked);
	}

}
