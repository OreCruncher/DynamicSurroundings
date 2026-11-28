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

package org.orecruncher.dsurround.client.fx.firefly;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * A slow, wandering flight, for small flying things like fireflies, from the 26.x line. The heading drifts by a turn
 * rate that itself wanders (and is damped, so the flier curves rather than spins), speed swells and eases a little,
 * there is a gentle bob, and the velocity eases toward all that each tick so turns are smooth. Looking a few ticks
 * ahead, it turns aside and lifts when something is in the way, rather than colliding.
 * <p>
 * How much it climbs or sinks is up to the flier, passed to each {@link #tick}: a firefly rises as it flashes. Holds
 * the velocity it steers; the flier moves by {@link #xd()}, {@link #yd()} and {@link #zd()}.
 */
public final class WanderingFlight {

	private static final float TWO_PI = (float) (Math.PI * 2D);
	private static final float HALF_PI = (float) (Math.PI / 2D);

	/**
	 * How a flight behaves.
	 */
	public static final class Settings {
		/** Slowest cruising speed, blocks per tick (each flight picks its own, between these) */
		public final float minSpeed;
		/** Fastest cruising speed */
		public final float maxSpeed;
		/** How much the turn rate wanders each tick, radians: more makes a more erratic flight */
		public final float turnWander;
		/** How much of the turn rate carries over to the next tick: less makes straighter flight */
		public final float turnDamping;
		/** How much of the way to its target the velocity moves each tick, 0 to 1 */
		public final float steer;
		/** Climb speed, blocks per tick, at a climb of 1 */
		public final float lift;
		/** Size of the bob, blocks per tick */
		public final float bob;
		/** How quickly it bobs, radians per tick */
		public final float bobRate;
		/** How many ticks ahead to look for something in the way */
		public final int lookAhead;

		public Settings(final float minSpeed, final float maxSpeed, final float turnWander, final float turnDamping,
				final float steer, final float lift, final float bob, final float bobRate, final int lookAhead) {
			this.minSpeed = minSpeed;
			this.maxSpeed = maxSpeed;
			this.turnWander = turnWander;
			this.turnDamping = turnDamping;
			this.steer = steer;
			this.lift = lift;
			this.bob = bob;
			this.bobRate = bobRate;
			this.lookAhead = lookAhead;
		}
	}

	/**
	 * Whether something is in the way at a point.
	 */
	@FunctionalInterface
	public interface Obstacles {
		boolean isBlocked(double x, double y, double z);
	}

	/**
	 * Blocks with a collision box are in the way: the ground, logs, leaves; not grass or flowers.
	 */
	@Nonnull
	public static Obstacles blocksIn(@Nonnull final IBlockAccess world) {
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		return (x, y, z) -> {
			pos.setPos(x, y, z);
			return world.getBlockState(pos).getCollisionBoundingBox(world, pos) != Block.NULL_AABB;
		};
	}

	// How much speed swells and eases, and how quickly
	private static final float SPEED_SWELL = 0.25F;
	private static final float SPEED_SWELL_RATE = 0.11F;

	private final Settings settings;
	private final Random random;
	private final float cruiseSpeed;
	private final float phase;
	private float heading;
	private float turnRate;
	private double xd;
	private double yd;
	private double zd;

	public WanderingFlight(@Nonnull final Settings settings, @Nonnull final Random random) {
		this.settings = settings;
		this.random = random;
		this.cruiseSpeed = settings.minSpeed + random.nextFloat() * (settings.maxSpeed - settings.minSpeed);
		this.phase = random.nextFloat() * TWO_PI;
		this.heading = random.nextFloat() * TWO_PI;
		this.turnRate = (float) random.nextGaussian() * settings.turnWander;

		// Already under way
		this.xd = Math.cos(this.heading) * this.cruiseSpeed;
		this.zd = Math.sin(this.heading) * this.cruiseSpeed;
	}

	/**
	 * Steers for the next tick.
	 *
	 * @param x         where the flier is
	 * @param age       its age in ticks, for the bob and speed swell
	 * @param climb     how much it wants to climb (positive) or sink (negative), as a fraction of the lift
	 * @param obstacles what is in the way
	 */
	public void tick(final double x, final double y, final double z, final int age, final float climb,
			@Nonnull final Obstacles obstacles) {
		final Settings s = this.settings;

		// Wander: the turn rate drifts, and is damped so the flier curves rather than circles
		this.turnRate = this.turnRate * s.turnDamping + (float) this.random.nextGaussian() * s.turnWander;
		this.heading += this.turnRate;

		final double speed = this.cruiseSpeed
				* (1F - SPEED_SWELL + SPEED_SWELL * Math.sin(age * SPEED_SWELL_RATE + this.phase));
		double targetX = Math.cos(this.heading) * speed;
		double targetZ = Math.sin(this.heading) * speed;
		double targetY = s.lift * climb + s.bob * Math.sin(age * s.bobRate + this.phase);

		// Something in the way: turn aside and lift over it
		final int ahead = s.lookAhead;
		if (obstacles.isBlocked(x + targetX * ahead, y + targetY * ahead, z + targetZ * ahead)) {
			this.heading += HALF_PI + this.random.nextFloat() * (float) Math.PI * 0.5F;
			this.turnRate = 0F;
			targetX = Math.cos(this.heading) * speed;
			targetZ = Math.sin(this.heading) * speed;
			targetY = Math.abs(targetY) + s.lift * 0.5F;
		}

		this.xd += (targetX - this.xd) * s.steer;
		this.yd += (targetY - this.yd) * s.steer;
		this.zd += (targetZ - this.zd) * s.steer;
	}

	public double xd() {
		return this.xd;
	}

	public double yd() {
		return this.yd;
	}

	public double zd() {
		return this.zd;
	}
}
