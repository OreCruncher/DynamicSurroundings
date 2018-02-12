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

package org.blockartistry.lib.sound;

import java.util.Random;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.client.sound.SoundState;
import org.blockartistry.lib.compat.ModEnvironment;
import org.blockartistry.lib.random.XorShiftRandom;

import com.google.common.base.MoreObjects;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicSound<T extends BasicSound<?>> extends PositionedSound implements INBTSerializable<NBTTagCompound> {

	protected static final float ATTENUATION_OFFSET = 32F;

	@FunctionalInterface
	public static interface ISoundScale {
		float getScale();
	}

	public static final ISoundScale DEFAULT_SCALE = () -> {
		return 1.0F;
	};

	public static class NBT {
		public static final String SOUND_EVENT = "s";
		public static final String SOUND_CATEGORY = "c";
		public static final String VOLUME = "v";
		public static final String PITCH = "p";
		public static final String X_COORD = "x";
		public static final String Y_COORD = "y";
		public static final String Z_COORD = "z";
	};

	protected final Random RANDOM = XorShiftRandom.current();
	protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	protected String id = StringUtils.EMPTY;
	protected float volumeThrottle = 1.0F;
	protected ISoundScale volumeScale;
	protected boolean route;
	protected SoundState state = SoundState.NONE;

	public BasicSound(@Nonnull final SoundEvent event, @Nonnull final SoundCategory cat) {
		this(event.getSoundName(), cat);
	}

	public BasicSound(@Nonnull final ResourceLocation soundResource, @Nonnull final SoundCategory cat) {
		super(soundResource, cat);

		this.volumeScale = DEFAULT_SCALE;

		this.volume = 1F;
		this.pitch = 1F;
		this.setPosition(0, 0, 0);
		this.repeat = false;
		this.repeatDelay = 0;
		this.attenuationType = ISound.AttenuationType.LINEAR;

		// Sounds are not routed by default. Need to be turned on
		// in a derived class or set via setter.
		this.route = false;

		super.sound = SoundHandler.MISSING_SOUND;

	}

	public SoundState getState() {
		return this.state;
	}

	@SuppressWarnings("unchecked")
	public T setState(@Nonnull final SoundState state) {
		this.state = state;
		return (T) this;
	}

	public boolean shouldRoute() {
		return this.route;
	}

	@SuppressWarnings("unchecked")
	public T setRoutable(final boolean flag) {
		this.route = flag;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setId(@Nonnull final String id) {
		this.id = id;
		return (T) this;
	}

	@Nonnull
	public String getId() {
		return this.id;
	}

	@SuppressWarnings("unchecked")
	public T setVolume(final float v) {
		this.volume = v;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setPitch(final float p) {
		this.pitch = p;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setPosition(final float x, final float y, final float z) {
		this.xPosF = x;
		this.yPosF = y;
		this.zPosF = z;
		this.pos.setPos(x, y, z);
		return (T) this;
	}

	public T setPosition(@Nonnull final Entity entity) {
		final Vec3d point = entity.getEntityBoundingBox().getCenter();
		return this.setPosition(point);
	}

	public T setPosition(@Nonnull final Vec3i pos) {
		return this.setPosition(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
	}

	public T setPosition(@Nonnull final Vec3d pos) {
		return this.setPosition((float) pos.x, (float) pos.y, (float) pos.z);
	}

	@Override
	public float getYPosF() {
		final float y = super.getYPosF();
		return this.getAttenuationType() == AttenuationType.NONE ? y + ATTENUATION_OFFSET : y;
	}

	@SuppressWarnings("unchecked")
	public T setAttenuationType(@Nonnull final ISound.AttenuationType type) {
		this.attenuationType = type;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setRepeat(final boolean flag) {
		this.repeat = flag;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setRepeatDelay(final int delay) {
		this.repeatDelay = delay;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setVolumeScale(@Nonnull final ISoundScale scale) {
		this.volumeScale = scale;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T setVolumeThrottle(final float throttle) {
		this.volumeThrottle = throttle;
		return (T) this;
	}

	@Override
	public float getVolume() {
		return super.getVolume() * this.volumeScale.getScale() * this.volumeThrottle;
	}

	public void fade() {

	}

	public void unfade() {

	}

	public boolean isFading() {
		return false;
	}

	public boolean isDonePlaying() {
		return false;
	}

	public boolean canSoundBeHeard(@Nonnull final BlockPos soundPos) {
		return this.getAttenuationType() == AttenuationType.NONE ? true
				: SoundUtils.canBeHeard(this.pos, soundPos, this.getVolume());
	}

	public Vec3d getLocusPosition() {
		return new Vec3d(this.xPosF, this.yPosF, this.zPosF);
	}

	@Nonnull
	@Override
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString(NBT.SOUND_EVENT, this.positionedSoundLocation.toString());
		nbt.setString(NBT.SOUND_CATEGORY, this.category.getName());
		nbt.setFloat(NBT.VOLUME, this.volume);
		nbt.setFloat(NBT.PITCH, this.pitch);
		nbt.setFloat(NBT.X_COORD, this.xPosF);
		nbt.setFloat(NBT.Y_COORD, this.yPosF);
		nbt.setFloat(NBT.Z_COORD, this.zPosF);
		return nbt;
	}

	@Override
	public void deserializeNBT(@Nonnull final NBTTagCompound nbt) {
		final String cat = nbt.getString(NBT.SOUND_CATEGORY);
		if (StringUtils.isEmpty(cat))
			this.category = SoundCategory.PLAYERS;
		else
			this.category = SoundCategory.getByName(cat);

		this.positionedSoundLocation = new ResourceLocation(nbt.getString(NBT.SOUND_EVENT));
		this.volume = nbt.getFloat(NBT.VOLUME);
		this.pitch = nbt.getFloat(NBT.PITCH);
		this.xPosF = nbt.getFloat(NBT.X_COORD);
		this.yPosF = nbt.getFloat(NBT.Y_COORD);
		this.zPosF = nbt.getFloat(NBT.Z_COORD);
		this.pos.setPos(this.xPosF, this.yPosF, this.zPosF);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(this.positionedSoundLocation.toString())
				.addValue(this.category.toString()).add("state", this.getState()).add("v", this.getVolume())
				.add("p", this.getPitch()).add("s", this.volumeScale.getScale()).addValue(this.getAttenuationType())
				.add("x", this.getXPosF()).add("y", this.getYPosF()).add("z", this.getZPosF()).toString();
	}

	public static AttenuationType noAttenuation() {
		return ModEnvironment.SoundPhysics.isLoaded() ? AttenuationType.LINEAR : AttenuationType.NONE;
	}
}
