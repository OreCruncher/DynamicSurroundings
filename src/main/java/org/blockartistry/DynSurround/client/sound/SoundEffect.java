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

package org.blockartistry.DynSurround.client.sound;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.client.fx.ISpecialEffect;
import org.blockartistry.DynSurround.client.handlers.SoundEffectHandler;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.data.xface.SoundConfig;
import org.blockartistry.DynSurround.data.xface.SoundType;
import org.blockartistry.DynSurround.registry.Evaluator;
import org.blockartistry.lib.SoundUtils;
import org.blockartistry.lib.WeightTable.IEntrySource;

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

public final class SoundEffect implements ISpecialEffect, IEntrySource<SoundEffect> {

	private static final float[] pitchDelta = { -0.2F, 0.0F, 0.0F, 0.2F, 0.2F, 0.2F };

	private final @Nullable SoundEvent sound;
	// Hack around SoundEvent.getName() being client sided
	private final String soundName;
	private SoundType type;
	private String conditions;
	private SoundCategory category;
	private float volume;
	private float pitch;
	private int weight;
	private boolean variable;
	private int repeatDelayRandom;
	private int repeatDelay;

	protected SoundEffect(final ResourceLocation resource, final SoundCategory category) {
		this(resource, category, 1.0F, 1.0F, 0, false);
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

	protected SoundEffect setVolume(final float vol) {
		this.volume = vol;
		return this;
	}

	protected SoundEffect setPitch(final float pitch) {
		this.pitch = pitch;
		return this;
	}

	protected SoundEffect setVariable(final boolean flag) {
		this.variable = flag;
		return this;
	}

	protected SoundEffect setSoundCategory(@Nonnull final SoundCategory cat) {
		this.category = cat;
		return this;
	}

	protected SoundEffect setConditions(@Nonnull final String cond) {
		this.conditions = cond;
		return this;
	}

	protected SoundEffect setWeight(final int w) {
		this.weight = w;
		return this;
	}

	protected SoundEffect setSoundType(@Nonnull final SoundType type) {
		this.type = type;
		return this;
	}

	protected SoundEffect setRepeatDelay(final int d) {
		this.repeatDelay = d;
		return this;
	}

	protected SoundEffect setRepeatDelayRandom(final int r) {
		this.repeatDelayRandom = r;
		return this;
	}

	public SoundEvent getSound() {
		return this.sound;
	}

	public SoundCategory getCategory() {
		return this.category;
	}

	// IEntrySource
	public int getWeight() {
		return this.weight;
	}

	// IEntrySource
	public SoundEffect getItem() {
		return this;
	}
	
	// IEntrySource
	public boolean matches() {
		return Evaluator.check(this.conditions);
	}

	public SoundType getSoundType() {
		return this.type;
	}

	protected float getVolume() {
		return this.volume;
	}

	protected float getPitch(final Random rand) {
		if (rand != null && this.variable)
			return this.pitch + pitchDelta[rand.nextInt(pitchDelta.length)];
		return this.pitch;
	}

	protected int getRepeat(final Random rand) {
		if (this.repeatDelayRandom <= 0)
			return Math.max(this.repeatDelay, 0);
		return this.repeatDelay + rand.nextInt(this.repeatDelayRandom);
	}

	protected boolean isRepeatable() {
		return this.type == SoundType.PERIODIC || this.type == SoundType.BACKGROUND;
	}

	@SideOnly(Side.CLIENT)
	public BasicSound<?> createSound(@Nonnull final BlockPos pos) {
		return new SpotSound(pos, this);
	}

	@SideOnly(Side.CLIENT)
	public BasicSound<?> createSound(@Nonnull final EntityPlayer player) {
		return new SpotSound(player, this);
	}

	@SideOnly(Side.CLIENT)
	public BasicSound<?> createSound(@Nonnull final EntityLivingBase player, final boolean fadeIn) {
		if (player instanceof EntityPlayer)
			return new PlayerTrackingSound(this, fadeIn);
		return new TrackingSound(player, this, fadeIn);
	}

	public boolean canSoundBeHeard(@Nonnull final BlockPos soundPos) {
		if (this.getVolume() == 0.0F)
			return false;
		final double distanceSq = EnvironState.getPlayerPosition().distanceSq(soundPos);
		final double DROPOFF = 16 * 16;
		if (distanceSq <= DROPOFF)
			return true;
		final double power = this.getVolume() * DROPOFF;
		return distanceSq <= power;
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

	public static class Builder {

		private final SoundEffect effect;

		public Builder(@Nonnull final String sound, @Nonnull final SoundCategory cat) {
			this(new ResourceLocation(DSurround.RESOURCE_ID, sound), cat);
		}

		public Builder(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
			this(event.getSoundName(), cat);
		}

		public Builder(@Nonnull final ResourceLocation resource, @Nonnull final SoundCategory cat) {
			this.effect = new SoundEffect(resource, cat);
		}

		public Builder(@Nonnull final SoundConfig record) {
			this.effect = new SoundEffect(new ResourceLocation(record.sound), null);

			this.setConditions(StringUtils.isEmpty(record.conditions) ? StringUtils.EMPTY : record.conditions.intern());
			this.setVolume(record.volume == null ? 1.0F : record.volume.floatValue());
			this.setPitch(record.pitch == null ? 1.0F : record.pitch.floatValue());
			this.setWeight(record.weight == null ? 10 : record.weight.intValue());
			this.setVariablePitch(record.variable != null && record.variable.booleanValue());
			this.setRepeatDelay(record.repeatDelay == null ? 0 : record.repeatDelay.intValue());
			this.setRepeatDelayRandom(record.repeatDelayRandom == null ? 0 : record.repeatDelayRandom.intValue());

			final SoundType t;
			if (record.soundType != null) {
				t = SoundType.getType(record.soundType);
			} else {
				if (record.repeatDelay != null && record.repeatDelay.intValue() > 0)
					t = SoundType.PERIODIC;
				else if (record.step != null && record.step.booleanValue())
					t = SoundType.STEP;
				else if (record.spotSound != null && record.spotSound.booleanValue())
					t = SoundType.SPOT;
				else
					t = SoundType.BACKGROUND;
			}

			this.setSoundType(t != null ? t : SoundType.BACKGROUND);

			final SoundCategory sc;
			if (record.soundCategory != null) {
				sc = SoundCategory.getByName(record.soundCategory);
			} else {
				switch (t) {
				case BACKGROUND:
				case PERIODIC:
				case SPOT:
					sc = SoundCategory.AMBIENT;
					break;
				case STEP:
				default:
					sc = SoundCategory.BLOCKS;
				}
			}

			this.setSoundCategory(sc != null ? sc : SoundCategory.AMBIENT);
		}

		public Builder setVolume(final float v) {
			this.effect.setVolume(v);
			return this;
		}

		public Builder setPitch(final float p) {
			this.effect.setPitch(p);
			return this;
		}

		public Builder setVariablePitch(final boolean flag) {
			this.effect.setVariable(flag);
			return this;
		}

		public Builder setSoundCategory(@Nonnull final SoundCategory cat) {
			this.effect.setSoundCategory(cat);
			return this;
		}

		public Builder setConditions(@Nonnull final String cond) {
			this.effect.setConditions(cond == null ? "" : cond);
			return this;
		}

		public Builder setWeight(final int w) {
			this.effect.setWeight(w);
			return this;
		}

		public Builder setRepeatDelay(final int d) {
			this.effect.setRepeatDelay(d);
			return this;
		}

		public Builder setRepeatDelayRandom(final int r) {
			this.effect.setRepeatDelayRandom(r);
			return this;
		}

		public Builder setSoundType(final SoundType type) {
			this.effect.setSoundType(type);
			return this;
		}

		public SoundEffect build() {
			return effect;
		}
	}

}