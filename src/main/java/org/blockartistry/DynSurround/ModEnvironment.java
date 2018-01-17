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

package org.blockartistry.DynSurround;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.Loader;

/**
 * Helper enum to track mods that Dynamic Surroundings is interested in.
 */
public enum ModEnvironment {

	//
	ToughAsNails("ToughAsNails"),
	//
	CalendarAPI("CalendarAPI"),
	//
	Weather2("weather2"),
	//
	EnderIO("EnderIO"),
	//
	Chisel("chisel"),
	//
	ChiselAPI("ctm-api"),
	//
	OpenTerrainGenerator("openterraingenerator"),
	//
	ActualMusic("actualmusic"),
	//
	GalacticraftCore("galacticraftcore"),
	//
	GalacticraftPlanets("galacticraftplanets"),
	//
	CoFHCore("cofhcore"),
	//
	SoundPhysics("soundphysics");
	
	protected final String modId;
	protected boolean isLoaded;

	private ModEnvironment(@Nonnull final String modId) {
		this.modId = modId;
	}

	public boolean isLoaded() {
		return this.isLoaded;
	}

	public static void initialize() {
		for (final ModEnvironment me : ModEnvironment.values())
			me.isLoaded = Loader.isModLoaded(me.modId);
	}

}
