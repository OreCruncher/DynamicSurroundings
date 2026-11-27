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

package org.orecruncher.dsurround.client.fx.particle.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.fx.WaterfallColumn;
import org.orecruncher.dsurround.client.sound.PositionedEmitter;
import org.orecruncher.dsurround.client.sound.SoundEffect;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.lib.random.XorShiftRandom;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The sounds of the waterfalls around the player, from the 26.x line's WaterfallSounds: a looping sound at each,
 * louder for a bigger drop and heard further (waterfall.4 and waterfall.5 have volumes above 1), but at most
 * {@link #SOUND_INSTANCE_CAP} at once, those with the most impact (loudness squared over distance squared: a loud near
 * one beats a quiet far one) and only those near enough to be heard.
 * Every waterfall had a loop of its own before, however many there were, crowding out other sounds at a big
 * cascade. The splashes ({@link ParticleWaterSplash}) are tracked while alive; ticked by the particle system handler.
 * Client thread only.
 */
@SideOnly(Side.CLIENT)
public final class WaterfallSounds {

	static final int SOUND_INSTANCE_CAP = 32;
	// How often which waterfalls are heard is worked out, in ticks
	static final int SOUND_CHECK_INTERVAL = 4;
	// A loop plays a little beyond where it can be heard, so it is there as the player comes nearer
	static final double SOUND_RANGE_MARGIN = 4D;

	private static final Set<ParticleWaterSplash> WATERFALLS = new ReferenceOpenHashSet<>();
	private static final Map<ParticleWaterSplash, PositionedEmitter> SOUNDS = new Reference2ObjectOpenHashMap<>();
	// Each tier's squared play range, from its volume in sounds.json
	private static final Map<SoundEffect, Double> RANGE_SQ = new Reference2ObjectOpenHashMap<>();
	private static final XorShiftRandom RANDOM = new XorShiftRandom();
	private static int ticks;

	private WaterfallSounds() {
	}

	/**
	 * The sound for a waterfall's strength, the height of its drop. Strength counted 2 more than the drop before;
	 * these tiers are the old ones moved down to match, so a waterfall sounds as it did. (waterfall.0 is gone, as in
	 * the 26.x line.)
	 */
	@Nonnull
	static SoundEffect soundFor(final int strength) {
		switch (Math.max(strength, 1)) {
		case 1:
			return Sounds.WATERFALL1;
		case 2:
			return Sounds.WATERFALL2;
		case 3:
		case 4:
			return Sounds.WATERFALL3;
		case 5:
		case 6:
			return Sounds.WATERFALL4;
		default:
			return Sounds.WATERFALL5;
		}
	}

	/**
	 * How far a sound is heard, as the game works it out: 16 blocks, times its volume where that is above 1 (it plays
	 * no louder than 1 close by, but carries further). waterfall.4 and waterfall.5 are louder in sounds.json, so a big
	 * waterfall is heard from further off.
	 */
	static double heardRange(final float volume) {
		return 16D * Math.max(volume, 1F);
	}

	/**
	 * The squared distance a waterfall's loop plays to, from its sound's volume in sounds.json (the game's sound
	 * registry, as resource packs left it)
	 */
	private static double rangeSq(final int strength) {
		final SoundEffect sound = soundFor(strength);
		Double rangeSq = RANGE_SQ.get(sound);
		if (rangeSq == null) {
			final double range = heardRange(volumeOf(sound)) + SOUND_RANGE_MARGIN;
			rangeSq = range * range;
			RANGE_SQ.put(sound, rangeSq);
		}
		return rangeSq;
	}

	private static float volumeOf(@Nonnull final SoundEffect sound) {
		final SoundEventAccessor accessor = Minecraft.getMinecraft().getSoundHandler()
				.getAccessor(new ResourceLocation(sound.getSoundName()));
		final Sound entry = accessor == null ? null : accessor.cloneEntry();
		return entry == null || entry == SoundHandler.MISSING_SOUND ? 1F : entry.getVolume();
	}

	/**
	 * A strength on the scale the "Waterfall Cutoff" option has always used, which counted 2 more than the drop, so a
	 * setting means what it did
	 */
	static int cutoffScale(final int strength) {
		return Math.min(strength + 2, WaterfallColumn.MAX_STRENGTH);
	}

	/**
	 * The waterfalls to hear: those within the range their sound plays to and at or above the cutoff, and of those, if
	 * there are more than {@code cap}, the {@code cap} with the most impact
	 */
	@Nonnull
	static <T> List<T> chooseHeard(@Nonnull final Collection<T> waterfalls, @Nonnull final ToIntFunction<T> strength,
			@Nonnull final ToDoubleFunction<T> distanceSq, final int cap, @Nonnull final ToDoubleFunction<T> rangeSq,
			final int cutoff) {
		final List<T> heard = new ArrayList<>();
		for (final T w : waterfalls)
			if (distanceSq.applyAsDouble(w) <= rangeSq.applyAsDouble(w)
					&& cutoffScale(strength.applyAsInt(w)) >= cutoff)
				heard.add(w);
		if (heard.size() > cap) {
			heard.sort(Comparator.comparingDouble((T w) -> {
				final double loudness = cutoffScale(strength.applyAsInt(w));
				return loudness * loudness / Math.max(distanceSq.applyAsDouble(w), 1D);
			}).reversed());
			heard.subList(cap, heard.size()).clear();
		}
		return heard;
	}

	/**
	 * A splash that is alive (idempotent)
	 */
	static void track(@Nonnull final ParticleWaterSplash waterfall) {
		WATERFALLS.add(waterfall);
	}

	/**
	 * A splash that has gone: its sound stops
	 */
	static void untrack(@Nonnull final ParticleWaterSplash waterfall) {
		WATERFALLS.remove(waterfall);
		stop(waterfall);
	}

	/**
	 * A splash whose drop changed: its sound stops, and the next check starts the one for the new strength
	 */
	static void restart(@Nonnull final ParticleWaterSplash waterfall) {
		stop(waterfall);
	}

	private static void stop(@Nonnull final ParticleWaterSplash waterfall) {
		final PositionedEmitter emitter = SOUNDS.remove(waterfall);
		if (emitter != null)
			emitter.stop();
	}

	/**
	 * Stops every waterfall sound and forgets the splashes (disconnecting)
	 */
	public static void clear() {
		SOUNDS.values().forEach(PositionedEmitter::stop);
		SOUNDS.clear();
		WATERFALLS.clear();
		// Resource packs may differ next time
		RANGE_SQ.clear();
	}

	/**
	 * Keeps the sounds playing, and every few ticks works out which waterfalls are heard, starting and stopping sounds
	 * to match
	 */
	public static void update(@Nonnull final EntityPlayer player) {
		if (WATERFALLS.isEmpty() && SOUNDS.isEmpty())
			return;
		if (++ticks % SOUND_CHECK_INTERVAL == 0)
			choose(player.getPositionEyes(1F));
		SOUNDS.values().forEach(PositionedEmitter::update);
	}

	private static void choose(@Nonnull final Vec3d listener) {
		final List<ParticleWaterSplash> heard = chooseHeard(WATERFALLS, ParticleWaterSplash::getStrength,
				w -> w.distanceSq(listener), SOUND_INSTANCE_CAP, w -> rangeSq(w.getStrength()),
				ModOptions.effects.waterfallCutoff);
		final Set<ParticleWaterSplash> keep = new ReferenceOpenHashSet<>(heard);
		SOUNDS.entrySet().removeIf(e -> {
			if (keep.contains(e.getKey()))
				return false;
			e.getValue().stop();
			return true;
		});
		for (final ParticleWaterSplash w : heard) {
			if (!SOUNDS.containsKey(w)) {
				final PositionedEmitter emitter = new PositionedEmitter(soundFor(w.getStrength()),
						new BlockPos(w.getPosX(), w.getPosY(), w.getPosZ()));
				emitter.setPitch(1F + 0.2F * (RANDOM.nextFloat() - RANDOM.nextFloat()));
				SOUNDS.put(w, emitter);
			}
		}
	}

	/**
	 * For the diagnostics overlay: waterfalls tracked, and how many are heard
	 */
	@Nonnull
	public static String diagnostic() {
		return String.format("Waterfalls: %d, sounds %d", WATERFALLS.size(), SOUNDS.size());
	}
}
