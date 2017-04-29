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

package org.blockartistry.DynSurround.api.events;

import javax.annotation.Nonnull;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fires when there is an update to Dynamic Surroundings weather effects. The
 * event will only fire client side.
 */
public class WeatherUpdateEvent extends Event {

	/**
	 * The world for which this event is intended.
	 */
	public final World world;

	/**
	 * The current rain intensity that is being set. 0 means no intensity, and
	 * 1.0F means full.
	 */
	public final float rainIntensity;

	/**
	 * The max possible intensity of the storm. 0 means none, and 1.0F means
	 * full.
	 */
	public final float maxRainIntensity;

	/**
	 * Number of ticks until the next rain change, either starting or stopping.
	 */
	public final int nextRainChange;

	/**
	 * Current strength of thunder, 0 means none, 1.0F means full.
	 */
	public final float thunderStrength;

	/**
	 * Number of ticks until the next thunder change, either starting or
	 * stopping.
	 */
	public final int nextThunderChange;

	/**
	 * Number of ticks until the next background thunder event.
	 */
	public final int nextThunderEvent;

	public WeatherUpdateEvent(@Nonnull final World world, final float rainIntensity, final float maxRainIntensity,
			final int nextRainChange, final float thunderStrength, final int nextThunderChange,
			final int nextThunderEvent) {
		this.world = world;
		this.maxRainIntensity = MathHelper.clamp(maxRainIntensity, 0, 1.0F);
		this.rainIntensity = MathHelper.clamp(rainIntensity, 0.0F, this.maxRainIntensity);
		this.nextRainChange = nextRainChange;
		this.thunderStrength = thunderStrength;
		this.nextThunderChange = nextThunderChange;
		this.nextThunderEvent = nextThunderEvent;
	}

}
