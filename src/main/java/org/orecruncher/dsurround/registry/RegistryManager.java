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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.event.ReloadEvent;
import org.orecruncher.dsurround.registry.config.ConfigData;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.lib.SideLocal;
import org.orecruncher.lib.Singleton;
import org.orecruncher.lib.task.Scheduler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class RegistryManager {

	// The cached configuration data is shared by both the server
	// and the client. Once loaded it is read-only so concurrent
	// access is safe. The instance reference itself is guarded
	// by the Singleton class.
	public static final Singleton<ConfigData> DATA = new Singleton<ConfigData>() {
		@Override
		@Nonnull
		protected ConfigData initialValue() {
			return ConfigData.load();
		}
	};

	private static final SideLocal<RegistryManager> managers = new SideLocal<RegistryManager>() {
		@Override
		protected RegistryManager initialValue(@Nonnull final Side side) {
			return new RegistryManager(side);
		}
	};

	@Nonnull
	public static RegistryManager get() {
		return managers.get();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onReload(@Nonnull final ReloadEvent.Resources event) {
		reloadResources(Side.CLIENT);
	}

	/**
	 * This event gets triggered when someone executes the /ds reload command. This
	 * is usually because a configuration file has changed and it needs to be
	 * reloaded. Helpful during pack development. Any cached data is released and
	 * the registries reinitialized with the appropriate info.
	 *
	 * @param event
	 */
	@SubscribeEvent
	public static void onReload(@Nonnull final ReloadEvent.Configuration event) {
		// Only reset the data if no side is specified. This is a from scratch
		// situation. If side is set it is assumed that the intent is to re-baseline
		// registries because the operational environment has changed.
		if (event.side == null) {
			DATA.clear();
		}

		if (event.side == null || event.side == Side.CLIENT)
			reloadResources(Side.CLIENT);
		if (event.side == null || event.side == Side.SERVER)
			reloadResources(Side.SERVER);
	}

	public static void reloadResources(@Nonnull final Side side) {
		// Reload can be called on either side so make sure we queue
		// up a scheduled task appropriately.
		if (managers.hasValue(side)) {
			Scheduler.schedule(side, () -> managers.get().reload());
		}
	}

	protected final Side side;
	protected final Map<Class<? extends Registry>, Registry> registries = new Object2ObjectOpenHashMap<>();
	protected final List<Registry> initOrder = new ArrayList<>();
	protected boolean initialized;

	RegistryManager(final Side side) {
		this.side = side;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(@Nonnull final Class<? extends Registry> reg) {
		final Registry o = this.registries.get(reg);
		if (o == null)
			throw new RuntimeException(
					"Attempt to get a registry that has not been configured [" + reg.getName() + "]");
		return (T) this.registries.get(reg);
	}

	public void register(@Nonnull final Registry reg) {
		this.registries.put(reg.getClass(), reg);
		this.initOrder.add(reg);
	}

	public void reload() {

		// Initialize the registries
		this.initOrder.forEach(Registry::init);

		// Process the configuration data
		ModBase.log().info("Loading configuration Json from sources");
		for (final ModConfiguration mcf : DATA.get()) {
			ModBase.log().info("Processing %s", mcf.source);
			this.initOrder.forEach(reg -> reg.configure(mcf));
		}

		// Complete the initialization
		this.initOrder.forEach(Registry::initComplete);

		// Let everyone know a reload happened
		MinecraftForge.EVENT_BUS.post(new ReloadEvent.Registry(this.side));
	}

}
