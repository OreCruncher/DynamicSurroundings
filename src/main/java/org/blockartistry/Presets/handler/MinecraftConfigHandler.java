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

package org.blockartistry.Presets.handler;

import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.Presets.Presets;
import org.blockartistry.Presets.api.PresetData;
import org.blockartistry.Presets.api.events.PresetEvent;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraft.entity.player.EntityPlayer.EnumChatVisibility;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class MinecraftConfigHandler {

	private final static String MINECRAFT = "minecraft";
	private final static String SOUND_PREFIX = "soundCategory_";
	private final static String KEYBIND_PREFIX = "key_";

	@SubscribeEvent
	public static void onConfigSave(@Nonnull final PresetEvent.Save event) {
		final PresetData data = event.getModData(MINECRAFT);
		final GameSettings settings = Minecraft.getMinecraft().gameSettings;

		for (final Options option : GameSettings.Options.values()) {
			final String theName = option.getEnumString();
			switch (option) {
			case INVERT_MOUSE:
				data.setBoolean(theName, settings.invertMouse);
				break;
			case SENSITIVITY:
				data.setDouble(theName, settings.mouseSensitivity);
				break;
			case FOV:
				data.setDouble(theName, settings.fovSetting);
				break;
			case GAMMA:
				data.setDouble(theName, settings.gammaSetting);
				break;
			case SATURATION:
				data.setDouble(theName, settings.saturation);
				break;
			case RENDER_DISTANCE:
				data.setInt(theName, settings.renderDistanceChunks);
				break;
			case VIEW_BOBBING:
				data.setBoolean(theName, settings.viewBobbing);
				break;
			case ANAGLYPH:
				data.setBoolean(theName, settings.anaglyph);
				break;
			case FRAMERATE_LIMIT:
				data.setInt(theName, settings.limitFramerate);
				break;
			case FBO_ENABLE:
				data.setBoolean(theName, settings.fboEnable);
				break;
			case CHAT_COLOR:
				data.setBoolean(theName, settings.chatColours);
				break;
			case CHAT_LINKS:
				data.setBoolean(theName, settings.chatLinks);
				break;
			case CHAT_OPACITY:
				data.setDouble(theName, settings.chatOpacity);
				break;
			case CHAT_LINKS_PROMPT:
				data.setBoolean(theName, settings.chatLinksPrompt);
				break;
			case SNOOPER_ENABLED:
				data.setBoolean(theName, settings.snooperEnabled);
				break;
			case USE_FULLSCREEN:
				data.setBoolean(theName, settings.fullScreen);
				break;
			case ENABLE_VSYNC:
				data.setBoolean(theName, settings.enableVsync);
				break;
			case USE_VBO:
				data.setBoolean(theName, settings.useVbo);
				break;
			case TOUCHSCREEN:
				data.setBoolean(theName, settings.touchscreen);
				break;
			case CHAT_SCALE:
				data.setDouble(theName, settings.chatScale);
				break;
			case CHAT_WIDTH:
				data.setDouble(theName, settings.chatWidth);
				break;
			case CHAT_HEIGHT_FOCUSED:
				data.setDouble(theName, settings.chatHeightFocused);
				break;
			case CHAT_HEIGHT_UNFOCUSED:
				data.setDouble(theName, settings.chatHeightUnfocused);
				break;
			case MIPMAP_LEVELS:
				data.setInt(theName, settings.mipmapLevels);
				break;
			case FORCE_UNICODE_FONT:
				data.setBoolean(theName, settings.forceUnicodeFont);
				break;
			case REDUCED_DEBUG_INFO:
				data.setBoolean(theName, settings.reducedDebugInfo);
				break;
			case ENTITY_SHADOWS:
				data.setBoolean(theName, settings.entityShadows);
				break;
			case ENABLE_WEAK_ATTACKS:
				data.setBoolean(theName, settings.enableWeakAttacks);
				break;
			case SHOW_SUBTITLES:
				data.setBoolean(theName, settings.showSubtitles);
				break;
			case REALMS_NOTIFICATIONS:
				data.setBoolean(theName, settings.realmsNotifications);
				break;
			case AUTO_JUMP:
				data.setBoolean(theName, settings.autoJump);
				break;
			case RENDER_CLOUDS:
				data.setInt(theName, settings.clouds);
				break;
			case GRAPHICS:
				data.setBoolean(theName, settings.fancyGraphics);
				break;
			case AMBIENT_OCCLUSION:
				data.setInt(theName, settings.ambientOcclusion);
				break;
			case GUI_SCALE:
				data.setInt(theName, settings.guiScale);
				break;
			case PARTICLES:
				data.setInt(theName, settings.particleSetting);
				break;
			case CHAT_VISIBILITY:
				data.setInt(theName, settings.chatVisibility.ordinal());
				break;
			case MAIN_HAND:
				data.setString(theName, settings.mainHand.name());
				break;
			case ATTACK_INDICATOR:
				data.setInt(theName, settings.attackIndicator);
				break;
			default:
			}
		}

		// Not to forget sounds...
		for (final SoundCategory cat : SoundCategory.values())
			data.setDouble(SOUND_PREFIX + cat.getName(), settings.getSoundLevel(cat));

		// ...and keybindings
		for (final KeyBinding keybinding : settings.keyBindings) {
			final String id = KEYBIND_PREFIX + keybinding.getKeyDescription();
			String val = Integer.toString(keybinding.getKeyCode());
			if (keybinding.getKeyModifier() != KeyModifier.NONE)
				val += ":" + keybinding.getKeyModifier();
			data.setString(id, val);
		}

	}

	@SubscribeEvent
	public static void onConfigLoad(@Nonnull final PresetEvent.Load event) {
		final PresetData data = event.getModData(MINECRAFT);
		if (data != null) {
			final GameSettings settings = Minecraft.getMinecraft().gameSettings;
			for (final Entry<String, String> e : data.getEntries()) {
				if (e.getKey().startsWith(SOUND_PREFIX)) {
					
					final String catName = e.getKey().replace(SOUND_PREFIX, "");
					final SoundCategory sc = SoundCategory.getByName(catName);
					if (sc != null) {
						settings.setSoundLevel(sc, (float) Double.parseDouble(e.getValue()));
					} else {
						Presets.log().warn("Unknown sound category: %s", catName);
					}
					
				} else if (e.getKey().startsWith(KEYBIND_PREFIX)) {

					boolean found = false;
					final String keyName = e.getKey().replace(KEYBIND_PREFIX, "");
					for (final KeyBinding binding : settings.keyBindings) {
						if (keyName.equals(binding.getKeyDescription())) {
							final String[] parts = StringUtils.split(e.getValue(), ':');
							final int keyCode = Integer.parseInt(parts[0]);
							final KeyModifier modifier = parts.length == 1 ? KeyModifier.NONE
									: KeyModifier.valueFromString(parts[1]);
							binding.setKeyModifierAndCode(modifier, keyCode);
							found = true;
							break;
						}
					}
					
					if(!found)
						Presets.log().warn("Unknown keybinding found: %s", keyName);

				} else {
					final String theName = e.getKey();
					final Options option = getOptionByName(theName);
					if (option != null) {
						switch (option) {
						case INVERT_MOUSE:
							settings.invertMouse = data.getBoolean(theName, settings.invertMouse);
							break;
						case SENSITIVITY:
							settings.mouseSensitivity = (float) data.getDouble(theName, settings.mouseSensitivity);
							break;
						case FOV:
							settings.fovSetting = (float) data.getDouble(theName, settings.fovSetting);
							break;
						case GAMMA:
							settings.gammaSetting = (float) data.getDouble(theName, settings.gammaSetting);
							break;
						case SATURATION:
							settings.saturation = (float) data.getDouble(theName, settings.saturation);
							break;
						case RENDER_DISTANCE:
							settings.renderDistanceChunks = data.getInt(theName, settings.renderDistanceChunks);
							break;
						case VIEW_BOBBING:
							settings.viewBobbing = data.getBoolean(theName, settings.viewBobbing);
							break;
						case ANAGLYPH:
							settings.anaglyph = data.getBoolean(theName, settings.anaglyph);
							break;
						case FRAMERATE_LIMIT:
							settings.limitFramerate = data.getInt(theName, settings.limitFramerate);
							break;
						case FBO_ENABLE:
							settings.fboEnable = data.getBoolean(theName, settings.fboEnable);
							break;
						case CHAT_COLOR:
							settings.chatColours = data.getBoolean(theName, settings.chatColours);
							break;
						case CHAT_LINKS:
							settings.chatLinks = data.getBoolean(theName, settings.chatLinks);
							break;
						case CHAT_OPACITY:
							settings.chatOpacity = (float) data.getDouble(theName, settings.chatOpacity);
							break;
						case CHAT_LINKS_PROMPT:
							settings.chatLinksPrompt = data.getBoolean(theName, settings.chatLinksPrompt);
							break;
						case SNOOPER_ENABLED:
							settings.snooperEnabled = data.getBoolean(theName, settings.snooperEnabled);
							break;
						case USE_FULLSCREEN:
							settings.fullScreen = data.getBoolean(theName, settings.fullScreen);
							break;
						case ENABLE_VSYNC:
							settings.enableVsync = data.getBoolean(theName, settings.enableVsync);
							break;
						case USE_VBO:
							settings.useVbo = data.getBoolean(theName, settings.useVbo);
							break;
						case TOUCHSCREEN:
							settings.touchscreen = data.getBoolean(theName, settings.touchscreen);
							break;
						case CHAT_SCALE:
							settings.chatScale = (float) data.getDouble(theName, settings.chatScale);
							break;
						case CHAT_WIDTH:
							settings.chatWidth = (float) data.getDouble(theName, settings.chatWidth);
							break;
						case CHAT_HEIGHT_FOCUSED:
							settings.chatHeightFocused = (float) data.getDouble(theName, settings.chatHeightFocused);
							break;
						case CHAT_HEIGHT_UNFOCUSED:
							settings.chatHeightUnfocused = (float) data.getDouble(theName,
									settings.chatHeightUnfocused);
							break;
						case MIPMAP_LEVELS:
							settings.mipmapLevels = data.getInt(theName, settings.mipmapLevels);
							break;
						case FORCE_UNICODE_FONT:
							settings.forceUnicodeFont = data.getBoolean(theName, settings.forceUnicodeFont);
							break;
						case REDUCED_DEBUG_INFO:
							settings.reducedDebugInfo = data.getBoolean(theName, settings.reducedDebugInfo);
							break;
						case ENTITY_SHADOWS:
							settings.entityShadows = data.getBoolean(theName, settings.entityShadows);
							break;
						case ENABLE_WEAK_ATTACKS:
							settings.enableWeakAttacks = data.getBoolean(theName, settings.enableWeakAttacks);
							break;
						case SHOW_SUBTITLES:
							settings.showSubtitles = data.getBoolean(theName, settings.showSubtitles);
							break;
						case REALMS_NOTIFICATIONS:
							settings.realmsNotifications = data.getBoolean(theName, settings.realmsNotifications);
							break;
						case AUTO_JUMP:
							settings.autoJump = data.getBoolean(theName, settings.autoJump);
							break;
						case RENDER_CLOUDS:
							settings.clouds = data.getInt(theName, settings.clouds);
							break;
						case GRAPHICS:
							settings.fancyGraphics = data.getBoolean(theName, settings.fancyGraphics);
							break;
						case AMBIENT_OCCLUSION:
							settings.ambientOcclusion = data.getInt(theName, settings.ambientOcclusion);
							break;
						case GUI_SCALE:
							settings.guiScale = data.getInt(theName, settings.guiScale);
							break;
						case PARTICLES:
							settings.particleSetting = data.getInt(theName, settings.particleSetting);
							break;
						case CHAT_VISIBILITY:
							settings.chatVisibility = EnumChatVisibility
									.getEnumChatVisibility(data.getInt(theName, settings.chatVisibility.ordinal()));
							break;
						case MAIN_HAND:
							settings.mainHand = EnumHandSide.valueOf(data.getString(theName, settings.mainHand.name()));
							break;
						case ATTACK_INDICATOR:
							settings.attackIndicator = data.getInt(theName, settings.attackIndicator);
							break;
						default:
						}
					} else {
						Presets.log().warn("Unknown option value: %s", theName);
					}

				}
			}
			settings.saveOptions();
			
			// Tickle the various modules of Minecraft to get the update settings
			// since we bypassed the get/set of GameSettings.
			final Minecraft mc = Minecraft.getMinecraft();
            mc.ingameGUI.getChatGUI().refreshChat();
            mc.getTextureMapBlocks().setMipmapLevels(settings.mipmapLevels);
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            mc.getTextureMapBlocks().setBlurMipmapDirect(false, settings.mipmapLevels > 0);
            mc.renderGlobal.setDisplayListEntitiesDirty();
            mc.fontRendererObj.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || settings.forceUnicodeFont);
            mc.refreshResources();
            Display.setVSyncEnabled(settings.enableVsync);
            
            if(settings.fullScreen != mc.isFullScreen())
            	mc.toggleFullscreen();

		}

	}

	@Nullable
	private static Options getOptionByName(@Nonnull final String name) {
		for (final Options o : Options.values())
			if (name.equals(o.getEnumString()))
				return o;
		return null;
	}
}
