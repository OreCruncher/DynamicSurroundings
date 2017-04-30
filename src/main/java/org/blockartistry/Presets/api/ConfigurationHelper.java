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

package org.blockartistry.Presets.api;

import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigurationHelper {

	protected final PresetData data;

	public ConfigurationHelper(@Nonnull final PresetData data) {
		this.data = data;
	}

	public ConfigurationHelper save(@Nonnull final String category, @Nonnull final Property prop) {
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

	public ConfigurationHelper load(@Nonnull final ConfigCategory category) {
		final String id = category.getQualifiedName();
		for (final Entry<String, Property> e : category.getValues().entrySet())
			this.load(id, e.getValue());
		for (final ConfigCategory c : category.getChildren())
			this.load(c);
		return this;
	}

	public ConfigurationHelper save(@Nonnull final ConfigCategory category) {
		final String id = category.getQualifiedName();
		for (final Entry<String, Property> e : category.getValues().entrySet())
			this.save(id, e.getValue());
		for (final ConfigCategory c : category.getChildren())
			this.save(c);
		return this;
	}

	public ConfigurationHelper load(@Nonnull final Configuration config) {
		for (final String cat : config.getCategoryNames())
			this.load(config.getCategory(cat));
		return this;
	}

	public ConfigurationHelper save(@Nonnull final Configuration config) {
		for (final String cat : config.getCategoryNames())
			this.save(config.getCategory(cat));
		return this;
	}
}
