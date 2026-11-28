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

package org.orecruncher.dsurround.client.weather.streaks;

import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.weather.Windows;
import org.orecruncher.dsurround.client.weather.particle.IColumnSource;
import org.orecruncher.dsurround.client.weather.particle.PrecipitationKind;
import org.orecruncher.dsurround.client.weather.wind.Wind;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Where rain starts running down windows, ported from the 26.x line's landings: a few times a tick, more the harder it
 * rains and the stronger the wind, a column near the camera is looked down for a window. A window whose windward side
 * is open to the rain, with nothing in front of it and the sky above, gets a drop somewhere on that side. Windows
 * indoors, under eaves, or facing away from the wind stay dry. This needs no drops falling, so it runs with the sheets
 * of rain as well as particle weather; with particle weather, drops blown into windows start streaks too
 * ({@link WindowRain#blownIntoWindow}).
 */
public final class RainOnWindows {

	/**
	 * Windows looked for a tick, at most: in a full storm with a full storm's wind. Each is a column near the camera;
	 * the first window in it, on its windward side, gets a drop if the rain reaches it.
	 */
	static final float WINDOW_SEED_TRIES = 4F;
	// In still air, a window still gets this share of those: rain falls on it at a slant from gusts and eddies
	static final float WINDOW_SEED_CALM_SHARE = 0.3F;
	// Below this share of a full storm's wind, the air is taken as still: rain reaches any side of a window
	static final float WINDOW_SEED_STILL = 0.05F;
	// How far from the camera windows are looked for, horizontally and up or down
	static final int WINDOW_SEED_RADIUS = 10;
	static final int WINDOW_SEED_HEIGHT = 6;
	// How far down a window a drop can run, in blocks of glass
	static final int MAX_WINDOW_DEPTH = 8;
	// Where a drop stops at the bottom edge of the glass: just above a frame or sill it runs onto; at the edge itself
	// where it drips off into open air
	static final double WINDOW_BOTTOM_MARGIN = 0.06D;
	static final double WINDOW_DRIP_MARGIN = 0.01D;
	// Something this close under the glass's bottom edge takes the drop: a sill, a frame, the wall below
	static final double WINDOW_SILL_REACH = 0.3D;
	// How far a line is followed to what it meets
	static final double REACH = 8D;

	private final BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();

	/**
	 * Rain on the windows near the camera: a drop on the windward side of a window open to the rain, now and then
	 *
	 * @param rainLevel the storm's strength, 0 to 1
	 */
	public void seedWindows(@Nonnull final World world, @Nonnull final IColumnSource columns, final double cameraX,
			final double cameraY, final double cameraZ, final float rainLevel, @Nonnull final Wind wind,
			@Nonnull final WindowStreaks streaks, @Nonnull final Random random) {
		if (rainLevel <= 0F)
			return;
		final float windShare = MathHelper.clamp(wind.speed() / Wind.speedOfPercent(100), 0F, 1F);
		final float wanted = WINDOW_SEED_TRIES * Math.min(rainLevel, 1F)
				* (WINDOW_SEED_CALM_SHARE + windShare * (1F - WINDOW_SEED_CALM_SHARE));
		final int tries = tries(wanted, random.nextFloat());
		for (int t = 0; t < tries; t++) {
			final int x = MathHelper.floor(cameraX) + random.nextInt(WINDOW_SEED_RADIUS * 2 + 1) - WINDOW_SEED_RADIUS;
			final int z = MathHelper.floor(cameraZ) + random.nextInt(WINDOW_SEED_RADIUS * 2 + 1) - WINDOW_SEED_RADIUS;
			final BlockPos window = findWindow(world, x, MathHelper.floor(cameraY), z);
			if (window == null)
				continue;
			final EnumFacing side = windwardFace(wind.directionX(), wind.directionZ(), windShare, random.nextFloat(),
					random.nextInt(4));
			// In front of that side: open, and open to the sky where it rains
			final BlockPos front = window.offset(side);
			final AxisAlignedBB inFront = world.getBlockState(front).getCollisionBoundingBox(world, front);
			if (inFront != null && inFront.maxY - inFront.minY > 1.0E-4D)
				continue;
			if (columns.groundHeight(front.getX(), front.getZ()) > front.getY())
				continue;
			if (columns.precipitation(front.getX(), front.getY(), front.getZ()) != PrecipitationKind.RAIN)
				continue;
			// Somewhere on that side: straight at the glass from in front of it, so a pane is hit where its glass is
			final double across = 0.1D + random.nextDouble() * 0.8D;
			final double up = 0.25D + random.nextDouble() * 0.7D;
			final Vec3d from = new Vec3d(
					side.getFrontOffsetX() != 0 ? window.getX() + 0.5D + side.getFrontOffsetX() : window.getX() + across,
					window.getY() + up,
					side.getFrontOffsetZ() != 0 ? window.getZ() + 0.5D + side.getFrontOffsetZ() : window.getZ() + across);
			final RayTraceResult hit = RayWalk.trace(world, from, -side.getFrontOffsetX(), 0F, -side.getFrontOffsetZ(),
					REACH);
			if (hit == null || hit.sideHit != side || !hit.getBlockPos().equals(window))
				continue;
			startStreak(world, window, side, hit.hitVec, random.nextFloat(), streaks, random);
		}
	}

	/**
	 * How many tries a tick for a number wanted on average: its whole part, and one more by the chance of the rest
	 */
	static int tries(final float wanted, final float chance) {
		final int whole = (int) wanted;
		return whole + (chance < wanted - whole ? 1 : 0);
	}

	/**
	 * The first window block in a column, from the top of the range around the camera down, or null if there is none
	 */
	@Nullable
	private BlockPos findWindow(@Nonnull final World world, final int x, final int cameraY, final int z) {
		for (int y = cameraY + WINDOW_SEED_HEIGHT; y >= cameraY - WINDOW_SEED_HEIGHT; y--) {
			if (Windows.isWindow(world.getBlockState(this.scanPos.setPos(x, y, z))))
				return this.scanPos.toImmutable();
		}
		return null;
	}

	/**
	 * The side of a window the rain is blown onto: the one facing into the wind. Which of the two the wind blows across
	 * is picked in proportion to how much it blows along each. In still air, any side, as gusts and eddies would.
	 *
	 * @param windX     the way the wind blows: a unit vector's x
	 * @param windZ     and its z
	 * @param windShare how strong the wind is, as a share of a full storm's, 0 to 1
	 * @param chance    a random number, 0 to 1, to pick between the two sides the wind blows onto
	 * @param anySide   a random number, 0 to 3, to pick a side in still air
	 */
	@Nonnull
	static EnumFacing windwardFace(final float windX, final float windZ, final float windShare, final float chance,
			final int anySide) {
		if (windShare < WINDOW_SEED_STILL)
			return EnumFacing.getHorizontal(anySide);
		final float alongX = Math.abs(windX), alongZ = Math.abs(windZ);
		// The side looks back the way the wind comes from
		if (chance * (alongX + alongZ) < alongX)
			return windX > 0F ? EnumFacing.WEST : EnumFacing.EAST;
		return windZ > 0F ? EnumFacing.NORTH : EnumFacing.SOUTH;
	}

	/**
	 * Starts a drop running down a window where something hit its side
	 *
	 * @param size how big the drop was, 0 to 1
	 */
	static void startStreak(@Nonnull final World world, @Nonnull final BlockPos window,
			@Nonnull final EnumFacing side, @Nonnull final Vec3d at, final float size,
			@Nonnull final WindowStreaks streaks, @Nonnull final Random random) {
		// Down the glass to where it ends: a drop runs down a window of several blocks, then drips off its bottom edge
		// or runs onto what is below
		final int bottomY = window.getY() - glassBelow(world, window, side, at, Windows::isWindow);
		final boolean drips = dripsOff(world, at, side, bottomY);
		final double bottom = bottomY + (drips ? WINDOW_DRIP_MARGIN : WINDOW_BOTTOM_MARGIN);
		final int light = world.getCombinedLight(window.offset(side), 0);
		streaks.add(at.x, at.y, at.z, face(side), bottom, drips, size, light, random);
	}

	/**
	 * How many blocks below a window's block the side a drop is on carries on: glass, its face on that side in the
	 * same place as where the drop is, and nothing in front of it. Not glass set back or standing out, a pane below a
	 * glass block, or glass behind something.
	 *
	 * @param at       where the drop is, on the glass
	 * @param isWindow whether a block is a window
	 */
	static int glassBelow(@Nonnull final IBlockAccess blocks, @Nonnull final BlockPos window,
			@Nonnull final EnumFacing side, @Nonnull final Vec3d at, @Nonnull final Predicate<IBlockState> isWindow) {
		final double plane = side.getAxis() == EnumFacing.Axis.X ? at.x : at.z;
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(window);
		int depth = 0;
		while (depth < MAX_WINDOW_DEPTH) {
			pos.move(EnumFacing.DOWN);
			if (!isWindow.test(blocks.getBlockState(pos)))
				break;
			// Straight at its side, from a little in front of where the drop runs down: what is met there must be its
			// glass, where the glass above is
			final Vec3d from = new Vec3d(at.x + side.getFrontOffsetX() * 0.25D, pos.getY() + 0.5D,
					at.z + side.getFrontOffsetZ() * 0.25D);
			final RayTraceResult hit = RayWalk.trace(blocks, from, -side.getFrontOffsetX(), 0F,
					-side.getFrontOffsetZ(), REACH);
			if (hit == null || hit.sideHit != side || !hit.getBlockPos().equals(pos))
				break;
			final double hitPlane = side.getAxis() == EnumFacing.Axis.X ? hit.hitVec.x : hit.hitVec.z;
			if (Math.abs(hitPlane - plane) > 1.0E-3D)
				break;
			depth++;
		}
		return depth;
	}

	/**
	 * Whether a drop at the bottom edge of a window drips off it: nothing is just under the edge where it runs down,
	 * such as a sill, a frame or the wall below
	 *
	 * @param at      where the drop is, on the glass
	 * @param bottomY the height of the glass's bottom edge
	 */
	static boolean dripsOff(@Nonnull final IBlockAccess blocks, @Nonnull final Vec3d at,
			@Nonnull final EnumFacing side, final int bottomY) {
		// A sill standing out under it: straight down from just in front of the glass, at its bottom edge
		final Vec3d from = new Vec3d(at.x + side.getFrontOffsetX() * 0.01D, bottomY + 0.01D,
				at.z + side.getFrontOffsetZ() * 0.01D);
		final RayTraceResult below = RayWalk.trace(blocks, from, 0F, -1F, 0F, REACH);
		if (below != null && below.hitVec.y >= bottomY - WINDOW_SILL_REACH)
			return false;
		// The wall below, flush with the glass: straight at it from in front, just under the edge, meeting it no further
		// back than the glass. Set back from the glass, the drop drips off in front of it.
		final double plane = side.getAxis() == EnumFacing.Axis.X ? at.x : at.z;
		final Vec3d front = new Vec3d(at.x + side.getFrontOffsetX() * 0.25D, bottomY - 0.05D,
				at.z + side.getFrontOffsetZ() * 0.25D);
		final RayTraceResult wall = RayWalk.trace(blocks, front, -side.getFrontOffsetX(), 0F, -side.getFrontOffsetZ(),
				REACH);
		if (wall == null || wall.sideHit != side)
			return true;
		final double wallPlane = side.getAxis() == EnumFacing.Axis.X ? wall.hitVec.x : wall.hitVec.z;
		// How far the wall's face is in front of the glass, the way the side looks
		final double standsOut = (wallPlane - plane)
				* (side.getAxis() == EnumFacing.Axis.X ? side.getFrontOffsetX() : side.getFrontOffsetZ());
		return standsOut < -1.0E-3D;
	}

	/**
	 * The streak face for a side of a block
	 */
	static byte face(@Nonnull final EnumFacing side) {
		switch (side) {
		case WEST:
			return WindowStreaks.FACE_WEST;
		case EAST:
			return WindowStreaks.FACE_EAST;
		case NORTH:
			return WindowStreaks.FACE_NORTH;
		default:
			return WindowStreaks.FACE_SOUTH;
		}
	}
}
