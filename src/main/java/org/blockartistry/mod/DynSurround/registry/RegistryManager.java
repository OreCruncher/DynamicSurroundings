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

package org.blockartistry.mod.DynSurround.registry;

import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.event.RegistryEvent;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class RegistryManager {

	public static class RegistryType {
		public final static int SOUND = 0;
		public final static int BIOME = 1;
		public final static int BLOCK = 2;
		public final static int DIMENSION = 3;
		public final static int FOOTSTEPS = 4;
		public final static int SEASON = 5;
		public final static int ITEMS = 6;
		
		public final static int LENGTH = 7;
	}

	private static final RegistryManager[] managers = { null, null };

	static RegistryManager getManager() {
		final Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT) {
			if (managers[1] == null) {
				managers[1] = new RegistryManagerClient();
				managers[1].reload();
			}
			return managers[1];
		} else {
			if (managers[0] == null) {
				managers[0] = new RegistryManager();
				managers[0].reload();
			}
			return managers[0];
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Registry> T get(@Nonnull int type) {
		return (T) getManager().getRegistry(type);
	}

	public static void reloadResources() {
		// Reload can be called on either side so make sure we queue
		// up a scheduled task appropriately.
		if (managers[0] != null) {
			final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if (server == null) {
				managers[0] = null;
			} else {
				server.addScheduledTask(new Runnable() {
					public void run() {
						managers[0].reload();
					}
				});
			}
		}

		if (managers[1] != null) {
			final Minecraft mc = Minecraft.getMinecraft();
			if (mc == null) {
				managers[1] = null;
			} else {
				mc.addScheduledTask(new Runnable() {
					public void run() {
						managers[1].reload();
					}
				});
			}
		}
	}

	protected final Side side;
	protected final Registry[] registries = new Registry[RegistryType.LENGTH];

	RegistryManager() {
		this(Side.SERVER);
	}

	RegistryManager(final Side side) {
		this.side = side;
		this.registries[RegistryType.DIMENSION] = new DimensionRegistry(side);
		this.registries[RegistryType.BIOME] = new BiomeRegistry(side);
		this.registries[RegistryType.SOUND] = new SoundRegistry(side);
		this.registries[RegistryType.SEASON] = new SeasonRegistry(side);
	}

	void reload() {
		for (final Registry r : this.registries)
			if (r != null)
				r.init();

		new DataScripts(this.side).execute(getAdditionalScripts());

		for (final Registry r : this.registries)
			if (r != null)
				r.initComplete();

		MinecraftForge.EVENT_BUS.post(new RegistryEvent.Reload(this.side));
	}

	@SuppressWarnings("unchecked")
	public <T> T getRegistry(final int type) {
		return (T) this.registries[type];
	}

	public List<InputStream> getAdditionalScripts() {
		return ImmutableList.of();
	}

}
