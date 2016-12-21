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

package org.blockartistry.mod.DynSurround.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.SoundRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.mod.DynSurround.util.ConfigProcessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class DynSurroundConfigGui extends GuiConfig {

	private final Configuration config = Module.config();

	private final ConfigElement soundElement;
	private final ConfigCategory soundCategory;

	private final ConfigElement soundVolumeElement;
	private final ConfigCategory soundVolumeCategory;
	
	public DynSurroundConfigGui(final GuiScreen parentScreen) {
		super(parentScreen, new ArrayList<IConfigElement>(), Module.MOD_ID, false, false, Module.MOD_NAME);
		this.titleLine2 = this.config.getConfigFile().getAbsolutePath();

		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_AURORA, ModOptions.CONFIG_AURORA_ENABLED,
				"Aurora Feature"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ALLOW_DESERT_FOG,
				"Desert Dust Feature"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_FOG,
				ModOptions.CONFIG_ENABLE_ELEVATION_HAZE, "Elevation Haze Feature"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ENABLE_BIOME_FOG,
				"Biomes Fog Feature"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND,
				ModOptions.CONFIG_ENABLE_BIOME_SOUNDS, "Biomes Sound Feature"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_JUMP_SOUND,
				"Player Jump Sound Effect"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND,
				ModOptions.CONFIG_ENABLE_SWING_SOUND, "Player Weapon Swing Sound Effect"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND,
				ModOptions.CONFIG_ENABLE_CRAFTING_SOUND, "Player Crafting Sound Effect"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND,
				ModOptions.CONFIG_ENABLE_BOW_PULL_SOUND, "Player Bow Pull Sound Effect"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SOUND,
				ModOptions.CONFIG_ENABLE_FOOTSTEPS_SOUND, "Footstep Sound Effects"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_POTION_HUD,
				ModOptions.CONFIG_POTION_HUD_ENABLE, "Potion HUD Overlay"));
		this.configElements.add(getPropertyConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES,
				ModOptions.CONFIG_OPTION_ENABLE_SPEECHBUBBLES, "Speech Bubbles"));

		this.soundCategory = new ConfigCategory("Blocked Sounds");
		this.soundCategory.setComment("Sounds that will be blocked from playing");
		this.soundElement = new MyConfigElement(this.soundCategory);
		generateSoundList(this.soundCategory);
		this.configElements.add(this.soundElement);

		this.soundVolumeCategory = new ConfigCategory("Sound Volumes");
		this.soundVolumeCategory.setComment("Individual sound volume control");
		this.soundVolumeElement = new MyConfigElement(this.soundVolumeCategory);
		generateSoundVolumeList(this.soundVolumeCategory);
		this.configElements.add(this.soundVolumeElement);

		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_GENERAL, "General Settings"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES, "Speech Bubbles"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_RAIN, "Rain Settings"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_FOG, "Fog Settings"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_AURORA, "Aurora Settings"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_BLOCK, "Block Settings"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_BIOMES, "Biomes Behaviors"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_SOUND, "Sound Effects"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_PLAYER, "Player Effects"));
		this.configElements.add(getCategoryConfigElement(ModOptions.CATEGORY_LOGGING_CONTROL, "Logging Options"));
	}

	private ConfigElement getCategoryConfigElement(final String category, final String label) {
		final ConfigCategory cat = this.config.getCategory(category);
		return new MyConfigElement(cat, label);
	}

	private ConfigElement getPropertyConfigElement(final String category, final String property, final String label) {
		final Property prop = this.config.getCategory(category).get(property);
		return new MyConfigElement(prop, label);
	}

	@Override
	protected void actionPerformed(final GuiButton button) {

		super.actionPerformed(button);

		// Done button was pressed
		if (button.id == 2000) {
			saveSoundList();
			saveSoundVolumeList();
			this.config.save();
			ConfigProcessor.process(this.config, ModOptions.class);
			RegistryManager.reloadResources();
		}
	}

	protected void saveSoundList() {
		final List<String> sounds = new ArrayList<String>();
		for (final Entry<String, Property> entry : this.soundCategory.entrySet()) {
			if (entry.getValue().getBoolean())
				sounds.add(entry.getKey());
		}

		final String[] results = sounds.toArray(new String[sounds.size()]);
		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_BLOCKED_SOUNDS).set(results);
	}

	protected void saveSoundVolumeList() {
		final List<String> sounds = new ArrayList<String>();
		for (final Entry<String, Property> entry : this.soundVolumeCategory.entrySet()) {
			final int value = entry.getValue().getInt();
			if (value != 100)
				sounds.add(entry.getKey() + "=" + value);
		}

		final String[] results = sounds.toArray(new String[sounds.size()]);
		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_SOUND_VOLUMES).set(results);
	}

	protected void generateSoundList(final ConfigCategory cat) {
		cat.setRequiresMcRestart(false);
		cat.setRequiresWorldRestart(false);

		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		final List<String> sounds = new ArrayList<String>();
		for (final Object resource : handler.soundRegistry.getKeys())
			sounds.add(resource.toString());
		Collections.sort(sounds);

		final SoundRegistry registry = RegistryManager.get(RegistryType.SOUND);
		for (final String sound : sounds) {
			final Property prop = new Property(sound, "false", Property.Type.BOOLEAN);
			prop.setDefaultValue(false);
			prop.setRequiresMcRestart(false);
			prop.setRequiresWorldRestart(false);
			prop.set(registry.isSoundBlocked(sound));
			cat.put(sound, prop);
		}
	}

	protected void generateSoundVolumeList(final ConfigCategory cat) {
		cat.setRequiresMcRestart(false);
		cat.setRequiresWorldRestart(false);
		
		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		final List<String> sounds = new ArrayList<String>();
		for (final Object resource : handler.soundRegistry.getKeys())
			sounds.add(resource.toString());
		Collections.sort(sounds);

		final SoundRegistry registry = RegistryManager.get(RegistryType.SOUND);
		for (final String sound : sounds) {
			final Property prop = new Property(sound, "100", Property.Type.INTEGER);
			prop.setMinValue(0);
			prop.setMaxValue(200);
			prop.setDefaultValue(100);
			prop.setRequiresMcRestart(false);
			prop.setRequiresWorldRestart(false);
			prop.set(MathHelper.floor_float(registry.getVolumeScale(sound) * 100));
			prop.setConfigEntryClass(NumberSliderEntry.class);
			cat.put(sound, prop);
		}
	}
}
