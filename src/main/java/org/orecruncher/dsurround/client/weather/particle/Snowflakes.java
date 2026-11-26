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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntBinaryOperator;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The snowflakes of the game's own snow texture, the one its sheets of falling snow are drawn with, so a resource
 * pack's snow is the snow particles fall as too. The texture is scanned for flakes: each separate group of visible
 * pixels is one, and is drawn on its own. Ported from the 26.x line's particle weather.
 * <p>
 * Looked up the first time they are wanted after resources load. Render thread only.
 */
@SideOnly(Side.CLIENT)
public final class Snowflakes {

	/**
	 * The game's snow texture
	 */
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/environment/snow.png");

	// Groups of pixels wider or taller than this aren't taken for flakes; nor are more than this many flakes kept
	static final int MAX_FLAKE_SIZE = 16;
	static final int MAX_FLAKES = 64;
	// Pixels this transparent or more aren't part of a flake
	static final int MIN_ALPHA = 16;

	/**
	 * Where a flake is in the texture, in texture coordinates, and its size in pixels: the larger of its width and
	 * height. Square, so the flake isn't stretched when drawn.
	 */
	public static final class Flake {
		public final float u0, v0, u1, v1;
		public final int size;

		public Flake(final float u0, final float v0, final float u1, final float v1, final int size) {
			this.u0 = u0;
			this.v0 = v0;
			this.u1 = u1;
			this.v1 = v1;
			this.size = size;
		}
	}

	private List<Flake> flakes = Collections.emptyList();
	private int largest = 1;
	private boolean loaded;

	/**
	 * The flakes, looked up if resources have loaded since the last time; empty if the texture has none
	 */
	@Nonnull
	public List<Flake> flakes(@Nonnull final IResourceManager resources) {
		if (!this.loaded) {
			this.loaded = true;
			this.flakes = load(resources);
			this.largest = 1;
			for (final Flake flake : this.flakes)
				this.largest = Math.max(this.largest, flake.size);
		}
		return this.flakes;
	}

	/**
	 * The size of the largest flake, in pixels, so the others can be drawn in proportion to it
	 */
	public int largest() {
		return this.largest;
	}

	/**
	 * Resources have loaded again: the flakes are looked up afresh when next wanted
	 */
	public void reload() {
		this.loaded = false;
	}

	@Nonnull
	private static List<Flake> load(@Nonnull final IResourceManager resources) {
		try (IResource resource = resources.getResource(TEXTURE); InputStream stream = resource.getInputStream()) {
			final BufferedImage image = TextureUtil.readBufferedImage(stream);
			// ARGB: alpha is the top byte
			final List<Flake> found = find(image.getWidth(), image.getHeight(),
					(x, y) -> (image.getRGB(x, y) >>> 24) & 0xFF);
			if (found.isEmpty())
				ModBase.log().warn("Found no snowflakes in %s; snow will fall as plain flakes", TEXTURE);
			return found;
		} catch (final Exception e) {
			ModBase.log().error("Unable to read the snow texture; snow will fall as plain flakes", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Finds the flakes in a picture: each group of visible pixels touching one another, side or corner, that is no
	 * larger than {@link #MAX_FLAKE_SIZE} each way.
	 *
	 * @param alphaAt the alpha of the pixel at (x, y), 0 to 255
	 */
	@Nonnull
	static List<Flake> find(final int width, final int height, @Nonnull final IntBinaryOperator alphaAt) {
		final boolean[] seen = new boolean[width * height];
		final List<Flake> flakes = new ArrayList<>();
		final int[] stack = new int[width * height];
		for (int y = 0; y < height && flakes.size() < MAX_FLAKES; y++) {
			for (int x = 0; x < width && flakes.size() < MAX_FLAKES; x++) {
				if (seen[y * width + x] || alphaAt.applyAsInt(x, y) < MIN_ALPHA)
					continue;

				// Gather the group, keeping its bounds
				int minX = x, maxX = x, minY = y, maxY = y;
				int top = 0;
				stack[top++] = y * width + x;
				seen[y * width + x] = true;
				while (top > 0) {
					final int at = stack[--top];
					final int px = at % width, py = at / width;
					minX = Math.min(minX, px);
					maxX = Math.max(maxX, px);
					minY = Math.min(minY, py);
					maxY = Math.max(maxY, py);
					for (int dy = -1; dy <= 1; dy++) {
						for (int dx = -1; dx <= 1; dx++) {
							final int nx = px + dx, ny = py + dy;
							if (nx < 0 || ny < 0 || nx >= width || ny >= height)
								continue;
							final int n = ny * width + nx;
							if (!seen[n] && alphaAt.applyAsInt(nx, ny) >= MIN_ALPHA) {
								seen[n] = true;
								stack[top++] = n;
							}
						}
					}
				}

				final int w = maxX - minX + 1, h = maxY - minY + 1;
				if (w > MAX_FLAKE_SIZE || h > MAX_FLAKE_SIZE)
					continue;
				// Square about the flake's middle, so drawing it on a square doesn't stretch it
				final int size = Math.max(w, h);
				final float left = minX - (size - w) / 2F, top0 = minY - (size - h) / 2F;
				flakes.add(new Flake(left / width, top0 / height, (left + size) / width, (top0 + size) / height, size));
			}
		}
		return flakes;
	}
}
