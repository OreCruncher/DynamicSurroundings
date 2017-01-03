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

package org.blockartistry.mod.DynSurround.data;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.api.events.AuroraSpawnEvent;
import org.blockartistry.mod.DynSurround.util.INBTSerialization;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public final class AuroraData implements INBTSerialization {

	private static final class NBT {
		public static final String DIMENSION = "d";
		public static final String XCOORD = "x";
		public static final String ZCOORD = "z";
		public static final String SEED = "t";
		public static final String COLOR_SET = "s";
		public static final String PRESET = "p";
	}

	public int dimensionId;
	public int posX;
	public int posZ;
	public long seed;
	public int colorSet;
	public int preset;

	public AuroraData() {
	}
	
	public AuroraData(@Nonnull final AuroraSpawnEvent event) {
		this.dimensionId = event.world.provider.getDimension();
		this.posX = event.posX;
		this.posZ = event.posZ;
		this.seed = event.seed;
		this.colorSet = event.colorSet;
		this.preset = event.preset;
	}

	public AuroraData(@Nonnull final EntityPlayer player, final int zOffset, final int colorSet, final int preset) {
		this(player.worldObj.provider.getDimension(), (int) player.posX, (int) player.posZ + zOffset,
				player.worldObj.getWorldTime(), colorSet, preset);
	}

	public AuroraData(final int dimensionId, final int x, final int z, final long seed, final int colorSet,
			final int preset) {
		this.dimensionId = dimensionId;
		this.posX = x;
		this.posZ = z;
		this.seed = seed;
		this.colorSet = colorSet;
		this.preset = preset;
	}
	
	public long distanceSq(final Entity entity, final int offset) {
		final long deltaX = this.posX - (int) entity.posX;
		final long deltaZ = this.posZ - (int) entity.posZ + offset;
		return deltaX * deltaX + deltaZ * deltaZ;
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		this.dimensionId = nbt.getInteger(NBT.DIMENSION);
		this.posX = nbt.getInteger(NBT.XCOORD);
		this.posZ = nbt.getInteger(NBT.ZCOORD);
		this.seed = nbt.getLong(NBT.SEED);
		this.colorSet = nbt.getInteger(NBT.COLOR_SET);
		this.preset = nbt.getInteger(NBT.PRESET);
	}

	@Override
	public void writeToNBT(@Nonnull final NBTTagCompound nbt) {
		nbt.setInteger(NBT.DIMENSION, this.dimensionId);
		nbt.setInteger(NBT.XCOORD, this.posX);
		nbt.setInteger(NBT.ZCOORD, this.posZ);
		nbt.setLong(NBT.SEED, this.seed);
		nbt.setInteger(NBT.COLOR_SET, this.colorSet);
		nbt.setInteger(NBT.PRESET, this.preset);
	}

	@Override
	public boolean equals(@Nonnull final Object anObj) {
		if (!(anObj instanceof AuroraData))
			return false;
		final AuroraData a = (AuroraData) anObj;
		return (this.dimensionId == a.dimensionId) && (this.posX == a.posX) && (this.posZ == a.posZ);
	}
	
	@Override
	public int hashCode() {
		return this.posX ^ this.posZ;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[x: ").append(this.posX).append(", z: ").append(this.posZ).append(']');
		builder.append(" color: ").append(this.colorSet);
		builder.append(" preset: ").append(this.preset);
		builder.append(" seed: ").append(this.seed);
		return builder.toString();
	}
}
