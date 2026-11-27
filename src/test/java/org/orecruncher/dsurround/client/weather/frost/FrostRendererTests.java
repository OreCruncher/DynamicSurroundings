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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.util.EnumFacing;

/**
 * Tests for how far the frost has grown across a face: in from the window's edges, thickest at its bottom. Ported
 * from the 26.x line's tests.
 */
public class FrostRendererTests {

	/**
	 * The middle face of a large window: its edges as far off as are looked for
	 */
	private static FrostFace middleOfAWindow(float shown) {
		final float reach = FrostFaces.EDGE_REACH;
		final FrostFace face = new FrostFace(0, 65, 0, EnumFacing.NORTH, 0F, 0F, 1F, 65F, 66F, -reach, 1F + reach, 65F - reach, 66F + reach, 0, 1F, 0F);
		face.show(shown);
		return face;
	}

	/**
	 * A window of a single block
	 */
	private static FrostFace smallWindow(float shown) {
		final FrostFace face = new FrostFace(0, 65, 0, EnumFacing.NORTH, 0F, 0F, 1F, 65F, 66F, 0F, 1F, 65F, 66F, 0, 1F, 0F);
		face.show(shown);
		return face;
	}

	@Test
	public void noFrostIsNoFrostAnywhere() {
		assertEquals(0F, FrostRenderer.grown(smallWindow(0F), 0F, 65F));
		assertEquals(0F, FrostRenderer.grown(middleOfAWindow(0F), 0.5F, 65.5F));
	}

	@Test
	public void fullFrostCoversAWindow() {
		assertEquals(1F, FrostRenderer.grown(middleOfAWindow(1F), 0.5F, 65.5F), 1.0E-5F);
		assertEquals(1F, FrostRenderer.grown(smallWindow(1F), 0.5F, 65.5F), 1.0E-5F);
	}

	@Test
	public void fullFrostIsTheSameAllOver() {
		// Drawn as one quad, so it must be: the edges, the bottom and the middle all fully grown
		for (final FrostFace face : new FrostFace[]{middleOfAWindow(1F), smallWindow(1F)}) {
			assertTrue(FrostRenderer.isUniform(face));
			for (float u = 0F; u <= 1F; u += 0.125F)
				for (float v = 65F; v <= 66F; v += 0.125F)
					assertEquals(1F, FrostRenderer.grown(face, u, v), 1.0E-5F);
		}
		assertFalse(FrostRenderer.isUniform(smallWindow(0.99F)));
	}

	@Test
	public void lightFrostIsAtTheEdgesOnly() {
		final FrostFace middle = middleOfAWindow(0.2F);
		assertEquals(0F, FrostRenderer.grown(middle, 0.5F, 65.5F));
		final FrostFace small = smallWindow(0.2F);
		final float edge = FrostRenderer.grown(small, 0F, 65.5F);
		assertTrue(edge > 0.2F);
		assertTrue(FrostRenderer.grown(small, 0.5F, 65.5F) < edge);
	}

	@Test
	public void frostGathersAtTheBottom() {
		final FrostFace face = smallWindow(0.4F);
		assertTrue(FrostRenderer.grown(face, 0.5F, 65.1F) > FrostRenderer.grown(face, 0.5F, 65.9F));
	}
}
