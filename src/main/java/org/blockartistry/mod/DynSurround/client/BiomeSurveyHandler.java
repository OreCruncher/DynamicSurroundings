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

package org.blockartistry.mod.DynSurround.client;

import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.data.BiomeInfo;
import org.blockartistry.mod.DynSurround.data.BiomeRegistry;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class BiomeSurveyHandler implements IClientEffectHandler {

	private static final int BIOME_SURVEY_RANGE = 6;

	private static int area;
	private static final TObjectIntHashMap<BiomeInfo> weights = new TObjectIntHashMap<BiomeInfo>();

	private static BiomeInfo lastPlayerBiome = null;
	private static int lastDimension = 0;
	private static int lastPlayerX = 0;
	private static int lastPlayerY = 0;
	private static int lastPlayerZ = 0;

	public static int getArea() {
		return area;
	}

	public static TObjectIntHashMap<BiomeInfo> getBiomes() {
		return weights;
	}

	/*
	 * Perform a biome survey around the player at the specified range.
	 */
	public static void doSurvey(final EntityPlayer player, final int range) {
		area = 0;
		weights.clear();
		
		if (EnvironState.getPlayerBiome().isFake()) {
			area = 1;
			weights.put(EnvironState.getPlayerBiome(), 1);
		} else {
			final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			final int x = MathHelper.floor_double(player.posX);
			final int z = MathHelper.floor_double(player.posZ);

			for (int dX = -range; dX <= range; dX++)
				for (int dZ = -range; dZ <= range; dZ++) {
					area++;
					pos.setPos(x + dX, 0, z + dZ);
					final BiomeInfo biome = BiomeRegistry.get(player.worldObj.getBiome(pos));
					weights.adjustOrPutValue(biome, 1, 1);
				}
		}
	}

	@Override
	public void process(final World world, final EntityPlayer player) {
		final int playerX = MathHelper.floor_double(player.posX);
		final int playerY = MathHelper.floor_double(player.posY);
		final int playerZ = MathHelper.floor_double(player.posZ);

		if (lastDimension != EnvironState.getDimensionId() || playerX != lastPlayerX || playerY != lastPlayerY
				|| playerZ != lastPlayerZ || lastPlayerBiome != EnvironState.getPlayerBiome()) {
			lastPlayerBiome = EnvironState.getPlayerBiome();
			lastDimension = EnvironState.getDimensionId();
			lastPlayerX = playerX;
			lastPlayerY = playerY;
			lastPlayerZ = playerZ;
			doSurvey(player, BIOME_SURVEY_RANGE);
		}
	}

	@Override
	public boolean hasEvents() {
		return false;
	}

}
