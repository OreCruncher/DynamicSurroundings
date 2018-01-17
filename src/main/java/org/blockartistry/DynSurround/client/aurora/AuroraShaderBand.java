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
import org.blockartistry.lib.OpenGlState;
import org.blockartistry.lib.random.XorShiftRandom;
import org.blockartistry.lib.shaders.ShaderProgram;
import org.blockartistry.lib.shaders.ShaderProgram.IShaderUseCallback;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
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
public class AuroraShaderBand implements IAurora {

	protected ShaderProgram program;
	protected IShaderUseCallback callback;

	protected static final Color topColor = new Color(1.0F, 0F, 0F);
	protected static final Color middleColor = new Color(0.5F, 1.0F, 0.0F);
	protected static final Color bottomColor = new Color(0.0F, 0.8F, 1.0F);

	protected AuroraLifeTracker tracker;
	protected final Random random;

	protected final AuroraBand band;
	protected final float auroraWidth;
	protected final float panelTexWidth;

	public AuroraShaderBand(final long seed) {
		// Setup the life cycle
		this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);
		this.random = new XorShiftRandom(seed);

		this.program = Shaders.AURORA;

		this.callback = shader -> {
			shader.set("time", AuroraUtils.getTimeSeconds() * 0.75F);
			shader.set("resolution", AuroraShaderBand.this.getAuroraWidth(), AuroraShaderBand.this.getAuroraHeight());
			shader.set("topColor", AuroraShader.topColor);
			shader.set("middleColor", AuroraShader.middleColor);
			shader.set("bottomColor", AuroraShader.bottomColor);
			shader.set("alpha", this.tracker.ageRatio());
		};

		final AuroraGeometry geo = AuroraGeometry.get(this.random);
		this.band = new AuroraBand(this.random, geo, true, true);
		this.auroraWidth = this.band.getNodeList().length * this.band.getNodeWidth();
		this.panelTexWidth = this.band.getNodeWidth() / this.auroraWidth;
	}

	protected float getZOffset() {
		return (AuroraUtils.getChunkRenderDistance() + 1) * 16;
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
		builder.append("alpha: ").append((int) (this.tracker.ageRatio() * 255));
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

		this.band.translate(partialTick);

		final float width = this.getAuroraWidth();
		final float xOffset = -(width / 2);
		final float yOffset = 20;

		final Minecraft mc = Minecraft.getMinecraft();
		final Tessellator tess = Tessellator.getInstance();
		final BufferBuilder renderer = tess.getBuffer();

		final double tranY = AuroraUtils.PLAYER_FIXED_Y_OFFSET;

		final double tranX = mc.player.posX
				- (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTick);

		final double tranZ = (mc.player.posZ - getZOffset())
				- (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTick);

		final OpenGlState glState = OpenGlState.push();

		GlStateManager.translate(tranX + xOffset, tranY + yOffset, tranZ);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		GL11.glFrontFace(GL11.GL_CW);

		try {
			this.program.use(this.callback);

			final Node[] array = this.band.getNodeList();
			for (int i = 0; i < array.length - 1; i++) {

				final Node node = array[i];

				final double posY = node.getModdedY();
				final double posX = node.posX;
				final double posZ = node.getModdedZ();

				final double posX2;
				final double posZ2;
				final double posY2;

				if (i < array.length - 2) {
					final Node nodePlus = array[i + 1];
					posX2 = nodePlus.posX;
					posZ2 = nodePlus.getModdedZ();
					posY2 = nodePlus.getModdedY();
				} else {
					posX2 = posX;
					posZ2 = posZ;
					posY2 = posY;
				}

				final float v1 = 0;
				final float v2 = 1F;
				final float u1 = i * this.panelTexWidth;
				final float u2 = u1 + this.panelTexWidth;

				renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
				renderer.pos(posX, posY, posZ).tex(u1, v1).endVertex();
				renderer.pos(posX, posY2, posZ).tex(u1, v2).endVertex();
				renderer.pos(posX2, posY2, posZ2).tex(u2, v2).endVertex();
				renderer.pos(posX2, posY, posZ2).tex(u2, v1).endVertex();
				tess.draw();

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
