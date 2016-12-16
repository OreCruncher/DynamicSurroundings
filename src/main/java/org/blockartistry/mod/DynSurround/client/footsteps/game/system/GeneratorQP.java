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

import java.util.Random;

import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IIsolator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GeneratorQP extends Generator {
	private int hoof = 0;
	private int USE_FUNCTION = 2;
	private float nextWalkDistanceMultiplier = 0.05f;
	private final Random rand = new Random();
	
	public GeneratorQP(IIsolator isolator) {
		super(isolator);
	}
	
	@Override
	protected void stepped(final EntityPlayer ply, final EventType event) {
		if (hoof == 0 || hoof == 2) {
			nextWalkDistanceMultiplier = rand.nextFloat();
		}
		
		if (hoof >= 3) {
			hoof = 0;
		} else {
			hoof++;
		}
		
		if (hoof == 3 && event == EventType.RUN) {
			produceStep(ply, event);
			hoof = 0;
		}
		
		if (event == EventType.WALK) {
			produceStep(ply, event);
		}
	}
	
	@Override
	protected float reevaluateDistance(final EventType event, final float distance) {
		float ret = distance;
		if (event == EventType.WALK) {
			if (USE_FUNCTION == 2) {
				final float overallMultiplier = 1.85f / 2;
				final float ndm = 0.2f;
				
				/*if (this.hoof == 1 || this.hoof == 3)
					return this.nextWalkDistanceMultiplier * 0.5f;
				else
					return ret * (1 - ndm) * overallMultiplier;*/
				float pond = nextWalkDistanceMultiplier;
				pond *= pond;
				pond *= ndm;
				if (hoof == 1 || hoof == 3) {
					return ret * pond * overallMultiplier;
				}
				return ret * (1 - pond) * overallMultiplier;
			} else if (USE_FUNCTION == 1) {
				final float overallMultiplier = 1.4f;
				final float ndm = 0.5f;
				
				if (hoof == 1 || hoof == 3) {
					return ret * (ndm + nextWalkDistanceMultiplier * ndm * 0.5f) * overallMultiplier;
				}
				return ret * (1 - ndm) * overallMultiplier;
			} else if (USE_FUNCTION == 0) {
				final float overallMultiplier = 1.5f;
				final float ndm = 0.425f + nextWalkDistanceMultiplier * 0.15f;
				
				if (hoof == 1 || hoof == 3) {
					return ret * ndm * overallMultiplier;
				}
				return ret * (1 - ndm) * overallMultiplier;
			}
		}
		
		if (event == EventType.RUN && hoof == 0) {
			return ret * 0.8f;
		}
		
		if (event == EventType.RUN) {
			return ret * 0.3f;
		}
		
		return ret;
	}
	
}
