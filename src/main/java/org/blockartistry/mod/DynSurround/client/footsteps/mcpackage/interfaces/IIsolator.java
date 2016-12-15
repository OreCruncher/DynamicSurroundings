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

package org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces;

import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ILibrary;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem.BlockMap;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IIsolator
{
	
	public abstract void onFrame();
	
	public abstract ILibrary getAcoustics();
	
	public abstract ISolver getSolver();
	
	public abstract BlockMap getBlockMap();
	
	public abstract IPrimitiveMap getPrimitiveMap();
	
	public abstract ISoundPlayer getSoundPlayer();
	
	public abstract IStepPlayer getDefaultStepPlayer();
	
	//
	
	public abstract void setAcoustics(final ILibrary acoustics);
	
	public abstract void setSolver(final ISolver solver);
	
	public abstract void setBlockMap(final BlockMap blockMap);
	
	public abstract void setPrimitiveMap(final IPrimitiveMap primitiveMap);
	
	public abstract void setSoundPlayer(final ISoundPlayer soundPlayer);
	
	public abstract void setDefaultStepPlayer(final IStepPlayer defaultStepPlayer);
	
}