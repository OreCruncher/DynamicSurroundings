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

package org.orecruncher.dsurround.client.weather.particle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.util.math.MathHelper;

/**
 * The wind that slants falling rain and snow. It blows steadily, from a direction that wanders slowly, and harder the
 * heavier the storm; every so often a gust passes through. A gust is a front: it reaches the upwind side of the area
 * around the camera first and sweeps across it, so its effect can be seen traveling through the drops.
 * <p>
 * The wind's direction starts from the day, so players in the same world see it blowing the same way; the gusts and
 * the wandering are each player's own. Speeds are in blocks a tick. Client thread only. Ported from the 26.x line.
 */
public final class Wind {

	// Keeps the wind's random numbers apart from other per-day ones
	private static final long WIND_SALT = 0x6A09E667F3BCC909L;

	/**
	 * The steady wind of a storm at full strength. Rain falls at about 1 block a tick, so this slants it by about 15
	 * degrees; snow, falling far slower, much more.
	 */
	static final float MAX_SPEED = 0.25F;
	// The steady wind of the lightest storm, as a fraction of a full storm's
	static final float CALM_FRACTION = 0.2F;
	/**
	 * Between storms, a light breeze: enough for mist and steam to drift, in blocks a tick
	 */
	static final float BREEZE = 0.03F;
	// Thunderstorms blow harder, and dust storms harder still: it is the wind that raises the dust
	static final float THUNDER_FACTOR = 1.3F;
	static final float DUST_FACTOR = 2F;
	// Fraction of the way to the storm's wind covered each tick, so it builds and dies away over a few seconds
	static final float EASE_PER_TICK = 0.02F;
	// How far the direction can wander each tick, in radians, and how fast that wandering itself changes
	static final float MAX_WANDER = 0.004F;
	static final float WANDER_CHANGE = 0.0005F;

	// A gust adds up to this fraction of the steady wind at its peak
	static final float MIN_GUST = 0.5F;
	static final float GUST_RANGE = 0.7F;
	// Ticks a gust takes to pass any one point, and between the end of one and the start of the next
	static final int MIN_GUST_TICKS = 20;
	static final int GUST_TICKS_RANGE = 30;
	static final int MIN_GUST_GAP = 60;
	static final int GUST_GAP_RANGE = 200;
	/**
	 * How fast a gust's front travels, in blocks a tick
	 */
	static final float GUST_TRAVEL = 0.6F;
	/**
	 * How far upwind of the camera a gust's front starts, unless set to another: the edge of the area with drops, at
	 * the default range. The wind service sets it to the range in use every tick.
	 */
	static final float DEFAULT_REACH = 12F;

	static final float PI = (float) Math.PI;

	private float reach = DEFAULT_REACH;
	// Set by command, held until cleared, or NaN for none: the speed, and the angle it blows toward
	private float speedSet = Float.NaN;
	private float angleSet = Float.NaN;
	private float angle;
	private float wander;
	private float speed;
	// From the angle and speed, worked out once a tick: every drop reads them, thousands of times a tick
	private float directionX = 1F;
	private float directionZ;
	private float x;
	private float z;
	private long day = Long.MIN_VALUE;

	// Ticks since the current gust's front set out, its length, and its strength; between gusts, ticks until the next
	private int gustAge;
	private int gustTicks;
	private float gustPower;
	private int untilGust = MIN_GUST_GAP;

	/**
	 * The steady wind, x and z
	 */
	public float x() {
		return this.x;
	}

	public float z() {
		return this.z;
	}

	/**
	 * The way the wind is blowing, as a unit vector, x and z
	 */
	public float directionX() {
		return this.directionX;
	}

	public float directionZ() {
		return this.directionZ;
	}

	private void updateVector() {
		this.directionX = MathHelper.cos(this.angle);
		this.directionZ = MathHelper.sin(this.angle);
		this.x = this.directionX * this.speed;
		this.z = this.directionZ * this.speed;
	}

	/**
	 * The way the wind blows toward on a day, as an angle in radians from east toward south (+x toward +z): the same
	 * for every player in a world. The wind starts each day from it, and the clouds' upper wind follows it.
	 */
	public static float dayAngle(long day) {
		return splitMix64(day ^ WIND_SALT) / (float) Long.MAX_VALUE * PI;
	}

	public float speed() {
		return this.speed;
	}

	/**
	 * The way the wind is blowing as a compass bearing, in degrees clockwise from north (toward negative z)
	 */
	public float bearing() {
		return bearing(this.directionX(), this.directionZ());
	}

	static float bearing(float x, float z) {
		final float degrees = (float) Math.toDegrees(Math.atan2(x, -z));
		return degrees < 0F ? degrees + 360F : degrees;
	}

	private static final String[] COMPASS_POINTS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

	/**
	 * The nearest of the eight compass points to a bearing
	 */
	public static String compassPoint(float bearing) {
		return COMPASS_POINTS[Math.floorMod(Math.round(bearing / 45F), 8)];
	}

	public boolean isGusting() {
		return this.gustTicks > 0;
	}

	/**
	 * Moves the wind on a tick.
	 *
	 * @param rainLevel   the storm's strength, 0 to 1; a light breeze without rain
	 * @param thundering  whether it is a thunderstorm
	 * @param dusty       whether the storm is raising dust where the camera is
	 * @param day         the day number, which sets the direction the wind starts from
	 */
	public void tick(float rainLevel, boolean thundering, boolean dusty, long day, Random random) {
		if (!Float.isNaN(this.angleSet)) {
			// Held where it was set: neither the day's direction nor wandering
			this.day = day;
			this.angle = this.angleSet;
		} else {
			if (day != this.day) {
				// A new day, or the first tick: the day's own direction. Mid storm this swings the wind round, rarely.
				this.day = day;
				this.angle = dayAngle(day);
			}
			this.wander = MathHelper.clamp(this.wander + (random.nextFloat() * 2F - 1F) * WANDER_CHANGE, -MAX_WANDER, MAX_WANDER);
			this.angle += this.wander;
		}

		if (!Float.isNaN(this.speedSet)) {
			this.speed = this.speedSet;
		} else {
			float target = rainLevel <= 0F ? BREEZE : MAX_SPEED * (CALM_FRACTION + (1F - CALM_FRACTION) * rainLevel);
			if (thundering)
				target *= THUNDER_FACTOR;
			if (dusty)
				target *= DUST_FACTOR;
			this.speed += (target - this.speed) * EASE_PER_TICK;
		}
		this.updateVector();

		this.tickGust(random);
	}

	private void tickGust(Random random) {
		if (this.gustTicks > 0) {
			// Over once its front has crossed the whole area and its tail has passed the far side
			if (++this.gustAge > this.gustTicks + 2F * this.reach / GUST_TRAVEL) {
				this.gustTicks = 0;
				this.untilGust = MIN_GUST_GAP + random.nextInt(GUST_GAP_RANGE);
			}
		} else if (--this.untilGust <= 0) {
			this.gustAge = 0;
			this.gustTicks = MIN_GUST_TICKS + random.nextInt(GUST_TICKS_RANGE);
			this.gustPower = MIN_GUST + random.nextFloat() * GUST_RANGE;
		}
	}

	/**
	 * How much stronger than the steady wind it is at a point, from a passing gust: 1 when there is none, rising
	 * smoothly to 1 plus the gust's power at its height and falling away again.
	 *
	 * @param along how far downwind of the camera the point is, in blocks; negative is upwind
	 */
	public float gustFactor(float along, float partialTick) {
		if (this.gustTicks == 0)
			return 1F;
		// When the front reaches this point, counting from when it set out upwind
		final float arrives = (along + this.reach) / GUST_TRAVEL;
		final float passing = this.gustAge + partialTick - arrives;
		if (passing <= 0F || passing >= this.gustTicks)
			return 1F;
		final float envelope = MathHelper.sin(PI * passing / this.gustTicks);
		return 1F + this.gustPower * envelope * envelope;
	}

	/**
	 * The wind as it is this tick, for working out at many points at once: its steady vector and direction, and the
	 * parts of the gust that don't depend on the point. It doesn't change as the wind does, so another thread can read it.
	 */
	public Snapshot snapshot() {
		return new Snapshot(this.x, this.z, this.directionX, this.directionZ,
				this.gustTicks == 0 ? 0F : this.gustAge - this.reach / GUST_TRAVEL, this.gustTicks,
				this.gustTicks == 0 ? 0F : PI / this.gustTicks, this.gustPower);
	}

	/**
	 * The wind of one tick (see {@link #snapshot})
	 *
	 * @param gustStart when the gust's front reached the camera, in ticks from now (negative before it has)
	 * @param gustTicks how long the gust takes to pass a point; 0 for no gust
	 * @param gustScale pi over its length, to turn ticks into its envelope's angle
	 */
	public static final class Snapshot {

		private static final float INVERSE_TRAVEL = 1F / GUST_TRAVEL;

		private final float x, z, directionX, directionZ, gustStart, gustScale, gustPower;
		private final int gustTicks;

		public Snapshot(final float x, final float z, final float directionX, final float directionZ,
				final float gustStart, final int gustTicks, final float gustScale, final float gustPower) {
			this.x = x;
			this.z = z;
			this.directionX = directionX;
			this.directionZ = directionZ;
			this.gustStart = gustStart;
			this.gustTicks = gustTicks;
			this.gustScale = gustScale;
			this.gustPower = gustPower;
		}

		/**
		 * The steady wind, x and z, in blocks a tick
		 */
		public float x() {
			return this.x;
		}

		public float z() {
			return this.z;
		}

		/**
		 * The way the wind is blowing, as a unit vector, x and z
		 */
		public float directionX() {
			return this.directionX;
		}

		public float directionZ() {
			return this.directionZ;
		}

		/**
		 * As {@link Wind#gustFactor}, at the tick itself
		 *
		 * @param along how far downwind of the camera the point is, in blocks; negative is upwind
		 */
		public float gustFactor(float along) {
			if (this.gustTicks == 0)
				return 1F;
			final float passing = this.gustStart - along * INVERSE_TRAVEL;
			if (passing <= 0F || passing >= this.gustTicks)
				return 1F;
			final float envelope = MathHelper.sin(passing * this.gustScale);
			return 1F + this.gustPower * envelope * envelope;
		}
	}

	/**
	 * Sets the steady wind and stops it changing by day, for tests
	 */
	void set(float angle, float speed, long day) {
		this.day = day;
		this.angle = angle;
		this.speed = speed;
		this.updateVector();
	}

	/**
	 * Holds the wind at a speed, in blocks a tick, until {@link #clearSet}: for seeing how a wind looks. Gusts still
	 * come and go.
	 */
	public void setSpeed(float speed) {
		this.speedSet = Math.max(speed, 0F);
		this.speed = this.speedSet;
		this.updateVector();
	}

	/**
	 * Holds the wind blowing toward a compass bearing, in degrees clockwise from north, until {@link #clearSet}
	 */
	public void setBearing(float bearing) {
		this.angleSet = angleFor(bearing);
		this.angle = this.angleSet;
		this.updateVector();
	}

	/**
	 * Lets the wind go its own way again: its speed eases back to the storm's, and its direction wanders from where it
	 * is until the next day's
	 */
	public void clearSet() {
		this.speedSet = Float.NaN;
		this.angleSet = Float.NaN;
	}

	/**
	 * Whether its speed or direction is held by {@link #setSpeed} or {@link #setBearing}
	 */
	public boolean isSet() {
		return !Float.isNaN(this.speedSet) || !Float.isNaN(this.angleSet);
	}

	public boolean isSpeedSet() {
		return !Float.isNaN(this.speedSet);
	}

	public boolean isBearingSet() {
		return !Float.isNaN(this.angleSet);
	}

	/**
	 * The direction angle, as {@link #x} and {@link #z} use it, for a compass bearing: degrees clockwise from north,
	 * which is toward negative z
	 */
	static float angleFor(float bearing) {
		final double radians = Math.toRadians(bearing);
		return (float) Math.atan2(-Math.cos(radians), Math.sin(radians));
	}

	/**
	 * A speed, in blocks a tick, as a percent of a full storm's steady wind
	 */
	public static int percentOfFullStorm(float speed) {
		return Math.round(speed / MAX_SPEED * 100F);
	}

	/**
	 * A percent of a full storm's steady wind, as a speed in blocks a tick
	 */
	public static float speedOfPercent(int percent) {
		return MAX_SPEED * percent / 100F;
	}

	/**
	 * The eight compass points, from north round to north-west
	 */
	public static List<String> compassPoints() {
		return Collections.unmodifiableList(Arrays.asList(COMPASS_POINTS));
	}

	/**
	 * The bearing of a compass point, in degrees clockwise from north
	 */
	public static float bearingOf(String compassPoint) {
		return compassPoints().indexOf(compassPoint) * 45F;
	}

	/**
	 * How far upwind of the camera a gust's front starts: the edge of the area with drops
	 */
	public float reach() {
		return this.reach;
	}

	/**
	 * Sets how far upwind a gust's front starts, to the range drops are drawn to, so a gust sweeps the whole of it
	 */
	public void setReach(float reach) {
		this.reach = reach;
	}

	/**
	 * Output number {@code index} (counting from 0) of SplitMix64 started from a seed of 0, worked out directly: the
	 * 26.x line's Mixers.splitMix64. Gives well-spread seeds from consecutive numbers, such as day numbers.
	 */
	static long splitMix64(final long index) {
		long z = (index + 1) * 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	/**
	 * Starts a gust now, for tests
	 */
	void gust(int ticks, float power) {
		this.gustAge = 0;
		this.gustTicks = ticks;
		this.gustPower = power;
	}
}
