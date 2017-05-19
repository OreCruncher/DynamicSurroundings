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
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCollectionFootprint extends ParticleCollection {

	public static enum Style {

		SHOE("textures/particles/footprint.png"),
		SQUARE("textures/particles/footprint_square.png"),
		HORSESHOE("textures/particles/footprint_horseshoe.png");

		private final ResourceLocation resource;

		private Style(@Nonnull final String texture) {
			this.resource = new ResourceLocation(DSurround.RESOURCE_ID, texture);
		}

		public ResourceLocation getTexture() {
			return this.resource;
		}

		public static Style getStyle(final int v) {
			if (v >= values().length)
				return SHOE;
			return values()[v];
		}
	}

	public ParticleCollectionFootprint(World world, ResourceLocation tex) {
		super(world, tex);

	}

	protected void bindTexture(@Nonnull final ResourceLocation resource) {
		final ResourceLocation res = Style.getStyle(ModOptions.footprintStyle).getTexture();
		super.bindTexture(res);
	}

	@Override
	public void renderParticle(final VertexBuffer buffer, final Entity entityIn, final float partialTicks,
			final float rotX, final float rotZ, final float rotYZ, final float rotXY, final float rotXZ) {

		this.bindTexture(this.texture);
		
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		for(int i = 0; i < this.myParticles.size(); i++)
			this.myParticles.get(i).renderParticle(buffer, entityIn, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);

		GlStateManager.disableBlend();
	}
	
}
