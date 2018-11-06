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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.effects.EntityEffectHandler;
import org.orecruncher.dsurround.client.effects.EntityEffectLibrary;
import org.orecruncher.dsurround.client.effects.EventEffectLibrary;
import org.orecruncher.dsurround.client.effects.IParticleHelper;
import org.orecruncher.dsurround.client.effects.ISoundHelper;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.handlers.effects.CraftingSoundEffect;
import org.orecruncher.dsurround.client.handlers.effects.EntityBowSoundEffect;
import org.orecruncher.dsurround.client.handlers.effects.EntityChatEffect;
import org.orecruncher.dsurround.client.handlers.effects.EntityFootprintEffect;
import org.orecruncher.dsurround.client.handlers.effects.EntityHealthPopoffEffect;
import org.orecruncher.dsurround.client.handlers.effects.EntitySwingEffect;
import org.orecruncher.dsurround.client.handlers.effects.FrostBreathEffect;
import org.orecruncher.dsurround.client.handlers.effects.PlayerToolBarSoundEffect;
import org.orecruncher.dsurround.client.handlers.effects.VillagerChatEffect;
import org.orecruncher.dsurround.client.sound.BasicSound;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.event.ReloadEvent;
import org.orecruncher.dsurround.lib.sound.ITrackedSound;
import org.orecruncher.lib.ThreadGuard;
import org.orecruncher.lib.ThreadGuard.Action;
import org.orecruncher.lib.collections.CollectionUtils;
import org.orecruncher.lib.gfx.ParticleHelper;
import org.orecruncher.lib.math.TimerEMA;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FxHandler extends EffectHandlerBase {

	private static final IParticleHelper PARTICLE_HELPER = (p) -> ParticleHelper.addParticle(p);
	private static final ISoundHelper SOUND_HELPER = new ISoundHelper() {
		@Override
		public String playSound(@Nonnull final ITrackedSound sound) {
			return SoundEffectHandler.INSTANCE.playSound((BasicSound<?>) sound);
		}

		@Override
		public void stopSound(@Nonnull final ITrackedSound sound) {
			SoundEffectHandler.INSTANCE.stopSound((BasicSound<?>) sound);
		}
	};

	// Used to process handler entries during the client tick
	private static final Predicate<? super Entry<UUID, EntityEffectHandler>> HANDLER_UPDATE_REMOVE = e -> {
		final EntityEffectHandler handler = e.getValue();
		handler.update();
		return !handler.isAlive();
	};

	private static final EntityEffectLibrary library = new EntityEffectLibrary(PARTICLE_HELPER, SOUND_HELPER);

	static {
		library.register(FrostBreathEffect.DEFAULT_FILTER, new FrostBreathEffect.Factory());
		library.register(EntityChatEffect.DEFAULT_FILTER, new EntityChatEffect.Factory());
		library.register(VillagerChatEffect.DEFAULT_FILTER, new VillagerChatEffect.Factory());
		library.register(PlayerToolBarSoundEffect.DEFAULT_FILTER, new PlayerToolBarSoundEffect.Factory());
		library.register(EntityFootprintEffect.DEFAULT_FILTER, new EntityFootprintEffect.Factory());
		library.register(EntitySwingEffect.DEFAULT_FILTER, new EntitySwingEffect.Factory());
		library.register(EntityBowSoundEffect.DEFAULT_FILTER, new EntityBowSoundEffect.Factory());
		library.register(EntityHealthPopoffEffect.DEFAULT_FILTER, new EntityHealthPopoffEffect.Factory());
	}

	private final Map<UUID, EntityEffectHandler> handlers = new Object2ObjectOpenHashMap<>(256);
	private final EventEffectLibrary eventLibrary = new EventEffectLibrary(PARTICLE_HELPER, SOUND_HELPER);

	private final TimerEMA compute = new TimerEMA("FxHandler Updates");
	private long nanos;

	private final ThreadGuard guard = new ThreadGuard(ModBase.log(), Side.CLIENT, "FxHandler")
			.setAction(Action.EXCEPTION);

	public FxHandler() {
		super("Special Effects");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		CollectionUtils.removeIf(this.handlers, HANDLER_UPDATE_REMOVE);
		this.compute.update(this.nanos);
		this.nanos = 0;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		final StringBuilder builder = new StringBuilder();
		builder.append("EffectHandlers: ").append(this.handlers.size());
		event.output.add(builder.toString());
	}

	/**
	 * Used for diagnostics to get data about an Entity.
	 *
	 * @param entity
	 *            Entity to get information on
	 * @return A list of EntityEffects, if any
	 */
	public List<String> getEffects(@Nonnull final Entity entity) {
		final EntityEffectHandler eh = this.handlers.get(entity.getUniqueID());
		if (eh != null) {
			return eh.getAttachedEffects();
		}
		return ImmutableList.of();
	}

	/**
	 * Whenever an Entity updates make sure we have an appropriate handler, and
	 * update it's state if necessary.
	 */
	@SubscribeEvent(receiveCanceled = true)
	public void onLivingUpdate(@Nonnull final LivingUpdateEvent event) {
		final Entity entity = event.getEntity();
		if (entity == null || !entity.getEntityWorld().isRemote)
			return;

		this.guard.check("onLivingUpdate");

		final long start = System.nanoTime();

		final double distanceThreshold = ModOptions.effects.specialEffectRange * ModOptions.effects.specialEffectRange;
		final boolean inRange = entity.getDistanceSq(EnvironState.getPlayer()) <= distanceThreshold
				&& entity.dimension == EnvironState.getDimensionId();

		final EntityEffectHandler handler = this.handlers.get(entity.getUniqueID());
		if (handler != null && !inRange) {
			handler.die();
		} else if (handler == null && inRange && entity.isEntityAlive()) {
			this.handlers.put(entity.getUniqueID(), library.create(entity).get());
		}

		this.nanos += (System.nanoTime() - start);
	}

	protected void clearHandlers() {
		this.handlers.values().forEach(EntityEffectHandler::die);
		this.handlers.clear();
	}

	/**
	 * Check if the player joining the world is the one sitting at the keyboard. If
	 * so we need to wipe out the existing handler list because the dimension
	 * changed.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityJoin(@Nonnull final EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote && event.getEntity() instanceof EntityPlayerSP)
			clearHandlers();
	}

	/**
	 * Wipe out the effect handlers on a registry reload. Possible something changed
	 * in the effect configuration.
	 */
	@SubscribeEvent
	public void registryReload(@Nonnull final ReloadEvent.Registry event) {
		if (event.side == Side.CLIENT)
			clearHandlers();
	}

	@Override
	public void onConnect() {
		clearHandlers();
		this.eventLibrary.register(new CraftingSoundEffect());
		((DiagnosticHandler) EffectManager.instance().lookupService(DiagnosticHandler.class)).addTimer(this.compute);
	}

	@Override
	public void onDisconnect() {
		clearHandlers();
		this.eventLibrary.cleanup();
	}
}
