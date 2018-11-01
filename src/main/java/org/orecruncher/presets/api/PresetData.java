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

package org.orecruncher.presets.api;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public final class PresetData {

	public static final char SPLIT_NEWLINE_CHAR = '\n';
	public static final char SPLIT_SPACE_CHAR = ' ';

	private final Map<String, String> data;
	private boolean restartRequired = false;

	public PresetData() {
		this.data = Maps.newHashMap();
	}

	public PresetData(@Nonnull final Map<String, String> data) {
		this.data = data;
	}

	public PresetData restartRequired() {
		this.restartRequired = true;
		return this;
	}

	public boolean isRestartRequired() {
		return this.restartRequired;
	}

	public PresetData setInt(@Nonnull final String id, final int value) {
		this.data.put(id, Integer.toString(value));
		return this;
	}

	public PresetData setIntList(@Nonnull final String id, @Nonnull final int[] values) {
		final String result = StringUtils.join(Utils.toStringArray(values), SPLIT_SPACE_CHAR);
		setString(id, result);
		return this;
	}

	public PresetData setBoolean(@Nonnull final String id, final boolean value) {
		this.data.put(id, Boolean.toString(value));
		return this;
	}

	public PresetData setBooleanList(@Nonnull final String id, @Nonnull final boolean[] values) {
		final String result = StringUtils.join(Utils.toStringArray(values), SPLIT_SPACE_CHAR);
		setString(id, result);
		return this;
	}

	public PresetData setDouble(@Nonnull final String id, final double value) {
		this.data.put(id, Double.toString(value));
		return this;
	}

	public PresetData setDoubleList(@Nonnull final String id, @Nonnull final double[] values) {
		final String result = StringUtils.join(Utils.toStringArray(values), SPLIT_SPACE_CHAR);
		setString(id, result);
		return this;
	}

	public PresetData setString(@Nonnull final String id, final String value) {
		this.data.put(id, value);
		return this;
	}

	public PresetData setStringList(@Nonnull final String id, @Nonnull final String[] values, final char splitChar) {
		setString(id, StringUtils.join(values, splitChar));
		return this;
	}

	public int getInt(@Nonnull final String id, final int def) {
		if (this.data.containsKey(id))
			return Integer.parseInt(this.data.get(id));
		return def;
	}

	public int[] getIntList(@Nonnull final String id, @Nullable final int[] def) {
		if (this.data.containsKey(id)) {
			return Utils.toIntArray(StringUtils.split(this.data.get(id), SPLIT_SPACE_CHAR));
		}
		return def;
	}

	public boolean getBoolean(@Nonnull final String id, final boolean def) {
		if (this.data.containsKey(id))
			return Boolean.parseBoolean(this.data.get(id));
		return def;
	}

	public boolean[] getBooleanList(@Nonnull final String id, @Nullable final boolean[] def) {
		if (this.data.containsKey(id)) {
			return Utils.toBooleanArray(StringUtils.split(this.data.get(id), SPLIT_SPACE_CHAR));
		}
		return def;
	}

	public double getDouble(@Nonnull final String id, final double def) {
		if (this.data.containsKey(id))
			return Double.parseDouble(this.data.get(id));
		return def;
	}

	public double[] getDoubleList(@Nonnull final String id, @Nullable final double[] def) {
		if (this.data.containsKey(id)) {
			return Utils.toDoubleArray(StringUtils.split(this.data.get(id), SPLIT_SPACE_CHAR));
		}
		return def;
	}

	@Nullable
	public String getString(@Nonnull final String id, @Nullable final String def) {
		if (this.data.containsKey(id))
			return this.data.get(id);
		return def;
	}

	public String[] getStringList(@Nonnull final String id, @Nullable final String[] def, final char splitChar) {
		if (this.data.containsKey(id)) {
			return StringUtils.split(this.data.get(id), splitChar);
		}
		return def;
	}

	public boolean contains(@Nonnull final String id) {
		return this.data.containsKey(id);
	}

	@Nonnull
	public Set<Entry<String, String>> getEntries() {
		return ImmutableSet.copyOf(this.data.entrySet());
	}

}
