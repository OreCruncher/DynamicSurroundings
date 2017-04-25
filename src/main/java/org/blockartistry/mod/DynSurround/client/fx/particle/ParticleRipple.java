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

import org.blockartistry.mod.DynSurround.DSurround;
import org.lwjgl.opengl.GL11;

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
public class ParticleRipple extends ParticleBase {

	private static final ResourceLocation TEXTURE = new ResourceLocation(DSurround.RESOURCE_ID,
			"textures/particles/ripple.png");
	private final float growthRate;
	private final BlockPos pos;

	protected int slX16;
	protected int blX16;

	public ParticleRipple(World worldIn, double posXIn, double posYIn, double posZIn) {
		super(worldIn, posXIn, posYIn, posZIn);

		this.particleMaxAge = 12 + this.rand.nextInt(8);
		this.growthRate = this.particleMaxAge / 500F;
		this.particleScale = this.growthRate;
		this.pos = new BlockPos(this.posX, this.posY, this.posZ);
		this.posY -= 0.2F;
	}

	@Override
	public void onUpdate() {

		if (this.isExpired)
			return;

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		} else {
			this.particleScale += this.growthRate;
			this.particleAlpha = (float) (this.particleMaxAge - this.particleAge) / (float) (this.particleMaxAge + 3);
			final int combinedLight = this.getBrightnessForRender(0);
			this.slX16 = combinedLight >> 16 & 65535;
			this.blX16 = combinedLight & 65535;
		}
	}

	@Override
	public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		final float x = (float) (this.posX - interpX());
		final float y = (float) (this.posY - interpY());
		final float z = (float) (this.posZ - interpZ());

		this.bindTexture(TEXTURE);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, this.slX16, this.blX16);
		final float bright = this.world.getLightBrightness(this.pos);

		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.translate(x, y, z);
		GlStateManager.scale(this.particleScale, this.particleScale, this.particleScale);
		GlStateManager.depthMask(false);

		final float width = 0.5F;
		final float length = 0.5F;

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		buffer.pos(-width, 0, length).tex(0, 1).color(bright, bright, bright, this.particleAlpha).endVertex();
		buffer.pos(width, 0, length).tex(1, 1).color(bright, bright, bright, this.particleAlpha).endVertex();
		buffer.pos(width, 0, -length).tex(1, 0).color(bright, bright, bright, this.particleAlpha).endVertex();
		buffer.pos(-width, 0, -length).tex(0, 0).color(bright, bright, bright, this.particleAlpha).endVertex();
		Tessellator.getInstance().draw();

		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

	@Override
	public int getBrightnessForRender(final float partialTick) {
		return this.world.isBlockLoaded(this.pos) ? this.world.getCombinedLight(this.pos, 0) : 0;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

}
