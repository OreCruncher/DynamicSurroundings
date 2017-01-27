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

package org.blockartistry.mod.DynSurround.client.fx.particle;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleFootprint extends Particle {

	private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/footprint.png");

	private int footstepAge;
	private final int footstepMaxAge;
	private final BlockPos pos;
	private final float rotation;
	private final boolean isRightFoot;

	public ParticleFootprint(@Nonnull final World world, final double x, final double y, final double z, final float rotation, final boolean isRightFoot) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.footstepMaxAge = 200;

		this.pos = new BlockPos(this.posX, this.posY, this.posZ);
		this.rotation = rotation;
		this.isRightFoot = isRightFoot;
	}

	/**
	 * Renders the particle
	 */
	@Override
	public void renderParticle(@Nonnull final VertexBuffer worldRendererIn, @Nonnull final Entity entityIn,
			final float partialTicks, final float rotationX, final float rotationZ, final float rotationYZ,
			final float rotationXY, final float rotationXZ) {

		float f = ((float) this.footstepAge + partialTicks) / (float) this.footstepMaxAge;
		f = f * f;
		float f1 = 2.0F - f * 2.0F;

		if (f1 > 1.0F) {
			f1 = 1.0F;
		}

		// Sets the alpha
		f1 = f1 * 0.4F;
		
		final double minU = this.isRightFoot ? 0.5D : 0D;
		final double maxU = this.isRightFoot ? 1.0D : 0.5D;
		final double minV = 0D;
		final double maxV = 1D;

		final int i = this.getBrightnessForRender(partialTicks);
		final int j = i % 65536;
		final int k = i / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);

		GlStateManager.disableLighting();

		//final float f2 = 0.25F;
		final float width = 0.125F;
		final float length = width * 2.0F;
		final float x = ((float) (this.prevPosX - interpPosX));
		final float y = ((float) (this.prevPosY - interpPosY));
		final float z = ((float) (this.prevPosZ - interpPosZ));
		final float f6 = this.worldObj.getLightBrightness(pos);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(FOOTPRINT_TEXTURE);
		
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(-this.rotation + 180, 0F, 1F, 0F);
		
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldRendererIn.pos((double) (-width), (double) 0, (double) (+length)).tex(minU, maxV).color(f6, f6, f6, f1)
				.endVertex();
		worldRendererIn.pos((double) (width), (double) 0, (double) (+ length)).tex(maxU, maxV).color(f6, f6, f6, f1)
				.endVertex();
		worldRendererIn.pos((double) (width), (double) 0, (double) (- length)).tex(maxU, minV).color(f6, f6, f6, f1)
				.endVertex();
		worldRendererIn.pos((double) (-width), (double) 0, (double) (- length)).tex(minU, minV).color(f6, f6, f6, f1)
				.endVertex();

		Tessellator.getInstance().draw();
		GlStateManager.disableBlend();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}

	@Override
	public void onUpdate() {
		++this.footstepAge;

		if (this.footstepAge == this.footstepMaxAge) {
			this.setExpired();
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
}