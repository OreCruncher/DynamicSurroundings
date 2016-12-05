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

package org.blockartistry.mod.DynSurround.client.footsteps.engine.implem;

import java.util.ArrayList;
import java.util.List;

import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProbabilityWeightsAcoustic implements IAcoustic {
	protected final List<IAcoustic> acoustics;
	protected final int[] weights;
	protected final int totalWeight;

	public ProbabilityWeightsAcoustic(final List<IAcoustic> acoustics, final List<Integer> weights) {
		this.acoustics = new ArrayList<IAcoustic>(acoustics);
		this.weights = new int[weights.size()];

		int tWeight = 0;
		for (int i = 0; i < weights.size(); i++) {
			this.weights[i] = weights.get(i).intValue();
			tWeight += this.weights[i];
		}

		this.totalWeight = tWeight;
	}

	@Override
	public void playSound(final ISoundPlayer player, final Object location, final EventType event,
			final IOptions inputOptions) {
		if (this.totalWeight <= 0)
			return;

		int targetWeight = player.getRNG().nextInt(this.totalWeight);

		int i = 0;
		for (i = this.weights.length; (targetWeight -= this.weights[i - 1]) >= 0; i--)
			;

		this.acoustics.get(i - 1).playSound(player, location, event, inputOptions);
	}
}
