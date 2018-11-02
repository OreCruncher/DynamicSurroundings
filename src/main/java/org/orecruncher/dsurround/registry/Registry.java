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

package org.orecruncher.dsurround.registry;

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.registry.config.ModConfiguration;

import net.minecraftforge.fml.relauncher.Side;

public abstract class Registry {

	public final Side side;

	public Registry(@Nonnull final Side side) {
		this.side = side;
	}

	public void init() {
		// Override to provide initialization prior to configure
	}

	public final void handleConfigure(@Nonnull final List<ModConfiguration> theList) {
		for (final ModConfiguration mcf : theList)
			configure(mcf);
	}

	public abstract void configure(@Nonnull final ModConfiguration cfg);

	public void initComplete() {
		// Override to provide post processing after configure
	}

	public void fini() {
		// Tear down anything that needs it because the registry is going away
	}
}
