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

package org.orecruncher.dsurround.profiling;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

/**
 * What /dsprofile sets up before it records: {@link #GENERAL} changes nothing, and the player plays as usual; the others
 * show an effect at its costliest, with server commands sent in single player only (on a server those would change the
 * world for everyone). They can't be read back from the client to put back, so the command says what it sent.
 */
public enum ProfileScene {

	GENERAL(3, ImmutableList.of()),
	AURORA(12, ImmutableList.of("time set 18000", "gamerule doDaylightCycle false", "weather clear 999999",
			"gamerule doWeatherCycle false")),
	WEATHER(6, ImmutableList.of("weather thunder 999999", "gamerule doWeatherCycle false", "ds setstr rain 100"));

	private final int settle;
	private final List<String> commands;

	ProfileScene(final int settle, @Nonnull final List<String> commands) {
		this.settle = settle;
		this.commands = commands;
	}

	/**
	 * Its name in the command
	 */
	@Nonnull
	public String commandName() {
		return name().toLowerCase(Locale.ROOT);
	}

	/**
	 * The translation key of what it shows (".what") and where to look while it records (".tip")
	 */
	@Nonnull
	String translationKey() {
		return "dsurround.profile.scene." + commandName();
	}

	/**
	 * Seconds to wait before recording, for the effect to fade in
	 */
	public int settle() {
		return this.settle;
	}

	/**
	 * The server commands it sends in single player (without the slash; they need cheats)
	 */
	@Nonnull
	public List<String> commands() {
		return this.commands;
	}
}
