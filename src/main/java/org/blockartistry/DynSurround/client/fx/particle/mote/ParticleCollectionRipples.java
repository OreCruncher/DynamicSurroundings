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

package org.blockartistry.DynSurround.client.fx.particle.mote;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.gfx.OpenGlUtil;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCollectionRipples extends ParticleCollection {

	public static enum Style {

		// Original texture
		ORIGINAL("textures/particles/ripple.png"),
		
		// Circle, a bit darker
		CIRCLE("textures/particles/ripple1.png"),

		// Square that matches Minecraft's blockiness
		SQUARE("textures/particles/ripple2.png");

		private final ResourceLocation resource;

		private Style(@Nonnull final String texture) {
			this.resource = new ResourceLocation(DSurround.RESOURCE_ID, texture);
		}

		@Nonnull
		public ResourceLocation getTexture() {
			return this.resource;
		}

		@Nonnull
		public static Style getStyle(final int v) {
			if (v >= values().length)
				return CIRCLE;
			return values()[v];
		}
	}

	public ParticleCollectionRipples(@Nonnull final World world, @Nonnull final ResourceLocation tex) {
		super(world, tex);
	}

	protected void bindTexture(@Nonnull final ResourceLocation resource) {
		final ResourceLocation res = Style.getStyle(ModOptions.rain.rainRippleStyle).getTexture();
		super.bindTexture(res);
	}

	@Override
	protected void preRender() {
		super.preRender();
		GlStateManager.enableDepth();
		OpenGlUtil.setStandardBlend();
		GlStateManager.depthMask(false);
	}

}
