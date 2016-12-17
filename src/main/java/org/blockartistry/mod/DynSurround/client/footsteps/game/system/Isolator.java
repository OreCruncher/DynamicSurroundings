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

import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem.AcousticsManager;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem.BlockMap;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem.PrimitiveMap;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IStepPlayer;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IIsolator;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.ISolver;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Isolator implements IIsolator {
	private AcousticsManager acoustics;
	private ISolver solver;
	private BlockMap blockMap;
	private PrimitiveMap primitiveMap;
	private ISoundPlayer soundPlayer;
	private IStepPlayer defaultStepPlayer;

	private Generator generator;

	public Isolator() {
		this.blockMap = new BlockMap(this);
	}

	@Override
	public void onFrame() {
		if (this.generator == null)
			return;

		this.generator.generateFootsteps(EnvironState.getPlayer());
		this.acoustics.think();
	}

	//

	public AcousticsManager getAcoustics() {
		return this.acoustics;
	}

	@Override
	public ISolver getSolver() {
		return this.solver;
	}

	@Override
	public BlockMap getBlockMap() {
		return this.blockMap;
	}

	@Override
	public PrimitiveMap getPrimitiveMap() {
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

	public void setAcoustics(final AcousticsManager acoustics) {
		this.acoustics = acoustics;
	}

	@Override
	public void setSolver(final ISolver solver) {
		this.solver = solver;
	}

	@Override
	public void setBlockMap(final BlockMap blockMap) {
		this.blockMap = blockMap;
	}

	@Override
	public void setPrimitiveMap(final PrimitiveMap primitiveMap) {
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

	public void setGenerator(final Generator generator) {
		this.generator = generator;
	}
}
