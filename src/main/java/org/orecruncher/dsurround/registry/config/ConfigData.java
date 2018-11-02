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

package org.orecruncher.dsurround.registry.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.data.Profiles;
import org.orecruncher.dsurround.data.Profiles.ProfileScript;
import org.orecruncher.dsurround.registry.config.packs.ResourcePacks;
import org.orecruncher.dsurround.registry.config.packs.ResourcePacks.Pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * Helper class that loads up the various configuration elements from JSON and
 * stores them in a compressed memory memory blob that can be iterated in
 * memory. Purpose is to retain the configuration data in a compact form to
 * allow quick resets of the Registries due to external events (like biome
 * changes on world load).
 */
public final class ConfigData implements Iterable<ModConfigurationFile> {

	// Holder of the compressed bytes containing our configuration
	private final byte[] crunchyBits;

	private ConfigData(@Nonnull final byte[] theBits) {
		this.crunchyBits = theBits;
	}

	// Injects a string into the array list. Used to put a marker that conveys
	// information about
	// the following Json object - which should be a ModConfiguration thing.
	private static void injectString(@Nonnull final String txt, @Nonnull final OutputStreamWriter out) {
		try {
			out.write('"');
			out.write(txt);
			out.write("\",");
		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Really??", t);
		}
	}

	// Does a simple comma append to the output stream.
	protected static void appendComma(@Nonnull final OutputStreamWriter out) {
		try {
			out.write(',');
		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Really??", t);
		}
	}

	// Reads strings from the buffered reader, trims them, and outputs to the output
	// writer
	// if the length of the trimmed string is > 0. Used to compress a lot of the
	// whitespace
	// that can be found in a hand edited Json file.
	protected static void copy(@Nonnull final BufferedReader reader, @Nonnull final OutputStreamWriter out) {
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				final String s = line.trim();
				if (!StringUtils.isEmpty(s))
					out.write(s);
			}
		} catch (@Nonnull final IOException e) {
			ModBase.log().error("Huh?", e);
		}
	}

	// Copies the data from the input stream to the output stream. The text is
	// injected before the write,
	// and a comma is prepended if the flag indicates to do so.
	protected static boolean copy(@Nonnull final InputStreamReader in, @Nonnull final OutputStreamWriter out,
			@Nonnull final String text, final boolean comma) {
		try {
			try (final BufferedReader reader = new BufferedReader(in)) {
				if (comma)
					appendComma(out);
				injectString(text, out);
				copy(reader, out);
				ModBase.log().debug("Loaded %s", text);
				return true;
			}
		} catch (@Nonnull final Throwable t) {
			ModBase.log().error(text, t);
		}
		return comma;
	}

	// Copies the specified resource from the given pack to the output location.
	// Essentially it will
	// be reading config Json information from resource packs and jars.
	protected static boolean copy(@Nonnull final Pack p, @Nonnull ResourceLocation rl,
			@Nonnull final OutputStreamWriter output, @Nullable String text, final boolean comma) {
		try (final InputStream is = p.getInputStream(rl)) {
			if (is != null) {
				try (final InputStreamReader stream = new InputStreamReader(is)) {
					return copy(stream, output, text, comma);
				} catch (@Nonnull final Throwable t) {
					ModBase.log().error(rl.toString(), t);
				}
			}
		} catch (@Nonnull final Throwable t) {
			ModBase.log().error(rl.toString(), t);
		}
		return comma;
	}

	@Nonnull
	private static File getFileReference(@Nonnull final String dataFile) {
		final String workingFile = StringUtils.appendIfMissing(Paths.get(dataFile).getFileName().toString(), ".json");
		return new File(ModBase.dataDirectory(), workingFile);
	}

	public static ConfigData load() {

		final ByteArrayOutputStream bits = new ByteArrayOutputStream();

		try (final OutputStreamWriter output = new OutputStreamWriter(new GZIPOutputStream(bits))) {

			// We are writing a Json array of objects so start with the open
			output.write("[");

			boolean prependComma = false;

			// Collect the locations where DS data is configured
			final List<Pack> packs = ResourcePacks.findResourcePacks();
			final List<ModContainer> activeMods = Loader.instance().getActiveModList();

			// Process the mod config from each of our packs. This includes the regular
			// files from the dsurround jar.
			for (final ModContainer mod : activeMods) {
				final ResourceLocation rl = new ResourceLocation(ModBase.MOD_ID,
						"data/" + mod.getModId().toLowerCase() + ".json");
				for (final Pack p : packs) {
					prependComma = copy(p, rl, output, "[" + rl.toString() + "] from [" + p.getModName() + "]",
							prependComma);
				}
			}

			// Get config data from our JAR.
			final ResourceLocation rl = ResourcePacks.CONFIGURE_RESOURCE;
			for (final Pack p : packs) {
				prependComma = copy(p, rl, output, "[" + rl.toString() + "] from [" + p.getModName() + "]",
						prependComma);
			}

			// Built in toggle profiles for turning feature sets on/off
			final List<ProfileScript> resources = Profiles.getProfileStreams();
			for (final ProfileScript script : resources) {
				try (final InputStreamReader reader = new InputStreamReader(script.stream)) {
					prependComma = copy(reader, output, script.packName, prependComma);
				} catch (@Nonnull final Throwable t) {
					ModBase.log().error("Error reading profile script", t);
				}
			}

			// Load scripts specified in the configuration file from disk. Usually supplied
			// by players or pack makers.
			for (final String cfg : ModOptions.general.externalScriptFiles) {
				final File file = getFileReference(cfg);
				if (file.exists()) {
					try (final InputStream stream = new FileInputStream(file)) {
						try (final InputStreamReader input = new InputStreamReader(stream)) {
							prependComma = copy(input, output, cfg, prependComma);
						} catch (final Throwable t) {
							ModBase.log().error("Really??", t);
						}
					} catch (final Throwable t) {
						ModBase.log().error("Really??", t);
					}
				}
			}

			// The tap - need to close out the json array and flush
			// the stream to make sure.
			output.write("]");
			output.flush();

		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Something went horribly wrong", t);
		}

		// dump(bits.toByteArray());

		return new ConfigData(bits.toByteArray());
	}

	protected static void dump(@Nonnull final byte[] demBytes) {

		try {
			String line = null;

			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new ByteArrayInputStream(demBytes)), StandardCharsets.UTF_8))) {
				while ((line = bufferedReader.readLine()) != null) {
					ModBase.log().info(line);
				}
			}

		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Error dumping bytes!", t);
		}

	}

	@Override
	@Nonnull
	public Iterator<ModConfigurationFile> iterator() {
		try {
			return new MCFIterator(this.crunchyBits);
		} catch (@Nonnull final Throwable t) {
			ModBase.log().error("Unable to create ModConfigurationFile iterator", t);
			return new Iterator<ModConfigurationFile>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public ModConfigurationFile next() {
					throw new IllegalStateException("Huh?");
				}
			};
		}
	}

	class MCFIterator implements Iterator<ModConfigurationFile>, Closeable {

		private final Gson gson;
		private final JsonReader reader;

		protected MCFIterator(@Nonnull final byte[] bits) throws IOException {
			this.gson = new GsonBuilder().create();
			this.reader = new JsonReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(bits))));
			this.reader.beginArray();
		}

		@Override
		public boolean hasNext() {
			try {
				return this.reader.hasNext();
			} catch (@Nonnull final IOException ex) {
				ModBase.log().error("Unable to read from memory!", ex);
			}
			return false;
		}

		@Override
		@Nullable
		public ModConfigurationFile next() {
			String source = null;
			try {
				// Should be a string followed by our Json object
				source = this.gson.fromJson(this.reader, String.class);
				final ModConfigurationFile mcf = this.gson.fromJson(this.reader, ModConfigurationFile.class);
				mcf.source = source;
				return mcf;
			} catch (@Nonnull final JsonSyntaxException | JsonIOException ex) {
				ModBase.log().error(source != null ? source : "Unable to parse Json from memory!", ex);
				return null;
			}
		}

		@Override
		public void close() throws IOException {
			this.reader.close();
		}

	}

}
