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

package org.blockartistry.DynSurround.client.weather;

import java.util.Random;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.DynSurround.registry.BiomeRegistry;
import org.blockartistry.DynSurround.registry.DimensionRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.SeasonRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.random.XorShiftRandom;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class StormRenderer {

	private static final double[] RAIN_X_COORDS = new double[1024];
	private static final double[] RAIN_Y_COORDS = new double[1024];

	static {
		for (int i = 0; i < 32; ++i) {
			for (int j = 0; j < 32; ++j) {
				final double f2 = (double) (j - 16);
				final double f3 = (double) (i - 16);
				final double f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);
				RAIN_X_COORDS[i << 5 | j] = (-f3 / f4) * 0.5D;
				RAIN_Y_COORDS[i << 5 | j] = (f2 / f4) * 0.5D;
			}
		}
	}

	private final Random random = new XorShiftRandom();
	private final BiomeRegistry biomes = RegistryManager.get(RegistryType.BIOME);
	private final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);
	private final SeasonRegistry season = RegistryManager.get(RegistryType.SEASON);
	private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	@Nonnull
	private BlockPos getPrecipitationHeight(@Nonnull final World world, @Nonnull final BlockPos pos) {
		return this.season.getPrecipitationHeight(world, pos);
	}

	private static void bindTexture(@Nonnull final ResourceLocation resource) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
	}

	/**
	 * Render rain and snow
	 */
	public void render(@Nonnull final EntityRenderer renderer, final float partialTicks) {

		// Don't use EnvironState - may not have been initialized when rendering
		// starts.
		final World world = Minecraft.getMinecraft().world;

		if (!this.dimensions.hasWeather(world))
			return;

		final float rainStrength = WeatherProperties.getIntensityLevel();
		if (rainStrength <= 0.0F)
			return;

		final float alphaRatio = rainStrength / WeatherProperties.getMaxIntensityLevel();

		renderer.enableLightmap();

		final Tessellator tess = Tessellator.getInstance();
		final VertexBuffer worldrenderer = tess.getBuffer();

		GlStateManager.disableCull();
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.alphaFunc(516, 0.1F);

		final int range = renderer.mc.gameSettings.fancyGraphics ? 10 : 5;
		float f1 = (float) renderer.rendererUpdateCount + partialTicks;

		final RenderManager manager = renderer.mc.getRenderManager();
		worldrenderer.setTranslation(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);
		final int locY = MathHelper.floor(manager.viewerPosY);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		final WeatherProperties props = WeatherProperties.getIntensity();
		final Entity entity = renderer.mc.getRenderViewEntity();

		boolean isDrawing = false;
		ResourceLocation currentTexture = null;

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
				final BiomeInfo biome = this.biomes.get(world.getBiome(this.mutable));

				if (biome.getHasDust() || biome.getHasPrecipitation()) {
					final int precipHeight = getPrecipitationHeight(world, this.mutable).getY();
					int k2 = playerY - range;
					int l2 = playerY + range;

					if (k2 < precipHeight) {
						k2 = precipHeight;
					}

					if (l2 < precipHeight) {
						l2 = precipHeight;
					}

					int i3 = precipHeight;

					if (precipHeight < locY) {
						i3 = locY;
					}

					if (k2 != l2) {

						this.random.setSeed((long) (gridX * gridX * 3121 + gridX * 45238971
								^ gridZ * gridZ * 418711 + gridZ * 13761));
						this.mutable.setPos(gridX, k2, gridZ);
						final boolean canSnow = this.season.canWaterFreeze(world, this.mutable);

						if (!biome.getHasDust() && !canSnow) {

							if (!isDrawing || currentTexture != props.getRainTexture()) {
								if (isDrawing) {
									tess.draw();
								}
								currentTexture = props.getRainTexture();
								bindTexture(currentTexture);
								worldrenderer.begin(GL11.GL_QUADS,
										DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
								isDrawing = true;
							}

							double d5 = ((double) (renderer.rendererUpdateCount + gridX * gridX * 3121
									+ gridX * 45238971 + gridZ * gridZ * 418711 + gridZ * 13761 & 31)
									+ (double) partialTicks) / 32.0D * (3.0D + this.random.nextDouble());
							double d6 = (double) ((float) gridX + 0.5F) - entity.posX;
							double d7 = (double) ((float) gridZ + 0.5F) - entity.posZ;
							float f3 = MathHelper.sqrt(d6 * d6 + d7 * d7) / (float) range;
							float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * alphaRatio;
							this.mutable.setPos(gridX, i3, gridZ);
							int j3 = world.getCombinedLight(this.mutable, 0);
							int k3 = j3 >> 16 & 65535;
							int l3 = j3 & 65535;
							worldrenderer.pos((double) gridX - rainX + 0.5D, (double) k2, (double) gridZ - rainY + 0.5D)
									.tex(0.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
							worldrenderer.pos((double) gridX + rainX + 0.5D, (double) k2, (double) gridZ + rainY + 0.5D)
									.tex(1.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
							worldrenderer.pos((double) gridX + rainX + 0.5D, (double) l2, (double) gridZ + rainY + 0.5D)
									.tex(1.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
							worldrenderer.pos((double) gridX - rainX + 0.5D, (double) l2, (double) gridZ - rainY + 0.5D)
									.tex(0.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3)
									.endVertex();
						} else {

							ResourceLocation texture = props.getSnowTexture();
							if (biome.getHasDust() && !canSnow)
								texture = props.getDustTexture();

							if (!isDrawing || currentTexture != texture) {
								if (isDrawing) {
									tess.draw();
								}
								currentTexture = texture;
								bindTexture(currentTexture);
								worldrenderer.begin(GL11.GL_QUADS,
										DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
								isDrawing = true;
							}

							final Color color;
							if (biome.getHasDust())
								color = biome.getDustColor();
							else
								color = new Color(1.0F, 1.0F, 1.0F);

							double d8 = (double) (((float) (renderer.rendererUpdateCount & 511) + partialTicks)
									/ 512.0F);
							// The 0.2F factor was originally 0.01F. It
							// affects the horizontal movement of particles,
							// which works well for dust.
							final float factor = biome.getHasDust() ? 0.2F : 0.01F;
							double d9 = this.random.nextDouble()
									+ (double) f1 * factor * (double) ((float) this.random.nextGaussian());
							double d10 = this.random.nextDouble() + (double) (f1 * (float) this.random.nextGaussian()) * 0.001D;
							double d11 = (double) ((float) gridX + 0.5F) - entity.posX;
							double d12 = (double) ((float) gridZ + 0.5F) - entity.posZ;
							float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / (float) range;
							float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * alphaRatio;
							this.mutable.setPos(gridX, i3, gridZ);
							int i4 = (world.getCombinedLight(this.mutable, 0) * 3 + 15728880) / 4;
							int j4 = i4 >> 16 & 65535;
							int k4 = i4 & 65535;
							worldrenderer.pos((double) gridX - rainX + 0.5D, (double) k2, (double) gridZ - rainY + 0.5D)
									.tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10)
									.color(color.red, color.green, color.blue, f5).lightmap(j4, k4).endVertex();
							worldrenderer.pos((double) gridX + rainX + 0.5D, (double) k2, (double) gridZ + rainY + 0.5D)
									.tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10)
									.color(color.red, color.green, color.blue, f5).lightmap(j4, k4).endVertex();
							worldrenderer.pos((double) gridX + rainX + 0.5D, (double) l2, (double) gridZ + rainY + 0.5D)
									.tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10)
									.color(color.red, color.green, color.blue, f5).lightmap(j4, k4).endVertex();
							worldrenderer.pos((double) gridX - rainX + 0.5D, (double) l2, (double) gridZ - rainY + 0.5D)
									.tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10)
									.color(color.red, color.green, color.blue, f5).lightmap(j4, k4).endVertex();
						}
					}
				}
			}
		}

		if (isDrawing) {
			tess.draw();
		}

		worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(516, 0.1F);
		renderer.disableLightmap();
	}
}
