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

package org.blockartistry.lib;

import javax.annotation.Nonnull;

import org.blockartistry.lib.DiurnalUtils.DayCycle;

import net.minecraft.world.World;

public class MinecraftClock {

	private static final String AM = Localization.format("dsurround.format.AM");
	private static final String PM = Localization.format("dsurround.format.PM");
	private static final String NO_SKY = Localization.format("dsurround.format.NoSky");
	private static final String SUNRISE = Localization.format("dsurround.format.Sunrise");
	private static final String SUNSET = Localization.format("dsurround.format.Sunset");
	private static final String DAYTIME = Localization.format("dsurround.format.Daytime");
	private static final String NIGHTTIME = Localization.format("dsurround.format.Nighttime");
	private static final String TIME_FORMAT = Localization.loadString("dsurround.format.TimeOfDay");

	protected int day;
	protected int hour;
	protected int minute;
	protected boolean isAM;
	protected DayCycle cycle = DayCycle.DAYTIME;

	public MinecraftClock() {

	}

	public MinecraftClock(@Nonnull final World world) {
		update(world);
	}

	public void update(@Nonnull final World world) {

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

		this.isAM = this.hour < 12;

		this.cycle = DiurnalUtils.getCycle(world);
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

	public String getTimeOfDay() {
		switch (this.cycle) {
		case NO_SKY:
			return MinecraftClock.NO_SKY;
		case SUNRISE:
			return MinecraftClock.SUNRISE;
		case SUNSET:
			return MinecraftClock.SUNSET;
		case DAYTIME:
			return MinecraftClock.DAYTIME;
		default:
			return MinecraftClock.NIGHTTIME;
		}
	}

	public String getFormattedTime() {
		return String.format(TIME_FORMAT, this.day, this.hour > 12 ? this.hour - 12 : this.hour, this.minute,
				this.isAM ? AM : PM);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		return builder.append('[').append(this.getFormattedTime()).append('.').append(this.getTimeOfDay()).append(']')
				.toString();
	}
}
