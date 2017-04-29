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

package org.blockartistry.mod.DynSurround.client.fx.particle.mote;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoteRainSplash extends MoteWaterSpray {

	public MoteRainSplash(final World world, final double x, final double y, final double z) {
		super(world, x, y, z, 0, 0, 0);

		// Setup motion
		this.motionX = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		this.motionY = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		this.motionZ = (RANDOM.nextDouble() * 2.0D - 1.0D) * 0.4000000059604645D;
		float f = (float) (RANDOM.nextDouble() + RANDOM.nextDouble() + 1.0D) * 0.15F;
		float f1 = MathHelper
				.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
		this.motionX = this.motionX / (double) f1 * (double) f * 0.4000000059604645D;
		this.motionY = this.motionY / (double) f1 * (double) f * 0.4000000059604645D + 0.10000000149011612D;
		this.motionZ = this.motionZ / (double) f1 * (double) f * 0.4000000059604645D;

		this.motionX *= 0.30000001192092896D;
		this.motionY = RANDOM.nextDouble() * 0.20000000298023224D + 0.10000000149011612D;
		this.motionZ *= 0.30000001192092896D;

	}

}
