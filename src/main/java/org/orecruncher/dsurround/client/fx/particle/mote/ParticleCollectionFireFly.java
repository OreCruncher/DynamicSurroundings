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

package org.orecruncher.dsurround.client.fx.particle.mote;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.fx.light.PointLights;
import org.orecruncher.dsurround.lib.profiling.RenderTimer;
import org.orecruncher.lib.gfx.OpenGlUtil;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The fireflies ({@link MoteFireFly}): their lights recorded for {@link PointLights}, their halos drawn, then the
 * fireflies themselves, without writing depth so halos don't hide each other.
 */
@SideOnly(Side.CLIENT)
public class ParticleCollectionFireFly extends ParticleCollection {

	private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(ModInfo.RESOURCE_ID,
			"textures/particles/firefly_glow.png");

	public ParticleCollectionFireFly(@Nonnull final World world, @Nonnull final ResourceLocation tex) {
		super(world, tex);
	}

	@Override
	protected void preRender() {
		super.preRender();
		GlStateManager.depthMask(false);
		OpenGlUtil.setStandardBlend();
	}

	@Override
	public void renderParticle(@Nonnull final BufferBuilder buffer, @Nonnull final Entity entityIn,
			final float partialTicks, final float rotX, final float rotZ, final float rotYZ, final float rotXY,
			final float rotXZ) {

		if (this.myParticles.size() == 0)
			return;

		final RenderTimer timer = timerFor(this.texture);
		timer.begin();
		try {
			if (PointLights.fireflyLightsOn())
				for (int i = 0; i < this.myParticles.size(); i++)
					((MoteFireFly) this.myParticles.get(i)).addLight(partialTicks);

			preRender();

			if (ModOptions.effects.enableFireflyGlow) {
				bindTexture(GLOW_TEXTURE);
				buffer.begin(GL11.GL_QUADS, getVertexFormat());
				for (int i = 0; i < this.myParticles.size(); i++)
					((MoteFireFly) this.myParticles.get(i)).renderHalo(buffer, partialTicks, rotX, rotZ, rotYZ, rotXY,
							rotXZ);
				Tessellator.getInstance().draw();
			}

			bindTexture(this.texture);
			buffer.begin(GL11.GL_QUADS, getVertexFormat());
			for (int i = 0; i < this.myParticles.size(); i++)
				this.myParticles.get(i).renderParticle(buffer, entityIn, partialTicks, rotX, rotZ, rotYZ, rotXY, rotXZ);
			Tessellator.getInstance().draw();

			postRender();
		} finally {
			timer.end();
		}
	}

	public static final ICollectionFactory FACTORY = (world, texture) -> {
		return new ParticleCollectionFireFly(world, texture);
	};

}
