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

package org.blockartistry.mod.DynSurround.registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.gui.ConfigSound;
import org.blockartistry.mod.DynSurround.util.MyUtils;
import org.blockartistry.mod.DynSurround.util.SoundUtils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventAccessor;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//@SideOnly(Side.CLIENT)
public final class SoundRegistry extends Registry {

	private final List<String> cullSoundNames = new ArrayList<String>();
	private final List<String> blockSoundNames = new ArrayList<String>();
	private final TObjectFloatHashMap<String> volumeControl = new TObjectFloatHashMap<String>(
			Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 1.0F);

	public SoundRegistry(@Nonnull final Side side) {
		super(side);

	}

	@Override
	public void init() {
		this.cullSoundNames.clear();
		this.blockSoundNames.clear();
		this.volumeControl.clear();

		MyUtils.addAll(this.cullSoundNames, ModOptions.culledSounds);
		MyUtils.addAll(this.blockSoundNames, ModOptions.blockedSounds);

		for (final String volume : ModOptions.soundVolumes) {
			final String[] tokens = StringUtils.split(volume, "=");
			if (tokens.length == 2) {
				try {
					final float vol = Integer.parseInt(tokens[1]) / 100.0F;
					this.volumeControl.put(tokens[0], vol);
				} catch (final Throwable t) {
					ModLog.error("Unable to process sound volume entry: " + volume, t);
				}
			}
		}

		if (ModOptions.enableDebugLogging && this.side == Side.CLIENT) {
			final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
			final List<String> sounds = new ArrayList<String>();

			// Make a map copy. The sound registry should be baked but don't
			// trust it. The map reference will be used in a separate thread to
			// detect
			// bogus entries.
			final Map<ResourceLocation, SoundEventAccessor> theClone = ImmutableMap
					.copyOf(handler.soundRegistry.soundRegistry);

			for (final Entry<ResourceLocation, SoundEventAccessor> e : theClone.entrySet()) {
				sounds.add(e.getKey().toString());
			}
			Collections.sort(sounds);

			ModLog.info("*** SOUND REGISTRY ***");
			for (final String sound : sounds)
				ModLog.info(sound);

			new Thread(new Runnable() {

				@Override
				public void run() {

					final List<String> smells = new ArrayList<String>();
					try {
						for (final Entry<ResourceLocation, SoundEventAccessor> e : theClone.entrySet()) {
							for (final ISoundEventAccessor<Sound> x : e.getValue().accessorList) {
								final ResourceLocation ogg = x.cloneEntry().getSoundAsOggLocation();
								try (final IResource resource = Minecraft.getMinecraft().getResourceManager()
										.getResource(ogg)) {
									resource.getInputStream();
								} catch (final Throwable t) {
									smells.add(String.format("INACCESSABLE [%s] [%s]", e.getKey().toString(),
											ogg.toString()));
								}
							}
						}

					} catch (final Throwable t) {

					}

					if (smells.size() > 0) {
						Minecraft.getMinecraft().addScheduledTask(new Runnable() {
							@Override
							public void run() {
								Collections.sort(smells);
								ModLog.info("*** SOUND SMELLS ***");
								for (final String smell : smells)
									ModLog.info(smell);
							}
						});
					}
				}

			}).start();

		}

	}

	@Override
	public void fini() {

	}

	public boolean isSoundCulled(@Nonnull final String sound) {
		return this.cullSoundNames.contains(sound);
	}

	public boolean isSoundBlocked(@Nonnull final String sound) {
		return this.blockSoundNames.contains(sound);
	}

	public float getVolumeScale(@Nonnull final String soundName) {
		return this.volumeControl.get(soundName);
	}

	public float getVolumeScale(@Nonnull final ISound sound) {
		return (sound.getSoundLocation() == null || sound instanceof ConfigSound) ? 1F : this.volumeControl.get(sound.getSoundLocation().toString());
	}

	// Not entirely sure why they changed things. This reads the mods
	// sounds.json and forces registration of all the mod sounds.
	// Code generally comes from the Minecraft sound processing logic.
	@SideOnly(Side.CLIENT)
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

		try (final InputStream stream = SoundRegistry.class.getResourceAsStream("/assets/dsurround/sounds.json")) {
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

}
