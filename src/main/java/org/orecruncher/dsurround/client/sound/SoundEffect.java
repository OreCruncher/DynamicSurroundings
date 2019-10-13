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

package org.orecruncher.dsurround.client.sound;

import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.client.fx.ISpecialEffect;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.handlers.SoundEffectHandler;
import org.orecruncher.dsurround.expression.ExpressionEngine;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.config.SoundConfig;
import org.orecruncher.dsurround.registry.config.SoundType;
import org.orecruncher.dsurround.registry.sound.SoundMetadata;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;
import org.orecruncher.lib.WeightTable;
import org.orecruncher.lib.WeightTable.IEntrySource;
import org.orecruncher.lib.WeightTable.IItem;
import org.orecruncher.lib.chunk.IBlockAccessEx;
import org.orecruncher.lib.random.XorShiftRandom;

import com.google.common.base.MoreObjects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoundEffect implements ISpecialEffect, IEntrySource<SoundEffect>, WeightTable.IItem<SoundEffect> {

	private static final int SPOT_SOUND_RANGE = 8;
	private static final float[] pitchDelta = { -0.2F, 0.0F, 0.0F, 0.2F, 0.2F, 0.2F };
	private static final Random RANDOM = XorShiftRandom.current();

	private final SoundEvent sound;
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
	private String soundTitle = StringUtils.EMPTY;

	protected SoundEffect(final ResourceLocation resource, final SoundCategory category) {
		this(resource, category, 1.0F, 1.0F, 0, false);
	}

	protected SoundEffect(final ResourceLocation resource, final SoundCategory category, final float volume,
			final float pitch, final int repeatDelay, final boolean variable) {
		this.soundName = resource.toString();
		this.sound = RegistryManager.SOUND.getSound(resource);
		this.volume = volume;
		this.pitch = pitch;
		this.conditions = StringUtils.EMPTY;
		this.weight = 10;
		this.type = SoundType.SPOT;
		this.category = MoreObjects.firstNonNull(category, SoundCategory.BLOCKS);
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

	protected SoundEffect setSoundTitle(@Nonnull final String title) {
		this.soundTitle = title;
		return this;
	}

	public String getSoundName() {
		return this.soundName;
	}

	public String getSoundTitle() {
		return this.soundTitle;
	}

	public SoundEvent getSound() {
		return this.sound;
	}

	public SoundCategory getCategory() {
		return this.category;
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

	private float randomRange(final int range) {
		return RANDOM.nextInt(range) - RANDOM.nextInt(range);
	}

	@SideOnly(Side.CLIENT)
	public SoundInstance createSoundAt(@Nonnull final BlockPos pos) {
		return SoundBuilder.builder(this.sound, SoundRegistry.BIOME).setPosition(pos).build();
	}

	@SideOnly(Side.CLIENT)
	public SoundInstance createSoundNear(@Nonnull final Entity player) {
		final float posX = (float) (player.posX + randomRange(SPOT_SOUND_RANGE));
		final float posY = (float) (player.posY + player.getEyeHeight() + randomRange(SPOT_SOUND_RANGE));
		final float posZ = (float) (player.posZ + randomRange(SPOT_SOUND_RANGE));
		return SoundBuilder.builder(this.sound, SoundRegistry.BIOME).setPosition(posX, posY, posZ).build();
	}

	@SideOnly(Side.CLIENT)
	public SoundInstance createTrackingSound(@Nonnull final Entity player, final boolean fadeIn) {
		final TrackingSoundInstance sound = new TrackingSoundInstance(player, this, fadeIn);
		if (EnvironState.isPlayer(player))
			sound.setAttenuationType(SoundInstance.noAttenuation());
		return sound;
	}

	@Override
	public boolean canTrigger(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		return true;
	}

	@Override
	public void doEffect(@Nonnull final IBlockAccessEx provider, @Nonnull final IBlockState state,
			@Nonnull final BlockPos pos, @Nonnull final Random random) {
		SoundEffectHandler.INSTANCE.playSoundAt(pos, this, 0);
	}

	@Override
	public boolean equals(final Object anObj) {
		return this == anObj || this.soundName.equals(((SoundEffect) anObj).soundName);
	}

	@Override
	public int hashCode() {
		return this.sound.hashCode();
	}

	// WeightTable.IItem<T>
	@Override
	public SoundEffect getItem() {
		return this;
	}

	// WeightTable.IItem<T>
	@Override
	public int getWeight() {
		return this.weight;
	}

	// IEntrySource<T>
	@Override
	public IItem<SoundEffect> getEntry() {
		return this;
	}

	// IEntrySource<T>
	@Override
	public boolean matches() {
		return ExpressionEngine.instance().check(this.conditions);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(this.soundName);
		builder.append('(').append(this.conditions).append(')');
		builder.append(", v:").append(this.volume);
		builder.append(", p:").append(this.pitch);
		builder.append(", t:").append(this.type);
		if (this.type == SoundType.SPOT)
			builder.append(", w:").append(this.weight);
		if (this.repeatDelay != 0 || this.repeatDelayRandom != 0)
			builder.append(", d:").append(this.repeatDelay).append('+').append(this.repeatDelayRandom);
		return builder.toString();
	}

	public static class Builder {

		private final SoundEffect effect;

		public Builder(@Nonnull final String sound, @Nonnull final SoundCategory cat) {
			this(new ResourceLocation(ModInfo.RESOURCE_ID, sound), cat);
		}

		public Builder(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
			this(event.getSoundName(), cat);
		}

		public Builder(@Nonnull final ResourceLocation resource, @Nonnull final SoundCategory cat) {
			this.effect = new SoundEffect(resource, cat);
		}

		public Builder(@Nonnull final SoundConfig record) {
			final ResourceLocation resource = new ResourceLocation(record.sound);
			this.effect = new SoundEffect(resource, null);

			setConditions(StringUtils.isEmpty(record.conditions) ? StringUtils.EMPTY : record.conditions.intern());
			setVolume(record.volume == null ? 1.0F : record.volume.floatValue());
			setPitch(record.pitch == null ? 1.0F : record.pitch.floatValue());
			setWeight(record.weight == null ? 10 : record.weight.intValue());
			setVariablePitch(record.variable != null && record.variable.booleanValue());
			setRepeatDelay(record.repeatDelay == null ? 0 : record.repeatDelay.intValue());
			setRepeatDelayRandom(record.repeatDelayRandom == null ? 0 : record.repeatDelayRandom.intValue());
			setSoundTitle(record.title != null ? record.title : StringUtils.EMPTY);

			SoundType t = null;
			if (record.soundType != null)
				t = SoundType.getType(record.soundType);

			if (t == null) {
				if (record.repeatDelay != null && record.repeatDelay.intValue() > 0)
					t = SoundType.PERIODIC;
				else if (record.spotSound != null && record.spotSound.booleanValue())
					t = SoundType.SPOT;
				else
					t = SoundType.BACKGROUND;
			}

			setSoundType(t);

			SoundCategory sc = null;
			if (record.soundCategory != null)
				sc = SoundCategory.getByName(record.soundCategory);

			if (sc == null) {
				// There isn't an override - defer to the category info in
				// the sounds.json.
				final SoundMetadata meta = RegistryManager.SOUND.getSoundMetadata(resource);
				if (meta != null)
					sc = meta.getCategory();

				// No info in sounds.json - best guess.
				if (sc == null)
					sc = SoundCategory.AMBIENT;
			}

			setSoundCategory(sc);
		}

		public Builder setSoundTitle(@Nonnull final String title) {
			this.effect.setSoundTitle(title);
			return this;
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
			return this.effect;
		}
	}

}