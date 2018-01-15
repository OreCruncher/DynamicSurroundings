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

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AuroraUtils {

	private AuroraUtils() {
		
	}
	
	public static final int AURORA_PEAK_AGE = 512;
	public static final int AURORA_AGE_RATE = 1;
	
	/*
	 * The range in chunks of the player view.
	 */
	public static int getChunkRenderDistance() {
		return Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
	}
	
	/*
	 * Use cached dimension info to obtain the dimensions sealevel setting.
	 */
	public static int getSeaLevel() {
		return EnvironState.getDimensionInfo().getSeaLevel();
	}
	
	/*
	 * Returns a time calculation based on the number of ticks that have occured
	 * combined with the current partial tick count.  Not usable for actual time
	 * calculations.
	 */
	public static float getTimeSeconds() {
		return (EnvironState.getTickCounter() + EnvironState.getPartialTick()) / 20F;
	}
	
	/*
	 * Use cached dimension info to determine if auroras are possible for
	 * the dimension.
	 */
	public static boolean hasAuroras() {
		return EnvironState.getDimensionInfo().getHasAuroras();
	}
}
