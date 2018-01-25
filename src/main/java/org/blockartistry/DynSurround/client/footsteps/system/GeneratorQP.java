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

package org.blockartistry.DynSurround.client.footsteps.system;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.footsteps.implem.Variator;
import org.blockartistry.DynSurround.client.footsteps.interfaces.EventType;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GeneratorQP extends Generator {
	
	private final int USE_FUNCTION = 2;

	private int hoof = 0;
	private float nextWalkDistanceMultiplier = 0.05f;

	public GeneratorQP(@Nonnull final Isolator isolator, @Nonnull final Variator var) {
		super(isolator, var);
	}

	@Override
	protected void stepped(@Nonnull final EntityLivingBase ply, @Nonnull final EventType event) {
		if (this.hoof == 0 || this.hoof == 2) {
			this.nextWalkDistanceMultiplier = RANDOM.nextFloat();
		}

		if (this.hoof >= 3) {
			this.hoof = 0;
		} else {
			this.hoof++;
		}

		if (this.hoof == 3 && event == EventType.RUN) {
			produceStep(ply, event);
			this.hoof = 0;
		}

		if (event == EventType.WALK) {
			produceStep(ply, event);
		}
	}

	@Override
	protected float reevaluateDistance(@Nonnull final EventType event, @Nonnull final float distance) {
		float ret = distance;
		if (event == EventType.WALK) {
			if (this.USE_FUNCTION == 2) {
				final float overallMultiplier = 1.85f / 2;
				final float ndm = 0.2f;
				float pond = this.nextWalkDistanceMultiplier;
				pond *= pond;
				pond *= ndm;
				if (this.hoof == 1 || this.hoof == 3) {
					return ret * pond * overallMultiplier;
				}
				return ret * (1 - pond) * overallMultiplier;
			} else if (this.USE_FUNCTION == 1) {
				final float overallMultiplier = 1.4f;
				final float ndm = 0.5f;

				if (this.hoof == 1 || this.hoof == 3) {
					return ret * (ndm + this.nextWalkDistanceMultiplier * ndm * 0.5f) * overallMultiplier;
				}
				return ret * (1 - ndm) * overallMultiplier;
			} else if (this.USE_FUNCTION == 0) {
				final float overallMultiplier = 1.5f;
				final float ndm = 0.425f + nextWalkDistanceMultiplier * 0.15f;

				if (this.hoof == 1 || this.hoof == 3) {
					return ret * ndm * overallMultiplier;
				}
				return ret * (1 - ndm) * overallMultiplier;
			}
		}

		if (event == EventType.RUN && this.hoof == 0) {
			return ret * 0.8f;
		}

		if (event == EventType.RUN) {
			return ret * 0.3f;
		}

		return ret;
	}

}