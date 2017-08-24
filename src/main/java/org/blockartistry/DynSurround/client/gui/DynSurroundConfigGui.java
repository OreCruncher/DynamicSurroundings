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

package org.blockartistry.DynSurround.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.Permissions;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.SoundRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.ConfigProcessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class DynSurroundConfigGui extends GuiConfig {

	private final Configuration config = DSurround.config();

	private final ConfigElement soundElement;
	private final ConfigCategory soundCategory;

	public DynSurroundConfigGui(final GuiScreen parentScreen) {
		super(parentScreen, new ArrayList<IConfigElement>(), DSurround.MOD_ID, false, false, DSurround.MOD_NAME);
		this.titleLine2 = this.config.getConfigFile().getAbsolutePath();

		// Quick select options. Allow the player to enable/disable features
		// easily without having to delve into the option tree.
		addConfigElement(ModOptions.CATEGORY_AURORA, ModOptions.CONFIG_AURORA_ENABLED);
		addConfigElement(ModOptions.CATEGORY_RAIN, ModOptions.CONFIG_ENABLE_BACKGROUND_THUNDER);
		addConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ALLOW_DESERT_FOG);
		addConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ENABLE_ELEVATION_HAZE);
		addConfigElement(ModOptions.CATEGORY_FOG, ModOptions.CONFIG_ENABLE_BIOME_FOG);
		addConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_BIOME_SOUNDS);
		addConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_JUMP_SOUND);
		addConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_EQUIP_SOUND);
		addConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_CRAFTING_SOUND);
		addConfigElement(ModOptions.CATEGORY_SOUND, ModOptions.CONFIG_ENABLE_ARMOR_SOUND);
		addConfigElement(ModOptions.CATEGORY_PLAYER, ModOptions.CONFIG_ENABLE_FOOTPRINTS);
		addConfigElement(ModOptions.CATEGORY_POTION_HUD, ModOptions.CONFIG_POTION_HUD_ENABLE);
		addConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES, ModOptions.CONFIG_OPTION_ENABLE_SPEECHBUBBLES);
		addConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES, ModOptions.CONFIG_OPTION_ENABLE_EMOJIS);
		addConfigElement(ModOptions.CATEGORY_SPEECHBUBBLES, ModOptions.CONFIG_OPTION_ENABLE_ENTITY_CHAT);
		addConfigElement(ModOptions.CATEGORY_EXPLOSIONS, ModOptions.CONFIG_ENABLE_EXPLOSIONS);

		if (Permissions.instance().allowCompassAndClockHUD()) {
			addConfigElement(ModOptions.CATEGORY_COMPASS, ModOptions.CONFIG_COMPASS_ENABLE);
			addConfigElement(ModOptions.CATEGORY_COMPASS, ModOptions.CONFIG_CLOCK_ENABLE);
		}

		// Synthetic options for handling sound blocking and volume
		this.soundCategory = new ConfigCategory("Individual Sound Configuration")
				.setLanguageKey("cfg.sound.SoundConfig");
		this.soundElement = new ConfigElement(this.soundCategory);
		generateSoundList(this.soundCategory);
		this.configElements.add(this.soundElement);

		// Tack on the rest of the categories for configuration
		addConfigCategory(ModOptions.CATEGORY_GENERAL);
		addConfigCategory(ModOptions.CATEGORY_PLAYER);
		if (Permissions.instance().allowCompassAndClockHUD())
			addConfigCategory(ModOptions.CATEGORY_COMPASS);
		addConfigCategory(ModOptions.CATEGORY_EXPLOSIONS);
		addConfigCategory(ModOptions.CATEGORY_RAIN);
		addConfigCategory(ModOptions.CATEGORY_FOG);
		addConfigCategory(ModOptions.CATEGORY_AURORA);
		addConfigCategory(ModOptions.CATEGORY_BLOCK);
		addConfigCategory(ModOptions.CATEGORY_BIOMES);
		addConfigCategory(ModOptions.CATEGORY_SOUND);
		addConfigCategory(ModOptions.CATEGORY_PROFILES);
		if (Permissions.instance().allowLightLevelHUD())
			addConfigCategory(ModOptions.CATEGORY_LIGHT_LEVEL);
		addConfigCategory(ModOptions.CATEGORY_SPEECHBUBBLES);
		addConfigCategory(ModOptions.CATEGORY_COMMANDS);
		addConfigCategory(ModOptions.CATEGORY_ASM);
		addConfigCategory(ModOptions.CATEGORY_LOGGING_CONTROL);
	}

	private void addConfigElement(@Nonnull final String category, @Nonnull final String prop) {
		final Property property = this.config.getCategory(category).get(prop);
		this.configElements.add(new ConfigElement(property));
	}

	private void addConfigCategory(@Nonnull final String category) {
		final ConfigCategory cat = this.config.getCategory(category);
		this.configElements.add(new ConfigElement(cat));
	}

	@Override
	protected void actionPerformed(final GuiButton button) {

		super.actionPerformed(button);

		// Done button was pressed
		if (button.id == 2000) {
			saveSoundList();
			this.config.save();
			ConfigProcessor.process(this.config, ModOptions.class);
			RegistryManager.reloadResources();
		}
	}

	protected void saveSoundList() {
		final List<String> culledSounds = new ArrayList<String>();
		final List<String> blockedSounds = new ArrayList<String>();
		final List<String> soundVolumes = new ArrayList<String>();

		for (final Entry<String, Property> entry : this.soundCategory.entrySet()) {
			final String sound = entry.getKey();
			String parms = entry.getValue().getString();
			if (StringUtils.isEmpty(parms))
				continue;

			if (parms.contains(GuiConstants.TOKEN_CULL)) {
				parms = parms.replace(GuiConstants.TOKEN_CULL, "");
				culledSounds.add(sound);
			}

			if (parms.contains(GuiConstants.TOKEN_BLOCK)) {
				parms = parms.replace(GuiConstants.TOKEN_BLOCK, "");
				blockedSounds.add(sound);
			}

			parms = parms.trim();
			if (StringUtils.isEmpty(parms))
				continue;

			final int volume = Integer.parseInt(parms);
			if (volume != 100) {
				soundVolumes.add(sound + "=" + volume);
			}
		}

		String[] results = culledSounds.toArray(new String[culledSounds.size()]);
		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_CULLED_SOUNDS).set(results);

		results = blockedSounds.toArray(new String[blockedSounds.size()]);
		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_BLOCKED_SOUNDS).set(results);

		results = soundVolumes.toArray(new String[soundVolumes.size()]);
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
			final Property prop = new Property(sound, "", Property.Type.STRING);
			prop.setDefaultValue("");
			prop.setRequiresMcRestart(false);
			prop.setRequiresWorldRestart(false);
			prop.setConfigEntryClass(SoundConfigEntry.class);
			final StringBuilder builder = new StringBuilder();
			if (registry.isSoundBlocked(sound))
				builder.append(GuiConstants.TOKEN_BLOCK).append(' ');
			if (registry.isSoundCulled(sound))
				builder.append(GuiConstants.TOKEN_CULL).append(' ');
			final float v = registry.getVolumeScale(sound);
			if (v != 1.0F)
				builder.append((int) (v * 100F));
			prop.setValue(builder.toString());
			cat.put(sound, prop);
		}
	}

}
