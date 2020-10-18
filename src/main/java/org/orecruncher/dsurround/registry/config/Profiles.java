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

package org.orecruncher.dsurround.registry.config;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.lib.JsonUtils;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

public class Profiles {

	// Path within the JAR where the Json configuration files are located
	private static final String PROFILE_PATH = "/assets/dsurround/profiles/";
	private static Profiles profiles;

	static {
		final String index = PROFILE_PATH + "_index.json";
		try {
			profiles = JsonUtils.loadFromJar(Profiles.class, index);
		} catch (@Nonnull final Throwable ignore) {
		}
	}

	private final List<ProfileEntry> entries = ImmutableList.of();

	// Traverses the list within the enum setting up the Forge
	// config entries.
	public static void tickle() {
		if (profiles != null)
			profiles.entries.forEach(ProfileEntry::isEnabled);
	}

	// Gets a list of all the InputStreams for the enabled
	// Json configurations.
	public static List<ProfileScript> getProfileStreams() {
		//@formatter:off
		return profiles != null ? profiles.entries.stream()
			.filter(ProfileEntry::isEnabled)
			.map(ProfileScript::new)
			.collect(Collectors.toList()) : ImmutableList.of();
		//@formatter:on
	}

	private static class ProfileEntry {
		@SerializedName("profileName")
		public String profileName;
		@SerializedName("optionName")
		public String optionName;
		@SerializedName("optionDescription")
		public String optionDescription;

		public boolean isValid() {
			return !(StringUtils.isEmpty(this.profileName) || StringUtils.isEmpty(this.optionName)
					|| StringUtils.isEmpty(this.optionDescription));
		}

		// Indicates whether the specific configuration is enabled as defined in the
		// configuration. If enabled the associated config file will be loaded and
		// applied.
		public boolean isEnabled() {
			if (!isValid())
				return false;
			final ConfigCategory cat = ModBase.config().getCategory(ModOptions.CATEGORY_PROFILES);
			Property prop = cat.get(this.optionName);
			if (prop == null) {
				prop = new Property(this.optionName, "false", Type.BOOLEAN);
				cat.put(this.optionName, prop);
				prop.setValue(false);
			}

			// Make sure the prop is properly filled out with the mods
			// latest info.
			prop.setComment(this.optionDescription);
			prop.setLanguageKey(this.optionName);
			prop.setRequiresMcRestart(false);
			prop.setRequiresWorldRestart(false);
			prop.setDefaultValue(false);

			return prop.getBoolean();
		}

		// Gets a stream to Json file within the JAR.
		public InputStream getStream() {
			final String path = PROFILE_PATH + this.profileName + ".json";
			return Profiles.class.getResourceAsStream(path);
		}

	}

	public static class ProfileScript {
		public final String packName;
		public final InputStream stream;

		public ProfileScript(@Nonnull final ProfileEntry entry) {
			this.packName = entry.profileName;
			this.stream = entry.getStream();
		}
	}

}
