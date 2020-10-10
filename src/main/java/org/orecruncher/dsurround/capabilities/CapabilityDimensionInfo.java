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

package org.orecruncher.dsurround.capabilities;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.dimension.DimensionInfo;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfo;
import org.orecruncher.lib.capability.CapabilityProviderSerializable;
import org.orecruncher.lib.capability.CapabilityUtils;
import org.orecruncher.lib.capability.SimpleStorage;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityDimensionInfo {

	@CapabilityInject(IDimensionInfo.class)
	public static final Capability<IDimensionInfo> DIMENSION_INFO = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ModInfo.MOD_ID, "dimensioninfo");

	public static void register() {
		CapabilityManager.INSTANCE.register(IDimensionInfo.class, new SimpleStorage<>(),
				DimensionInfo::new);
	}

	public static IDimensionInfo getCapability(@Nonnull final World world) {
		return CapabilityUtils.getCapability(world, DIMENSION_INFO, null);
	}

	@Nonnull
	public static ICapabilityProvider createProvider(final IDimensionInfo data) {
		return new CapabilityProviderSerializable<>(DIMENSION_INFO, null, data);
	}

	@EventBusSubscriber(modid = ModInfo.MOD_ID)
	public static class EventHandler {
		@SubscribeEvent
		public static void attachCapabilities(@Nonnull final AttachCapabilitiesEvent<World> event) {
			
			if (!ModBase.isInitialized()) {
				ModBase.log().debug(ModOptions.Trace.WORLD_CAPABILITIES, "Attempt to attach world capability before mod is initialized - silly fake worlds.");
				return;
			}
			
			final World world = event.getObject();
			if (world != null) {
				final String side = ModBase.proxy().effectiveSide().toString();
				ModBase.log().debug(ModOptions.Trace.WORLD_CAPABILITIES, "Attaching capabilities to world [%s] (%s)",
						world.provider.getDimensionType().getName(), side);
				final DimensionInfo info = new DimensionInfo(world);
				event.addCapability(CAPABILITY_ID, createProvider(info));

				// Dump out some diagnostics...
				final ToStringHelper builder = MoreObjects.toStringHelper(info);
				builder.add("id", info.getId());
				builder.add("name", info.getName());
				builder.add("seaLevel", info.getSeaLevel());
				builder.add("cloudHeight", info.getCloudHeight());
				builder.add("skyHeight", info.getSkyHeight());
				builder.add("haze", info.hasHaze());
				builder.add("aurora", info.hasAuroras());
				builder.add("weather", info.hasWeather());
				builder.add("fog", info.hasFog());
				ModBase.log().debug(builder.toString());
			}
		}
	}

}
