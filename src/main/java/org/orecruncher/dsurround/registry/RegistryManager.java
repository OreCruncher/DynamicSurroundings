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
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.registry.acoustics.AcousticRegistry;
import org.orecruncher.dsurround.registry.biome.BiomeRegistry;
import org.orecruncher.dsurround.registry.blockstate.BlockStateRegistry;
import org.orecruncher.dsurround.registry.config.ConfigData;
import org.orecruncher.dsurround.registry.dimension.DimensionRegistry;
import org.orecruncher.dsurround.registry.effect.EffectRegistry;
import org.orecruncher.dsurround.registry.footstep.FootstepsRegistry;
import org.orecruncher.dsurround.registry.item.ItemRegistry;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;
import org.orecruncher.lib.Singleton;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.task.Scheduler;

import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = ModInfo.MOD_ID)
public final class RegistryManager {

	@SideOnly(Side.CLIENT)
	public static SoundRegistry SOUND;
	@SideOnly(Side.CLIENT)
	public static AcousticRegistry ACOUSTICS;
	@SideOnly(Side.CLIENT)
	public static BiomeRegistry BIOME;
	@SideOnly(Side.CLIENT)
	public static BlockStateRegistry BLOCK;
	@SideOnly(Side.CLIENT)
	public static ItemRegistry ITEMS;
	@SideOnly(Side.CLIENT)
	public static FootstepsRegistry FOOTSTEPS;
	@SideOnly(Side.CLIENT)
	public static EffectRegistry EFFECTS;

	// This guy is the only one shared between client and server
	public static DimensionRegistry DIMENSION;

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

	final static ObjectArray<Registry> REGISTRIES = new ObjectArray<>(8);

	/**
	 * Generally speaking the reload of the registry is handled by the client thread
	 * if running as a client. If on a dedicated server, though, it will be handled
	 * by the server thread.
	 *
	 * @return true if the thread is to handle the reload; false otherwise.
	 */
	private static boolean handleReload() {
		return ModBase.proxy().isRunningAsServer() || ModBase.proxy().effectiveSide() == Side.CLIENT;
	}

	/**
	 * Should be called during postInit
	 */
	public static void initialize() {

		if (!ModBase.proxy().isRunningAsServer()) {
			// Sound is first because other registries depend on it
			SOUND = new SoundRegistry();
			ACOUSTICS = new AcousticRegistry();
			BIOME = new BiomeRegistry();
			BLOCK = new BlockStateRegistry();
			FOOTSTEPS = new FootstepsRegistry();
			ITEMS = new ItemRegistry();
			EFFECTS = new EffectRegistry();
		}

		DIMENSION = new DimensionRegistry();

		load();
	}

	/**
	 * Called by the command routines to reload the configuration
	 */
	public static void doReload() {
		if (ModBase.proxy().isRunningAsServer()) {
			load();
		} else {
			Scheduler.schedule(Side.CLIENT, () -> load());
		}
	}

	/**
	 * The mod configuration file may have changed. If it did then we need to reload
	 * the registries.
	 *
	 * @param event
	 */
	@SubscribeEvent
	public static void onReload(@Nonnull final OnConfigChangedEvent event) {
		if (event.getModID().equals(ModInfo.MOD_ID) && handleReload()) {
			load();
		}
	}

	private static void load() {
		DATA.clear();
		REGISTRIES.forEach(r -> r.initialize(DATA.get()));
		REGISTRIES.forEach(Registry::complete);
	}
}
