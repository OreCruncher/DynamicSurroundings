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

	/**
	 * Initializes the registry to base known state.  Internal data structures
	 * are initialized and made ready to recieve information.
	 */
	protected void preInit() {
		// Override to provide initialization prior to configure
	}

	/**
	 * Configure the registry based on the data provided in the ModConfiguration
	 * object.  The registry may be called one or more times, each time with a
	 * different configuration.
	 * 
	 * @param cfg
	 */
	protected abstract void init(@Nonnull final ModConfiguration cfg);

	/**
	 * Post process any additional information based on the content of other
	 * state/registries.  The available of information in other registeries
	 * is determined by the registry implementation.
	 */
	protected void postInit() {
		// Override to provide completion routine prior to notifying
		// registry listeners.
	}
	
	/**
	 * Hook provided to allow for additional functionality.  It is invoked after
	 * all registries have fully initialized and event listeners notified.  Typically
	 * logging and additional state cleanup is done here.  (State cleanup as in
	 * releasing memory that is no longer needed, etc.)
	 */
	protected void complete() {
		// Override to provide completion routine.
	}

	/**
	 * Called by the RegistryManager when the registry is to initialize
	 * its state from config data.  It is of no interest to derived
	 * classes.
	 */
	final void initialize(@Nonnull final ConfigData data) {
		ModBase.log().info("Initializing registry [%s]", getName());
		preInit();
		for (final ModConfiguration mcf : data)
			init(mcf);
		postInit();
		MinecraftForge.EVENT_BUS.post(new RegistryDataEvent.Reload(this));
	}

}
