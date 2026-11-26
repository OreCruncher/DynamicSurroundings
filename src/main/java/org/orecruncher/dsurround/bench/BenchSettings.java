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

package org.orecruncher.dsurround.bench;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.lib.ConfigProcessor;
import org.orecruncher.lib.ConfigProcessor.Category;
import org.orecruncher.lib.ConfigProcessor.Option;
import org.orecruncher.lib.ConfigProcessor.RestartRequired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * The settings a bench run sets, named by their place in {@link ModOptions}: the nested class, then the field
 * ({@code aurora.maxBands}, {@code rain.enableBackgroundThunder}, {@code commands.calc.commandNameCalc}). Each is the
 * Forge config property behind that field; a run sets the properties in memory and has {@link ConfigProcessor} bring
 * the fields in step, as the config screen does. Nothing is written to the config file, so a session that ends partway
 * leaves no setting changed. One that needs the game restarted is refused up front.
 */
final class BenchSettings {

	private BenchSettings() {
	}

	/**
	 * The config property behind a ModOptions path
	 */
	@Nonnull
	static Property resolve(@Nonnull final String path) {
		final String[] parts = path.split("\\.");
		if (parts.length < 2)
			throw new IllegalArgumentException("setting '" + path + "' needs a group and a field (aurora.maxBands)");
		Class<?> group = ModOptions.class;
		String category = null;
		for (int i = 0; i < parts.length - 1; i++) {
			Class<?> found = null;
			for (final Class<?> c : group.getDeclaredClasses())
				if (c.getSimpleName().equalsIgnoreCase(parts[i]) && c.getAnnotation(Category.class) != null)
					found = c;
			if (found == null)
				throw new IllegalArgumentException("setting '" + path + "': no group '" + parts[i] + "'");
			final String name = found.getAnnotation(Category.class).value();
			category = category == null ? name : category + "." + name;
			group = found;
		}
		final Field field;
		try {
			field = group.getField(parts[parts.length - 1]);
		} catch (final NoSuchFieldException e) {
			throw new IllegalArgumentException("setting '" + path + "': no field '" + parts[parts.length - 1] + "'");
		}
		final Option option = field.getAnnotation(Option.class);
		if (option == null)
			throw new IllegalArgumentException("setting '" + path + "' isn't a config option");
		final RestartRequired restart = field.getAnnotation(RestartRequired.class);
		final RestartRequired groupRestart = group.getAnnotation(RestartRequired.class);
		if (restart != null && restart.server() || groupRestart != null && groupRestart.server())
			throw new IllegalArgumentException("setting '" + path + "' needs the game restarted");
		final Configuration config = ModBase.config();
		final Property property = config.getCategory(category).get(option.value());
		if (property == null)
			throw new IllegalArgumentException("setting '" + path + "' has no property in the config");
		return property;
	}

	/**
	 * The value a setting has now, as JSON
	 */
	@Nonnull
	static JsonElement current(@Nonnull final String path) {
		final Property property = resolve(path);
		if (property.isList()) {
			final JsonArray array = new JsonArray();
			for (final String s : property.getStringList())
				array.add(s);
			return array;
		}
		switch (property.getType()) {
		case BOOLEAN:
			return new JsonPrimitive(property.getBoolean());
		case INTEGER:
			return new JsonPrimitive(property.getInt());
		case DOUBLE:
			return new JsonPrimitive(property.getDouble());
		default:
			return new JsonPrimitive(property.getString());
		}
	}

	/**
	 * The values the settings have now, to put back
	 */
	@Nonnull
	static Map<String, JsonElement> current(@Nonnull final Iterable<String> paths) {
		final Map<String, JsonElement> values = new LinkedHashMap<>();
		for (final String path : paths)
			values.put(path, current(path));
		return values;
	}

	/**
	 * Sets the values, then brings ModOptions in step with them
	 */
	static void apply(@Nonnull final Map<String, JsonElement> values) {
		for (final Map.Entry<String, JsonElement> entry : values.entrySet())
			set(resolve(entry.getKey()), entry.getKey(), entry.getValue());
		ConfigProcessor.process(ModBase.config(), ModOptions.class);
	}

	/**
	 * Checks each setting can be set with its value, without setting it
	 *
	 * @return what is wrong, or null if nothing
	 */
	@Nullable
	static String check(@Nonnull final Map<String, JsonElement> values) {
		final StringBuilder problems = new StringBuilder();
		for (final Map.Entry<String, JsonElement> entry : values.entrySet()) {
			try {
				final Property property = resolve(entry.getKey());
				final JsonElement value = entry.getValue();
				if (property.isList() ? !value.isJsonArray() : !value.isJsonPrimitive())
					throw new IllegalArgumentException("setting '" + entry.getKey() + "' needs "
							+ (property.isList() ? "a list" : "a single value"));
			} catch (final IllegalArgumentException e) {
				problems.append(problems.length() > 0 ? "; " : "").append(e.getMessage());
			}
		}
		return problems.length() == 0 ? null : problems.toString();
	}

	private static void set(@Nonnull final Property property, @Nonnull final String path,
			@Nonnull final JsonElement value) {
		if (property.isList()) {
			final JsonArray array = value.getAsJsonArray();
			final String[] list = new String[array.size()];
			for (int i = 0; i < list.length; i++)
				list[i] = array.get(i).getAsString();
			property.set(list);
			return;
		}
		switch (property.getType()) {
		case BOOLEAN:
			property.set(value.getAsBoolean());
			break;
		case INTEGER:
			property.set(value.getAsInt());
			break;
		case DOUBLE:
			property.set(value.getAsDouble());
			break;
		default:
			property.set(value.getAsString());
			break;
		}
	}
}
