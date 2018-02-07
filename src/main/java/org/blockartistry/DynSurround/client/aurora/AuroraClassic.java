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

import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.lib.Color;
import org.blockartistry.lib.random.XorShiftRandom;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public final class AuroraClassic implements IAurora {

	protected final Random random;
	protected final AuroraBand[] bands;

	// Base color of the aurora
	protected final Color baseColor;
	// Fade color of the aurora
	protected final Color fadeColor;

	protected final AuroraLifeTracker tracker;

	public AuroraClassic(final long seed) {
		this.tracker = new AuroraLifeTracker(AuroraUtils.AURORA_PEAK_AGE, AuroraUtils.AURORA_AGE_RATE);
		this.random = new XorShiftRandom(seed);
		this.bands = new AuroraBand[this.random.nextInt(3) + 1];
		final AuroraColor pair = AuroraColor.get(this.random);
		this.baseColor = pair.baseColor;
		this.fadeColor = pair.fadeColor;

		final AuroraGeometry geo = AuroraGeometry.get(this.random);
		this.bands[0] = new AuroraBand(this.random, geo);
		if (this.bands.length > 1) {
			for (int i = 1; i < this.bands.length; i++)
				this.bands[i] = this.bands[0].copy(geo.bandOffset * i);
		}
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
	public boolean isComplete() {
		return !this.isAlive();
	}

	@Override
	public void update() {
		this.tracker.update();
		for (int i = 0; i < this.bands.length; i++)
			this.bands[i].update();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<CLASSIC> ");
		builder.append("bands: ").append(this.bands.length);
		builder.append(", base: ").append(this.baseColor.toString());
		builder.append(", fade: ").append(this.fadeColor.toString());
		builder.append(", alpha: ").append(this.getAlpha());
		if (this.tracker.isFading())
			builder.append(", FADING");
		return builder.toString();
	}

	@Nonnull
	protected Color getBaseColor() {
		return this.baseColor;
	}

	@Nonnull
	protected Color getFadeColor() {
		return this.fadeColor;
	}

	protected int getAlpha() {
		return (int) (this.tracker.ageRatio() * this.bands[0].getAlphaLimit());
	}

	protected float getAlphaf() {
		return this.getAlpha() / 255.0F;
	}

	protected int getZOffset() {
		return AuroraUtils.PLAYER_FIXED_Z_OFFSET;
	}

	@Override
	public void render(final float partialTick) {

		final float alpha = this.getAlphaf();
		if (alpha <= 0.0F)
			return;

		final Minecraft mc = Minecraft.getMinecraft();
		final Tessellator tess = Tessellator.getInstance();
		final VertexBuffer renderer = tess.getBuffer();

		final double tranY = AuroraUtils.PLAYER_FIXED_Y_OFFSET;

		final double tranX = mc.thePlayer.posX
				- (mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTick);

		final double tranZ = (mc.thePlayer.posZ - getZOffset())
				- (mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTick);

		final Color base = this.getBaseColor();
		final Color fade = this.getFadeColor();
		final double zero = 0.0D;

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.translate(tranX, tranY, tranZ);
		GlStateManager.scale(0.5D, 8.0D, 0.5D);
		GlStateManager.disableTexture2D();
		GlStateManager.disableFog();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.depthMask(false);

		renderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

		for (int b = 0; b < this.bands.length; b++) {
			this.bands[b].translate(partialTick);

			final Node[] array = this.bands[b].getNodeList();
			for (int i = 0; i < array.length - 1; i++) {

				final Node node = array[i];

				final double posY = node.getModdedY();
				final double posX = node.tetX;
				final double posZ = node.tetZ;
				final double tetX = node.tetX2;
				final double tetZ = node.tetZ2;

				final double posX2;
				final double posZ2;
				final double tetX2;
				final double tetZ2;
				final double posY2;

				if (i < array.length - 2) {
					final Node nodePlus = array[i + 1];
					posX2 = nodePlus.tetX;
					posZ2 = nodePlus.tetZ;
					tetX2 = nodePlus.tetX2;
					tetZ2 = nodePlus.tetZ2;
					posY2 = nodePlus.getModdedY();
				} else {
					posX2 = tetX2 = node.posX;
					posZ2 = tetZ2 = node.getModdedZ();
					posY2 = 0.0D;
				}

				// Front
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, posY, posZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, posY2, posZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, posY2, posZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();

				// Bottom
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX2, zero, posZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(posX, zero, posZ).color(base.red, base.green, base.blue, alpha).endVertex();

				// Back
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, posY, tetZ).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, posY2, tetZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, posY2, tetZ2).color(fade.red, fade.green, fade.blue, 0).endVertex();
				renderer.pos(tetX2, zero, tetZ2).color(base.red, base.green, base.blue, alpha).endVertex();
				renderer.pos(tetX, zero, tetZ).color(base.red, base.green, base.blue, alpha).endVertex();
			}
		}

		tess.draw();

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.enableFog();
		GlStateManager.enableTexture2D();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.disableBlend();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

}
