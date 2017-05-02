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

import java.util.Set;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.Presets.api.ConfigurationHelper;
import org.blockartistry.Presets.api.ConfigurationHelper.IConfigFilter;
import org.blockartistry.Presets.api.PresetData;
import org.blockartistry.Presets.api.events.PresetEvent;
import org.blockartistry.lib.ConfigProcessor;

import com.google.common.collect.Sets;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class PresetHandler {

	private static final Set<String> categoriesToIgnore = Sets.newHashSet();

	static {
		categoriesToIgnore.add("asm");
		categoriesToIgnore.add("logging");
	}

	private static final IConfigFilter FILTER = new IConfigFilter() {
		@Override
		public boolean skipCategory(@Nonnull final ConfigCategory category) {
			return categoriesToIgnore.contains(category.getQualifiedName());
		}

		@Override
		public boolean skipProperty(@Nonnull final ConfigCategory category, @Nonnull final Property property) {
			return false;
		}
	};

	@Optional.Method(modid = "presets")
	@SubscribeEvent
	public static void presetSave(@Nonnull final PresetEvent.Save event) {
		final PresetData data = event.getModData(DSurround.MOD_ID);
		final ConfigurationHelper helper = new ConfigurationHelper(data);
		helper.save(DSurround.config(), FILTER);
	}

	@Optional.Method(modid = "presets")
	@SubscribeEvent
	public static void presetLoad(@Nonnull final PresetEvent.Load event) {
		final PresetData data = event.getModData(DSurround.MOD_ID);
		if (data != null) {
			final ConfigurationHelper helper = new ConfigurationHelper(data);
			helper.load(DSurround.config(), FILTER);
			DSurround.config().save();
			ConfigProcessor.process(DSurround.config(), ModOptions.class);
			RegistryManager.reloadResources(null);
		}
	}
}
