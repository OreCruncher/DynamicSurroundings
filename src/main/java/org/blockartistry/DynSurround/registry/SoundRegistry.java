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

package org.blockartistry.DynSurround.registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.gui.ConfigSound;
import org.blockartistry.lib.MyUtils;
import org.blockartistry.lib.SoundUtils;
import com.google.gson.Gson;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.client.audio.ISound;
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
					DSurround.log().error("Unable to process sound volume entry: " + volume, t);
				}
			}
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
			DSurround.log().error("Unable to read the mod sound file!", t);
		}

	}

}
