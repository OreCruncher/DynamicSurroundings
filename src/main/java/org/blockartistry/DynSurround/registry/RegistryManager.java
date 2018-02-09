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

import java.io.IOException;
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
import org.blockartistry.lib.SideLocal;
import org.blockartistry.lib.task.Scheduler;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
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
	protected final ResourceLocation SCRIPT;
	protected final Map<Class<? extends Registry>, Registry> registries = new IdentityHashMap<>();
	protected final List<Registry> initOrder = new ArrayList<>();
	protected boolean initialized;

	RegistryManager(final Side side) {
		this.side = side;
		if (side == Side.CLIENT) {
			this.SCRIPT = new ResourceLocation(DSurround.RESOURCE_ID, "configure.json");
		} else {
			this.SCRIPT = null;
		}
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

	@SideOnly(Side.CLIENT)
	private boolean checkCompatible(@Nonnull final ResourcePackRepository.Entry pack) {
		return pack.getResourcePack().resourceExists(SCRIPT);
	}

	@SideOnly(Side.CLIENT)
	private InputStream openScript(@Nonnull final IResourcePack pack) throws IOException {
		return pack.getInputStream(SCRIPT);
	}

	public void reload() {
		this.initOrder.forEach(reg -> reg.init());

		for (final ModContainer mod : Loader.instance().getActiveModList()) {
			final ModConfigurationFile cfg = DataScripts.loadFromArchive(mod.getModId());
			if (cfg != null) {
				DSurround.log().info("Loading from archive [%s]", mod.getModId());
				this.initOrder.forEach(reg -> {
					try {
						reg.configure(cfg);
					} catch (@Nonnull final Throwable t) {
						final String txt = String.format("[%s] had issues with [%s]!", reg.getClass().getSimpleName(),
								mod.getModId());
						DSurround.log().error(txt, t);
					}
				});
			}
		}

		final List<ProfileScript> resources = getAdditionalScripts();
		for (final ProfileScript script : resources) {
			try (final InputStreamReader reader = new InputStreamReader(script.stream)) {
				final ModConfigurationFile cfg = DataScripts.loadFromStream(reader);
				if (cfg != null) {
					DSurround.log().info("Loading from resource pack [%s]", script.packName);
					this.initOrder.forEach(reg -> {
						try {
							reg.configure(cfg);
						} catch (@Nonnull final Throwable t) {
							final String txt = String.format("[%s] had issues with resource pack [%s]!",
									reg.getClass().getSimpleName(), script.packName);
							DSurround.log().error(txt, t);
						}
					});
				}
			} catch (@Nonnull final Throwable ex) {
				DSurround.log().error("Unable to read script from resource pack!", ex);
			}
		}

		// Load scripts specified in the configuration
		final String[] configFiles = ModOptions.general.externalScriptFiles;
		for (final String file : configFiles) {
			final ModConfigurationFile cfg = DataScripts.loadFromDirectory(file);
			if (cfg != null) {
				DSurround.log().info("Loading from directory [%s]", file);
				this.initOrder.forEach(reg -> {
					try {
						reg.configure(cfg);
					} catch (@Nonnull final Throwable t) {
						final String txt = String.format("[%s] had issues with [%s]!", reg.getClass().getSimpleName(),
								file);
						DSurround.log().error(txt, t);
					}
				});
			}
		}

		this.initOrder.forEach(reg -> reg.initComplete());
		MinecraftForge.EVENT_BUS.post(new ReloadEvent.Registry(this.side));
	}

	// NOTE: Server side has no resource packs so the client specific
	// code is not executed when initializing a server side registry.
	protected List<ProfileScript> getAdditionalScripts() {
		if (this.side == Side.SERVER)
			return ImmutableList.of();

		final List<ResourcePackRepository.Entry> repo = Minecraft.getMinecraft().getResourcePackRepository()
				.getRepositoryEntries();

		final List<ProfileScript> streams = new ArrayList<>();

		// Look in other resource packs for more configuration data
		for (final ResourcePackRepository.Entry pack : repo) {
			if (checkCompatible(pack)) {
				DSurround.log().debug("Found script in resource pack: %s", pack.getResourcePackName());
				try {
					final InputStream stream = openScript(pack.getResourcePack());
					if (stream != null)
						streams.add(new ProfileScript(pack.getResourcePackName(), stream));
				} catch (final Throwable t) {
					DSurround.log().error("Unable to open script in resource pack", t);
				}
			}
		}

		// Tack on built-in profiles
		streams.addAll(Profiles.getProfileStreams());

		return streams;
	}

}
