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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.sound.ConfigSound;
import org.orecruncher.dsurround.client.sound.SoundEngine;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.dsurround.registry.config.SoundMetadataConfig;
import org.orecruncher.lib.math.MathStuff;
import org.orecruncher.lib.sound.SoundConfigProcessor;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoundRegistry extends Registry {

	public static final float MIN_SOUNDFACTOR = 0F;
	public static final float MAX_SOUNDFACTOR = 4F;
	public static final float DEFAULT_SOUNDFACTOR = 1F;

	private static final String ARMOR_SOUND_PREFIX = ModBase.MOD_ID + ":fs.armor.";

	private final List<String> cullSoundNames = new ArrayList<>();
	private final List<String> blockSoundNames = new ArrayList<>();
	private final Object2FloatOpenHashMap<String> volumeControl;

	private final Map<ResourceLocation, SoundMetadata> soundMetadata = new Object2ObjectOpenHashMap<>();
	private final Map<ResourceLocation, SoundEvent> myRegistry = new Object2ObjectOpenHashMap<>();
	private SoundEvent SILENCE;

	public SoundRegistry(@Nonnull final Side side) {
		super(side);
		this.volumeControl = new Object2FloatOpenHashMap<>();
		this.volumeControl.defaultReturnValue(DEFAULT_SOUNDFACTOR);
	}

	@Override
	public void init() {
		this.cullSoundNames.clear();
		this.blockSoundNames.clear();
		this.volumeControl.clear();
		this.soundMetadata.clear();
		this.myRegistry.clear();
		
		bakeSoundRegistry();

		for(final String line: ModOptions.sound.soundSettings) {
			final String[] parts = line.split(" ");
			if(parts.length < 2) {
				ModBase.log().warn("Missing tokens in sound settings? (%s)", line);
			} else {
				final String soundName = parts[0];
				for(int i = 1; i < parts.length; i++) {
					if ("cull".compareToIgnoreCase(parts[i]) == 0) {
						this.cullSoundNames.add(soundName);
					} else if("block".compareToIgnoreCase(parts[i]) == 0) {
						this.blockSoundNames.add(soundName);
					} else {
						try {
							final int volume = Integer.parseInt(parts[i]);
							this.volumeControl.put(soundName, MathStuff.clamp((float)volume / 100F, MIN_SOUNDFACTOR, MAX_SOUNDFACTOR));
						} catch(final Throwable t) {
							ModBase.log().warn("Unrecognized token %s (%s)", parts[i], line);
						}
					}
				}
			}
		}
	}

	@Override
	public void configure(@Nonnull final ModConfiguration cfg) {
		for (final Entry<String, SoundMetadataConfig> entry : cfg.sounds.entrySet()) {
			final SoundMetadata data = new SoundMetadata(entry.getValue());
			final ResourceLocation resource = new ResourceLocation(entry.getKey());
			this.soundMetadata.put(resource, data);
		}
	}
	
	protected void bakeSoundRegistry() {
		
		final ResourceLocation soundFile = new ResourceLocation(ModBase.MOD_ID, "sounds.json");
		try (final SoundConfigProcessor proc = new SoundConfigProcessor(soundFile)) {
			proc.forEach((sound, meta) -> {
				final SoundMetadata data = new SoundMetadata(meta);
				final ResourceLocation resource = new ResourceLocation(ModBase.RESOURCE_ID, sound);
				this.soundMetadata.put(resource, data);
			});
		} catch( @Nonnull final Exception ex) {
			ex.printStackTrace();
		}
		
		SoundEngine.instance().getSoundRegistry().getKeys().forEach(rl -> this.myRegistry.put(rl, new SoundEvent(rl)));
		this.SILENCE = this.myRegistry.get(new ResourceLocation(ModBase.MOD_ID, "silence"));

		ModBase.log().info("%d sound events in private registry", this.myRegistry.size());
	}

	@Nonnull
	public SoundEvent getSound(final ResourceLocation sound) {
		final SoundEvent evt = myRegistry.get(sound);
		if (evt == null) {
			ModBase.log().warn("Cannot find sound that should be registered [%s]", sound.toString());
			return SILENCE;
		}
		return evt;
	}

	public boolean isSoundCulled(@Nonnull final String sound) {
		return this.cullSoundNames.contains(sound);
	}

	public boolean isSoundBlocked(@Nonnull final String sound) {
		return this.blockSoundNames.contains(sound);
	}

	public boolean isSoundBlockedLogical(@Nonnull final String sound) {
		return isSoundBlocked(sound) || (!ModOptions.sound.enableArmorSounds && sound.startsWith(ARMOR_SOUND_PREFIX));
	}

	public float getVolumeScale(@Nonnull final String soundName) {
		return this.volumeControl.getFloat(soundName);
	}

	public float getVolumeScale(@Nonnull final ISound sound) {
		return (sound.getSoundLocation() == null || sound instanceof ConfigSound) ? 1F
				: this.volumeControl.getFloat(sound.getSoundLocation().toString());
	}
	
	@Nullable
	public SoundMetadata getSoundMetadata(@Nonnull final ResourceLocation resource) {
		return this.soundMetadata.get(resource);
	}

}
