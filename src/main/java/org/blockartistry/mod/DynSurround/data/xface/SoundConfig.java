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

package org.blockartistry.mod.DynSurround.data.xface;

import javax.annotation.Nonnull;

import com.google.gson.annotations.SerializedName;

public class SoundConfig {
	@SerializedName("sound")
	public String sound = null;
	@SerializedName("conditions")
	public String conditions = ".*";
	@SerializedName("soundType")
	public String soundType = null;
	@SerializedName("volume")
	public Float volume = null;
	@SerializedName("pitch")
	public Float pitch = null;
	@SerializedName("weight")
	public Integer weight = null;
	@SerializedName("variable")
	public Boolean variable = null;
	@SerializedName("repeatDelayRandom")
	public Integer repeatDelayRandom = null;
	@SerializedName("repeatDelay")
	public Integer repeatDelay = null;
	@SerializedName("spot")
	public Boolean spotSound = null;
	@SerializedName("step")
	public Boolean step = null;
	
	public SoundConfig() {
		
	}
	
	public SoundConfig(@Nonnull final String soundName) {
		this.sound = soundName;
	}

	@Nonnull
	public SoundConfig setSoundName(@Nonnull final String name) {
		this.sound = name;
		return this;
	}

	@Nonnull
	public SoundConfig setConditions(@Nonnull final String conditions) {
		this.conditions = conditions;
		return this;
	}

	@Nonnull
	public SoundConfig setSoundType(@Nonnull final SoundType type) {
		this.soundType = type.getName();
		return this;
	}

	@Nonnull
	public SoundConfig setVolume(final float volume) {
		this.volume = volume;
		return this;
	}

	@Nonnull
	public SoundConfig setPitch(final float pitch) {
		this.pitch = pitch;
		return this;
	}

	@Nonnull
	public SoundConfig setWeight(final int weight) {
		this.weight = weight;
		return this;
	}

	@Nonnull
	public SoundConfig setRepeatDelayRandom(final int delay) {
		this.repeatDelayRandom = delay;
		return this;
	}

	@Nonnull
	public SoundConfig setRepeatDelay(final int delay) {
		this.repeatDelay = delay;
		return this;
	}

	@Nonnull
	public SoundConfig setIsVariable(final boolean flag) {
		this.variable = flag;
		return this;
	}

	@Nonnull
	public SoundConfig setIsSpotSound(final boolean flag) {
		this.spotSound = flag;
		return this;
	}

	@Nonnull
	public SoundConfig setIsStepSound(final boolean flag) {
		this.step = flag;
		return this;
	}
}
