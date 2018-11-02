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

package org.orecruncher.dsurround.client.weather;

import java.util.Random;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.orecruncher.dsurround.client.ClientChunkCache;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.weather.compat.RandomThings;
import org.orecruncher.dsurround.registry.PrecipitationType;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.dsurround.registry.season.SeasonInfo;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class StormRenderer {

	private static final double[] RAIN_X_COORDS = new double[1024];
	private static final double[] RAIN_Y_COORDS = new double[1024];

	static {
		for (int i = 0; i < 32; ++i) {
			for (int j = 0; j < 32; ++j) {
				final double f2 = j - 16;
				final double f3 = i - 16;
				final double f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);
				RAIN_X_COORDS[i << 5 | j] = (-f3 / f4) * 0.5D;
				RAIN_Y_COORDS[i << 5 | j] = (f2 / f4) * 0.5D;
			}
		}
	}

	private final Random random = new XorShiftRandom();
	private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	private static ResourceLocation effectTexture = null;
	private static boolean isDrawing = false;
	private static BufferBuilder worldrenderer = null;
	private static int locY;

	private static void setupForRender(@Nonnull final ResourceLocation r) {
		if (effectTexture != r) {
			if (worldrenderer == null) {
				worldrenderer = Tessellator.getInstance().getBuffer();
				final RenderManager manager = Minecraft.getMinecraft().getRenderManager();
				worldrenderer.setTranslation(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);
				locY = MathHelper.floor(manager.viewerPosY);
			}
			if (isDrawing)
				Tessellator.getInstance().draw();
			effectTexture = r;
			Minecraft.getMinecraft().getTextureManager().bindTexture(r);
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			isDrawing = true;
		}
	}

	private static void closeRender() {
		if (isDrawing)
			Tessellator.getInstance().draw();
		if (worldrenderer != null)
			worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
		isDrawing = false;
		effectTexture = null;
		worldrenderer = null;
	}

	/**
	 * Render rain and snow
	 */
	public void render(@Nonnull final EntityRenderer renderer, final float partialTicks) {

		// Don't use EnvironState - may not have been initialized when rendering
		// starts.
		final Minecraft mc = Minecraft.getMinecraft();
		final World world = mc.world;

		if (!ClientRegistry.DIMENSION.hasWeather(world))
			return;

		final float rainStrength = Weather.getIntensityLevel();
		if (rainStrength <= 0.0F)
			return;

		final float alphaRatio = rainStrength / Weather.getMaxIntensityLevel();

		renderer.enableLightmap();

		GlStateManager.disableCull();
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.alphaFunc(516, 0.1F);

		final int range = mc.gameSettings.fancyGraphics ? 10 : 5;
		final float f1 = RenderWeather.rendererUpdateCount + partialTicks;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		final SeasonInfo season = ClientRegistry.SEASON.getData(world);
		final Weather.Properties props = Weather.getWeatherProperties();
		final Entity entity = mc.getRenderViewEntity();

		final BlockPos playerPos = EnvironState.getPlayerPosition();
		final int playerX = playerPos.getX();
		final int playerY = playerPos.getY();
		final int playerZ = playerPos.getZ();

		for (int gridZ = playerZ - range; gridZ <= playerZ + range; ++gridZ) {
			for (int gridX = playerX - range; gridX <= playerX + range; ++gridX) {
				final int idx = (gridZ - playerZ + 16) * 32 + gridX - playerX + 16;
				final double rainX = RAIN_X_COORDS[idx];
				final double rainY = RAIN_Y_COORDS[idx];
				this.mutable.setPos(gridX, 0, gridZ);

				if (!RandomThings.shouldRain(world, this.mutable))
					continue;

				final BiomeInfo biome = ClientRegistry.BIOME.get(ClientChunkCache.INSTANCE.getBiome(this.mutable));

				final int precipHeight = season.getPrecipitationHeight(world, this.mutable).getY();
				final int k2 = Math.max(playerY - range, precipHeight);
				final int l2 = Math.max(playerY + range, precipHeight);
				if (k2 == l2)
					continue;

				this.mutable.setPos(gridX, k2, gridZ);

				final PrecipitationType pt = season.getPrecipitationType(world, this.mutable, biome);
				if (pt == PrecipitationType.NONE)
					continue;

				this.random.setSeed(gridX * gridX * 3121 + gridX * 45238971 ^ gridZ * gridZ * 418711 + gridZ * 13761);
				final int i3 = Math.max(precipHeight, locY);

				final double d6 = gridX + 0.5F - entity.posX;
				final double d7 = gridZ + 0.5F - entity.posZ;
				final float f3 = MathHelper.sqrt(d6 * d6 + d7 * d7) / range;
				this.mutable.setPos(gridX, i3, gridZ);

				final int combinedLight = ClientChunkCache.INSTANCE.getCombinedLight(this.mutable, 0);

				if (pt == PrecipitationType.RAIN) {

					setupForRender(props.getRainTexture());

					// d8 makes the rain fall down. Assumes texture height of 512 pixels.
					final double d5 = ((double) (RenderWeather.rendererUpdateCount + gridX * gridX * 3121
							+ gridX * 45238971 + gridZ * gridZ * 418711 + gridZ * 13761 & 31) + (double) partialTicks)
							/ 32.0D * (3.0D + this.random.nextDouble());

					final float alpha = ((1.0F - f3 * f3) * 0.5F + 0.5F) * alphaRatio;
					final int slX16 = combinedLight >> 16 & 65535;
					final int blX16 = combinedLight & 65535;

					worldrenderer.pos(gridX - rainX + 0.5D, k2, gridZ - rainY + 0.5D).tex(0.0D, k2 * 0.25D + d5)
							.color(1.0F, 1.0F, 1.0F, alpha).lightmap(slX16, blX16).endVertex();
					worldrenderer.pos(gridX + rainX + 0.5D, k2, gridZ + rainY + 0.5D).tex(1.0D, k2 * 0.25D + d5)
							.color(1.0F, 1.0F, 1.0F, alpha).lightmap(slX16, blX16).endVertex();
					worldrenderer.pos(gridX + rainX + 0.5D, l2, gridZ + rainY + 0.5D).tex(1.0D, l2 * 0.25D + d5)
							.color(1.0F, 1.0F, 1.0F, alpha).lightmap(slX16, blX16).endVertex();
					worldrenderer.pos(gridX - rainX + 0.5D, l2, gridZ - rainY + 0.5D).tex(0.0D, l2 * 0.25D + d5)
							.color(1.0F, 1.0F, 1.0F, alpha).lightmap(slX16, blX16).endVertex();
				} else {

					final Color color;
					final ResourceLocation texture;

					if (pt == PrecipitationType.DUST) {
						color = biome.getDustColor();
						texture = props.getDustTexture();
					} else {
						color = Color.WHITE;
						texture = props.getSnowTexture();
					}

					setupForRender(texture);

					// d8 makes the snow fall down. Assumes texture height of 512 pixels.
					final double d8 = ((RenderWeather.rendererUpdateCount & 511) + partialTicks) / 512.0F;
					// The 0.2F factor was originally 0.01F. It
					// affects the horizontal movement of particles,
					// which works well for dust.
					final float factor = biome.getHasDust() ? 0.2F : 0.01F;
					// d9 shifts the texture left/right
					final double d9 = this.random.nextDouble()
							+ (double) f1 * factor * ((float) this.random.nextGaussian());
					// d10 shifts the texture up/down
					final double d10 = this.random.nextDouble() + f1 * (float) this.random.nextGaussian() * 0.001D;

					final float alpha = ((1.0F - f3 * f3) * 0.3F + 0.5F) * alphaRatio;
					final int cl = (combinedLight * 3 + 15728880) / 4;
					final int slX16 = cl >> 16 & 65535;
					final int blX16 = cl & 65535;

					worldrenderer.pos(gridX - rainX + 0.5D, k2, gridZ - rainY + 0.5D)
							.tex(0.0D + d9, k2 * 0.25D + d8 + d10).color(color.red, color.green, color.blue, alpha)
							.lightmap(slX16, blX16).endVertex();
					worldrenderer.pos(gridX + rainX + 0.5D, k2, gridZ + rainY + 0.5D)
							.tex(1.0D + d9, k2 * 0.25D + d8 + d10).color(color.red, color.green, color.blue, alpha)
							.lightmap(slX16, blX16).endVertex();
					worldrenderer.pos(gridX + rainX + 0.5D, l2, gridZ + rainY + 0.5D)
							.tex(1.0D + d9, l2 * 0.25D + d8 + d10).color(color.red, color.green, color.blue, alpha)
							.lightmap(slX16, blX16).endVertex();
					worldrenderer.pos(gridX - rainX + 0.5D, l2, gridZ - rainY + 0.5D)
							.tex(0.0D + d9, l2 * 0.25D + d8 + d10).color(color.red, color.green, color.blue, alpha)
							.lightmap(slX16, blX16).endVertex();
				}
			}
		}

		closeRender();

		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
		renderer.disableLightmap();
	}
}
