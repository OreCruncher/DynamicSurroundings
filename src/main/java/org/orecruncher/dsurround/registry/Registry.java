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

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.config.ConfigData;
import org.orecruncher.dsurround.registry.config.ModConfiguration;

import net.minecraftforge.common.MinecraftForge;

public abstract class Registry {

	private final String name;

	public Registry(@Nonnull final String name) {
		this.name = name;
		RegistryManager.REGISTRIES.add(this);
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	protected void init() {
		// Override to provide initialization prior to configure
	}

	final void _configure(@Nonnull final ConfigData cfg) {
		for (final ModConfiguration mcf : cfg) {
			configure(mcf);
		}
	}

	protected abstract void configure(@Nonnull final ModConfiguration cfg);

	protected void initComplete() {
		// Override to provide completion routine prior to notifying
		// registry listeners.
	}

	/*
	 * Called by the RegistryManager when the registry is to initialize
	 * its state from config data.
	 */
	void initialize(@Nonnull final ConfigData data) {
		ModBase.log().info("Initializing registry [%s]", getName());
		init();
		_configure(data);
		initComplete();
		MinecraftForge.EVENT_BUS.post(new RegistryDataEvent.Reload(this));
	}

}
