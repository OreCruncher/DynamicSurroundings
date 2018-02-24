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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.Presets.Presets;
import org.blockartistry.Presets.api.events.PresetEvent;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraftforge.common.MinecraftForge;

public class PresetConfig {

	protected static final String PRESET_EXT = ".presets";
	protected static final FilenameFilter FILTER = (dir, name) -> name.endsWith(PRESET_EXT);

	protected final File dir;
	protected final List<PresetInfo> presets = Lists.newArrayList();
	protected boolean hasScanned = false;

	public PresetConfig(@Nonnull final File directory) {
		this.dir = directory;
	}

	/*
	 * Retrieves an immutable list of presets that are present within the Presets!
	 * config directory.
	 */
	@Nonnull
	public List<PresetInfo> getPresets() {
		if (!this.hasScanned)
			scan();
		return ImmutableList.copyOf(this.presets);
	}

	/*
	 * Forces a scan of the Presets! directory to find presets. The internal list of
	 * presets will be updated with the changes.
	 */
	public void scan() {
		this.presets.clear();
		for (final File f : this.dir.listFiles(FILTER)) {
			final PresetInfo info = load0(f);
			if (info != null)
				this.presets.add(info);
		}
		Collections.sort(this.presets);
		this.hasScanned = true;
	}

	/*
	 * Takes the given preset and fires an event for mods to load the contained data
	 * into their configurations.
	 */
	public void applyPreset(@Nonnull final PresetInfo info) {
		final PresetEvent.Load event = new PresetEvent.Load(info.getData());
		MinecraftForge.EVENT_BUS.post(event);
	}

	/*
	 * Requests that mods provide their setting information into the PresetInfo
	 * object in preparation for saving.
	 */
	public PresetInfo collectPreset(@Nonnull final PresetInfo info) {
		final PresetEvent.Save event = new PresetEvent.Save();
		MinecraftForge.EVENT_BUS.post(event);
		info.set1(event.getData());
		return info;
	}

	/*
	 * Saves the preset to a configuration file on disk.
	 */
	public void save(@Nonnull final PresetInfo info) {
		if (!info.getFilename().endsWith(PRESET_EXT))
			info.setFilename(info.getFilename() + PRESET_EXT);

		save0(info);

		final Optional<PresetInfo> existing = Iterators.tryFind(this.presets.iterator(),
				(@Nonnull final PresetInfo input) -> input.getFilename().equals(info.getFilename()));

		if (existing.isPresent()) {
			this.presets.remove(existing.get());
		}
		this.presets.add(info);
	}

	/*
	 * Deletes the preset information from disk. Caller should rescan after
	 * performing any deletions to ensure a current image of the file system.
	 */
	public void delete(@Nonnull final PresetInfo info) {
		final File file = new File(this.dir, info.fileName);
		if (file.exists())
			file.delete();
	}

	@Nullable
	protected PresetInfo load0(@Nonnull final File dataFile) {
		try (final Reader in = new FileReader(dataFile)) {
			final Gson gson = new GsonBuilder().create();
			final PresetDataFile dataOnDisk = gson.fromJson(in, PresetDataFile.class);
			return new PresetInfo(dataFile.getName()).set(dataOnDisk);
		} catch (final Throwable t) {
			Presets.log().error("Unable to load data " + dataFile.getName(), t);
		}
		return null;
	}

	@Nonnull
	protected PresetInfo save0(@Nonnull PresetInfo info) {
		try {

			final File dataFile = new File(this.dir, info.getFilename());
			try (final Writer out = new FileWriter(dataFile)) {
				final Gson gson = new GsonBuilder().setPrettyPrinting().create();
				gson.toJson(new PresetDataFile(info), out);
			}

		} catch (final Throwable t) {
			Presets.log().error("Unable to save data " + info.getFilename(), t);
		}

		return info;
	}

}
