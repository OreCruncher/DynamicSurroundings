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

import org.blockartistry.DynSurround.client.ClientChunkCache;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.weather.Weather;
import org.blockartistry.DynSurround.registry.BiomeInfo;
import org.blockartistry.lib.chunk.IBlockAccessEx;
import org.blockartistry.lib.math.MathStuff;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Scans the biome area around the player to determine the fog parameters.
 */
@SideOnly(Side.CLIENT)
public class BiomeFogRangeCalculator extends VanillaFogRangeCalculator {

	protected static final int DISTANCE = 20;
	protected static final float DUST_FOG_IMPACT = 0.9F;

	private static class Context {
		public int posX;
		public int posZ;
		public float rain;
		public float lastFarPlane;
		public boolean doScan = true;
		public final FogResult cached = new FogResult();

		public boolean returnCached(final int pX, final int pZ, final float r,
				@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {
			return !this.doScan && pX == this.posX && pZ == this.posZ && r == this.rain
					&& this.lastFarPlane == event.getFarPlaneDistance() && this.cached.isValid(event);
		}
	}

	protected final Context[] context = { new Context(), new Context() };

	public BiomeFogRangeCalculator() {

	}

	@Override
	@Nonnull
	public FogResult calculate(@Nonnull final EntityViewRenderEvent.RenderFogEvent event) {

		final EntityLivingBase player = EnvironState.getPlayer();
		final IBlockAccessEx provider = ClientChunkCache.INSTANCE;
		final int playerX = MathStuff.floor(player.posX);
		final int playerZ = MathStuff.floor(player.posZ);
		final float rainStr = Weather.getIntensityLevel();

		final Context ctx = this.context[event.getFogMode() == -1 ? 0 : 1];

		if (ctx.returnCached(playerX, playerZ, rainStr, event))
			return ctx.cached;

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);

		float fpDistanceBiomeFog = 0F;
		float weightBiomeFog = 0;

		final boolean isRaining = Weather.isRaining();
		ctx.rain = rainStr;
		ctx.doScan = false;

		for (int x = -DISTANCE; x <= DISTANCE; ++x) {
			for (int z = -DISTANCE; z <= DISTANCE; ++z) {
				pos.setPos(playerX + x, 0, playerZ + z);

				// If the chunk is not available doScan will be set true. This will force
				// another scan on the next tick.
				ctx.doScan = ctx.doScan | !provider.isAvailable(pos);
				final BiomeInfo biome = ClientRegistry.BIOME.get(provider.getBiome(pos));

				float distancePart = 1F;
				final float weightPart = 1;

				if (isRaining && biome.getHasDust()) {
					distancePart = 1F - DUST_FOG_IMPACT * rainStr;
				} else if (biome.getHasFog()) {
					distancePart = biome.getFogDensity();
				}

				fpDistanceBiomeFog += distancePart;
				weightBiomeFog += weightPart;
			}
		}

		final float weightMixed = (DISTANCE * 2 + 1) * (DISTANCE * 2 + 1);
		final float weightDefault = weightMixed - weightBiomeFog;

		final float fpDistanceBiomeFogAvg = (weightBiomeFog == 0) ? 0 : fpDistanceBiomeFog / weightBiomeFog;

		final float rangeConst = Math.max(240, event.getFarPlaneDistance() - 16);
		float farPlaneDistance = (fpDistanceBiomeFog * rangeConst + event.getFarPlaneDistance() * weightDefault) / weightMixed;
		final float farPlaneDistanceScaleBiome = (0.1f * (1 - fpDistanceBiomeFogAvg) + 0.75f * fpDistanceBiomeFogAvg);
		final float farPlaneDistanceScale = (farPlaneDistanceScaleBiome * weightBiomeFog + 0.75f * weightDefault)
				/ weightMixed;

		ctx.posX = playerX;
		ctx.posZ = playerZ;
		ctx.lastFarPlane = event.getFarPlaneDistance();
		farPlaneDistance = Math.min(farPlaneDistance, event.getFarPlaneDistance());

		ctx.cached.set(event.getFogMode(), farPlaneDistance, farPlaneDistanceScale);

		return ctx.cached;
	}
}
