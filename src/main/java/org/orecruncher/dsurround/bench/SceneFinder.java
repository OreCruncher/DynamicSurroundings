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

package org.orecruncher.dsurround.bench;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

/**
 * Finds a scene for a biome, on the server's thread: the nearest of the biome to the origin (1.12.2 has no /locate
 * biome: rings out from it, sampling the biome provider, which needs no chunk generated), then the biome is sampled on
 * a coarse grid around there for where the most of it is, and on a fine one around that for the place with the most of
 * the ground around it being that biome; then dry ground of it near there, the height from the generated chunk, looking
 * toward the most of it. Ported from the 26.x line.
 */
final class SceneFinder {

	// The rings searched for the nearest of the biome
	private static final int SEARCH_RADIUS = 6400;
	private static final int SEARCH_STEP = 32;
	// The coarse pass: every COARSE_STEP blocks out to COARSE_SPAN from the find, counting the biome within COARSE_RADIUS
	private static final int COARSE_SPAN = 2048;
	private static final int COARSE_STEP = 64;
	private static final int COARSE_RADIUS = 128;
	// The fine pass, around the coarse pass's best
	private static final int FINE_SPAN = 256;
	private static final int FINE_STEP = 16;
	private static final int FINE_RADIUS = 64;
	// How far to look for dry ground of the biome to stand on, and in what steps
	private static final int DRY_RADIUS = 32;
	private static final int DRY_STEP = 4;
	// Looking a little down at the scene
	private static final float PITCH = 5F;

	private SceneFinder() {
	}

	@Nonnull
	static SceneLocations.Location find(@Nonnull final WorldServer world, @Nonnull final String biomeName,
			final int originX, final int originZ) {
		final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeName));
		if (biome == null)
			throw new IllegalArgumentException("this game has no biome " + biomeName);
		final BiomeProvider provider = world.getBiomeProvider();
		final int[] found = nearest(provider, biome, originX, originZ);
		if (found == null)
			throw new IllegalArgumentException("no " + biomeName + " within " + SEARCH_RADIUS + " blocks");
		final Best coarse = best(provider, biome, found[0], found[1], COARSE_SPAN, COARSE_STEP, COARSE_RADIUS);
		final Best fine = best(provider, biome, coarse.x, coarse.z, FINE_SPAN, FINE_STEP, FINE_RADIUS);
		// Generates the chunk if it must: the height is then the real ground's
		final int[] dry = dryGround(world, biome, fine.x, fine.z);
		final int x = dry[0], z = dry[1];
		final int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
		// Minecraft's yaw: 0 looks south (+z), 90 west (-x)
		final float yaw = fine.lookX == 0 && fine.lookZ == 0 ? 0F
				: (float) Math.toDegrees(Math.atan2(-fine.lookX, fine.lookZ));
		return new SceneLocations.Location(biomeName, x + 0.5, y, z + 0.5, yaw, PITCH, fine.share, false);
	}

	/**
	 * The location moved {@code dx} east and {@code dz} south, on the ground there (its chunk generated)
	 */
	@Nonnull
	static SceneLocations.Location offset(@Nonnull final WorldServer world,
			@Nonnull final SceneLocations.Location location, final int dx, final int dz) {
		final double x = location.x + dx, z = location.z + dz;
		final int bx = (int) Math.floor(x), bz = (int) Math.floor(z);
		world.getChunkFromChunkCoords(bx >> 4, bz >> 4);
		final int y = world.getTopSolidOrLiquidBlock(new BlockPos(bx, 0, bz)).getY();
		return new SceneLocations.Location(location.biome, x, y, z, location.yaw, location.pitch, location.biomeShare,
				location.marked);
	}

	/**
	 * Whether a remembered scene is still in its biome (a worldgen mod added or updated moves them)
	 */
	static boolean stillThere(@Nonnull final WorldServer world, @Nonnull final SceneLocations.Location location) {
		final Biome biome = world.getBiomeProvider()
				.getBiome(new BlockPos(location.x, location.y, location.z));
		final ResourceLocation key = Biome.REGISTRY.getNameForObject(biome);
		return key != null && key.toString().equals(location.biome);
	}

	// The nearest sample of the biome, ring by ring out from the origin; null if none
	private static int[] nearest(@Nonnull final BiomeProvider provider, @Nonnull final Biome biome, final int ox,
			final int oz) {
		if (provider.getBiome(new BlockPos(ox, 64, oz)) == biome)
			return new int[] { ox, oz };
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int r = SEARCH_STEP; r <= SEARCH_RADIUS; r += SEARCH_STEP) {
			for (int d = -r; d < r; d += SEARCH_STEP) {
				// The ring's four sides, each from one corner up to the next
				final int[][] points = { { ox + d, oz - r }, { ox + r, oz + d }, { ox - d, oz + r }, { ox - r, oz - d } };
				for (final int[] p : points)
					if (provider.getBiome(pos.setPos(p[0], 64, p[1])) == biome)
						return p;
			}
		}
		return null;
	}

	private static final class Best {
		final int x;
		final int z;
		final double share;
		final double lookX;
		final double lookZ;

		Best(final int x, final int z, final double share, final double lookX, final double lookZ) {
			this.x = x;
			this.z = z;
			this.share = share;
			this.lookX = lookX;
			this.lookZ = lookZ;
		}
	}

	private static Best best(@Nonnull final BiomeProvider provider, @Nonnull final Biome biome, final int cx,
			final int cz, final int span, final int step, final int radius) {
		// The biome, sampled over the area; then the sum of each corner's rectangle, to count any square of it at once
		final int cells = span / step;
		final int size = 2 * cells + 1;
		final boolean[][] is = new boolean[size][size];
		final int[][] sums = new int[size + 1][size + 1];
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				final int px = cx + (i - cells) * step, pz = cz + (j - cells) * step;
				is[i][j] = provider.getBiome(pos.setPos(px, 64, pz)) == biome;
				sums[i + 1][j + 1] = sums[i][j + 1] + sums[i + 1][j] - sums[i][j] + (is[i][j] ? 1 : 0);
			}
		}
		final int w = radius / step;
		int bestI = cells, bestJ = cells, bestCount = -1, bestDistance = Integer.MAX_VALUE;
		for (int i = w; i < size - w; i++) {
			for (int j = w; j < size - w; j++) {
				if (!is[i][j])
					continue;
				final int count = sums[i + w + 1][j + w + 1] - sums[i - w][j + w + 1] - sums[i + w + 1][j - w]
						+ sums[i - w][j - w];
				final int distance = (i - cells) * (i - cells) + (j - cells) * (j - cells);
				if (count > bestCount || count == bestCount && distance < bestDistance) {
					bestI = i;
					bestJ = j;
					bestCount = count;
					bestDistance = distance;
				}
			}
		}
		double lookX = 0D, lookZ = 0D;
		for (int di = -w; di <= w; di++) {
			for (int dj = -w; dj <= w; dj++) {
				final int i = bestI + di, j = bestJ + dj;
				if ((di != 0 || dj != 0) && i >= 0 && j >= 0 && i < size && j < size && is[i][j]) {
					final double length = Math.sqrt(di * di + dj * dj);
					lookX += di / length;
					lookZ += dj / length;
				}
			}
		}
		final double share = Math.max(0, bestCount) / (double) ((2 * w + 1) * (2 * w + 1));
		return new Best(cx + (bestI - cells) * step, cz + (bestJ - cells) * step, share, lookX, lookZ);
	}

	private static int[] dryGround(@Nonnull final WorldServer world, @Nonnull final Biome biome, final int x,
			final int z) {
		for (int r = 0; r <= DRY_RADIUS; r += DRY_STEP) {
			for (int dx = -r; dx <= r; dx += DRY_STEP) {
				for (int dz = -r; dz <= r; dz += DRY_STEP) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r)
						continue;
					final int px = x + dx, pz = z + dz;
					world.getChunkFromChunkCoords(px >> 4, pz >> 4);
					final BlockPos feet = world.getTopSolidOrLiquidBlock(new BlockPos(px, 0, pz));
					if (!world.getBlockState(feet.down()).getMaterial().isLiquid() && world.getBiome(feet) == biome)
						return new int[] { px, pz };
				}
			}
		}
		return new int[] { x, z };
	}
}
