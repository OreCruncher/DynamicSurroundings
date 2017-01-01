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

package org.blockartistry.mod.DynSurround.registry.season;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.registry.SeasonType;

import foxie.calendar.api.CalendarAPI;
import foxie.calendar.api.ICalendarProvider;
import foxie.calendar.api.ISeasonProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeasonInfoCalendar extends SeasonInfo {

	private final ICalendarProvider calendarProvider;
	private final ISeasonProvider seasonProvider;

	public SeasonInfoCalendar(@Nonnull final World world) {
		super(world);

		this.seasonProvider = CalendarAPI.getSeasonProvider(this.world.provider.getDimension());
		this.calendarProvider = CalendarAPI.getCalendarInstance(this.world);
	}

	@Override
	@Nonnull
	public SeasonType getSeasonType() {
		final String name = this.seasonProvider.getSeason(this.calendarProvider).getName();
		switch (name) {
		case "summer":
			return SeasonType.SUMMER;
		case "autumn":
			return SeasonType.AUTUMN;
		case "winter":
			return SeasonType.WINTER;
		case "spring":
			return SeasonType.SPRING;
		default:
			return SeasonType.NONE;
		}

	}

	@Override
	public float getTemperature(@Nonnull final BlockPos pos) {
		return this.seasonProvider.getTemperature(this.world, pos.getX(), pos.getY(), pos.getZ());
	}

}
