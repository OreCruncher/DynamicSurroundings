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

package org.blockartistry.DynSurround.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

public enum Profiles {

	//
	NO_NETHER_WEATHER("noNetherWeather", "No Nether Weather", "Disable weather effects in the Nether"),
	//
	NO_DUST_EFFECTS("noDustEffects", "No Dust Effects", "Turns off dust effects for all biomes");

	// Path within the JAR where the Json configuration files are located
	private final static String PROFILE_PATH = "/assets/dsurround/data/profiles/";

	// Token used to uniquely identify this entry. It is used to reference a
	// Json config in the JAR as well as lookup string translations for the
	// config GUI.
	private final String root;

	// Key within the config file on disk. It is in English and not translated.
	private final String key;

	// Description of the entry in the config file. It is in English and not
	// translated.
	private final String desc;

	private Profiles(@Nonnull final String rootName, @Nonnull final String propName, @Nonnull final String comment) {
		this.root = rootName;
		this.key = propName;
		this.desc = comment;
	}

	// Indicates whether the specific configuration is enabled as defined in the
	// configuration. If enabled the associated config file will be loaded and
	// applied.
	public boolean isEnabled() {
		final ConfigCategory cat = DSurround.config().getCategory(ModOptions.CATEGORY_PROFILES);
		Property prop = cat.get(this.key);
		if (prop == null) {
			prop = new Property(this.key, "false", Type.BOOLEAN);
			cat.put(this.key, prop);
			prop.setValue(false);
		}

		// Make sure the prop is properly filled out with the mods
		// latest info.
		prop.setComment(this.desc);
		prop.setLanguageKey("dsurround.cfg." + ModOptions.CATEGORY_PROFILES + "." + this.root);
		prop.setRequiresMcRestart(false);
		prop.setRequiresWorldRestart(false);
		prop.setDefaultValue(false);

		return prop.getBoolean();
	}

	// Gets a stream to Json file within the JAR.
	private InputStream getStream() {
		final String path = PROFILE_PATH + this.root + ".json";
		return Profiles.class.getResourceAsStream(path);
	}

	// Traverses the list within the enum setting up the Forge
	// config entries.
	public static void tickle() {
		for (final Profiles p : values())
			p.isEnabled();
	}

	// Gets a list of all the InputStreams for the enabled
	// Json configurations.
	public static List<InputStream> getProfileStreams() {
		final List<InputStream> results = new ArrayList<InputStream>();
		for (final Profiles p : values())
			if (p.isEnabled())
				results.add(p.getStream());

		return results;
	}
}
