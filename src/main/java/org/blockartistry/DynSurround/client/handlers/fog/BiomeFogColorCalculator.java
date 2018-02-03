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
package org.blockartistry.DynSurround.client.handlers.fog;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.weather.Weather;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.lib.BlockStateProvider;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.WorldUtils;
import org.blockartistry.lib.math.MathStuff;

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

	@Override
	@Nonnull
	public Color calculate(@Nonnull final EntityViewRenderEvent.FogColors event) {

		// ForgeHooksClient.getSkyBlendColour()
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		final int[] ranges = ForgeModContainer.blendRanges;
		int distance = 6;
		if (settings.fancyGraphics && ranges.length > 0) {
			distance = ranges[MathStuff.clamp(settings.renderDistanceChunks, 0, ranges.length - 1)];
		}

		final EntityLivingBase player = EnvironState.getPlayer();
		final World world = EnvironState.getWorld();

		final int playerX = MathStuff.floor(player.posX);
		final int playerZ = MathStuff.floor(player.posZ);

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
		final BlockStateProvider provider = WorldUtils.getDefaultBlockStateProvider().setWorld(world);

		Color biomeFogColor = new Color(0, 0, 0);
		double weightBiomeFog = 0;

		for (int x = -distance; x <= distance; ++x) {
			for (int z = -distance; z <= distance; ++z) {
				pos.setPos(playerX + x, 0, playerZ + z);
				final BiomeInfo biome = ClientRegistry.BIOME.get(provider.getBiome(pos));

				final Color color;
				float weightPart = 1F;

				// Fetch the color we are dealing with.
				if (biome.getHasDust()) {
					color = biome.getDustColor();
				} else if (biome.getHasFog()) {
					color = biome.getFogColor();
				} else {
					color = null;
				}

				if (color != null) {
					biomeFogColor.add(color);
					weightBiomeFog += weightPart;
				}
			}
		}

		// If we have nothing then just return whatever Vanilla wanted
		if (weightBiomeFog == 0 || distance == 0)
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
		biomeFogColor.scale(
				//
				(float) (rScale / weightBiomeFog),
				//
				(float) (gScale / weightBiomeFog),
				//
				(float) (bScale / weightBiomeFog));

		final Color processedColor = applyPlayerEffects(world, player, biomeFogColor, partialTicks);

		final double weightMixed = (distance * 2 + 1) * (distance * 2 + 1);
		final double weightDefault = weightMixed - weightBiomeFog;
		final Color vanillaColor = super.calculate(event);

		processedColor.scale((float) weightBiomeFog);
		vanillaColor.scale((float) weightDefault);
		return processedColor.add(vanillaColor).scale((float) (1 / weightMixed));
	}

	protected Color applyPlayerEffects(@Nonnull final World world, @Nonnull final EntityLivingBase player,
			@Nonnull final Color biomeFogColor, final float renderPartialTicks) {
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
			biomeFogColor.scale(darkScale);
		}

		// EntityRenderer.updateFogColor() - If the player has nightvision going
		// need to lighten it a bit
		if (player.isPotionActive(MobEffects.NIGHT_VISION)) {
			final int duration = player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
			final float brightness = (duration > 200) ? 1
					: 0.7f + MathStuff.sin((float) ((duration - renderPartialTicks) * MathStuff.PI_F * 0.2f)) * 0.3f;

			float scale = 1 / biomeFogColor.red;
			scale = Math.min(scale, 1F / biomeFogColor.green);
			scale = Math.min(scale, 1F / biomeFogColor.blue);

			return biomeFogColor.scale((1F - brightness) + scale * brightness);
		}

		return biomeFogColor;
	}
}
