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

package org.orecruncher.dsurround.proxy;

import java.util.Arrays;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilitySeasonInfo;
import org.orecruncher.dsurround.capabilities.CapabilitySpeechData;
import org.orecruncher.dsurround.client.fx.ParticleCollections;
import org.orecruncher.dsurround.client.fx.particle.ParticleDripOverride;
import org.orecruncher.dsurround.client.gui.HumDinger;
import org.orecruncher.dsurround.client.handlers.EffectManager;
import org.orecruncher.dsurround.client.hud.GuiHUDHandler;
import org.orecruncher.dsurround.client.hud.InspectionHUD;
import org.orecruncher.dsurround.client.hud.LightLevelHUD;
import org.orecruncher.dsurround.client.keyboard.KeyHandler;
import org.orecruncher.dsurround.client.renderer.AnimaniaBadge;
import org.orecruncher.dsurround.client.sound.BackgroundMute;
import org.orecruncher.dsurround.client.sound.SoundEngine;
import org.orecruncher.dsurround.client.weather.RenderWeather;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.commands.CommandCalc;
import org.orecruncher.dsurround.event.WorldEventDetector;
import org.orecruncher.dsurround.lib.compat.ModEnvironment;
import org.orecruncher.dsurround.registry.RegistryDataEvent;
import org.orecruncher.lib.ForgeUtils;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.chunk.ClientChunkCache;
import org.orecruncher.lib.task.Scheduler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy implements ISelectiveResourceReloadListener {

	@Override
	protected void registerLanguage() {
		Localization.initialize(Side.CLIENT, ModInfo.MOD_ID);
	}

	@Override
	protected void eventBusRegistrations() {
		super.eventBusRegistrations();

		register(HumDinger.class);
		register(InspectionHUD.class);
		register(LightLevelHUD.class);
		register(KeyHandler.class);
		register(BackgroundMute.class);
		register(RenderWeather.class);
		register(Weather.class);
		register(WorldEventDetector.class);
		register(LightLevelHUD.class);
		register(ParticleCollections.class);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public boolean isRunningAsServer() {
		return false;
	}

	@Override
	public Side effectiveSide() {
		return FMLCommonHandler.instance().getEffectiveSide();
	}

	@Override
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		super.preInit(event);
		CapabilitySpeechData.register();
		CapabilitySeasonInfo.register();
	}

	@Override
	public void init(@Nonnull final FMLInitializationEvent event) {
		super.init(event);

		KeyHandler.init();
		ParticleDripOverride.register();

		ClientCommandHandler.instance.registerCommand(new CommandCalc());

		if (ModOptions.effects.disableWaterSuspendParticle)
			Minecraft.getMinecraft().effectRenderer.registerParticle(EnumParticleTypes.SUSPENDED.getParticleID(), null);

		if (ModEnvironment.AmbientSounds.isLoaded())
			SoundEngine.configureSound(null);
	}

	@Override
	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
		super.postInit(event);

		// Patch up metadata
		final ModMetadata data = ForgeUtils.getModMetadata(ModInfo.MOD_ID);
		if (data != null) {
			data.name = Localization.format("dsurround.metadata.Name");
			data.credits = Localization.format("dsurround.metadata.Credits");
			data.description = Localization.format("dsurround.metadata.Description");
			data.authorList = Arrays.asList(StringUtils.split(Localization.format("dsurround.metadata.Authors"), ','));
		}

		// Register for resource load events
		final IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		((IReloadableResourceManager) resourceManager).registerReloadListener(this);

		if (ModEnvironment.Animania.isLoaded())
			AnimaniaBadge.intitialize();

		int r = Math.max(32, ModOptions.effects.specialEffectRange);
		r = Math.max(r, ModOptions.huds.lightlevel.llBlockRange);
		r += 2;
		ClientChunkCache.initialize(r, !ModOptions.general.enableClientChunkCaching);
	}

	@Override
	public void clientConnect(@Nonnull final ClientConnectedToServerEvent event) {
		Scheduler.schedule(Side.CLIENT, () -> {
			EffectManager.connect();
			GuiHUDHandler.register();
			Weather.register(ModBase.isInstalledOnServer());
			ProxyClient.this.connectionTime = System.currentTimeMillis();
		});
	}

	@Override
	public void clientDisconnect(@Nonnull final ClientDisconnectionFromServerEvent event) {
		Scheduler.schedule(Side.CLIENT, () -> {
			EffectManager.disconnect();
			GuiHUDHandler.unregister();
			Weather.unregister();
			ProxyClient.this.connectionTime = 0;
		});
	}

	@Override
	public void onResourceManagerReload(@Nonnull final IResourceManager resourceManager,
			@Nonnull final Predicate<IResourceType> resourcePredicate) {
		if (resourcePredicate.test(VanillaResourceType.SOUNDS)) {
			MinecraftForge.EVENT_BUS.post(new RegistryDataEvent.Resources(resourceManager));
		}
	}

	@Override
	public IThreadListener getThreadListener(@Nonnull final MessageContext context) {
		if (context.side.isClient()) {
			return Minecraft.getMinecraft();
		} else {
			return context.getServerHandler().player.getServer();
		}
	}

}
