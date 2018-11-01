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

import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigurationHelper {

	public static interface IConfigFilter {

		boolean skipCategory(@Nonnull final ConfigCategory category);

		boolean skipProperty(@Nonnull final ConfigCategory category, @Nonnull final Property property);
	}

	protected static final IConfigFilter DEFAULT_FILTER = new IConfigFilter() {
		@Override
		public boolean skipCategory(@Nonnull final ConfigCategory category) {
			return false;
		}

		@Override
		public boolean skipProperty(@Nonnull final ConfigCategory category, @Nonnull final Property property) {
			return false;
		}
	};

	protected final PresetData data;

	public ConfigurationHelper(@Nonnull final PresetData data) {
		this.data = data;
	}

	@Nonnull
	public ConfigurationHelper save(@Nonnull final ConfigCategory category, @Nonnull final Property prop) {
		if (category.requiresMcRestart() || category.requiresWorldRestart())
			this.data.restartRequired();
		return save(category.getQualifiedName(), prop);
	}

	@Nonnull
	public ConfigurationHelper save(@Nonnull final String category, @Nonnull final Property prop) {
		if (prop.requiresMcRestart() || prop.requiresWorldRestart())
			this.data.restartRequired();
		final String id = category + "." + prop.getName();
		switch (prop.getType()) {
		case STRING:
			if (prop.isList())
				this.data.setStringList(id, prop.getStringList(), PresetData.SPLIT_NEWLINE_CHAR);
			else
				this.data.setString(id, prop.getString());
			break;
		case INTEGER:
			if (prop.isList())
				this.data.setIntList(id, prop.getIntList());
			else
				this.data.setInt(id, prop.getInt());
			break;
		case BOOLEAN:
			if (prop.isList())
				this.data.setBooleanList(id, prop.getBooleanList());
			else
				this.data.setBoolean(id, prop.getBoolean());
			break;
		case DOUBLE:
			if (prop.isList())
				this.data.setDoubleList(id, prop.getDoubleList());
			else
				this.data.setDouble(id, prop.getDouble());
			break;
		case COLOR:
		case MOD_ID:
		default:
		}
		return this;
	}

	@Nonnull
	public ConfigurationHelper load(@Nonnull final ConfigCategory category, @Nonnull final Property prop) {
		return load(category.getQualifiedName(), prop);
	}

	@Nonnull
	public ConfigurationHelper load(@Nonnull final String category, @Nonnull final Property prop) {
		final String id = category + "." + prop.getName();
		switch (prop.getType()) {
		case STRING:
			if (prop.isList())
				prop.set(this.data.getStringList(id, prop.getDefaults(), PresetData.SPLIT_NEWLINE_CHAR));
			else
				prop.set(this.data.getString(id, prop.getDefault()));
			break;
		case INTEGER:
			if (prop.isList())
				prop.set(this.data.getIntList(id, Utils.toIntArray(prop.getDefaults())));
			else
				prop.set(this.data.getInt(id, Integer.parseInt(prop.getDefault())));
			break;
		case BOOLEAN:
			if (prop.isList())
				prop.set(this.data.getBooleanList(id, Utils.toBooleanArray(prop.getDefaults())));
			else
				prop.set(this.data.getBoolean(id, Boolean.parseBoolean(prop.getDefault())));
			break;
		case DOUBLE:
			if (prop.isList())
				prop.set(this.data.getDoubleList(id, Utils.toDoubleArray(prop.getDefaults())));
			else
				prop.set(this.data.getDouble(id, Double.parseDouble(prop.getDefault())));
			break;
		case COLOR:
		case MOD_ID:
		default:
		}
		return this;
	}

	@Nonnull
	public ConfigurationHelper load(@Nonnull final ConfigCategory category) {
		return load(category, DEFAULT_FILTER);
	}

	@Nonnull
	public ConfigurationHelper load(@Nonnull final ConfigCategory category, @Nonnull final IConfigFilter filter) {
		if (!filter.skipCategory(category)) {
			for (final Entry<String, Property> e : category.getValues().entrySet())
				if (!filter.skipProperty(category, e.getValue()))
					this.load(category, e.getValue());
			for (final ConfigCategory c : category.getChildren())
				this.load(c, filter);
		}
		return this;
	}

	@Nonnull
	public ConfigurationHelper save(@Nonnull final ConfigCategory category) {
		return save(category, DEFAULT_FILTER);
	}

	@Nonnull
	public ConfigurationHelper save(@Nonnull final ConfigCategory category, @Nonnull final IConfigFilter filter) {
		if (!filter.skipCategory(category)) {
			if (category.requiresMcRestart() || category.requiresWorldRestart())
				this.data.restartRequired();
			for (final Entry<String, Property> e : category.getValues().entrySet())
				if (!filter.skipProperty(category, e.getValue()))
					this.save(category, e.getValue());
			for (final ConfigCategory c : category.getChildren())
				this.save(c, filter);
		}
		return this;
	}

	@Nonnull
	public ConfigurationHelper load(@Nonnull final Configuration config) {
		return load(config, DEFAULT_FILTER);
	}

	@Nonnull
	public ConfigurationHelper load(@Nonnull final Configuration config, @Nonnull final IConfigFilter filter) {
		for (final String cat : config.getCategoryNames())
			this.load(config.getCategory(cat), filter);
		return this;
	}

	@Nonnull
	public ConfigurationHelper save(@Nonnull final Configuration config) {
		return save(config, DEFAULT_FILTER);
	}

	@Nonnull
	public ConfigurationHelper save(@Nonnull final Configuration config, @Nonnull final IConfigFilter filter) {
		for (final String cat : config.getCategoryNames())
			this.save(config.getCategory(cat), filter);
		return this;
	}
}
