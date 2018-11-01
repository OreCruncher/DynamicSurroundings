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

package org.orecruncher.presets.data;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.presets.api.PresetData;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;

class PresetDataFile {

	@SerializedName("title")
	public String title = StringUtils.EMPTY;
	@SerializedName("description")
	public String description = StringUtils.EMPTY;
	@SerializedName("restartRequired")
	public boolean restartRequired = false;
	@SerializedName("data")
	public Map<String, Map<String, String>> data = Maps.newHashMap();

	public PresetDataFile() {

	}

	public PresetDataFile(@Nonnull final PresetInfo info) {
		this.title = info.getTitle();
		this.description = info.getDescription();
		this.restartRequired = info.isRestartRequired();
		this.data = Maps.newHashMap();
		for (final Entry<String, PresetData> d : info.getData().entrySet()) {
			final Map<String, String> data = Maps.newHashMap();
			for (final Entry<String, String> e : d.getValue().getEntries())
				data.put(e.getKey(), e.getValue());
			this.data.put(d.getKey(), data);
		}
	}

}
