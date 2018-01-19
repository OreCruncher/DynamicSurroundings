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

package org.blockartistry.DynSurround.client.aurora;

import java.util.Random;

import org.blockartistry.DynSurround.client.shader.Shaders;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.gfx.OpenGlState;
import org.blockartistry.lib.gfx.OpenGlUtil;
import org.blockartistry.lib.gfx.shaders.ShaderException;
import org.blockartistry.lib.gfx.shaders.ShaderProgram;
import org.blockartistry.lib.gfx.shaders.ShaderProgram.IShaderUseCallback;
import org.blockartistry.lib.math.MathStuff;
import org.blockartistry.lib.random.XorShiftRandom;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Renders a shader generated aurora along a curved path.  Makes it ribbon like.
 */
@SideOnly(Side.CLIENT)
public class AuroraShaderBand implements IAurora {

	protected ShaderProgram program;
	protected IShaderUseCallback callback;

	// Base color of the aurora
	protected final Color baseColor;
	// Fade color of the aurora
	protected final Color fadeColor;
	// Middle color
	protected final Color middleColor;

	protected AuroraLifeTracker tracker;
	protected final Random random;

	protected final AuroraBand[] bands;

	protected final float auroraWidth;
	protected final float panelTexWidth;

	public AuroraShaderBand(final long seed) {
		// Setup the life cycle
		this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);
		this.random = new XorShiftRandom(seed * 2);
		this.bands = new AuroraBand[this.random.nextInt(3) + 1];
		final AuroraColor colors = AuroraColor.get(this.random);
		this.baseColor = colors.baseColor;
		this.fadeColor = colors.fadeColor;
		this.middleColor = colors.middleColor;

		this.program = Shaders.AURORA;

		this.callback = new IShaderUseCallback() {
			@Override
			public void call(final ShaderProgram shader) throws ShaderException {
				shader.set("time", AuroraUtils.getTimeSeconds() * 0.75F);
				shader.set("resolution", AuroraShaderBand.this.getAuroraWidth(),
						AuroraShaderBand.this.getAuroraHeight());
				shader.set("topColor", AuroraShaderBand.this.fadeColor);
				shader.set("middleColor", AuroraShaderBand.this.middleColor);
				shader.set("bottomColor", AuroraShaderBand.this.baseColor);
				shader.set("alpha", AuroraShaderBand.this.getAlpha());
			}
		};

		final AuroraGeometry geo = AuroraGeometry.get(this.random);
		this.bands[0] = new AuroraBand(this.random, geo, true, true);
		if (this.bands.length > 1) {
			for (int i = 1; i < this.bands.length; i++)
				this.bands[i] = this.bands[0].copy(geo.bandOffset * i);
		}

		this.auroraWidth = this.bands[0].getNodeList().length * this.bands[0].getNodeWidth();
		this.panelTexWidth = this.bands[0].getNodeWidth() / this.auroraWidth;
	}

	protected float getAlpha() {
		return MathStuff.clamp((this.bands[0].getAlphaLimit() / 255F) * this.tracker.ageRatio() * 2.0F, 0F, 1F);
	}

	protected float getZOffset() {
		return AuroraUtils.PLAYER_FIXED_Z_OFFSET;
	}

	protected float getAuroraWidth() {
		return this.auroraWidth;
	}

	protected float getAuroraHeight() {
		return AuroraBand.AURORA_AMPLITUDE;
	}

	@Override
	public boolean isAlive() {
		return this.tracker.isAlive();
	}

	@Override
	public void setFading(final boolean flag) {
		this.tracker.setFading(flag);
	}

	@Override
	public boolean isDying() {
		return this.tracker.isFading();
	}

	@Override
	public void update() {
		this.tracker.update();
	}

	@Override
	public boolean isComplete() {
		return !this.tracker.isAlive();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<SHADER> ");
		builder.append(" base: ").append(this.baseColor.toString());
		builder.append(", mid: ").append(this.middleColor.toString());
		builder.append(", fade: ").append(this.fadeColor.toString());
		builder.append(", alpha: ").append((int) (this.getAlpha() * 255));
		if (!this.tracker.isAlive())
			builder.append(", DEAD");
		else if (this.tracker.isFading())
			builder.append(", FADING");
		return builder.toString();
	}

	@Override
	public void render(final float partialTick) {

		if (this.program == null)
			return;

		final Minecraft mc = Minecraft.getMinecraft();
		final Tessellator tess = Tessellator.getInstance();
		final VertexBuffer renderer = tess.getBuffer();

		final double tranY = AuroraUtils.PLAYER_FIXED_Y_OFFSET - 20;

		final double tranX = mc.player.posX
				- (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTick);

		final double tranZ = (mc.player.posZ - getZOffset())
				- (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTick);

		final OpenGlState glState = OpenGlState.push();

		GlStateManager.translate(tranX, tranY, tranZ);
		GlStateManager.scale(0.5D, 10.0D, 0.5D);
		GlStateManager.disableLighting();
		OpenGlUtil.setAuroraBlend();

		GL11.glFrontFace(GL11.GL_CW);

		try {
			this.program.use(this.callback);

			for (int b = 0; b < this.bands.length; b++) {
				this.bands[b].translate(partialTick);

				final Node[] array = this.bands[b].getNodeList();
				for (int i = 0; i < array.length - 1; i++) {

					final float v1 = 0;
					final float v2 = 1F;
					final float u1 = i * this.panelTexWidth;
					final float u2 = u1 + this.panelTexWidth;

					final Node node = array[i];

					final double posY = node.getModdedY();
					final double posX = node.tetX;
					final double posZ = node.tetZ;
					final double zero = 0;

					final double posX2;
					final double posZ2;
					final double posY2;

					if (i < array.length - 2) {
						final Node nodePlus = array[i + 1];
						posX2 = nodePlus.tetX;
						posZ2 = nodePlus.tetZ;
						posY2 = nodePlus.getModdedY();
					} else {
						posX2 = node.posX;
						posZ2 = node.getModdedZ();
						posY2 = 0.0D;
					}

					renderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
					renderer.pos(posX, zero, posZ).tex(u1, v1).endVertex();
					renderer.pos(posX, posY, posZ).tex(u1, v2).endVertex();
					renderer.pos(posX2, posY2, posZ2).tex(u2, v2).endVertex();
					renderer.pos(posX2, zero, posZ2).tex(u2, v1).endVertex();
					tess.draw();

				}
			}

			this.program.unUse();
		} catch (final Exception ex) {
			ex.printStackTrace();
			this.program = null;
		}

		GL11.glFrontFace(GL11.GL_CCW);
		OpenGlState.pop(glState);
	}
}
