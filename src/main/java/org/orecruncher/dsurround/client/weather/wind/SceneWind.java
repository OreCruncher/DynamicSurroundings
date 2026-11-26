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

package org.orecruncher.dsurround.client.weather.wind;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The wind around the player, for effects to lean with: smoke, steam, breath, flames. Set each tick by the
 * {@link WindService}; read by particles, which are made far from anything the service could be handed to. Before the
 * first tick, and out of the game, it is still air. Ported from the 26.x line.
 * <p>
 * Effects lean only as far as they are out in the weather ({@link Shelter#exposure}): a cave or a building keeps the
 * wind off. Client thread only, which in Minecraft is also the render thread.
 */
public final class SceneWind {

	/**
	 * How much of the wind's speed something light, like smoke or steam, is carried at, once it has caught it
	 */
	public static final float CARRIED_SHARE = 0.5F;
	// The share of the gap to the wind a drift closes each tick: what the mod's smoke loses to its drag (0.93)
	static final float CATCH_RATE = 0.07F;
	private static Wind wind;
	private static double originX;
	private static double originZ;

	private SceneWind() {
	}

	/**
	 * The wind now, and where the player is: a gust passes across the area from upwind of them
	 */
	static void update(@Nonnull final Wind current, final double cameraX, final double cameraZ) {
		wind = current;
		originX = cameraX;
		originZ = cameraZ;
	}

	/**
	 * Still air: out of the game, or where there is no weather
	 */
	static void clear() {
		wind = null;
	}

	/**
	 * The wind at a place, east-west and north-south, in blocks a tick: the steady wind, stronger where a gust is
	 * passing
	 */
	public static float x(final double x, final double z) {
		final Wind current = wind;
		return current == null ? 0F : current.x() * gustAt(current, x, z);
	}

	public static float z(final double x, final double z) {
		final Wind current = wind;
		return current == null ? 0F : current.z() * gustAt(current, x, z);
	}

	private static float gustAt(@Nullable final Wind current, final double x, final double z) {
		final float along = (float) ((x - originX) * current.directionX() + (z - originZ) * current.directionZ());
		return current.gustFactor(along, 0F);
	}

	/**
	 * The push a tick that carries something with the given drag at {@link #CARRIED_SHARE} of the wind's speed, once
	 * it has caught it: against drag that keeps {@code friction} of its speed each tick, it settles where the push makes
	 * up what the drag takes
	 *
	 * @param wind     the wind's speed this way, from {@link #x} or {@link #z}
	 * @param exposure from {@link Shelter#exposure}
	 * @param friction the share of its speed it keeps each tick, as a particle's friction
	 */
	public static float push(final float wind, final float exposure, final float friction) {
		return wind * CARRIED_SHARE * exposure * (1F - friction);
	}

	/**
	 * The drift a tick, after this one, of something blown whose drag can't be relied on (vanilla's and other mods'
	 * particles). Added to how far it moves, not to its speed, it closes {@link #CATCH_RATE} of the gap to
	 * {@link #CARRIED_SHARE} of the wind each tick: a new particle catches the wind, and a gust eases in and out.
	 *
	 * @param current  the drift last tick, 0 for a new particle
	 * @param wind     the wind's speed this way, from {@link #x} or {@link #z}
	 * @param exposure from {@link Shelter#exposure}
	 */
	public static float drift(final float current, final float wind, final float exposure) {
		return current + (wind * CARRIED_SHARE * exposure - current) * CATCH_RATE;
	}
}
