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
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nonnull;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.data.Profiles;
import org.blockartistry.DynSurround.data.xface.DataScripts;
import org.blockartistry.DynSurround.data.xface.ModConfigurationFile;
import org.blockartistry.DynSurround.event.RegistryEvent;
import org.blockartistry.lib.SideLocal;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class RegistryManager {

	public static enum RegistryType {
		SOUND, BIOME, BLOCK, DIMENSION, FOOTSTEPS, SEASON, ITEMS
	}

	private static final SideLocal<RegistryManager> managers = new SideLocal<RegistryManager>() {
		@Override
		protected RegistryManager initialValue(@Nonnull final Side side) {
			return new RegistryManager(side);
		}
	};

	@Nonnull
	public static <T extends Registry> T get(@Nonnull final RegistryType type) {
		return (T) managers.get().<T>getRegistry(type);
	}

	public static void reloadResources() {
		reloadResources(Side.CLIENT);
		reloadResources(Side.SERVER);
	}

	public static void reloadResources(@Nonnull final Side side) {
		// Reload can be called on either side so make sure we queue
		// up a scheduled task appropriately.
		if (managers.hasValue(side)) {
			final IThreadListener tl = side == Side.SERVER ? FMLCommonHandler.instance().getMinecraftServerInstance()
					: Minecraft.getMinecraft();
			if (tl == null)
				managers.clear(side);
			else
				tl.addScheduledTask(new Runnable() {
					public void run() {
						managers.get().reload();
					}
				});
		}
	}

	protected final Side side;
	protected final ResourceLocation SCRIPT;
	protected final EnumMap<RegistryType, Registry> registries = new EnumMap<RegistryType, Registry>(
			RegistryType.class);
	protected boolean initialized;

	RegistryManager(final Side side) {
		this.side = side;
		this.registries.put(RegistryType.DIMENSION, new DimensionRegistry(side));

		if (side == Side.CLIENT) {
			this.registries.put(RegistryType.SOUND, new SoundRegistry(side));
			this.registries.put(RegistryType.BIOME, new BiomeRegistry(side));
			this.registries.put(RegistryType.SEASON, new SeasonRegistry(side));
			this.registries.put(RegistryType.BLOCK, new BlockRegistry(side));
			this.registries.put(RegistryType.FOOTSTEPS, new FootstepsRegistry(side));
			this.registries.put(RegistryType.ITEMS, new ItemRegistry(side));
			this.SCRIPT = new ResourceLocation(DSurround.RESOURCE_ID, "configure.json");
		} else {
			this.SCRIPT = null;
		}
	}

	@SideOnly(Side.CLIENT)
	private boolean checkCompatible(@Nonnull final ResourcePackRepository.Entry pack) {
		return pack.getResourcePack().resourceExists(SCRIPT);
	}

	@SideOnly(Side.CLIENT)
	private InputStream openScript(@Nonnull final IResourcePack pack) throws IOException {
		return pack.getInputStream(SCRIPT);
	}

	protected void processConfiguration(@Nonnull final ModConfigurationFile cfg) {
		for (final Registry r : this.registries.values())
			if (r != null)
				r.configure(cfg);
	}

	protected void reload() {
		for (final Registry r : this.registries.values())
			if (r != null)
				r.init();

		for (final ModContainer mod : Loader.instance().getActiveModList()) {
			DSurround.log().info("Loading from archive [%s]", mod.getModId());
			final ModConfigurationFile cfg = DataScripts.loadFromArchive(mod.getModId());
			if (cfg != null)
				processConfiguration(cfg);
			else
				DSurround.log().warn("Unable to load configuration data!");
		}

		final List<InputStream> resources = getAdditionalScripts();
		for (final InputStream stream : resources) {
			try (final InputStreamReader reader = new InputStreamReader(stream)) {
				final ModConfigurationFile cfg = DataScripts.loadFromStream(reader);
				if (cfg != null)
					processConfiguration(cfg);
			} catch (@Nonnull final Throwable ex) {
				DSurround.log().error("Unable to read script from resource pack!", ex);
			}
		}

		// Load scripts specified in the configuration
		final String[] configFiles = ModOptions.externalScriptFiles;
		for (final String file : configFiles) {
			DSurround.log().info("Loading from directory [%s]", file);
			final ModConfigurationFile cfg = DataScripts.loadFromDirectory(file);
			if (cfg != null)
				processConfiguration(cfg);
			else
				DSurround.log().warn("Unable to load configuration data!");
		}

		for (final Registry r : this.registries.values())
			if (r != null)
				r.initComplete();

		MinecraftForge.EVENT_BUS.post(new RegistryEvent.Reload(this.side));
	}

	@SuppressWarnings("unchecked")
	protected <T> T getRegistry(@Nonnull final RegistryType type) {
		if (!this.initialized) {
			this.initialized = true;
			this.reload();
		}

		final Object result = this.registries.get(type);
		if (result == null)
			throw new RuntimeException(
					"Attempt to get registry [" + type.name() + "] that is not configured for the side!");
		return (T) result;
	}

	// NOTE: Server side has no resource packs so the client specific
	// code is not executed when initializing a server side registry.
	protected List<InputStream> getAdditionalScripts() {
		if (this.side == Side.SERVER)
			return ImmutableList.of();

		final List<ResourcePackRepository.Entry> repo = Minecraft.getMinecraft().getResourcePackRepository()
				.getRepositoryEntries();

		final List<InputStream> streams = new ArrayList<InputStream>();

		// Look in other resource packs for more configuration data
		for (final ResourcePackRepository.Entry pack : repo) {
			if (checkCompatible(pack)) {
				DSurround.log().debug("Found script in resource pack: %s", pack.getResourcePackName());
				try {
					final InputStream stream = openScript(pack.getResourcePack());
					if (stream != null)
						streams.add(stream);
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
