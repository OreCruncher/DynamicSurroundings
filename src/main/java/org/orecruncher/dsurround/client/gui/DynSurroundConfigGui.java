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

package org.orecruncher.dsurround.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.sound.SoundEngine;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.sound.SoundRegistry;
import org.orecruncher.lib.ConfigProcessor;
import org.orecruncher.lib.gui.GuiConfigBase;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DynSurroundConfigGui extends GuiConfigBase {

	private final Configuration config = ModBase.config();

	private final ConfigElement soundElement;
	private final ConfigCategory soundCategory;

	public DynSurroundConfigGui(final GuiScreen parentScreen) {
		super(parentScreen, new ArrayList<IConfigElement>(), ModInfo.MOD_ID, false, false, ModInfo.MOD_NAME);
		this.titleLine2 = this.config.getConfigFile().getAbsolutePath();

		// Synthetic options for handling sound blocking and volume
		this.soundCategory = new ConfigCategory("Individual Sound Configuration")
				.setLanguageKey("dsurround.cfg.sound.SoundConfig");
		this.soundElement = new ConfigElement(this.soundCategory);
		generateSoundList(this.soundCategory);
		this.configElements.add(this.soundElement);

		// Tack on the rest of the categories for configuration
		addConfigCategory(ModOptions.general.PATH);
		addConfigCategory(ModOptions.player.PATH);
		addConfigCategory(ModOptions.huds.PATH);
		addConfigCategory(ModOptions.rain.PATH);
		addConfigCategory(ModOptions.fog.PATH);
		addConfigCategory(ModOptions.aurora.PATH);
		addConfigCategory(ModOptions.effects.PATH);
		addConfigCategory(ModOptions.biomes.PATH);
		addConfigCategory(ModOptions.sound.PATH);
		addConfigCategory(ModOptions.profiles.PATH);
		addConfigCategory(ModOptions.speechbubbles.PATH);
		addConfigCategory(ModOptions.commands.PATH);
		addConfigCategory(ModOptions.asm.PATH);
		addConfigCategory(ModOptions.logging.PATH);
	}

	private void addConfigCategory(@Nonnull final String category) {
		final ConfigCategory cat = this.config.getCategory(category);
		this.configElements.add(new ConfigElement(cat));
	}

	@Override
	protected void doFixups() {
		saveSoundList();
		this.config.save();
		ConfigProcessor.process(this.config, ModOptions.class);
	}

	protected void saveSoundList() {
		final List<String> soundVolumes = new ArrayList<>();

		for (final Entry<String, Property> entry : this.soundCategory.entrySet()) {
			final String parms = entry.getValue().getString();
			if (StringUtils.isEmpty(parms))
				continue;
			soundVolumes.add(entry.getKey() + " " + parms);
		}

		final String[] results = soundVolumes.toArray(new String[soundVolumes.size()]);
		this.config.getCategory(ModOptions.sound.PATH).get(ModOptions.CONFIG_SOUND_SETTINGS).set(results);
	}

	protected void generateSoundList(final ConfigCategory cat) {
		cat.setRequiresMcRestart(false);
		cat.setRequiresWorldRestart(false);

		final List<String> sounds = new ArrayList<>();
		for (final Object resource : SoundEngine.instance().getSoundRegistry().getKeys())
			sounds.add(resource.toString());
		Collections.sort(sounds);

		final SoundRegistry registry = RegistryManager.SOUND;
		for (final String sound : sounds) {
			final Property prop = new Property(sound, "", Property.Type.STRING);
			prop.setDefaultValue("");
			prop.setRequiresMcRestart(false);
			prop.setRequiresWorldRestart(false);
			prop.setConfigEntryClass(SoundConfigEntry.class);
			final StringBuilder builder = new StringBuilder();
			final ResourceLocation res = new ResourceLocation(sound);
			if (registry.isSoundBlocked(res))
				builder.append(GuiConstants.TOKEN_BLOCK).append(' ');
			if (registry.isSoundCulled(res))
				builder.append(GuiConstants.TOKEN_CULL).append(' ');
			final float v = registry.getVolumeScale(res);
			if (v != 1.0F)
				builder.append((int) (v * 100F));
			prop.setValue(builder.toString());
			cat.put(sound, prop);
		}
	}

}
