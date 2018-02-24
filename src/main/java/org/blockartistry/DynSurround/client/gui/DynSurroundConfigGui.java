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
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.registry.SoundRegistry;
import org.blockartistry.lib.ConfigProcessor;
import org.blockartistry.lib.gui.GuiConfigBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DynSurroundConfigGui extends GuiConfigBase {

	private final Configuration config = DSurround.config();

	private final ConfigElement soundElement;
	private final ConfigCategory soundCategory;

	public DynSurroundConfigGui(final GuiScreen parentScreen) {
		super(parentScreen, new ArrayList<IConfigElement>(), DSurround.MOD_ID, false, false, DSurround.MOD_NAME);
		this.titleLine2 = this.config.getConfigFile().getAbsolutePath();

		// Quick select options. Allow the player to enable/disable features
		// easily without having to delve into the option tree.
		addConfigElement(ModOptions.aurora.PATH, ModOptions.CONFIG_AURORA_ENABLED);
		addConfigElement(ModOptions.rain.PATH, ModOptions.CONFIG_ENABLE_BACKGROUND_THUNDER);
		addConfigElement(ModOptions.fog.PATH, ModOptions.CONFIG_ALLOW_DESERT_FOG);
		addConfigElement(ModOptions.fog.PATH, ModOptions.CONFIG_ENABLE_ELEVATION_HAZE);
		addConfigElement(ModOptions.fog.PATH, ModOptions.CONFIG_ENABLE_BIOME_FOG);
		addConfigElement(ModOptions.sound.PATH, ModOptions.CONFIG_ENABLE_BIOME_SOUNDS);
		addConfigElement(ModOptions.sound.PATH, ModOptions.CONFIG_ENABLE_JUMP_SOUND);
		addConfigElement(ModOptions.sound.PATH, ModOptions.CONFIG_ENABLE_EQUIP_SOUND);
		addConfigElement(ModOptions.sound.PATH, ModOptions.CONFIG_ENABLE_CRAFTING_SOUND);
		addConfigElement(ModOptions.sound.PATH, ModOptions.CONFIG_ENABLE_ARMOR_SOUND);
		addConfigElement(ModOptions.player.PATH, ModOptions.CONFIG_ENABLE_FOOTPRINTS);
		addConfigElement(ModOptions.player.potionHUD.PATH, ModOptions.CONFIG_POTION_HUD_ENABLE);
		addConfigElement(ModOptions.speechbubbles.PATH, ModOptions.CONFIG_OPTION_ENABLE_SPEECHBUBBLES);
		addConfigElement(ModOptions.speechbubbles.PATH, ModOptions.CONFIG_OPTION_ENABLE_EMOJIS);
		addConfigElement(ModOptions.speechbubbles.PATH, ModOptions.CONFIG_OPTION_ENABLE_ENTITY_CHAT);
		addConfigElement(ModOptions.explosions.PATH, ModOptions.CONFIG_ENABLE_EXPLOSIONS);

		if (Permissions.instance().allowCompassAndClockHUD()) {
			addConfigElement(ModOptions.compass.PATH, ModOptions.CONFIG_COMPASS_ENABLE);
			addConfigElement(ModOptions.compass.PATH, ModOptions.CONFIG_CLOCK_ENABLE);
		}

		// Synthetic options for handling sound blocking and volume
		this.soundCategory = new ConfigCategory("Individual Sound Configuration")
				.setLanguageKey("dsurround.cfg.sound.SoundConfig");
		this.soundElement = new ConfigElement(this.soundCategory);
		generateSoundList(this.soundCategory);
		this.configElements.add(this.soundElement);

		// Tack on the rest of the categories for configuration
		addConfigCategory(ModOptions.general.PATH);
		addConfigCategory(ModOptions.player.PATH);
		if (Permissions.instance().allowCompassAndClockHUD())
			addConfigCategory(ModOptions.compass.PATH);
		addConfigCategory(ModOptions.explosions.PATH);
		addConfigCategory(ModOptions.rain.PATH);
		addConfigCategory(ModOptions.fog.PATH);
		addConfigCategory(ModOptions.aurora.PATH);
		addConfigCategory(ModOptions.block.PATH);
		addConfigCategory(ModOptions.biomes.PATH);
		addConfigCategory(ModOptions.sound.PATH);
		addConfigCategory(ModOptions.lighting.PATH);
		addConfigCategory(ModOptions.profiles.PATH);
		if (Permissions.instance().allowLightLevelHUD())
			addConfigCategory(ModOptions.lightlevel.PATH);
		addConfigCategory(ModOptions.speechbubbles.PATH);
		addConfigCategory(ModOptions.commands.PATH);
		addConfigCategory(ModOptions.asm.PATH);
		addConfigCategory(ModOptions.logging.PATH);
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
	protected void doFixups() {
		saveSoundList();
		this.config.save();
		ConfigProcessor.process(this.config, ModOptions.class);
	}

	protected void saveSoundList() {
		final List<String> culledSounds = new ArrayList<>();
		final List<String> blockedSounds = new ArrayList<>();
		final List<String> soundVolumes = new ArrayList<>();

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
		this.config.getCategory(ModOptions.sound.PATH).get(ModOptions.CONFIG_CULLED_SOUNDS).set(results);

		results = blockedSounds.toArray(new String[blockedSounds.size()]);
		this.config.getCategory(ModOptions.sound.PATH).get(ModOptions.CONFIG_BLOCKED_SOUNDS).set(results);

		results = soundVolumes.toArray(new String[soundVolumes.size()]);
		this.config.getCategory(ModOptions.sound.PATH).get(ModOptions.CONFIG_SOUND_VOLUMES).set(results);
	}

	protected void generateSoundList(final ConfigCategory cat) {
		cat.setRequiresMcRestart(false);
		cat.setRequiresWorldRestart(false);

		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		final List<String> sounds = new ArrayList<>();
		for (final Object resource : handler.soundRegistry.getKeys())
			sounds.add(resource.toString());
		Collections.sort(sounds);

		final SoundRegistry registry = ClientRegistry.SOUND;
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
