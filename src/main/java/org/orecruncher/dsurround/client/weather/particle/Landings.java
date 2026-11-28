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

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.weather.Windows;
import org.orecruncher.dsurround.client.weather.streaks.RayWalk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

/**
 * What particle weather does where it comes down, from the 26.x line's landings: snowflakes leave a fleck that melts
 * away ({@link SnowFlecks}), on the ground or, blown by the wind, on the side of a wall or tree; rain drops blown
 * against a window run down it (handed to the window streaks). The 26.x line's splashes, smoke and damp spots aren't
 * here: the rain's splashes are still the storm splash renderer's. Render thread only.
 */
public final class Landings {

	/**
	 * How far a drop or snowflake blown in under an overhang, a canopy or into a wall is followed, in blocks, to what it
	 * lands on: a trunk, the ground, the wall behind
	 */
	static final double BLOWN_IN_REACH = 8D;
	/**
	 * Rain drops and snowflakes blown in that are followed a tick, at most, to what they hit: heavy rain or snow in a
	 * strong wind blows many into walls and trees
	 */
	static final int MAX_TRACES_PER_TICK = 32;

	/**
	 * Where a rain drop blown in met the side of a window
	 */
	@FunctionalInterface
	public interface WindowHit {
		/**
		 * @param size how big the drop was, 0 to 1
		 */
		void blownInto(@Nonnull BlockPos window, @Nonnull EnumFacing side, @Nonnull Vec3d at, float size);
	}

	private final BlockPos.MutableBlockPos landingPos = new BlockPos.MutableBlockPos();

	/**
	 * Where rain drops and snowflakes landed in the field's last tick. A snowflake leaves a fleck that melts away,
	 * except on water, on something hot or on snow, where it wouldn't show; one blown in from the side lands where it
	 * was blowing (see {@link #snowBlownIn}). A rain drop blown against a window goes to {@code windows}. Follows the
	 * Particles option, as vanilla's splashes do: half the landings at Decreased, none at Minimal.
	 *
	 * @param particleSetting the Particles option: 0 all, 1 decreased, 2 minimal
	 * @param snowOnSides     whether snow blown against the side of something sticks to it
	 * @param windows         where a rain drop blown in met a window; null if rain on windows is off
	 */
	public void land(@Nonnull final IBlockAccess blocks, @Nonnull final PrecipitationField field,
			@Nonnull final SnowFlecks flecks, final int particleSetting, final boolean snowOnSides,
			@Nullable final WindowHit windows, @Nonnull final Random random) {
		if (particleSetting >= 2)
			return;
		final int landings = particleSetting == 1 ? field.landings() / 2 : field.landings();
		int traces = 0;
		for (int i = 0; i < landings; i++) {
			final double x = field.landingX(i);
			final double z = field.landingZ(i);
			final boolean snow = field.landingIsSnow(i);
			if (field.landingBlownIn(i)) {
				if (!snow && windows == null)
					continue;
				if (traces++ >= MAX_TRACES_PER_TICK)
					continue;
				final double y = field.landingY(i);
				final float mx = field.landingMotionX(i), my = field.landingMotionY(i), mz = field.landingMotionZ(i);
				if (snow)
					snowBlownIn(blocks, x, y, z, mx, my, mz, field.landingSize(i), field.landingVariant(i),
							snowOnSides, flecks, random);
				else
					rainBlownIn(blocks, x, y, z, mx, my, mz, field.landingSize(i), windows);
			} else if (snow) {
				snowLanded(blocks, x, field.landingGround(i), z, field.landingSize(i), field.landingVariant(i),
						field.landingLight(i), flecks, random);
			}
		}
	}

	/**
	 * A snowflake that came down on top of its column's ground: a fleck on what it landed on, at the top of its shape
	 * where it landed (a slab, a carpet), unless that is water, something hot or snow
	 *
	 * @param ground the column's ground height: the block landed on is the one below
	 */
	void snowLanded(@Nonnull final IBlockAccess blocks, final double x, final int ground, final double z,
			final float size, final float variant, final int light, @Nonnull final SnowFlecks flecks,
			@Nonnull final Random random) {
		final BlockPos pos = this.landingPos.setPos(MathHelper.floor(x), ground - 1, MathHelper.floor(z));
		final IBlockState state = blocks.getBlockState(pos);
		if (state.getMaterial().isLiquid() || isHot(state) || isSnow(state))
			return;
		final AxisAlignedBB box = state.getCollisionBoundingBox(blocks, pos);
		final double top = surfaceTop(box, x - pos.getX(), z - pos.getZ());
		flecks.add(x, pos.getY() + top, z, size, variant, light, random);
	}

	/**
	 * A snowflake carried into a column below its top: followed on from where it was, the way it was moving, to what it
	 * hits. Into a wall or a tree beside the open air it was in, it sticks to the side (if {@code sides}); in under a
	 * canopy or an overhang, it goes on to a trunk, or down to the ground beneath. Anything in the way counts as its
	 * collision box does: leaves, a log, a slab's lower half. Not on water, something hot or snow, and not on the
	 * underside of anything.
	 */
	void snowBlownIn(@Nonnull final IBlockAccess blocks, final double x, final double y, final double z,
			final float mx, final float my, final float mz, final float size, final float variant,
			final boolean sides, @Nonnull final SnowFlecks flecks, @Nonnull final Random random) {
		final RayTraceResult hit = RayWalk.trace(blocks, new Vec3d(x, y, z), mx, my, mz, BLOWN_IN_REACH);
		if (hit == null)
			return;
		final byte face = face(hit.sideHit);
		if (face < 0 || face != SnowFlecks.FACE_TOP && !sides)
			return;
		final BlockPos pos = hit.getBlockPos();
		final IBlockState state = blocks.getBlockState(pos);
		if (state.getMaterial().isLiquid() || isHot(state) || isSnow(state))
			return;
		// Lit as the spot in front of the face is: in under a canopy, darker than the open air it came from
		final int light = blocks.getCombinedLight(pos.offset(hit.sideHit), 0);
		flecks.add(hit.hitVec.x, hit.hitVec.y, hit.hitVec.z, face, size, variant, light, random);
	}

	/**
	 * A rain drop carried into a column below its top, followed on to what it hits: if that is the side of a window, it
	 * runs down the glass. Into anything else, it runs off unseen.
	 */
	static void rainBlownIn(@Nonnull final IBlockAccess blocks, final double x, final double y, final double z,
			final float mx, final float my, final float mz, final float size, @Nonnull final WindowHit windows) {
		final RayTraceResult hit = RayWalk.trace(blocks, new Vec3d(x, y, z), mx, my, mz, BLOWN_IN_REACH);
		if (hit == null || !hit.sideHit.getAxis().isHorizontal())
			return;
		final BlockPos pos = hit.getBlockPos();
		if (Windows.isWindow(blocks.getBlockState(pos)))
			windows.blownInto(pos, hit.sideHit, hit.hitVec, size);
	}

	/**
	 * The fleck face for the face of a block that was hit, or -1 for its underside, where nothing settles
	 */
	static byte face(@Nonnull final EnumFacing side) {
		switch (side) {
		case UP:
			return SnowFlecks.FACE_TOP;
		case WEST:
			return SnowFlecks.FACE_WEST;
		case EAST:
			return SnowFlecks.FACE_EAST;
		case NORTH:
			return SnowFlecks.FACE_NORTH;
		case SOUTH:
			return SnowFlecks.FACE_SOUTH;
		default:
			return -1;
		}
	}

	/**
	 * How far up its block the surface a flake landed on is: the top of the block's collision box, where the flake
	 * came down within it. A block with no box there (a flower, a fence post it fell beside) has none to offer, so the
	 * top of the block.
	 *
	 * @param box    the block's collision box, in the block, or null for none
	 * @param inX    where in the block it landed, 0 to 1
	 * @param inZ    and along z
	 */
	static double surfaceTop(@Nullable final AxisAlignedBB box, final double inX, final double inZ) {
		if (box == null || box.maxY <= 0D || inX < box.minX || inX > box.maxX || inZ < box.minZ || inZ > box.maxZ)
			return 1D;
		return box.maxY;
	}

	/**
	 * Whether a block is hot enough that snow melts on it at once
	 */
	private static boolean isHot(@Nonnull final IBlockState state) {
		return state.getBlock() == Blocks.MAGMA;
	}

	/**
	 * Whether a block is snow, where a fleck wouldn't show
	 */
	private static boolean isSnow(@Nonnull final IBlockState state) {
		return state.getBlock() == Blocks.SNOW_LAYER || state.getBlock() == Blocks.SNOW;
	}
}
