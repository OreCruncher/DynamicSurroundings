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

package org.blockartistry.mod.DynSurround.api.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event raised when an aurora spawns.  This event will only fire
 * client side.
 * 
 * Can be canceled.
 */
@Cancelable
public class AuroraSpawnEvent extends Event {
	
	/**
	 * Dimension for which this event is intended.
	 */
	public final int dimensionId;
	
	/**
	 * Location of the Aurora
	 */
	public final int posX;
	public final int posZ;
	
	/**
	 * Random seed to use when generating the Aurora visuals.
	 */
	public final long seed;
	
	/**
	 * Which color set to apply when rendering the Aurora.
	 * 
	 * @see org.blockartistry.mod.DynSurround.data.ColorPair
	 */
	public final int colorSet;
	
	/**
	 * Geometry preset of the Aurora.
	 * 
	 * @see org.blockartistry.mod.DynSurround.data.AuroraPreset
	 */
	public final int preset;
	
	public AuroraSpawnEvent(final int dimensionId, final int x, final int z, final long seed, final int colorSet, final int preset) {
		this.dimensionId = dimensionId;
		this.posX = x;
		this.posZ = z;
		this.seed = seed;
		this.colorSet = colorSet;
		this.preset = preset;
		
	}

}
