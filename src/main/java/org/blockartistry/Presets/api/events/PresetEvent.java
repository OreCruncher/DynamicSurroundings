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
package org.blockartistry.Presets.api.events;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.Presets.api.PresetData;

import com.google.common.collect.ImmutableMap;

import net.minecraftforge.fml.common.eventhandler.Event;

/*
 * Mods can listen for this event to load/save their configuration
 * data at the appropriate time.
 */
public class PresetEvent extends Event {

	protected final Map<String, PresetData> data;

	protected PresetEvent(@Nonnull final Map<String, PresetData> data) {
		this.data = data;
	}
	
	/*
	 * Get a read only copy of the underlying data.
	 */
	@Nonnull
	public Map<String, PresetData> getData() {
		return ImmutableMap.copyOf(this.data);
	}

	/*
	 * Retrieves the configuration data for the specified mod from
	 * the preset data.  If not present a null will be returned.
	 */
	@Nullable
	public PresetData getModData(@Nonnull final String modId) {
		return this.data.get(modId);
	}

	/*
	 * Event to read configuration data from a preset.  Typically
	 * the data is read and set in a mod's Configuration object
	 * then saved to disk as needed.  Actual implementation is up
	 * to a given mod.
	 */
	public static class Load extends PresetEvent {

		public Load(@Nonnull final Map<String, PresetData> data) {
			super(data);
		}

	}

	/*
	 * Event to save mod configuration data into a preset.  Typically
	 * data is read from a mod's Configuration object, however, the
	 * actual implementation is up to the mod in question.
	 */
	public static class Save extends PresetEvent {

		public Save() {
			super(new HashMap<String, PresetData>());
		}

		/*
		 * Retrieves the configuration data for the specified mod
		 * from the preset data.  If not present a new section will
		 * be created and returned.
		 */
		@Override
		@Nonnull
		public PresetData getModData(@Nonnull final String modId) {
			PresetData result = super.getModData(modId);
			if (result == null)
				this.data.put(modId, result = new PresetData());
			return result;
		}

	}

}
