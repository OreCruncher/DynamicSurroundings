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

package org.orecruncher.dsurround.client.weather;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;

/**
 * Tests for which blocks are windows: glass and panes, stained or not, and nothing else that is glass by material
 */
public class WindowsTests {

	static {
		Bootstrap.register();
	}

	@Test
	public void glassAndPanesAreWindows() {
		assertTrue(Windows.isWindow(Blocks.GLASS.getDefaultState()));
		assertTrue(Windows.isWindow(Blocks.STAINED_GLASS.getDefaultState()));
		assertTrue(Windows.isWindow(Blocks.GLASS_PANE.getDefaultState()));
		assertTrue(Windows.isWindow(Blocks.STAINED_GLASS_PANE.getDefaultState()));
	}

	@Test
	public void otherGlassAndOtherPanesAreNot() {
		assertFalse(Windows.isWindow(Blocks.GLOWSTONE.getDefaultState()), "glass by material");
		assertFalse(Windows.isWindow(Blocks.SEA_LANTERN.getDefaultState()), "glass by material");
		assertFalse(Windows.isWindow(Blocks.IRON_BARS.getDefaultState()), "a pane of iron");
		assertFalse(Windows.isWindow(Blocks.ICE.getDefaultState()));
		assertFalse(Windows.isWindow(Blocks.STONE.getDefaultState()));
	}
}
