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

package org.blockartistry.DynSurround.client.handlers;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.collections.ObjectArray;
import org.blockartistry.lib.compat.EntityLivingBaseUtil;
import org.blockartistry.lib.math.TimerEMA;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EffectManager {

	private static EffectManager instance_ = null;

	public static EffectManager instance() {
		return instance_;
	}

	private final ObjectArray<EffectHandlerBase> effectHandlers = new ObjectArray<>();
	private final Map<Class<? extends EffectHandlerBase>, EffectHandlerBase> services = new IdentityHashMap<>();
	private TimerEMA computeTime;

	private EffectManager() {
	}

	private void register(@Nonnull final EffectHandlerBase handler) {
		this.effectHandlers.add(handler);
		this.services.put(handler.getClass(), handler);
	}

	private void init() {
		// This one goes first since it sets up the state
		// for the remainder during this tick.
		register(new EnvironStateHandler());

		register(new AreaBlockEffectsHandler());
		register(new EnvironmentEffectHandler());
		register(new ParticleSystemHandler());
		register(new BiomeSoundEffectsHandler());
		register(new EntityEmojiHandler());
		register(new AuroraEffectHandler());
		register(new SpeechBubbleHandler());
		register(new WeatherHandler());

		register(new FxHandler());

		// These two go last in order
		register(SoundEffectHandler.INSTANCE);
		register(new DiagnosticHandler());

		for (final EffectHandlerBase h : this.effectHandlers)
			h.connect0();

		this.computeTime = new TimerEMA("Processing");
		((DiagnosticHandler) lookupService(DiagnosticHandler.class)).addTimer(this.computeTime);
	}

	private void fini() {
		for (final EffectHandlerBase h : this.effectHandlers)
			h.disconnect0();

		this.effectHandlers.clear();
		this.services.clear();
		
		this.computeTime = null;
	}

	@SuppressWarnings("unchecked")
	public <T> T lookupService(@Nonnull final Class<? extends EffectHandlerBase> service) {
		return (T) this.services.get(service);
	}

	public static void register() {
		instance_ = new EffectManager();
		instance_.init();
		MinecraftForge.EVENT_BUS.register(instance_);
	}

	public static void unregister() {
		if (instance_ != null) {
			MinecraftForge.EVENT_BUS.unregister(instance_);
			instance_.fini();
			instance_ = null;
		}
	}

	protected EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	// All the strange crap I have seen...
	protected boolean checkReady(@Nonnull final TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().gameSettings == null)
			return false;

		if (Minecraft.getMinecraft().getRenderManager().options == null)
			return false;

		final EntityPlayer player = getPlayer();

		if (player == null || player.getEntityWorld() == null)
			return false;

		return true;
	}

	@SubscribeEvent
	public void onTick(@Nonnull final TickEvent.ClientTickEvent event) {

		if (event.side == Side.SERVER || event.phase == Phase.END || Minecraft.getMinecraft().isGamePaused())
			return;

		if (!checkReady(event))
			return;

		final long start = System.nanoTime();

		if (ModOptions.player.suppressPotionParticles)
			getPlayer().getDataManager().set(EntityLivingBaseUtil.getHideParticles(), true);

		final int tick = EnvironState.getTickCounter();

		for (int i = 0; i < this.effectHandlers.size(); i++) {
			final EffectHandlerBase handler = this.effectHandlers.get(i);
			final long mark = System.nanoTime();
			if (handler.doTick(tick))
				handler.process(getPlayer());
			handler.updateTimer(System.nanoTime() - mark);
		}

		this.computeTime.update(System.nanoTime() - start);
	}

}
