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

package org.blockartistry.mod.DynSurround.data.xface;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class BiomeConfig {
	@SerializedName("biomeName")
	public String biomeName = null;
	@SerializedName("precipitation")
	public Boolean hasPrecipitation = null;
	@SerializedName("dust")
	public Boolean hasDust = null;
	@SerializedName("aurora")
	public Boolean hasAurora = null;
	@SerializedName("fog")
	public Boolean hasFog = null;
	@SerializedName("dustColor")
	public String dustColor = null;
	@SerializedName("fogColor")
	public String fogColor = null;
	@SerializedName("fogDensity")
	public Float fogDensity = null;
	@SerializedName("soundReset")
	public Boolean soundReset = null;
	@SerializedName("spotSoundChance")
	public Integer spotSoundChance = null;
	@SerializedName("sounds")
	public List<SoundConfig> sounds = new ArrayList<SoundConfig>();
}
