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

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Implemented by an aurora so that it can go through it's life cycle.
 */
@SideOnly(Side.CLIENT)
public interface IAurora {

	/*
	 * Indicates if the aurora can be considered active
	 */
	boolean isAlive();
	
	/*
	 * Instructs the aurora to start the process of decay
	 * (i.e. start to fade)
	 */
	void setFading(final boolean flag);
	
	/*
	 * Indicates if the aurora is in the process of dying
	 */
	boolean isDying();
	
	/*
	 * Perform the necessary housekeeping for the aurora.  Occurs
	 * once a tick.
	 */
	void update();
	
	/*
	 * Indicates if an aurora as completed it's life cycle and can be removed.
	 */
	boolean isComplete();
	
	/*
	 * Render the aurora to the client screen.  It is possible that
	 * other updates can occur to the state, such as doing the
	 * transformations to animate.
	 */
	public void render(final float partialTick);
	
}
