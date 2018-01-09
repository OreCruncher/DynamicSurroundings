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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCollectionFootprint extends ParticleCollection {

	public static enum Style {

		// Regular shoe print style
		SHOE("textures/particles/footprint.png"),

		// Print that looks like a square and matches Minecraft blockiness
		SQUARE("textures/particles/footprint_square.png"),

		// Horseshoe shaped print. Good with Quadruped feature enabled
		HORSESHOE("textures/particles/footprint_horseshoe.png"),
		
		// Bird 3 toed prints.
		BIRD("textures/particles/footprint_bird.png"),

		// Animal paw
		PAW("textures/particles/footprint_paw.png"),
		
		// Solid Square
		SQUARE_SOLID("textures/particles/footprint_square_solid.png"),
		
		// Low resolution 4x4 square
		LOWRES_SQUARE("textures/particles/footprint_lowres_square.png");
		
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
				return SHOE;
			return values()[v];
		}
	}

	public ParticleCollectionFootprint(@Nonnull final World world, @Nonnull final ResourceLocation tex) {
		super(world, tex);

	}

	protected void bindTexture(@Nonnull final ResourceLocation resource) {
		final ResourceLocation res = Style.getStyle(ModOptions.footprintStyle).getTexture();
		super.bindTexture(res);
	}

	@Override
	protected void preRender() {
		super.preRender();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}

	@Override
	protected void postRender() {
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		super.postRender();
	}
	
}
