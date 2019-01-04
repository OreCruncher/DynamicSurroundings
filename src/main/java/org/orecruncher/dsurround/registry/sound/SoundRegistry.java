/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.registry.sound;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.sound.ConfigSoundInstance;
import org.orecruncher.dsurround.client.sound.SoundBuilder;
import org.orecruncher.dsurround.client.sound.SoundConfigProcessor;
import org.orecruncher.dsurround.client.sound.SoundEngine;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.lib.compat.PositionedSoundUtil;
import org.orecruncher.lib.math.MathStuff;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoundRegistry extends Registry {

	// Mod added categories
	public static final SoundCategory FOOTSTEPS = SoundCategory.valueOf("DS_FOOTSTEPS");
	public static final SoundCategory BIOME = SoundCategory.valueOf("DS_BIOME");

	public static final float MIN_SOUNDFACTOR = 0F;
	public static final float MAX_SOUNDFACTOR = 4F;
	public static final float DEFAULT_SOUNDFACTOR = 1F;

	private final Set<ResourceLocation> blockedSounds = new ObjectOpenHashSet<>(32);
	private final Object2IntOpenHashMap<ResourceLocation> soundCull = new Object2IntOpenHashMap<>(32);
	private final Object2FloatOpenHashMap<ResourceLocation> volumeControl = new Object2FloatOpenHashMap<>(32);
	private final Map<ResourceLocation, SoundMetadata> soundMetadata = new Object2ObjectOpenHashMap<>();
	private final Map<ResourceLocation, SoundEvent> myRegistry = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<ResourceLocation, SoundEvent> replacements = new Object2ObjectOpenHashMap<>();

	public SoundRegistry() {
		super("Sound Registry");
		this.volumeControl.defaultReturnValue(DEFAULT_SOUNDFACTOR);
	}

	@Override
	protected void preInit() {
		this.soundCull.clear();
		this.blockedSounds.clear();
		this.volumeControl.clear();
		this.soundMetadata.clear();
		this.myRegistry.clear();
		this.replacements.clear();

		bakeSoundRegistry();

		for (final String line : ModOptions.sound.soundSettings) {
			final String[] parts = line.split(" ");
			if (parts.length < 2) {
				ModBase.log().warn("Missing tokens in sound settings? (%s)", line);
			} else {
				final ResourceLocation res = new ResourceLocation(parts[0]);
				for (int i = 1; i < parts.length; i++) {
					if ("cull".compareToIgnoreCase(parts[i]) == 0) {
						this.soundCull.put(res, -ModOptions.sound.soundCullingThreshold);
					} else if ("block".compareToIgnoreCase(parts[i]) == 0) {
						this.blockedSounds.add(res);
					} else {
						try {
							final int volume = Integer.parseInt(parts[i]);
							this.volumeControl.put(res,
									MathStuff.clamp(volume / 100F, MIN_SOUNDFACTOR, MAX_SOUNDFACTOR));
						} catch (final Throwable t) {
							ModBase.log().warn("Unrecognized token %s (%s)", parts[i], line);
						}
					}
				}
			}
		}

		final ResourceLocation bowLooseResource = new ResourceLocation(ModInfo.MOD_ID, "bow.loose");
		if (!isSoundBlocked(bowLooseResource)) {
			final SoundEvent bowLoose = getSound(bowLooseResource);
			this.replacements.put(new ResourceLocation("minecraft:entity.arrow.shoot"), bowLoose);
			this.replacements.put(new ResourceLocation("minecraft:entity.skeleton.shoot"), bowLoose);
		}

	}

	@Override
	protected void init(@Nonnull final ModConfiguration cfg) {
		//@formatter:off
		this.soundMetadata.putAll(
			cfg.sounds.entrySet().stream()
				.collect(
					Collectors.toMap(e -> new ResourceLocation(e.getKey()), e -> new SoundMetadata(e.getValue()))
				)
		);
		//@formatter:on
	}

	@Override
	protected void complete() {
		ModBase.log().info("[%s] %d sound events in private registry", getName(), this.myRegistry.size());
	}

	private void bakeSoundRegistry() {
		final ResourceLocation soundFile = new ResourceLocation(ModInfo.MOD_ID, "sounds.json");
		try (final SoundConfigProcessor proc = new SoundConfigProcessor(soundFile)) {
			proc.forEach((sound, meta) -> {
				final SoundMetadata data = new SoundMetadata(meta);
				final ResourceLocation resource = new ResourceLocation(ModInfo.RESOURCE_ID, sound);
				this.soundMetadata.put(resource, data);
			});
		} catch (@Nonnull final Exception ex) {
			ex.printStackTrace();
		}

		SoundEngine.instance().getSoundRegistry().getKeys().forEach(rl -> this.myRegistry.put(rl, new SoundEvent(rl)));
	}

	@Nonnull
	public SoundEvent getSound(final ResourceLocation sound) {
		final SoundEvent evt = this.myRegistry.get(sound);
		if (evt == null) {
			ModBase.log().warn("Cannot find sound that should be registered [%s]", sound.toString());
			return new SoundEvent(sound);
		}
		return evt;
	}

	public boolean isSoundBlocked(@Nonnull final ResourceLocation sound) {
		return this.blockedSounds.contains(sound);
	}

	public boolean isSoundCulled(@Nonnull final ResourceLocation sound) {
		return this.soundCull.containsKey(sound);
	}

	public float getVolumeScale(@Nonnull final ResourceLocation soundName) {
		return this.volumeControl.getFloat(soundName);
	}

	public float getVolumeScale(@Nonnull final ISound sound) {
		return (sound.getSoundLocation() == null || sound instanceof ConfigSoundInstance) ? 1F
				: this.volumeControl.getFloat(sound.getSoundLocation());
	}

	@Nullable
	public SoundMetadata getSoundMetadata(@Nonnull final ResourceLocation resource) {
		return this.soundMetadata.get(resource);
	}

	private boolean isSoundCulledLogical(@Nonnull final ResourceLocation res) {
		if (ModOptions.sound.soundCullingThreshold > 0) {
			// Get the last time the sound was seen
			final int lastOccurance = this.soundCull.getInt(res);
			if (lastOccurance != 0) {
				final int currentTick = EnvironState.getTickCounter();
				if ((currentTick - lastOccurance) < ModOptions.sound.soundCullingThreshold) {
					return true;
				} else {
					// Set when it happened and fall through for remapping and stuff
					this.soundCull.put(res, currentTick);
				}
			}
		}
		return false;
	}

	private boolean blockSoundProcess(@Nonnull final ResourceLocation res) {
		return res == null || isSoundBlocked(res) || isSoundCulledLogical(res);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void soundPlay(@Nonnull final PlaySoundEvent e) {
		// Don't mess with our ConfigSoundInstance instances from the config
		// menu
		final ISound theSound = e.getSound();
		if (theSound == null || theSound instanceof ConfigSoundInstance)
			return;

		// Check to see if we need to block sound processing
		final ResourceLocation soundResource = theSound.getSoundLocation();
		if (blockSoundProcess(soundResource)) {
			e.setResultSound(null);
			return;
		}

		// If it is Minecraft thunder handle the sound remapping to Dynamic Surroundings
		// thunder and set the appropriate volume.
		if ("entity.lightning.thunder".equals(e.getName())) {
			final ResourceLocation thunderSound = Sounds.THUNDER.getSound().getSoundName();
			if (!isSoundBlocked(thunderSound)) {
				final PositionedSound sound = (PositionedSound) theSound;
				if (PositionedSoundUtil.getVolume(sound) > 16) {
					final BlockPos pos = new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
					final ISound newSound = Sounds.THUNDER.createSoundAt(pos).setVolume(ModOptions.sound.thunderVolume);
					e.setResultSound(newSound);
				}
				return;
			}
		}

		// Check to see if the sound is going to be replaced with another sound
		if (theSound instanceof PositionedSound) {
			final SoundEvent rep = this.replacements.get(soundResource);
			if (rep != null) {
				e.setResultSound(SoundBuilder.builder(rep).from((PositionedSound) theSound).build());
			}
		}
	}
}
