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

package org.orecruncher.dsurround.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.weather.wind.Wind;
import org.orecruncher.dsurround.client.weather.wind.WindService;
import org.orecruncher.lib.Localization;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * /dsweather holds the weather's look for testing, as the 26.x line's command does; this line has only the wind to
 * hold. /dsweather wind shows the wind; wind speed 0-300 holds its speed, as a percent of a full storm's wind (a
 * thunderstorm or a dust storm blows harder); wind direction holds the way it blows toward, as degrees clockwise from
 * north or a compass point; wind clear, or /dsweather clear, lets it go its own way. Disconnecting clears it too. A
 * client command: the wind is the client's own.
 */
@SideOnly(Side.CLIENT)
public final class CommandDSWeather extends CommandBase {

	private static final String COMMAND = "dsweather";
	private static final String CLEAR = "clear";
	private static final String WIND = "wind";
	private static final String SPEED = "speed";
	private static final String DIRECTION = "direction";
	// A full storm's wind is 100: thunderstorms and dust storms blow harder
	private static final int MAX_WIND_PERCENT = 300;

	@Override
	@Nonnull
	public String getName() {
		return COMMAND;
	}

	@Override
	@Nonnull
	public String getUsage(@Nonnull final ICommandSender sender) {
		return "/" + COMMAND + " wind [speed 0-" + MAX_WIND_PERCENT + " | direction <0-360|"
				+ String.join("|", Wind.compassPoints()) + "> | clear] | /" + COMMAND + " clear";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public boolean checkPermission(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender) {
		return true;
	}

	@Override
	public void execute(@Nullable final MinecraftServer server, @Nonnull final ICommandSender sender,
			@Nonnull final String[] args) throws CommandException {
		final Wind wind = WindService.wind();
		final String message;
		if (args.length == 1 && CLEAR.equalsIgnoreCase(args[0])) {
			message = clear(wind);
		} else if (args.length == 1 && WIND.equalsIgnoreCase(args[0])) {
			message = status(wind);
		} else if (args.length == 2 && WIND.equalsIgnoreCase(args[0]) && CLEAR.equalsIgnoreCase(args[1])) {
			message = clear(wind);
		} else if (args.length == 3 && WIND.equalsIgnoreCase(args[0]) && SPEED.equalsIgnoreCase(args[1])) {
			final int percent = parseInt(args[2], 0, MAX_WIND_PERCENT);
			wind.setSpeed(Wind.speedOfPercent(percent));
			message = Localization.format("dsurround.msg.dsweather.wind.speed", percent);
		} else if (args.length == 3 && WIND.equalsIgnoreCase(args[0]) && DIRECTION.equalsIgnoreCase(args[1])) {
			final float bearing = bearingOf(args[2]);
			wind.setBearing(bearing);
			message = Localization.format("dsurround.msg.dsweather.wind.direction", Wind.compassPoint(bearing),
					Math.round(bearing));
		} else {
			throw new WrongUsageException(getUsage(sender));
		}
		sender.sendMessage(new TextComponentString(message));
	}

	@Nonnull
	private static String status(@Nonnull final Wind wind) {
		if (WindService.isStill())
			return Localization.format("dsurround.msg.dsweather.wind.still");
		final String key = wind.isSet() ? "dsurround.msg.dsweather.wind.status.set"
				: "dsurround.msg.dsweather.wind.status";
		return Localization.format(key, Wind.percentOfFullStorm(wind.speed()), Wind.compassPoint(wind.bearing()),
				Math.round(wind.bearing()));
	}

	@Nonnull
	private static String clear(@Nonnull final Wind wind) {
		if (!wind.isSet())
			return Localization.format("dsurround.msg.dsweather.wind.clear.notset");
		wind.clearSet();
		return Localization.format("dsurround.msg.dsweather.wind.clear");
	}

	// Degrees clockwise from north, or a compass point
	private static float bearingOf(@Nonnull final String text) throws CommandException {
		final String point = text.toUpperCase(Locale.ROOT);
		if (Wind.compassPoints().contains(point))
			return Wind.bearingOf(point);
		return parseInt(text, 0, 360) % 360;
	}

	@Override
	@Nonnull
	public List<String> getTabCompletions(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender,
			@Nonnull final String[] args, @Nullable final BlockPos targetPos) {
		if (args.length == 1)
			return getListOfStringsMatchingLastWord(args, WIND, CLEAR);
		if (args.length == 2 && WIND.equalsIgnoreCase(args[0]))
			return getListOfStringsMatchingLastWord(args, SPEED, DIRECTION, CLEAR);
		if (args.length == 3 && WIND.equalsIgnoreCase(args[0]) && DIRECTION.equalsIgnoreCase(args[1]))
			return getListOfStringsMatchingLastWord(args, new ArrayList<>(Wind.compassPoints()));
		return Collections.emptyList();
	}
}
