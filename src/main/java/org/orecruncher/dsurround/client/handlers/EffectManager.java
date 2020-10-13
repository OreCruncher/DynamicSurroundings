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

package org.orecruncher.dsurround.client.handlers;

import java.util.Map;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.lib.compat.EntityLivingBaseUtil;
import org.orecruncher.lib.math.TimerEMA;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ModInfo.MOD_ID)
public class EffectManager {

	private static final EffectManager instance_ = new EffectManager();
	private static boolean isConnected = false;

	public static EffectManager instance() {
		return instance_;
	}

	private final ObjectArray<EffectHandlerBase> effectHandlers = new ObjectArray<>();
	private final Map<Class<? extends EffectHandlerBase>, EffectHandlerBase> services = new Reference2ObjectOpenHashMap<>();
	private final TimerEMA computeTime = new TimerEMA("Processing");

	private EffectManager() {
		init();
	}

	private void register(@Nonnull final EffectHandlerBase handler) {
		this.effectHandlers.add(handler);
		this.services.put(handler.getClass(), handler);
		ModBase.log().debug("Registered handler [%s]", handler.getClass().getName());
	}

	private void init() {
		// This one goes first since it sets up the state
		// for the remainder during this tick.
		register(new EnvironStateHandler());

		register(new AreaBlockEffectsHandler());
		register(new FogHandler());
		register(new ParticleSystemHandler());
		register(new BiomeSoundEffectsHandler());
		register(new AuroraEffectHandler());
		register(new WeatherHandler());

		register(new FxHandler());

		// These two go last in order
		register(SoundEffectHandler.INSTANCE);
		register(new DiagnosticHandler());
	}

	private void onConnect() {
		for (final EffectHandlerBase h : this.effectHandlers)
			h.connect0();
		MinecraftForge.EVENT_BUS.register(this);
		((DiagnosticHandler) lookupService(DiagnosticHandler.class)).addTimer(this.computeTime);
	}

	private void onDisconnect() {
		MinecraftForge.EVENT_BUS.unregister(this);
		for (final EffectHandlerBase h : this.effectHandlers)
			h.disconnect0();
	}

	@SuppressWarnings("unchecked")
	public <T> T lookupService(@Nonnull final Class<? extends EffectHandlerBase> service) {
		final EffectHandlerBase eh = this.services.get(service);
		if (eh == null)
			ModBase.log().warn("Unable to locate handler service [%s]", service.getName());
		return (T) eh;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public static void connect() {
		if (isConnected) {
			ModBase.log().warn("Attempt to initialize EffectManager when it is already initialized");
			disconnect();
		}
		instance_.onConnect();
		isConnected = true;
	}

	public static void disconnect() {
		if (isConnected) {
			instance_.onDisconnect();
			isConnected = false;
		}
	}

	protected static EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().player;
	}

	// All the strange crap I have seen...
	protected boolean checkReady(@Nonnull final TickEvent.ClientTickEvent event) {
		if (event.side == Side.SERVER || event.phase == Phase.END || Minecraft.getMinecraft().isGamePaused())
			return false;

		if (Minecraft.getMinecraft().gameSettings == null)
			return false;

		if (Minecraft.getMinecraft().getRenderManager().options == null)
			return false;

		final EntityPlayer player = getPlayer();

		return player != null && player.getEntityWorld() != null;
	}

	public void onTick(@Nonnull final TickEvent.ClientTickEvent event) {

		if (!checkReady(event))
			return;

		final long start = System.nanoTime();

		final EntityPlayer player = getPlayer();

		if (ModOptions.player.suppressPotionParticles)
			player.getDataManager().set(EntityLivingBaseUtil.getHideParticles(), true);

		final int tick = EnvironState.getTickCounter();

		for (int i = 0; i < this.effectHandlers.size(); i++) {
			final EffectHandlerBase handler = this.effectHandlers.get(i);
			final long mark = System.nanoTime();
			if (handler.doTick(tick))
				handler.process(player);
			handler.updateTimer(System.nanoTime() - mark);
		}

		this.computeTime.update(System.nanoTime() - start);
	}

	@SubscribeEvent
	public static void clientTick(@Nonnull final TickEvent.ClientTickEvent event) {
		if (isConnected)
			instance_.onTick(event);
	}
}
