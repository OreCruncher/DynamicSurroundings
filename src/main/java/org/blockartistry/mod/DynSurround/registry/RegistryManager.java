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

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.event.RegistryEvent;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public final class RegistryManager {

	public static enum RegistryType {
		SOUND(0), BIOME(1), BLOCK(2), DIMENSION(3), FOOTSTEPS(4);

		private final int id;

		RegistryType(final int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}
	}

	private static final RegistryManager[] managers = { null, null };

	static RegistryManager getManager() {
		final Side side = FMLCommonHandler.instance().getEffectiveSide();
		final int idx = side == Side.CLIENT ? 1 : 0;
		if (managers[idx] == null) {
			// Separate lines. The initialize will cause a recursion into
			// this method and the array slot needs to be initialized to
			// avoid infinite recursion.
			managers[idx] = new RegistryManager(side);
			managers[idx].reload();
		}
		// Do a reload/check to ensure state
		return managers[idx];
	}

	@SuppressWarnings("unchecked")
	public static <T extends Registry> T get(@Nonnull RegistryType type) {
		return (T) getManager().getRegistry(type);
	}

	public static void reloadResources() {
		// Reload the server side.  This should be called
		// by the server thread and not the client because
		// the /ds command runs server side.
		if (managers[0] != null) {
			managers[0].reload();
		}
		
		// Schedule reload on the client thread
		if (managers[1] != null) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				public void run() {
					managers[1].reload();
				}
			});
		}
	}

	private final Side side;
	private final Registry[] registries = new Registry[RegistryType.values().length];

	RegistryManager(final Side side) {
		this.side = side;
		this.registries[RegistryType.DIMENSION.getId()] = new DimensionRegistry();
		this.registries[RegistryType.BIOME.getId()] = new BiomeRegistry();
		this.registries[RegistryType.SOUND.getId()] = new SoundRegistry();

		if(this.side == Side.CLIENT) {
			this.registries[RegistryType.BLOCK.getId()] = new BlockRegistry();
			this.registries[RegistryType.FOOTSTEPS.getId()] = new FootstepsRegistry();
		}
	}

	void reload() {
		for (final Registry r : this.registries)
			if(r != null)
				r.init();

		new DataScripts(this.side).execute(null);

		for (final Registry r : this.registries)
			if(r != null)
				r.initComplete();

		MinecraftForge.EVENT_BUS.post(new RegistryEvent.Reload(this.side));
	}

	@SuppressWarnings("unchecked")
	public <T> T getRegistry(final RegistryType type) {
		return (T) this.registries[type.getId()];
	}

}
