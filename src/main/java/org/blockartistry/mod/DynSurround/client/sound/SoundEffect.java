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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.client.fx.ISpecialEffect;
import org.blockartistry.mod.DynSurround.client.handlers.SoundEffectHandler;
import org.blockartistry.mod.DynSurround.data.xface.SoundConfig;
import org.blockartistry.mod.DynSurround.data.xface.SoundType;
import org.blockartistry.mod.DynSurround.registry.Evaluator;
import org.blockartistry.mod.DynSurround.util.SoundUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class SoundEffect implements ISpecialEffect {

	private static final float[] pitchDelta = { -0.2F, 0.0F, 0.0F, 0.2F, 0.2F, 0.2F };

	public final @Nullable SoundEvent sound;
	// Hack around SoundEvent.getName() being client sided
	private final String soundName;
	public final String conditions;
	public final SoundType type;
	public final SoundCategory category;
	public float volume;
	public float pitch;
	public final int weight;
	public boolean variable;
	public final int repeatDelayRandom;
	public final int repeatDelay;

	public SoundEffect(@Nonnull final String sound, @Nonnull final SoundCategory category) {
		this(new ResourceLocation(DSurround.RESOURCE_ID, sound), category);
	}

	public SoundEffect(final ResourceLocation resource, final SoundCategory category) {
		this(resource, category, 1.0F, 1.0F, 0, false);
	}

	public SoundEffect(final ResourceLocation resource, final SoundCategory category, final float volume,
			final float pitch) {
		this(resource, category, volume, pitch, 0, false);
	}

	protected SoundEffect(final ResourceLocation resource, final SoundCategory category, final float volume,
			final float pitch, final int repeatDelay, final boolean variable) {
		this.soundName = resource.toString();
		this.sound = SoundUtils.getOrRegisterSound(resource);
		this.volume = volume;
		this.pitch = pitch;
		this.conditions = StringUtils.EMPTY;
		this.weight = 10;
		this.type = SoundType.SPOT;
		this.category = category == null ? SoundCategory.BLOCKS : category;
		this.variable = variable;
		this.repeatDelayRandom = 0;
		this.repeatDelay = repeatDelay;
	}

	public SoundEffect(final SoundConfig record) {
		this.soundName = StringUtils.isEmpty(record.sound) ? "NO SOUND SPECIFIED" : record.sound;
		this.sound = StringUtils.isEmpty(record.sound) ? null : SoundUtils.getOrRegisterSound(record.sound);
		this.conditions = StringUtils.isEmpty(record.conditions) ? StringUtils.EMPTY : record.conditions.intern();
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

		switch (this.type) {
		case BACKGROUND:
			this.category = SoundCategory.AMBIENT;
			break;
		case PERIODIC:
			this.category = SoundCategory.PLAYERS;
			break;
		case STEP:
		case SPOT:
		default:
			this.category = SoundCategory.BLOCKS;
		}
	}

	public SoundEffect setVolume(final float vol) {
		this.volume = vol;
		return this;
	}

	public SoundEffect setPitch(final float pitch) {
		this.pitch = pitch;
		return this;
	}

	public SoundEffect setVariable(final boolean flag) {
		this.variable = flag;
		return this;
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
			return Math.max(this.repeatDelay, 1);
		return this.repeatDelay + rand.nextInt(this.repeatDelayRandom);
	}

	public boolean isRepeatable() {
		return this.type == SoundType.PERIODIC || this.type == SoundType.BACKGROUND;
	}

	@SideOnly(Side.CLIENT)
	public IMySound createSound(@Nonnull final BlockPos pos, final Random rand) {
		return new SpotSound(pos, this, getRepeat(rand));
	}

	@SideOnly(Side.CLIENT)
	public IMySound createSound(@Nonnull final EntityPlayer player) {
		return new SpotSound(player, this);
	}

	@SideOnly(Side.CLIENT)
	public IMySound createSound(@Nonnull final BlockPos pos, final int tickDelay) {
		return new SpotSound(pos, this, tickDelay);
	}

	@SideOnly(Side.CLIENT)
	public IMySound createSound(@Nonnull final EntityLivingBase player, final boolean fadeIn, final Random rand) {
		if(player instanceof EntityPlayer)
			return new PlayerTrackingSound(this, fadeIn);
		return new TrackingSound(player, this, fadeIn);
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos,
			@Nonnull final Random random) {
		return true;
	}

	@Override
	public void doEffect(final IBlockState state, final World world, final BlockPos pos, final Random random) {
		SoundEffectHandler.INSTANCE.playSoundAt(pos, this, 0);
	}

	@Override
	public boolean equals(final Object anObj) {
		if (this == anObj)
			return true;
		if (!(anObj instanceof SoundEffect))
			return false;
		return this.soundName.equals(((SoundEffect) anObj).soundName);
	}

	@Override
	public int hashCode() {
		return this.sound.hashCode();
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append('[').append(sound == null ? "MISSING_SOUND" : this.soundName);
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