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

package org.orecruncher.dsurround.client.aurora;

import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.client.shader.Shaders;
import org.orecruncher.lib.gfx.OpenGlState;
import org.orecruncher.lib.gfx.OpenGlUtil;
import org.orecruncher.lib.gfx.shaders.ShaderProgram;
import org.orecruncher.lib.gfx.shaders.ShaderProgram.IShaderUseCallback;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Renders a shader generated aurora along a curved path.  Makes it ribbon like.
 */
@SideOnly(Side.CLIENT)
public class AuroraShaderBand extends AuroraBase {

	protected ShaderProgram program;
	protected IShaderUseCallback callback;
	protected final float auroraWidth;
	protected final float panelTexWidth;

	public AuroraShaderBand(final long seed) {
		super(seed, true);

		this.program = Shaders.AURORA;

		this.callback = shader -> {
			shader.set("time", AuroraUtils.getTimeSeconds() * 0.75F);
			shader.set("resolution", AuroraShaderBand.this.getAuroraWidth(), AuroraShaderBand.this.getAuroraHeight());
			shader.set("topColor", AuroraShaderBand.this.getFadeColor());
			shader.set("middleColor", AuroraShaderBand.this.getMiddleColor());
			shader.set("bottomColor", AuroraShaderBand.this.getBaseColor());
			shader.set("alpha", AuroraShaderBand.this.getAlpha());
		};

		this.auroraWidth = this.band.getNodeList().length * this.band.getNodeWidth();
		this.panelTexWidth = this.band.getNodeWidth() / this.auroraWidth;
	}

	@Override
	protected float getAlpha() {
		return MathStuff.clamp((this.band.getAlphaLimit() / 255F) * this.tracker.ageRatio() * 2.0F, 0F, 1F);
	}

	protected float getAuroraWidth() {
		return this.auroraWidth;
	}

	protected float getAuroraHeight() {
		return AuroraBand.AURORA_AMPLITUDE;
	}

	@Override
	public void render(final float partialTick) {

		if (this.program == null)
			return;

		final Tessellator tess = Tessellator.getInstance();
		final BufferBuilder renderer = tess.getBuffer();

		final double tranY = getTranslationY(partialTick);
		final double tranX = getTranslationX(partialTick);
		final double tranZ = getTranslationZ(partialTick);

		final OpenGlState glState = OpenGlState.push();

		GlStateManager.disableLighting();
		OpenGlUtil.setAuroraBlend();

		GL11.glFrontFace(GL11.GL_CW);

		this.band.translate(partialTick);
		final Panel[] array = this.band.getNodeList();

		try {

			this.program.use(this.callback);

			for (int b = 0; b < this.bandCount; b++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(tranX, tranY, tranZ + this.offset * b);
				GlStateManager.scale(0.5D, 10.0D, 0.5D);

				renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
				for (int i = 0; i < array.length - 1; i++) {

					final float v1 = 0;
					final float v2 = 1F;
					final float u1 = i * this.panelTexWidth;
					final float u2 = u1 + this.panelTexWidth;

					final Panel node = array[i];

					final double posY = node.getModdedY();
					final double posX = node.tetX;
					final double posZ = node.tetZ;
					final double zero = 0;

					final double posX2;
					final double posZ2;
					final double posY2;

					if (i < array.length - 2) {
						final Panel nodePlus = array[i + 1];
						posX2 = nodePlus.tetX;
						posZ2 = nodePlus.tetZ;
						posY2 = nodePlus.getModdedY();
					} else {
						posX2 = node.posX;
						posZ2 = node.getModdedZ();
						posY2 = 0.0D;
					}

					// Only render this on the first pass.
					if (i == 0) {
						renderer.pos(posX, zero, posZ).tex(u1, v1).endVertex();
						renderer.pos(posX, posY, posZ).tex(u1, v2).endVertex();
					}
					renderer.pos(posX2, zero, posZ2).tex(u2, v1).endVertex();
					renderer.pos(posX2, posY2, posZ2).tex(u2, v2).endVertex();
				}
				tess.draw();

				GlStateManager.popMatrix();
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
			this.program = null;
		} finally {
			try {
				if (this.program != null)
					this.program.unUse();
			} catch (final Throwable t) {
				;
			}
		}

		GL11.glFrontFace(GL11.GL_CCW);
		OpenGlState.pop(glState);
	}

	@Override
	public String toString() {
		return "<SHADER> " + super.toString();
	}
}
