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

package org.blockartistry.DynSurround.client.footsteps.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.blockartistry.DynSurround.ModLog;
import org.blockartistry.lib.JsonUtils;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ConfigProperty {
	private Map<String, String> properties;

	public ConfigProperty() {
		this.properties = new HashMap<String, String>();
	}

	public String getString(final String name) {
		if (!this.properties.containsKey(name))
			throw new PropertyMissingException();

		return this.properties.get(name);
	}

	public boolean getBoolean(final String name) {
		if (!this.properties.containsKey(name))
			throw new PropertyMissingException();

		try {
			return Boolean.parseBoolean(this.properties.get(name));
		} catch (NumberFormatException e) {
			throw new PropertyTypeException();
		}
	}

	public int getInteger(final String name) {
		if (!this.properties.containsKey(name))
			throw new PropertyMissingException();

		try {
			return Integer.parseInt(this.properties.get(name));
		} catch (NumberFormatException e) {
			throw new PropertyTypeException();
		}
	}

	public float getFloat(final String name) {
		if (!this.properties.containsKey(name))
			throw new PropertyMissingException();

		try {
			return Float.parseFloat(this.properties.get(name));
		} catch (NumberFormatException e) {
			throw new PropertyTypeException();
		}
	}

	public long getLong(final String name) {
		if (!this.properties.containsKey(name))
			throw new PropertyMissingException();

		try {
			return Long.parseLong(this.properties.get(name));
		} catch (NumberFormatException e) {
			throw new PropertyTypeException();
		}
	}

	public double getDouble(final String name) {
		if (!this.properties.containsKey(name))
			throw new PropertyMissingException();

		try {
			return Double.parseDouble(this.properties.get(name));
		} catch (NumberFormatException e) {
			throw new PropertyTypeException();
		}
	}

	public void setProperty(final String name, Object o) {
		this.properties.put(name, o.toString());
	}

	public Map<String, String> getAllProperties() {
		return this.properties;
	}

	public static ConfigProperty fromStream(final InputStream stream) {
		final ConfigProperty props = new ConfigProperty();
		loadStream(props, stream);
		return props;
	}

	@SuppressWarnings("unchecked")
	public static boolean loadStream(final ConfigProperty properties, final InputStream stream) {
		if(stream == null)
			return false;
		try {
			properties.properties = JsonUtils.load(stream, properties.properties.getClass());
		} catch(final Throwable t) {
			ModLog.error("Unable to load properties", t);
		}
		return true;
	}

}
