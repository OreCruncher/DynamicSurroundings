/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
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
package org.blockartistry.DynSurround.client.weather.compat;

import java.lang.reflect.Method;

import org.blockartistry.DynSurround.DSurround;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Hook to check RandomThings in support of the Rain Shield
 */
@SideOnly(Side.CLIENT)
public final class RandomThings {

	private RandomThings() {

	}

	private static Method shouldRain;

	static {

		try {
			final Class<?> rtClass = Class.forName("lumien.randomthings.tileentity.TileEntityRainShield");
			shouldRain = ReflectionHelper.findMethod(rtClass, "shouldRain", null, World.class, BlockPos.class);
			if (shouldRain != null) {
				DSurround.log().info("RandomThings rain shield detected!");
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean shouldRain(final World world, final BlockPos pos) {
		if (shouldRain == null)
			return true;

		try {
			return (boolean) shouldRain.invoke(null, world, pos);
		} catch (final Exception ex) {
			ex.printStackTrace();
			DSurround.log().warn("Exception checking rain shield!");
			shouldRain = null;
		}

		return true;
	}
}
