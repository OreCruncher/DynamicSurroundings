/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
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

package org.blockartistry.mod.DynSurround.client.sound;

import java.util.Random;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.client.handlers.SoundEffectHandler;
import org.blockartistry.mod.DynSurround.data.xface.SoundConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundType;
import org.blockartistry.mod.DynSurround.registry.Evaluator;
import org.blockartistry.mod.DynSurround.util.SoundUtils;

import com.google.common.base.Objects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class SoundEffect {

	private static final float[] pitchDelta = { -0.2F, 0.0F, 0.0F, 0.2F, 0.2F, 0.2F };

	public final @Nullable SoundEvent sound;
	// Hack around SoundEvent.getName() being client sided
	private final String soundName;
	public final String conditions;
	public final SoundType type;
	public float volume;
	public final float pitch;
	public final int weight;
	public final boolean variable;
	public final int repeatDelayRandom;
	public final int repeatDelay;

	public SoundEffect(final ResourceLocation resource) {
		this(resource, 1.0F, 1.0F, 0, false);
	}

	public SoundEffect(final ResourceLocation resource, final float volume, final float pitch) {
		this(resource, volume, pitch, 0, false);
	}

	public SoundEffect(final ResourceLocation resource, final float volume, final float pitch, final boolean variable) {
		this(resource, volume, pitch, 0, variable);
	}

	public SoundEffect(final ResourceLocation resource, final float volume, final float pitch, final int repeatDelay,
			final boolean variable) {
		this.soundName = resource.toString();
		this.sound = SoundUtils.getOrRegisterSound(resource);
		this.volume = volume;
		this.pitch = pitch;
		this.conditions = ".*";
		this.weight = 1;
		this.type = SoundType.SPOT;
		this.variable = variable;
		this.repeatDelayRandom = 0;
		this.repeatDelay = repeatDelay;
	}

	public SoundEffect(final SoundEffect effect) {
		this.soundName = effect.soundName;
		this.sound = effect.sound;
		this.volume = effect.volume;
		this.pitch = effect.pitch;
		this.conditions = effect.conditions;
		this.weight = effect.weight;
		this.type = effect.type;
		this.variable = effect.variable;
		this.repeatDelayRandom = effect.repeatDelayRandom;
		this.repeatDelay = effect.repeatDelay;
	}

	public SoundEffect(final SoundConfig record) {
		this.soundName = StringUtils.isEmpty(record.sound) ? "NO SOUND SPECIFIED" : record.sound;
		this.sound = StringUtils.isEmpty(record.sound) ? null : SoundUtils.getOrRegisterSound(record.sound);
		this.conditions = StringUtils.isEmpty(record.conditions) ? ".*" : record.conditions;
		this.volume = record.volume == null ? 1.0F : record.volume.floatValue();
		this.pitch = record.pitch == null ? 1.0F : record.pitch.floatValue();
		this.weight = record.weight == null ? 10 : record.weight.intValue();
		this.variable = record.variable != null && record.variable.booleanValue();
		this.repeatDelayRandom = record.repeatDelayRandom == null ? 0 : record.repeatDelayRandom.intValue();
		this.repeatDelay = record.repeatDelay == null ? 0 : record.repeatDelay.intValue();

		if (record.soundType != null) {
			this.type = SoundType.getType(record.soundType);
		} else {
			if (record.repeatDelay != null && record.repeatDelay.intValue() > 0)
				this.type = SoundType.PERIODIC;
			else if (record.step != null && record.step.booleanValue())
				this.type = SoundType.STEP;
			else if (record.spotSound != null && record.spotSound.booleanValue())
				this.type = SoundType.SPOT;
			else
				this.type = SoundType.BACKGROUND;
		}
	}

	public boolean matches() {
		return Evaluator.check(this.conditions);
	}

	public float getVolume() {
		return this.volume;
	}

	public float getPitch(final Random rand) {
		if (rand != null && this.variable)
			return this.pitch + pitchDelta[rand.nextInt(pitchDelta.length)];
		return this.pitch;
	}

	public int getRepeat(final Random rand) {
		if (this.repeatDelayRandom <= 0)
			return this.repeatDelay;
		return this.repeatDelay + rand.nextInt(this.repeatDelayRandom);
	}

	public boolean isRepeatable() {
		return this.type == SoundType.PERIODIC;
	}

	public void doEffect(final IBlockState state, final World world, final BlockPos pos,
			@Nullable final SoundCategory categoryOverride, final Random random) {
		SoundEffectHandler.INSTANCE.playSoundAt(pos, this, 0, categoryOverride);
	}

	@Override
	public boolean equals(final Object anObj) {
		if (this == anObj)
			return true;
		if (!(anObj instanceof SoundEffect))
			return false;
		final SoundEffect s = (SoundEffect) anObj;
		return Objects.equal(this.sound, s.sound);
	}

	@Override
	public int hashCode() {
		return this.sound.hashCode();
	}

	public static SoundEffect scaleVolume(final SoundEffect sound, final float scale) {
		final SoundEffect newEffect = new SoundEffect(sound);
		newEffect.volume *= scale;
		return newEffect;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append('[').append(sound == null ? "MISSING_SOUND" : this.soundName);
		if (!StringUtils.isEmpty(this.conditions))
			builder.append('(').append(this.conditions).append(')');
		builder.append(", v:").append(this.volume);
		builder.append(", p:").append(this.pitch);
		builder.append(", t:").append(this.type);
		if (this.type == SoundType.SPOT)
			builder.append(", w:").append(this.weight);
		if (this.repeatDelay != 0 || this.repeatDelayRandom != 0)
			builder.append(", d:").append(this.repeatDelay).append('+').append(this.repeatDelayRandom);
		builder.append(']');
		return builder.toString();
	}
}