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

package org.blockartistry.mod.DynSurround.client.footsteps.game.system;

import org.blockartistry.mod.DynSurround.client.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ILibrary;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IBlockMap;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IStepPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IGenerator;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IGeneratorSettable;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IIsolator;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IPrimitiveMap;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.ISolver;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IVariator;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IVariatorSettable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Isolator implements IIsolator, IVariatorSettable, IGeneratorSettable {
	private ILibrary acoustics;
	private ISolver solver;
	private IBlockMap blockMap;
	private IPrimitiveMap primitiveMap;
	private ISoundPlayer soundPlayer;
	private IStepPlayer defaultStepPlayer;

	private IVariator VAR;

	private IGenerator generator;

	public Isolator() {
	}

	@Override
	public void onFrame() {
		if (this.generator == null)
			return;

		this.generator.generateFootsteps(EnvironState.getPlayer());
		this.acoustics.think();
	}

	//

	@Override
	public ILibrary getAcoustics() {
		return this.acoustics;
	}

	@Override
	public ISolver getSolver() {
		return this.solver;
	}

	@Override
	public IBlockMap getBlockMap() {
		return this.blockMap;
	}

	@Override
	public IPrimitiveMap getPrimitiveMap() {
		return this.primitiveMap;
	}

	@Override
	public ISoundPlayer getSoundPlayer() {
		return this.soundPlayer;
	}

	@Override
	public IStepPlayer getDefaultStepPlayer() {
		return this.defaultStepPlayer;
	}

	//

	@Override
	public void setAcoustics(final ILibrary acoustics) {
		this.acoustics = acoustics;
	}

	@Override
	public void setSolver(final ISolver solver) {
		this.solver = solver;
	}

	@Override
	public void setBlockMap(final IBlockMap blockMap) {
		this.blockMap = blockMap;
	}

	@Override
	public void setPrimitiveMap(final IPrimitiveMap primitiveMap) {
		this.primitiveMap = primitiveMap;
	}

	@Override
	public void setSoundPlayer(final ISoundPlayer soundPlayer) {
		this.soundPlayer = soundPlayer;
	}

	@Override
	public void setDefaultStepPlayer(final IStepPlayer defaultStepPlayer) {
		this.defaultStepPlayer = defaultStepPlayer;
	}

	//

	@Override
	public void setVariator(final IVariator var) {
		this.VAR = var;
		fixVariator(this.generator);
	}

	//

	@Override
	public void setGenerator(final IGenerator generator) {
		this.generator = generator;
		fixVariator(this.generator);
	}

	/**
	 * Propagate variators.
	 * 
	 * @param possiblyAVariator
	 */
	private void fixVariator(final Object possiblyAVariator) {
		if (possiblyAVariator == null)
			return;

		if (possiblyAVariator instanceof IVariatorSettable) {
			((IVariatorSettable) possiblyAVariator).setVariator(this.VAR);
		}
	}
}
