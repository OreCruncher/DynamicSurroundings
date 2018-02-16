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

package org.blockartistry.DynSurround.registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.data.Profiles;
import org.blockartistry.DynSurround.data.Profiles.ProfileScript;
import org.blockartistry.DynSurround.data.xface.DataScripts;
import org.blockartistry.DynSurround.data.xface.ModConfigurationFile;
import org.blockartistry.DynSurround.event.ReloadEvent;
import org.blockartistry.DynSurround.packs.ResourcePacks;
import org.blockartistry.DynSurround.packs.ResourcePacks.Pack;
import org.blockartistry.lib.SideLocal;
import org.blockartistry.lib.task.Scheduler;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class RegistryManager {

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

	@SubscribeEvent
	public static void onReload(@Nonnull final ReloadEvent.Configuration event) {
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
	protected final Map<Class<? extends Registry>, Registry> registries = new IdentityHashMap<>();
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

	protected void configRegistries(@Nonnull final ModConfigurationFile cfg, @Nonnull final String txt) {
		if (cfg != null) {
			DSurround.log().info("Loading %s", txt);
			this.initOrder.forEach(reg -> {
				try {
					reg.configure(cfg);
				} catch (@Nonnull final Throwable t) {
					final String temp = String.format("[%s] had issues loading %s!", reg.getClass().getSimpleName(),
							txt);
					DSurround.log().error(temp, t);
				}
			});
		}
	}

	protected void process(@Nonnull final Pack p, @Nonnull ResourceLocation rl, @Nonnull final String txt) {
		try (final InputStream stream = p.getInputStream(rl)) {
			if (stream != null) {
				try (final InputStreamReader reader = new InputStreamReader(stream)) {
					final ModConfigurationFile cfg = DataScripts.loadFromStream(reader);
					this.configRegistries(cfg, txt);
				}
			}
		} catch (@Nonnull final Throwable t) {
			final String temp = String.format("Error loading %s", txt);
			DSurround.log().error(temp, t);
		}
	}

	public void reload() {

		// Collect the locations where DS data is configured
		final List<Pack> packs = ResourcePacks.findResourcePacks();
		final List<ModContainer> activeMods = Loader.instance().getActiveModList();

		DSurround.log().info("Identified the following resource pack locations");
		packs.stream().map(Pack::toString).forEach(DSurround.log()::info);

		// Do the preinit
		this.initOrder.forEach(reg -> reg.init());

		// Process mod specific config files from our packs
		for (final ModContainer mod : activeMods) {
			final String name = mod.getModId().toLowerCase();
			final ResourceLocation rl = new ResourceLocation(DSurround.MOD_ID, "data/" + name + ".json");
			for (final Pack p : packs) {
				final String loadingText = "[" + rl.toString() + "] <- [" + p.getModName() + "]";
				this.process(p, rl, loadingText);
			}
		}

		// Process general config files from our packs
		final ResourceLocation rl = ResourcePacks.CONFIGURE_RESOURCE;
		for (final Pack p : packs) {
			final String loadingText = "[" + rl.toString() + "] <- [" + p.getModName() + "]";
			this.process(p, rl, loadingText);
		}

		// Apply built-in profiles
		final List<ProfileScript> resources = Profiles.getProfileStreams();
		for (final ProfileScript script : resources) {
			try (final InputStreamReader reader = new InputStreamReader(script.stream)) {
				final ModConfigurationFile cfg = DataScripts.loadFromStream(reader);
				final String loadingText = "[" + DSurround.MOD_ID + "] <- [" + script.packName + "]";
				this.configRegistries(cfg, loadingText);
			} catch (@Nonnull final Throwable ex) {
				final String temp = String.format("Unable to load profile [%s]", script.packName);
				DSurround.log().error(temp, ex);
			}
		}

		// Load scripts specified in the configuration
		final String[] configFiles = ModOptions.general.externalScriptFiles;
		for (final String file : configFiles) {
			final ModConfigurationFile cfg = DataScripts.loadFromDirectory(file);
			final String loadingText = "[" + file + "]";
			this.configRegistries(cfg, loadingText);
		}

		// Have the registries finalize their settings
		this.initOrder.forEach(reg -> reg.initComplete());
		
		// Let everyone know a reload happened
		MinecraftForge.EVENT_BUS.post(new ReloadEvent.Registry(this.side));
	}

}
