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
package org.blockartistry.Presets.data;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.Presets.api.PresetData;

import com.google.common.collect.Maps;

public class PresetInfo implements Comparable<PresetInfo> {

	protected String fileName = StringUtils.EMPTY;
	protected String title = StringUtils.EMPTY;
	protected String description = StringUtils.EMPTY;
	protected boolean restartRequired = false;
	protected Map<String, PresetData> data = Maps.newHashMap();

	public PresetInfo() {

	}

	public PresetInfo(@Nonnull final PresetInfo src) {
		this.title = src.title;
		this.description = src.description;
		this.fileName = src.fileName;
		this.data = src.data;
	}

	public PresetInfo(@Nonnull String fileName) {
		this.fileName = fileName;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public String getFilename() {
		return this.fileName;
	}

	public boolean isRestartRequired() {
		return this.restartRequired;
	}

	public PresetInfo setTitle(@Nonnull final String title) {
		this.title = title;
		return this;
	}

	public PresetInfo setDescription(@Nonnull final String description) {
		this.description = description;
		return this;
	}

	public PresetInfo setFilename(@Nonnull final String fileName) {
		this.fileName = fileName;
		return this;
	}

	public PresetInfo setRestartRequired(final boolean flag) {
		this.restartRequired = flag;
		return this;
	}

	@Override
	public int compareTo(@Nonnull final PresetInfo o) {
		return this.title.compareTo(o.title);
	}

	// THESE METHODS ARE INTERNAL TO PRESETS

	PresetInfo set0(@Nonnull final Map<String, Map<String, String>> data) {
		this.data = Maps.newHashMap();
		for (final Entry<String, Map<String, String>> d : data.entrySet()) {
			this.data.put(d.getKey(), new PresetData(d.getValue()));
		}
		return this;
	}

	PresetInfo set1(@Nonnull final Map<String, PresetData> data) {
		this.data = data;
		this.restartRequired = false;
		for (final PresetData d : this.data.values())
			if (d.isRestartRequired()) {
				this.restartRequired = true;
				break;
			}
		return this;
	}

	PresetInfo set(@Nonnull final PresetDataFile data) {
		setTitle(data.title);
		setDescription(data.description);
		set0(data.data);
		setRestartRequired(data.restartRequired);
		return this;
	}

	Map<String, PresetData> getData() {
		return this.data;
	}

}
