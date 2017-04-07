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

package org.blockartistry.mod.DynSurround.util;

import net.minecraft.world.World;

public class MinecraftClock {

	protected int day;
	protected int hour;
	protected int minute;
	protected boolean isAM;

	public MinecraftClock() {
		
	}
	
	public MinecraftClock(final World world) {
		long time = world.getWorldTime();
		this.day = (int) (time / 24000);
		time -= this.day * 24000;
		this.day++; // It's day 1, not 0 :)
		this.hour = (int) (time / 1000);
		time -= this.hour * 1000;
		this.minute = (int) (time / 16.666D);

		this.hour += 6;
		if (this.hour >= 24) {
			this.hour -= 24;
			this.day++;
		}
		
		if(this.hour >= 12) {
			this.isAM = false;
			if(this.hour > 12)
				this.hour -= 12;
		} else {
			this.isAM = true;
			if(this.hour == 0)
				this.hour = 12;
		}
	}
	
	public int getDay() {
		return this.day;
	}
	
	public int getHour() {
		return this.hour;
	}
	
	public int getMinute() {
		return this.minute;
	}
	
	public boolean isAM() {
		return this.isAM;
	}
	
	@Override
	public String toString() {
		return String.format("Day %d, %d:%02d %s", this.day, this.hour, this.minute, this.isAM ? "AM" : "PM");
	}
}
