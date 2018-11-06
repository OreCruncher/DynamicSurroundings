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
package org.orecruncher.dsurround.client.handlers.fog;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BiomeFogColorCalculator extends VanillaFogColorCalculator {

	protected int posX;
	protected int posZ;

	// Last pass calculations. We can reuse if possible to avoid scanning
	// the area, again.
	protected double weightBiomeFog;
	protected Color biomeFogColor;
	protected boolean doScan = true;

	@Override
	@Nonnull
	public Color calculate(@Nonnull final EntityViewRenderEvent.FogColors event) {

		final EntityLivingBase player = EnvironState.getPlayer();
		final World world = EnvironState.getWorld();
		final IBlockAccessEx provider = ClientChunkCache.instance();
		final int playerX = MathStuff.floor(player.posX);
		final int playerZ = MathStuff.floor(player.posZ);

		// ForgeHooksClient.getSkyBlendColour()
		final GameSettings settings = Minecraft.getMinecraft().gameSettings;
		final int[] ranges = ForgeModContainer.blendRanges;
		int distance = 6;
		if (settings.fancyGraphics && ranges.length > 0) {
			distance = ranges[MathStuff.clamp(settings.renderDistanceChunks, 0, ranges.length - 1)];
		}

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
		this.doScan |= this.posX != playerX || this.posZ != playerZ;

		if (this.doScan) {
			this.doScan = false;
			this.posX = playerX;
			this.posZ = playerZ;
			this.biomeFogColor = new Color(0, 0, 0);
			this.weightBiomeFog = 0;

			for (int x = -distance; x <= distance; ++x) {
				for (int z = -distance; z <= distance; ++z) {
					pos.setPos(playerX + x, 0, playerZ + z);

					// If the chunk is not available doScan will be set true. This will force
					// another scan on the next tick.
					this.doScan = this.doScan | !provider.isAvailable(pos);
					final BiomeInfo biome = ClientRegistry.BIOME.get(provider.getBiome(pos));
					final Color color;

					// Fetch the color we are dealing with.
					if (biome.getHasDust()) {
						color = biome.getDustColor();
					} else if (biome.getHasFog()) {
						color = biome.getFogColor();
					} else {
						color = null;
					}

					if (color != null) {
						this.biomeFogColor.add(color);
						this.weightBiomeFog += 1F;
					}
				}
			}
		}

		// If we have nothing then just return whatever Vanilla wanted
		if (this.weightBiomeFog == 0 || distance == 0)
			return super.calculate(event);

		// WorldProvider.getFogColor() - need to calculate the scale based
		// on sunlight and stuff.
		final float partialTicks = (float) event.getRenderPartialTicks();
		final float celestialAngle = world.getCelestialAngle(partialTicks);
		final float baseScale = MathStuff.clamp(MathStuff.cos(celestialAngle * MathStuff.PI_F * 2.0F) * 2.0F + 0.5F, 0,
				1);

		double rScale = baseScale * 0.94F + 0.06F;
		double gScale = baseScale * 0.94F + 0.06F;
		double bScale = baseScale * 0.91F + 0.09F;

		// EntityRenderer.updateFogColor() - adjust the scale further
		// based on rain and thunder.
		final float rainStrength = Weather.getIntensityLevel();
		if (rainStrength > 0) {
			rScale *= 1 - rainStrength * 0.5f;
			gScale *= 1 - rainStrength * 0.5f;
			bScale *= 1 - rainStrength * 0.4f;
		}

		final float thunderStrength = Weather.getThunderStrength();
		if (thunderStrength > 0) {
			rScale *= 1 - thunderStrength * 0.5f;
			gScale *= 1 - thunderStrength * 0.5f;
			bScale *= 1 - thunderStrength * 0.5f;
		}

		// Normalize the blended color components based on the biome weight.
		// The components contain a summation of all the fog components
		// in the area around the player.
		final Color fogColor = new Color(this.biomeFogColor);
		fogColor.scale(
				//
				(float) (rScale / this.weightBiomeFog),
				//
				(float) (gScale / this.weightBiomeFog),
				//
				(float) (bScale / this.weightBiomeFog));

		final Color processedColor = applyPlayerEffects(world, player, fogColor, partialTicks);

		final double weightMixed = (distance * 2 + 1) * (distance * 2 + 1);
		final double weightDefault = weightMixed - this.weightBiomeFog;
		final Color vanillaColor = super.calculate(event);

		processedColor.scale((float) this.weightBiomeFog);
		vanillaColor.scale((float) weightDefault);
		return processedColor.add(vanillaColor).scale((float) (1 / weightMixed));
	}

	protected Color applyPlayerEffects(@Nonnull final World world, @Nonnull final EntityLivingBase player,
			@Nonnull final Color fogColor, final float renderPartialTicks) {
		float darkScale = (float) ((player.lastTickPosY + (player.posY - player.lastTickPosY) * renderPartialTicks)
				* world.provider.getVoidFogYFactor());

		// EntityRenderer.updateFogColor() - If the player is blind need to
		// darken it further
		if (player.isPotionActive(MobEffects.BLINDNESS)) {
			final int duration = player.getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
			darkScale *= (duration < 20) ? (1 - duration / 20f) : 0;
		}

		if (darkScale < 1) {
			darkScale = (darkScale < 0) ? 0 : darkScale * darkScale;
			fogColor.scale(darkScale);
		}

		// EntityRenderer.updateFogColor() - If the player has nightvision going
		// need to lighten it a bit
		if (player.isPotionActive(MobEffects.NIGHT_VISION)) {
			final int duration = player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
			final float brightness = (duration > 200) ? 1
					: 0.7f + MathStuff.sin((duration - renderPartialTicks) * MathStuff.PI_F * 0.2f) * 0.3f;

			float scale = 1 / fogColor.red;
			scale = Math.min(scale, 1F / fogColor.green);
			scale = Math.min(scale, 1F / fogColor.blue);

			return fogColor.scale((1F - brightness) + scale * brightness);
		}

		return fogColor;
	}
}
