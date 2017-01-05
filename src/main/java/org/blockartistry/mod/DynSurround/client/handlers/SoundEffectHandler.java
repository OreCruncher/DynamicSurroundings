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

package org.blockartistry.mod.DynSurround.client.handlers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.event.DiagnosticEvent;
import org.blockartistry.mod.DynSurround.client.event.RegistryEvent;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.sound.Emitter;
import org.blockartistry.mod.DynSurround.client.sound.SoundEffect;
import org.blockartistry.mod.DynSurround.client.sound.SpotSound;
import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModEnvironment;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.registry.SoundRegistry;
import org.blockartistry.mod.DynSurround.util.SoundUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystemConfig;

@SideOnly(Side.CLIENT)
public class SoundEffectHandler extends EffectHandlerBase implements ISoundEventListener {

	private static final int AGE_THRESHOLD_TICKS = 5;
	private static final int SOUND_QUEUE_SLACK = 6;

	private static final SoundEvent THUNDER = SoundUtils
			.getOrRegisterSound(new ResourceLocation(DSurround.RESOURCE_ID, "thunder"));

	public static SoundEffectHandler INSTANCE = null;

	private final Map<SoundEffect, Emitter> emitters = new HashMap<SoundEffect, Emitter>();
	private final List<SpotSound> pending = new ArrayList<SpotSound>();

	public SoundEffectHandler() {
		INSTANCE = this;
	}

	@Override
	public String getHandlerName() {
		return "SoundEffectHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		// Dead players hear no sounds...
		if (!player.isEntityAlive()) {
			clearSounds();
			return;
		}

		for (final Emitter emitter : this.emitters.values())
			emitter.update();

		final Iterator<SpotSound> pitr = this.pending.iterator();
		while (pitr.hasNext()) {
			final SpotSound sound = pitr.next();
			if (sound.getTickAge() >= AGE_THRESHOLD_TICKS) {
				ModLog.debug("AGING: " + sound.toString());
				pitr.remove();
			} else if (sound.getTickAge() >= 0 && canFitSound()) {
				playSound(sound);
				pitr.remove();
			}
		}
	}

	@Override
	public void onConnect() {
		clearSounds();
		Minecraft.getMinecraft().getSoundHandler().addListener(this);
	}

	@Override
	public void onDisconnect() {
		Minecraft.getMinecraft().getSoundHandler().removeListener(this);
		clearSounds();
		INSTANCE = null;
	}

	public void clearSounds() {
		for (final Emitter emit : this.emitters.values())
			emit.fade();
		this.emitters.clear();
		this.pending.clear();
	}

	public void queueAmbientSounds(@Nonnull final List<SoundEffect> sounds) {
		// Need to remove sounds that are active but not
		// in the incoming list
		final List<SoundEffect> active = new ArrayList<SoundEffect>(this.emitters.keySet());
		for (final SoundEffect effect : active) {
			if (!sounds.contains(effect))
				this.emitters.remove(effect).fade();
			else {
				final Emitter emitter = this.emitters.get(effect);
				SoundEffect incoming = null;
				for (final SoundEffect sound : sounds)
					if (sound.equals(effect)) {
						incoming = sound;
						break;
					}
				emitter.setVolume(incoming.getVolume());
				sounds.remove(effect);
			}
		}

		// Add sounds from the incoming list that are not
		// active.
		for (final SoundEffect sound : sounds)
			this.emitters.put(sound, new Emitter(sound));
	}

	public int currentSoundCount() {
		return Minecraft.getMinecraft().getSoundHandler().sndManager.playingSounds.size();
	}

	public int maxSoundCount() {
		return SoundSystemConfig.getNumberNormalChannels() + SoundSystemConfig.getNumberStreamingChannels();
	}

	private boolean canFitSound() {
		return currentSoundCount() < (SoundSystemConfig.getNumberNormalChannels() - SOUND_QUEUE_SLACK);
	}

	public boolean isSoundPlaying(@Nonnull final ISound sound) {
		// Have to find the hidden sound in the sound engine to see if Minecraft
		// is still working with it.
		final net.minecraft.client.audio.SoundManager mgr = Minecraft.getMinecraft().getSoundHandler().sndManager;
		return mgr.isSoundPlaying(sound) || mgr.playingSounds.containsValue(sound)
				|| mgr.delayedSounds.containsKey(sound);
	}

	public boolean isSoundPlaying(@Nonnull final String soundId) {
		if (StringUtils.isEmpty(soundId))
			return false;
		final net.minecraft.client.audio.SoundManager mgr = Minecraft.getMinecraft().getSoundHandler().sndManager;
		return mgr.playingSounds.containsKey(soundId);
	}

	private ISound currentSound;
	private String soundId;

	@Nullable
	public String playSound(@Nonnull final ISound sound) {
		if (sound != null) {
			if (ModOptions.enableDebugLogging)
				ModLog.debug("PLAYING: " + sound.toString());
			this.currentSound = sound;
			this.soundId = null;
			Minecraft.getMinecraft().getSoundHandler().playSound(sound);
		} else {
			this.soundId = null;
		}
		this.currentSound = null;
		return this.soundId;
	}

	/**
	 * This event hook attempts to associate the internal UUID of the sound play
	 * event with a sound.
	 */
	@SubscribeEvent
	public void onSoundSourceEvent(@Nonnull final SoundSourceEvent event) {
		if (event.getSound() == this.currentSound)
			this.soundId = event.getUuid();
	}

	@SubscribeEvent
	public void soundPlay(@Nonnull final PlaySoundEvent e) {
		// Don't patch up - Weather2 has it's own sound
		if (ModEnvironment.Weather2.isLoaded())
			return;

		if (e.getName().equals("entity.lightning.thunder")) {
			final ISound sound = e.getSound();
			final ISound newSound = new PositionedSoundRecord(THUNDER, sound.getCategory(), 10000.0F, 1.0F,
					sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
			e.setResultSound(newSound);
		}
	}

	@Nullable
	public String playSoundAtPlayer(@Nullable EntityPlayer player, @Nonnull final SoundEffect sound,
			@Nullable final SoundCategory categoryOverride) {

		if (player == null)
			player = EnvironState.getPlayer();

		String soundId = null;

		if (canFitSound()) {
			final SpotSound s = new SpotSound(player, sound, categoryOverride);
			soundId = playSound(s);
		}
		return soundId;
	}

	/**
	 * Estimate whether a sound can be heard based on it's volume and distance.
	 */
	public static boolean canSoundBeHeard(@Nonnull final BlockPos soundPos, final float volume) {
		if (volume == 0.0F)
			return false;
		final BlockPos playerPos = EnvironState.getPlayerPosition();
		final double distanceSq = playerPos.distanceSq(soundPos);
		final double DROPOFF = 16 * 16;
		if (distanceSq <= DROPOFF)
			return true;
		final double power = volume * DROPOFF;
		return distanceSq <= power;
	}

	@Nullable
	public String playSoundAt(@Nonnull final BlockPos pos, @Nonnull final SoundEffect sound, final int tickDelay,
			@Nullable final SoundCategory categoryOverride) {

		if (!canSoundBeHeard(pos, sound.getVolume()))
			return null;

		String soundId = null;

		if(tickDelay > 0 || canFitSound()) {
			final SpotSound s = new SpotSound(pos, sound, tickDelay, categoryOverride);
	
			if (tickDelay > 0)
				this.pending.add(s);
			else
				soundId = playSound(s);
		}
		
		return soundId;
	}

	public static void configureSound() {
		int totalChannels = -1;

		try {
			final boolean create = !AL.isCreated();
			if (create)
				AL.create();
			final IntBuffer ib = BufferUtils.createIntBuffer(1);
			ALC10.alcGetInteger(AL.getDevice(), ALC11.ALC_MONO_SOURCES, ib);
			totalChannels = ib.get(0);
			if (create)
				AL.destroy();
		} catch (final Throwable e) {
			e.printStackTrace();
		}

		int normalChannels = ModOptions.normalSoundChannelCount;
		int streamChannels = ModOptions.streamingSoundChannelCount;

		if (ModOptions.autoConfigureChannels && totalChannels > 64) {
			final int maxCount = Math.max((totalChannels + 1) / 2, 32);
			normalChannels = MathHelper.floor_float(maxCount * 0.875F);
			streamChannels = maxCount - normalChannels;
		}

		ModLog.info("Sound channels: %d normal, %d streaming (total avail: %s)", normalChannels, streamChannels,
				totalChannels == -1 ? "UNKNOWN" : Integer.toString(totalChannels));
		SoundSystemConfig.setNumberNormalChannels(normalChannels);
		SoundSystemConfig.setNumberStreamingChannels(streamChannels);
	}

	// Not entirely sure why they changed things. This reads the mods
	// sounds.json
	// and forces registration of all the mod sounds. Code generally comes from
	// the Minecraft sound processing logic.
	public static void initializeRegistry() {
		final ParameterizedType TYPE = new ParameterizedType() {
			public Type[] getActualTypeArguments() {
				return new Type[] { String.class, Object.class };
			}

			public Type getRawType() {
				return Map.class;
			}

			public Type getOwnerType() {
				return null;
			}
		};

		try (final InputStream stream = SoundEffectHandler.class.getResourceAsStream("/assets/dsurround/sounds.json")) {
			if (stream != null) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> sounds = (Map<String, Object>) new Gson()
						.fromJson(new InputStreamReader(stream), TYPE);
				for (final String s : sounds.keySet())
					SoundUtils.getOrRegisterSound(new ResourceLocation(DSurround.RESOURCE_ID, s));

			}
		} catch (final Throwable t) {
			ModLog.error("Unable to read the mod sound file!", t);
		}

	}

	// Redirect hook from Minecraft's SoundEffectHandler so we can scale the
	// volume
	// for each individual sound.
	public static float getClampedVolume(@Nonnull final ISound sound) {
		float result = 0.0F;
		if (sound == null) {
			ModLog.warn("getNormalizedVolume(): Null sound parameter");
		} else {
			final String soundName = sound.getSoundLocation().toString();
			try {
				final SoundRegistry registry = RegistryManager.get(RegistryType.SOUND);
				final float volumeScale = registry.getVolumeScale(soundName);
				result = (float) MathHelper.clamp_double(
						(double) sound.getVolume() * (double) getSoundCategoryVolume(sound.getCategory()) * volumeScale,
						0.0D, 1.0D);
			} catch (final Throwable t) {
				ModLog.error("getNormalizedVolume(): Unable to calculate " + soundName, t);
			}
		}
		return result;
	}

	private static float getSoundCategoryVolume(@Nullable final SoundCategory category) {
		return category != null && category != SoundCategory.MASTER
				? Minecraft.getMinecraft().gameSettings.getSoundLevel(category) : 1.0F;
	}

	/*
	 * Fired when the underlying biome config is reloaded.
	 */
	@SubscribeEvent
	public void registryReloadEvent(@Nonnull final RegistryEvent.Reload event) {
		clearSounds();
	}

	/*
	 * Fired when the player joins a world, such as when the dimension changes.
	 */
	@SubscribeEvent
	public void playerJoinWorldEvent(@Nonnull final EntityJoinWorldEvent event) {
		if (event.getEntity().worldObj.isRemote && EnvironState.isPlayer(event.getEntity()))
			clearSounds();
	}

	@SubscribeEvent
	public void diagnostics(@Nonnull final DiagnosticEvent.Gather event) {
		final StringBuilder builder = new StringBuilder();
		builder.append("SoundSystem: ").append(currentSoundCount()).append('/').append(maxSoundCount());
		event.output.add(builder.toString());

		for (final SoundEffect effect : this.emitters.keySet())
			event.output.add("EMITTER: " + effect.toString() + "[vol:" + this.emitters.get(effect).getVolume() + "]");
		for (final SpotSound effect : pending)
			event.output
					.add((effect.getTickAge() < 0 ? "DELAYED: " : "PENDING: ") + effect.getSoundEffect().toString());
	}

	@Override
	public void soundPlay(@Nonnull final ISound soundIn, @Nonnull final SoundEventAccessor accessor) {
		if (!ModOptions.enableDebugLogging)
			return;

		if (soundIn.getSoundLocation().getResourceDomain().equals(DSurround.RESOURCE_ID))
			return;

		ModLog.debug("Sound callback: [%s]", soundIn.getSoundLocation().toString());
	}

}
