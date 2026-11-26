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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

/**
 * Tests for finding the flakes in a snow texture. Ported from the 26.x line's tests.
 */
public class SnowflakesTests {

	/**
	 * A picture drawn as text: '#' is a visible pixel, '.' a clear one
	 */
	private static Snowflakes.Flake[] find(final String... rows) {
		final int width = rows[0].length(), height = rows.length;
		return Snowflakes.find(width, height, (x, y) -> rows[y].charAt(x) == '#' ? 255 : 0)
				.toArray(new Snowflakes.Flake[0]);
	}

	@Test
	public void eachGroupOfPixelsIsAFlake() {
		final Snowflakes.Flake[] flakes = find(
				"........",
				"..#.....",
				".###..#.",
				"..#.....",
				"........",
				"....#.#.",
				".....#..",
				"....#.#.");
		assertEquals(3, flakes.length, "a cross, a speck, and an X joined at its corners");
		assertEquals(3, flakes[0].size);
		assertEquals(1, flakes[1].size);
		assertEquals(3, flakes[2].size);
	}

	@Test
	public void aFlakesCoordinatesFrameIt() {
		// A 3 x 3 cross at (1, 1) in an 8 x 4 picture
		final Snowflakes.Flake flake = find(
				"..#.....",
				".###....",
				"..#.....",
				"........")[0];
		assertEquals(1F / 8F, flake.u0, 1e-6F);
		assertEquals(4F / 8F, flake.u1, 1e-6F);
		assertEquals(0F, flake.v0, 1e-6F);
		assertEquals(3F / 4F, flake.v1, 1e-6F);
	}

	@Test
	public void aFlakeIsFramedSquare() {
		// Three wide and one tall: framed three by three, centered
		final Snowflakes.Flake flake = find(
				".....",
				".###.",
				".....")[0];
		assertEquals(3, flake.size);
		assertEquals((flake.u1 - flake.u0) * 5F, (flake.v1 - flake.v0) * 3F, 1e-5F);
		assertEquals(0F, flake.v0, 1e-6F, "one row above the pixels' row");
	}

	@Test
	public void largeShapesAreNotFlakes() {
		final StringBuilder row = new StringBuilder();
		for (int i = 0; i < Snowflakes.MAX_FLAKE_SIZE + 1; i++)
			row.append('#');
		row.append("..#");
		final Snowflakes.Flake[] flakes = find(row.toString());
		assertEquals(1, flakes.length, "only the speck");
		assertEquals(1, flakes[0].size);
	}

	@Test
	public void faintPixelsAreNotPartOfAFlake() {
		final List<Snowflakes.Flake> flakes = Snowflakes.find(3, 1, (x, y) -> x == 1 ? Snowflakes.MIN_ALPHA - 1 : 0);
		assertTrue(flakes.isEmpty());
	}

	@Test
	public void theGamesSnowHasItsFlakesFound() throws IOException {
		final URL url = SnowflakesTests.class.getResource("/assets/minecraft/textures/environment/snow.png");
		assertNotNull(url, "the game's snow texture isn't on the test class path");
		final BufferedImage image = ImageIO.read(url);
		final List<Snowflakes.Flake> flakes = Snowflakes.find(image.getWidth(), image.getHeight(),
				(x, y) -> (image.getRGB(x, y) >>> 24) & 0xFF);
		// The game's snow is crosses, rings and Xs three pixels across, and single pixel specks
		assertFalse(flakes.isEmpty());
		final Set<Integer> sizes = new HashSet<>();
		for (final Snowflakes.Flake flake : flakes)
			sizes.add(flake.size);
		assertEquals(new HashSet<>(Arrays.asList(1, 3)), sizes);
		assertEquals(44, flakes.size());
	}
}
