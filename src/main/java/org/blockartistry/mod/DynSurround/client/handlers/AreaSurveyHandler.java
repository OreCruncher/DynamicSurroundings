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

package org.blockartistry.mod.DynSurround.client.handlers;

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.registry.BiomeInfo;
import org.blockartistry.mod.DynSurround.registry.BiomeRegistry;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AreaSurveyHandler extends ClientEffectBase {

	private static final int BIOME_SURVEY_RANGE = 6;
	private static final int INSIDE_SURVEY_RANGE = 3;
	private static final int INSIDE_AREA = (INSIDE_SURVEY_RANGE * 2 + 1) * (INSIDE_SURVEY_RANGE * 2 + 1);

	// Used to throttle processing
	private static final int SURVEY_INTERVAL = 2;
	private static int intervalTicker = SURVEY_INTERVAL;
	
	private static int biomeArea;
	private static final TObjectIntHashMap<BiomeInfo> weights = new TObjectIntHashMap<BiomeInfo>();

	// "Finger print" of the last area survey.
	private static BiomeInfo surveyedBiome = null;
	private static int surveyedDimension = 0;
	private static BlockPos surveyedPosition = BlockPos.ORIGIN;

	private static float ceilingCoverageRatio = 0.0F;

	public static int getBiomeArea() {
		return biomeArea;
	}

	public static float getCeilingCoverageRatio() {
		return ceilingCoverageRatio;
	}

	public static boolean isReallyInside() {
		return ceilingCoverageRatio > 0.42F;
	}

	public static TObjectIntHashMap<BiomeInfo> getBiomes() {
		return weights;
	}

	private static void doCeilingCoverageRatio() {
		final World world = EnvironState.getWorld();
		final BlockPos position = EnvironState.getPlayerPosition();
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		final int targetY = position.getY();
		int seeSky = 0;
		for (int x = -INSIDE_SURVEY_RANGE; x <= INSIDE_SURVEY_RANGE; x++)
			for (int z = -INSIDE_SURVEY_RANGE; z <= INSIDE_SURVEY_RANGE; z++) {
				pos.setPos(x + position.getX(), 0, z + position.getZ());
				final int y = world.getTopSolidOrLiquidBlock(pos).getY();
				if ((y - targetY) < 3)
					++seeSky;
			}
		ceilingCoverageRatio = 1.0F - ((float) seeSky / INSIDE_AREA);
	}

	/*
	 * Perform a biome survey around the player at the specified range.
	 */
	private static void doSurvey() {
		biomeArea = 0;
		weights.clear();

		if (EnvironState.getPlayerBiome().isFake()) {
			biomeArea = 1;
			weights.put(EnvironState.getPlayerBiome(), 1);
		} else {
			final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			final int x = surveyedPosition.getX();
			final int z = surveyedPosition.getZ();

			for (int dX = -BIOME_SURVEY_RANGE; dX <= BIOME_SURVEY_RANGE; dX++)
				for (int dZ = -BIOME_SURVEY_RANGE; dZ <= BIOME_SURVEY_RANGE; dZ++) {
					biomeArea++;
					pos.setPos(x + dX, 0, z + dZ);
					final BiomeInfo biome = BiomeRegistry.get(EnvironState.getWorld().getBiome(pos));
					weights.adjustOrPutValue(biome, 1, 1);
				}
		}
	}

	// Analyzes the area around the player and caches the results.
	// Generally it is called once a tick.
	@Override
	public void process(final World world, final EntityPlayer player) {
		// Only process on the correct interval
		intervalTicker++;
		if(intervalTicker < SURVEY_INTERVAL)
			return;
		intervalTicker = 0;
		
		final BlockPos position = EnvironState.getPlayerPosition();

		if (surveyedBiome != EnvironState.getPlayerBiome() || surveyedDimension != EnvironState.getDimensionId()
				|| surveyedPosition.compareTo(position) != 0) {
			surveyedBiome = EnvironState.getPlayerBiome();
			surveyedDimension = EnvironState.getDimensionId();
			surveyedPosition = position;
			doSurvey();
		}

		doCeilingCoverageRatio();
	}

	@Override
	public void onConnect() {
		intervalTicker = SURVEY_INTERVAL;
	}
}
