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

import org.blockartistry.mod.DynSurround.Module;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public final class DimensionEffectDataFile extends WorldSavedData {

	private final static String IDENTIFIER = Module.MOD_ID;

	private final class NBT {
		public final static String ENTRIES = "e";
	};

	private final TIntObjectHashMap<DimensionEffectData> dataList = new TIntObjectHashMap<DimensionEffectData>();

	public DimensionEffectDataFile() {
		this(IDENTIFIER);
	}

	public DimensionEffectDataFile(@Nonnull final String id) {
		super(id);
	}

	private static DimensionEffectDataFile getFile(@Nonnull final World world) {
		DimensionEffectDataFile data = (DimensionEffectDataFile) world.loadItemData(DimensionEffectDataFile.class, IDENTIFIER);
		if (data == null) {
			data = new DimensionEffectDataFile();
			world.setItemData(IDENTIFIER, data);
		}
		data.markDirty();
		return data;
	}

	private DimensionEffectData getData(final int dimensionId) {
		DimensionEffectData data = this.dataList.get(dimensionId);
		if (data != null)
			return data;
		data = new DimensionEffectData(dimensionId);
		this.dataList.put(dimensionId, data);
		return data;
	}

	public static DimensionEffectData get(@Nonnull final World world) {
		return getFile(world).getData(world.provider.getDimension());
	}

	@Override
	public void readFromNBT(@Nonnull final NBTTagCompound nbt) {
		final NBTTagList list = nbt.getTagList(NBT.ENTRIES, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			final NBTTagCompound tag = list.getCompoundTagAt(i);
			final DimensionEffectData data = new DimensionEffectData();
			data.readFromNBT(tag);
			this.dataList.put(data.getDimensionId(), data);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(@Nonnull final NBTTagCompound nbt) {
		final NBTTagList list = new NBTTagList();
		for (final DimensionEffectData data : this.dataList.valueCollection()) {
			final NBTTagCompound tag = new NBTTagCompound();
			data.writeToNBT(tag);
			list.appendTag(tag);
		}
		nbt.setTag(NBT.ENTRIES, list);
		return nbt;
	}
}
