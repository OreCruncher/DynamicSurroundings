/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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
package org.blockartistry.lib.effects;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.data.xface.EntityConfig;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityEffectInfo {
	
	public final String effects;
	public final float stepVolume;
	public final int footprintStyle;
	
	public EntityEffectInfo() {
		this.effects = StringUtils.EMPTY;
		this.stepVolume = 1.0F;
		this.footprintStyle = 6;
	}
	
	public EntityEffectInfo(@Nonnull final EntityConfig ec) {
		this.effects = ec.effects;
		this.stepVolume = ec.stepVolume;
		this.footprintStyle = 6;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("effects: ").append(this.effects).append(", stepVolume: ")
				.append(this.stepVolume).toString();
	}

}
