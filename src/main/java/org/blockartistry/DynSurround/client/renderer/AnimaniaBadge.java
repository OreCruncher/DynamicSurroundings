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

package org.blockartistry.DynSurround.client.renderer;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// There are two styles of interface available with Animania.  The classic one
// depends on crawling the entity lists matching various critters.  The other
// uses a standard interface.

@SideOnly(Side.CLIENT)
public final class AnimaniaBadge {
	
	private AnimaniaBadge() {
		
	}
	
	
	public static void intitialize() {
		if (!ModOptions.speechbubbles.enableAnimaniaBadges)
			return;
		
		boolean useNew = false;
		
		try {
			// Will throw exception if the interface is not present
			Class.forName("com.animania.common.entities.interfaces.IFoodEating");
			useNew = true;
		} catch(@Nonnull final Throwable t) {
			;
		}
		
		if (useNew)
			AnimaniaBadge2.intitialize();
		else
			AnimaniaBadge1.intitialize();
	}
}
