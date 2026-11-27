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

package org.orecruncher.dsurround.client.weather.frost;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.orecruncher.lib.random.XorShiftRandom;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The frost on windows, made rather than drawn (ported from the 26.x line): feathery crystals, each a trunk with
 * branches off it at the sixty degrees ice grows at, and branches off those, over a fine haze of hoar frost. Brightness
 * is in the color; the alpha says when each texel frosts over as the frost grows, from 0 (first) to 1 (never): a
 * crystal grows out from its root, and the haze fills in last. The shader shows a texel once the frost has grown past
 * its alpha, so one texture shows frost from the first crystals to a window frosted over, and melting back. It repeats
 * both ways, and has mipmaps for windows seen from afar.
 * <p>
 * Made the first time it is asked for: the pattern on a thread of its own (it takes a good part of a second), then
 * uploaded on the render thread once it is ready, and kept. Until then there is no texture, and no frost is drawn.
 */
public final class FrostTexture {

	private static final float PI = (float) Math.PI;
	private static final float TWO_PI = PI * 2F;

	// Texels each way: the texture covers FrostRenderer.TEXTURE_BLOCKS blocks, so 128 a block, fine enough for crystals
	// a fraction of a block long to keep their barbs
	static final int SIZE = 512;
	static final int LEVELS = 6;
	// The crystals: how many there are, how long a trunk is at least and how much longer it can be, in texels
	static final int CRYSTALS = 550;
	static final float TRUNK_MIN = 20F;
	static final float TRUNK_RANGE = 35F;
	// How much a trunk curls as it grows, at most, in radians a texel
	static final float CURL = 0.03F;
	// How late a crystal starts growing at most, and how far its frost has grown by the time it reaches a texel, per
	// texel from its root
	static final float START_RANGE = 0.3F;
	static final float GROWTH_PER_TEXEL = 0.01F;
	// Branches come off in pairs, one each side, like the barbs of a feather: how far apart, at least and how much more;
	// how long, as a share of what is left of the one they come off; how much thinner and dimmer they are; and how
	// many times branches branch
	static final float BRANCH_GAP_MIN = 2.5F;
	static final float BRANCH_GAP_RANGE = 2F;
	static final float BRANCH_LENGTH = 0.35F;
	static final float BRANCH_THINNER = 0.65F;
	static final float BRANCH_DIMMER = 0.85F;
	static final int BRANCH_DEPTH = 2;
	static final float TRUNK_WIDTH = 1.0F;
	// The haze: when it frosts over, and how bright it is
	static final float HAZE_GROWTH = 0.55F;
	static final float HAZE_GROWTH_RANGE = 0.4F;
	static final float HAZE_BRIGHTNESS = 0.3F;
	static final float HAZE_BRIGHTNESS_RANGE = 0.25F;
	// The latest a crystal's texel frosts: before all of the haze
	static final float CRYSTAL_LATEST = 0.9F;
	private static final long SEED = 0x66726F7374L;

	// The texture's name, or -1 until it is made
	private static int texture = -1;
	private static CompletableFuture<Pattern> making;

	private FrostTexture() {
	}

	/**
	 * The texture's name, or -1 while it is still being made: the first call starts making it. Sampled smoothly
	 * between texels and mipmaps, repeating.
	 */
	@SideOnly(Side.CLIENT)
	public static int texture() {
		if (texture >= 0)
			return texture;
		if (making == null)
			making = CompletableFuture.supplyAsync(() -> generate(SIZE, SEED));
		if (making.isDone()) {
			texture = upload(making.join());
			making = null;
		}
		return texture;
	}

	/**
	 * The frost pattern: for each texel, row by row, its brightness and when it frosts over, both 0 to 1
	 */
	static final class Pattern {
		final int size;
		final float[] brightness;
		final float[] growth;

		Pattern(final int size, @Nonnull final float[] brightness, @Nonnull final float[] growth) {
			this.size = size;
			this.brightness = brightness;
			this.growth = growth;
		}

		int size() {
			return this.size;
		}

		float[] brightness() {
			return this.brightness;
		}

		float[] growth() {
			return this.growth;
		}

		int index(final int x, final int y) {
			return Math.floorMod(y, this.size) * this.size + Math.floorMod(x, this.size);
		}
	}

	static Pattern generate(int size, long seed) {
		final Random random = new XorShiftRandom(seed);
		final Pattern pattern = new Pattern(size, new float[size * size], new float[size * size]);
		haze(pattern, random);
		for (int i = 0; i < CRYSTALS; i++) {
			float x = random.nextFloat() * size;
			float y = random.nextFloat() * size;
			float angle = random.nextFloat() * TWO_PI;
			float length = TRUNK_MIN + random.nextFloat() * TRUNK_RANGE;
			float curl = (random.nextFloat() - 0.5F) * 2F * CURL;
			branch(pattern, random, x, y, angle, curl, length, random.nextFloat() * START_RANGE, TRUNK_WIDTH, 1F, 0);
		}
		return pattern;
	}

	/**
	 * The fine haze under the crystals: a random brightness each texel, frosting over late, patchily
	 */
	private static void haze(Pattern pattern, Random random) {
		int size = pattern.size;
		// Patches: two lattices of random values, coarse and fine, read smoothly, repeating with the texture
		final float[] coarse = lattice(random, 6);
		final float[] fine = lattice(random, 16);
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++) {
				float patch = 0.65F * value(coarse, 6, x, y, size) + 0.35F * value(fine, 16, x, y, size);
				int i = pattern.index(x, y);
				pattern.growth[i] = HAZE_GROWTH + HAZE_GROWTH_RANGE * (0.7F * patch + 0.3F * random.nextFloat());
				pattern.brightness[i] = HAZE_BRIGHTNESS + HAZE_BRIGHTNESS_RANGE * random.nextFloat();
			}
	}

	private static float[] lattice(Random random, int cells) {
		final float[] lattice = new float[cells * cells];
		for (int i = 0; i < lattice.length; i++)
			lattice[i] = random.nextFloat();
		return lattice;
	}

	/**
	 * A lattice of {@code cells} each way read smoothly at a texel, stretched over the texture
	 */
	private static float value(float[] lattice, int cells, int x, int y, int size) {
		float fx = (float) x * cells / size, fy = (float) y * cells / size;
		int cx = MathHelper.floor(fx), cy = MathHelper.floor(fy);
		float tx = smooth(fx - cx), ty = smooth(fy - cy);
		float a = lattice[Math.floorMod(cy, cells) * cells + Math.floorMod(cx, cells)];
		float b = lattice[Math.floorMod(cy, cells) * cells + Math.floorMod(cx + 1, cells)];
		float c = lattice[Math.floorMod(cy + 1, cells) * cells + Math.floorMod(cx, cells)];
		float d = lattice[Math.floorMod(cy + 1, cells) * cells + Math.floorMod(cx + 1, cells)];
		return lerp(ty, lerp(tx, a, b), lerp(tx, c, d));
	}

	private static float smooth(float t) {
		return t * t * (3F - 2F * t);
	}

	/**
	 * Grows a branch from a point, a texel at a time, curling and wandering a little, with pairs of branches off it
	 *
	 * @param curl  how much it turns each texel
	 * @param start how far the frost has grown when the branch starts
	 * @param depth how many branches it is off a trunk
	 */
	private static void branch(Pattern pattern, Random random, float x, float y, float angle, float curl, float length, float start,
							   float width, float brightness, int depth) {
		float nextBranch = BRANCH_GAP_MIN + random.nextFloat() * BRANCH_GAP_RANGE;
		for (float travelled = 0F; travelled < length; travelled += 0.7F) {
			float growth = Math.min(CRYSTAL_LATEST, start + travelled * GROWTH_PER_TEXEL);
			// Thinning toward the tip
			float w = width * (1F - 0.5F * travelled / length);
			plot(pattern, x, y, growth, w, brightness);
			angle += curl * 0.7F + (random.nextFloat() - 0.5F) * 0.06F;
			x += MathHelper.cos(angle) * 0.7F;
			y += MathHelper.sin(angle) * 0.7F;
			if (depth < BRANCH_DEPTH && travelled >= nextBranch) {
				nextBranch = travelled + BRANCH_GAP_MIN + random.nextFloat() * BRANCH_GAP_RANGE;
				float remaining = length - travelled;
				for (int side = -1; side <= 1; side += 2) {
					float turn = (PI / 3F + (random.nextFloat() - 0.5F) * 0.2F) * side;
					branch(pattern, random, x, y, angle + turn, curl * 0.5F, remaining * BRANCH_LENGTH * (0.6F + 0.8F * random.nextFloat()),
							growth + 0.02F, width * BRANCH_THINNER, brightness * BRANCH_DIMMER, depth + 1);
				}
			}
		}
	}

	/**
	 * Marks the texels around a point as frosted at a growth, brightest at the middle of the stroke
	 */
	private static void plot(Pattern pattern, float x, float y, float growth, float width, float brightness) {
		int r = MathHelper.ceil(width);
		int cx = MathHelper.floor(x), cy = MathHelper.floor(y);
		for (int j = -r; j <= r; j++)
			for (int i = -r; i <= r; i++) {
				float dx = cx + i + 0.5F - x, dy = cy + j + 0.5F - y;
				float cover = MathHelper.clamp(width - MathHelper.sqrt(dx * dx + dy * dy) + 0.5F, 0F, 1F);
				if (cover <= 0F)
					continue;
				int index = pattern.index(cx + i, cy + j);
				// The stroke's soft edge frosts a little after its middle
				pattern.growth[index] = Math.min(pattern.growth[index], growth + (1F - cover) * 0.08F);
				pattern.brightness[index] = Math.max(pattern.brightness[index], brightness * (0.6F + 0.4F * cover));
			}
	}

	/**
	 * The pattern at half the size, each texel the average of four, for the next mipmap
	 */
	static Pattern half(Pattern pattern) {
		int size = pattern.size / 2;
		final Pattern half = new Pattern(size, new float[size * size], new float[size * size]);
		for (int y = 0; y < size; y++)
			for (int x = 0; x < size; x++) {
				int a = pattern.index(2 * x, 2 * y), b = pattern.index(2 * x + 1, 2 * y);
				int c = pattern.index(2 * x, 2 * y + 1), d = pattern.index(2 * x + 1, 2 * y + 1);
				int i = half.index(x, y);
				half.brightness[i] = (pattern.brightness[a] + pattern.brightness[b] + pattern.brightness[c] + pattern.brightness[d]) / 4F;
				half.growth[i] = (pattern.growth[a] + pattern.growth[b] + pattern.growth[c] + pattern.growth[d]) / 4F;
			}
		return half;
	}

	private static float lerp(final float t, final float a, final float b) {
		return a + t * (b - a);
	}

	/**
	 * Uploads the pattern and its mipmaps as an RGBA texture: the brightness in the color, when it frosts in the alpha
	 */
	@SideOnly(Side.CLIENT)
	private static int upload(@Nonnull final Pattern pattern) {
		final int name = GlStateManager.generateTexture();
		GlStateManager.bindTexture(name);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, LEVELS - 1);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		final ByteBuffer bytes = GLAllocation.createDirectByteBuffer(pattern.size * pattern.size * 4);
		Pattern level = pattern;
		for (int i = 0; i < LEVELS; i++) {
			bytes.clear();
			for (int y = 0; y < level.size; y++)
				for (int x = 0; x < level.size; x++) {
					final int index = level.index(x, y);
					final byte grey = (byte) MathHelper.clamp(Math.round(level.brightness[index] * 255F), 0, 255);
					bytes.put(grey).put(grey).put(grey)
							.put((byte) MathHelper.clamp(Math.round(level.growth[index] * 255F), 0, 255));
				}
			bytes.flip();
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA8, level.size, level.size, 0, GL11.GL_RGBA,
					GL11.GL_UNSIGNED_BYTE, bytes);
			level = half(level);
		}
		GlStateManager.bindTexture(0);
		return name;
	}
}
