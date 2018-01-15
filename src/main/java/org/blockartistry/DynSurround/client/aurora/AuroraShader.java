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

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.registry.DimensionRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.shaders.ShaderProgram;
import org.blockartistry.lib.shaders.ShaderProgram.IShaderUseCallback;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AuroraShader implements IAurora {

	protected final DimensionRegistry dimensions = RegistryManager.<DimensionRegistry>get(RegistryType.DIMENSION);

	// New school shader for aurora - WIP
	protected ShaderProgram program;
	protected IShaderUseCallback callback;

	protected static final Color topColor = new Color(1.0F, 0F, 0F);
	protected static final Color middleColor = new Color(0.5F, 1.0F, 0.0F);
	protected static final Color bottomColor = new Color(0.0F, 0.8F, 1.0F);

	protected static final int MAX_ALPHA_TIME = 128;
	protected static final int ALPHA_TIME_DELTA = 1;

	protected AuroraLifeTracker tracker;

	public AuroraShader() {
		try {

			// Setup the life cycle
			this.tracker = new AuroraLifeTracker(MAX_ALPHA_TIME, ALPHA_TIME_DELTA);

			this.program = ShaderProgram.createProgram("Aurora Shader",
					new ResourceLocation(DSurround.MOD_ID, "shaders/aurora.vert"),
					new ResourceLocation(DSurround.MOD_ID, "shaders/aurora.frag"));

			this.callback = shader -> {
				shader.set("time", (EnvironState.getTickCounter() + EnvironState.getPartialTick()) / 20F * 0.75F);
				shader.set("resolution", AuroraShader.this.getAuroraWidth(), AuroraShader.this.getAuroraHeight());
				shader.set("topColor", AuroraShader.topColor);
				shader.set("middleColor", AuroraShader.middleColor);
				shader.set("bottomColor", AuroraShader.bottomColor);
				shader.set("alpha", this.tracker.ageRatio());
			};

		} catch (final Exception ex) {
			ex.printStackTrace();
			this.program = null;
			this.callback = null;
			this.tracker = null;
		}
	}

	protected int getZOffset() {
		return (Minecraft.getMinecraft().gameSettings.renderDistanceChunks + 1) * 16;
	}

	protected int getAuroraWidth() {
		final int chunks = Math.min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks - 1, 8);
		return chunks * 16 * 5;
	}

	protected int getAuroraHeight() {
		return 200;
	}

	@Override
	public boolean isAlive() {
		return this.tracker.isAlive();
	}

	@Override
	public void die() {
		this.tracker.setFading(true);
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
		builder.append("alpha: ").append((int)(this.tracker.ageRatio() * 255));
		if (!this.tracker.isAlive())
			builder.append(", DEAD");
		else if (this.tracker.isFading())
			builder.append(", FADING");
		return builder.toString();
	}

	@Override
	public void render(final float partialTick) {

		try {

			final int width = this.getAuroraWidth();
			final int height = this.getAuroraHeight();
			final int xOffset = -(width / 2);
			final int yOffset = 20;

			final Minecraft mc = Minecraft.getMinecraft();

			final double tranY = this.dimensions.getSeaLevel(mc.world)
					- ((mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTick));

			final double tranX = mc.player.posX
					- (mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTick);

			final double tranZ = (mc.player.posZ - getZOffset())
					- (mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTick);

			GlStateManager.pushMatrix();
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

			GlStateManager.translate(tranX + xOffset, tranY + yOffset, tranZ);
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();

			this.program.use(this.callback);

			final BufferBuilder renderer = Tessellator.getInstance().getBuffer();
			GL11.glFrontFace(GL11.GL_CW);
			renderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
			renderer.pos(0, 0, 0).tex(0, 0).endVertex();
			renderer.pos(0, height, 0).tex(0, 1).endVertex();
			renderer.pos(width, 0, 0).tex(1, 0).endVertex();
			renderer.pos(width, height, 0).tex(1, 1).endVertex();
			Tessellator.getInstance().draw();
			GL11.glFrontFace(GL11.GL_CCW);

			this.program.unUse();

			GlStateManager.disableBlend();
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();

		} catch (final Exception ex) {
			ex.printStackTrace();
			// Disable the shader - something went wrong
			this.program.delete();
			this.program = null;
		}
	}
}
