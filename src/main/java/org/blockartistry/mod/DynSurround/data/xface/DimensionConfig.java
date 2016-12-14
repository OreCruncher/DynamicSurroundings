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

import com.google.gson.annotations.SerializedName;

public class DimensionConfig {
	@SerializedName("dimId")
	public Integer dimensionId = null;
	@SerializedName("name")
	public String name = null;
	@SerializedName("seaLevel")
	public Integer seaLevel = null;
	@SerializedName("skyHeight")
	public Integer skyHeight = null;
	@SerializedName("cloudHeight")
	public Integer cloudHeight = null;
	@SerializedName("haze")
	public Boolean hasHaze = null;
	@SerializedName("aurora")
	public Boolean hasAurora = null;
	@SerializedName("weather")
	public Boolean hasWeather = null;

	public DimensionConfig setDimensionId(final int id) {
		this.dimensionId = id;
		return this;
	}

	public DimensionConfig setDimensionName(final String name) {
		this.name = name;
		return this;
	}

	public DimensionConfig setSeaLevel(final int level) {
		this.seaLevel = level;
		return this;
	}

	public DimensionConfig setSkyHeight(final int height) {
		this.skyHeight = height;
		return this;
	}

	public DimensionConfig setCloudHeight(final int height) {
		this.cloudHeight = height;
		return this;
	}

	public DimensionConfig setHasCloudHaze(final boolean flag) {
		this.hasHaze = flag;
		return this;
	}

	public DimensionConfig setHasWeather(final boolean flag) {
		this.hasWeather = flag;
		return this;
	}

	public DimensionConfig setHasAuroras(final boolean flag) {
		this.hasAurora = flag;
		return this;
	}
	
	public void register() throws InvalidArgument {
		Dimensions.register(this);
	}
}
