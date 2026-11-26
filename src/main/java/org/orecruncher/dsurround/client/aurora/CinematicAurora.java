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

package org.orecruncher.dsurround.client.aurora;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import org.orecruncher.lib.random.XorShiftRandom;

/**
 * One night's cinematic aurora: the stylized ribbons of photographs and games rather than the real thing. Wide, soft
 * curtains of saturated green, pale at their lower edges and violet toward their tops, folding in long S-curves and
 * free to sweep overhead from one horizon to the other. Drawn from the night's seed, so everyone in a world sees the
 * same ribbons on the same night. Ported from the 26.x line.
 * <p>
 * A ribbon is a curtain hanging straight down from a line in a layer of the sky {@link #LOWER_EDGE} kilometers up. The
 * line runs along a heading (any way at all), a distance to the side of the viewer, folded two ways (large folds that
 * sway, a wave that travels) and bowed at its ends. Perspective does the rest: overhead a ribbon is wide, toward the
 * horizon it narrows to a thread.
 * <p>
 * Most ribbons also curl: around a point on or near the line, the layer is turned (a twist, {@link #twist}), most at
 * the point and less with distance from it, farther along the ribbon than across it, so the line bends into a long hook
 * or S, doubling back on itself as photographs show.
 * <p>
 * How active it is through the night comes from {@link AuroraNight}: quiet nights stay calm, and as a night breaks up
 * the ribbons brighten, draw in toward overhead ({@link #distanceAt}), move faster and show more violet. Its colors are
 * fixed, one signature palette (in the shader).
 */
public final class CinematicAurora {

	/**
	 * The most ribbons there can be: the shader has room for this many
	 */
	public static final int MAX_RIBBONS = 4;
	/**
	 * How high a ribbon's lower edge is, in kilometers, before a breakup lowers it
	 */
	public static final float LOWER_EDGE = 100F;
	/**
	 * How far along a ribbon, either way, its bow has bent it half as far as it goes, in kilometers (in the shader,
	 * BOW_LENGTH)
	 */
	public static final float BOW_LENGTH = 400F;
	// How much of the way in toward overhead the ribbons are drawn at the height of a breakup
	static final float SURGE_DRAW = 0.6F;
	// How far a breakup lowers the lower edge at its height, in kilometers
	static final float EDGE_DROP = 12F;
	// How likely a ribbon is to curl, in percent: the first (brightest) ribbon, and the others
	static final int FIRST_CURL_CHANCE = 80;
	static final int CURL_CHANCE = 50;
	// How much more a curl is twisted at the height of a breakup
	static final float SURGE_TWIST = 0.3F;
	/**
	 * How many times farther a curl's twist reaches along its ribbon than across it (in the shader, CURL_STRETCH)
	 */
	public static final float CURL_STRETCH = 2.5F;
	// How far a curl's twist breathes, either way, as a share of it
	static final float TWIST_BREATH = 0.3F;
	private static final float TWO_PI = (float) (2 * Math.PI);

	// Apart from the classic aurora's shape, though both come from the night's seed
	private static final long SALT = 0x63696E65L;
	// The curls come from a randomizer of their own, so a night's ribbons are the ones they were before curls
	private static final long CURL_SALT = 0x6375726CL;

	/**
	 * How a night's ribbons are laid out: side by side in waves, or one passing near overhead with its ends bent far
	 * back (a great horseshoe over the viewer)
	 */
	public enum Layout {
		WAVES, HORSESHOE
	}

	/**
	 * One ribbon. Its line, in a frame turned to its heading (along it, and across it to the left), is
	 * {@link #centerAt}. Distances in kilometers, speeds a second when calm.
	 */
	public static final class Ribbon {
		// Which way it runs, in radians from the world's +x toward +z
		public final float heading;
		// How far across from the viewer its line is, before a breakup draws it in
		public final float distance;
		public final float foldAmplitude;
		public final float foldWavelength;
		public final float foldPhase;
		public final float foldSpeed;
		public final float waveAmplitude;
		public final float waveWavelength;
		public final float waveSpeed;
		// How far its ends bend away (toward, if negative) from the viewer's side
		public final float bow;
		// How thick the curtain is (the spread of its soft sheet), and how far it rises above its lower edge
		public final float thickness;
		public final float height;
		// How bright it is, relative to a typical ribbon
		public final float brightness;
		// Where in the shader's pattern its rays and patches start, how fast its rays move along it, and how distinct
		// they are (0 smoky to 1 crisp pillars)
		public final float rayOffset;
		public final float raySpeed;
		public final float raySharpness;
		public final Curl curl;

		Ribbon(final float heading, final float distance, final float foldAmplitude, final float foldWavelength,
				final float foldPhase, final float foldSpeed, final float waveAmplitude, final float waveWavelength,
				final float waveSpeed, final float bow, final float thickness, final float height,
				final float brightness, final float rayOffset, final float raySpeed, final float raySharpness,
				@Nonnull final Curl curl) {
			this.heading = heading;
			this.distance = distance;
			this.foldAmplitude = foldAmplitude;
			this.foldWavelength = foldWavelength;
			this.foldPhase = foldPhase;
			this.foldSpeed = foldSpeed;
			this.waveAmplitude = waveAmplitude;
			this.waveWavelength = waveWavelength;
			this.waveSpeed = waveSpeed;
			this.bow = bow;
			this.thickness = thickness;
			this.height = height;
			this.brightness = brightness;
			this.rayOffset = rayOffset;
			this.raySpeed = raySpeed;
			this.raySharpness = raySharpness;
			this.curl = curl;
		}

		/**
		 * How far across its line is, {@code along} kilometers along it (in the shader, ribbon_center)
		 *
		 * @param distance how far across it is now (see {@link CinematicAurora#distanceAt})
		 * @param fold     where along their cycle the folds are, in radians (see {@link CinematicAuroraMotion})
		 * @param wave     how far the wave has traveled along it, in kilometers
		 */
		public float centerAt(final float along, final float distance, final float fold, final float wave) {
			return distance + bowAt(along)
					+ this.foldAmplitude * (float) Math.sin(along * TWO_PI / this.foldWavelength + fold)
					+ this.waveAmplitude * (float) Math.sin((along - wave) * TWO_PI / this.waveWavelength);
		}

		/**
		 * How far its bow bends it {@code along} kilometers along it (in the shader, ribbon_bow)
		 */
		public float bowAt(final float along) {
			final float a2 = along * along;
			return this.bow * a2 / (a2 + BOW_LENGTH * BOW_LENGTH);
		}

		/**
		 * The steepest its folds together bend it, in kilometers across for each kilometer along
		 */
		public float steepest() {
			return TWO_PI * (this.foldAmplitude / this.foldWavelength + this.waveAmplitude / this.waveWavelength);
		}
	}

	/**
	 * A ribbon's curl: how far along the ribbon it is, how far across from its line its middle is (0 winds the line
	 * into an S, further out a hook to one side), how far out the twist reaches across the ribbon
	 * ({@link #CURL_STRETCH} times as far along it), how far the layer is turned at its middle (radians, either way),
	 * and where its breathing starts and how fast it goes
	 */
	public static final class Curl {
		public static final Curl NONE = new Curl(0F, 0F, 1F, 0F, 0F, 0F);

		public final float along;
		public final float offset;
		public final float radius;
		public final float twist;
		public final float phase;
		public final float speed;

		Curl(final float along, final float offset, final float radius, final float twist, final float phase,
				final float speed) {
			this.along = along;
			this.offset = offset;
			this.radius = radius;
			this.twist = twist;
			this.phase = phase;
			this.speed = speed;
		}

		/**
		 * How far its twist turns the layer now
		 *
		 * @param breath where its breathing is, in radians (see {@link CinematicAuroraMotion#curl})
		 * @param surge  how far into its breakup the night is
		 */
		public float twistAt(final float breath, final float surge) {
			return this.twist * (1F + TWIST_BREATH * (float) Math.sin(breath))
					* (1F + SURGE_TWIST * Math.max(0F, Math.min(1F, surge)));
		}

		/**
		 * The farthest a twist moves a point, in kilometers: where to look for the ribbon beyond where its line can be
		 * (in the shader, curl_reach)
		 */
		public static float reachOf(final float radius, final float twist) {
			return radius * CURL_STRETCH * Math.min(0.43F * Math.abs(twist), 2.6F);
		}
	}

	public final long seed;
	public final Layout layout;
	// Its ribbons, the first brightest
	public final List<Ribbon> ribbons;

	private CinematicAurora(final long seed, @Nonnull final Layout layout, @Nonnull final List<Ribbon> ribbons) {
		this.seed = seed;
		this.layout = layout;
		this.ribbons = ribbons;
	}

	/**
	 * Where a point on the ground is looked for in the untwisted layer: turned about the curl's middle ({@code cx},
	 * {@code cz}) by {@code twist} there, less with distance as exp(-q), q the distance squared over the radius squared
	 * with the part along the ribbon ({@code ax}, {@code az}, a unit vector) shortened {@link #CURL_STRETCH} times (in
	 * the shader, curl_warp)
	 *
	 * @return the point, x and z
	 */
	@Nonnull
	public static float[] twist(final float x, final float z, final float cx, final float cz, final float radius,
			final float twist, final float ax, final float az) {
		final float rx = x - cx;
		final float rz = z - cz;
		final float along = (rx * ax + rz * az) / CURL_STRETCH;
		final float across = -rx * az + rz * ax;
		final float angle = twist * (float) Math.exp(-(along * along + across * across) / (radius * radius));
		final float c = (float) Math.cos(angle);
		final float s = (float) Math.sin(angle);
		return new float[] { cx + rx * c - rz * s, cz + rx * s + rz * c };
	}

	/**
	 * The aurora for the given night's seed
	 */
	@Nonnull
	public static CinematicAurora create(final long seed) {
		final Random random = new XorShiftRandom(Aurora.mix(seed ^ SALT));
		final float heading = random.nextFloat() * TWO_PI;
		final Layout layout = random.nextInt(100) < 35 ? Layout.HORSESHOE : Layout.WAVES;
		final int count = layout == Layout.HORSESHOE ? 1 + random.nextInt(2) : 2 + random.nextInt(3);
		// The first ribbon near overhead, the others further out to one side
		float distance = layout == Layout.HORSESHOE ? range(random, -100F, 100F) : range(random, -150F, 250F);
		final float side = random.nextBoolean() ? 1F : -1F;
		float brightness = range(random, 0.85F, 1.1F);

		final Random curls = new XorShiftRandom(Aurora.mix(seed ^ CURL_SALT));
		final List<Ribbon> ribbons = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			final float foldWavelength = range(random, 350F, 800F);
			final float waveWavelength = range(random, 120F, 260F);
			// A horseshoe's first ribbon bends its ends far back, away from the side it is on
			final float bow = layout == Layout.HORSESHOE && i == 0
					? range(random, 350F, 600F) * (distance < 0F ? -1F : 1F)
					: range(random, -150F, 150F);
			ribbons.add(new Ribbon(heading + range(random, -0.2F, 0.2F), distance,
					amplitude(range(random, 0.3F, 0.75F), foldWavelength), foldWavelength,
					random.nextFloat() * TWO_PI, range(random, 0.01F, 0.03F) * sign(random),
					amplitude(range(random, 0.1F, 0.3F), waveWavelength), waveWavelength,
					range(random, 1.5F, 5F) * sign(random), bow, range(random, 2F, 4F), range(random, 110F, 220F),
					brightness, random.nextFloat() * 1000F, range(random, 0.5F, 2.5F) * sign(random),
					range(random, 0.3F, 1F), curl(curls, i)));
			distance += side * range(random, 70F, 180F);
			brightness *= range(random, 0.6F, 0.9F);
		}
		return new CinematicAurora(seed, layout, Collections.unmodifiableList(ribbons));
	}

	/**
	 * A ribbon's curl, if it has one: in the stretch of sky a viewer sees well, a long hook of a third to half a turn.
	 * Small beside the ribbon, and its middle off to one side of the line
	 */
	@Nonnull
	private static Curl curl(@Nonnull final Random random, final int index) {
		// Drawn either way, so each ribbon's draws are the same whether or not it curls
		final boolean curls = random.nextInt(100) < (index == 0 ? FIRST_CURL_CHANCE : CURL_CHANCE);
		final float radius = range(random, 25F, 60F);
		final Curl curl = new Curl(range(random, -350F, 350F), range(random, 0.4F, 1F) * sign(random) * radius, radius,
				range(random, 2F, 3.2F) * sign(random), random.nextFloat() * TWO_PI, range(random, 0.02F, 0.05F));
		return curls ? curl : Curl.NONE;
	}

	/**
	 * How far across from the viewer a ribbon's line is at a point in its night: drawn in toward overhead as the night
	 * breaks up
	 */
	public static float distanceAt(@Nonnull final Ribbon ribbon, final float surge) {
		return ribbon.distance * (1F - SURGE_DRAW * Math.max(0F, Math.min(1F, surge)));
	}

	/**
	 * How high the lower edge is at a point in its night: lowered as the night breaks up
	 */
	public static float lowerEdgeAt(final float surge) {
		return LOWER_EDGE - EDGE_DROP * Math.max(0F, Math.min(1F, surge));
	}

	// How far a fold reaches across for it to bend the ribbon at most slope kilometers across for each along
	private static float amplitude(final float slope, final float wavelength) {
		return slope * wavelength / TWO_PI;
	}

	private static float range(@Nonnull final Random random, final float low, final float high) {
		return low + random.nextFloat() * (high - low);
	}

	private static float sign(@Nonnull final Random random) {
		return random.nextBoolean() ? 1F : -1F;
	}
}
