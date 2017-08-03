/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher, Abastro
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

package org.blockartistry.DynSurround.client.sound;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.data.xface.SoundMetadataConfig;
import org.blockartistry.DynSurround.registry.SoundMetadata;
import org.blockartistry.DynSurround.registry.SoundRegistry;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = DSurround.MOD_ID)
public final class Sounds {

	private Sounds() {

	}

	public static SoundEffect JUMP;
	public static SoundEffect CRAFTING;

	public static SoundEffect SWORD_EQUIP;
	public static SoundEffect SWORD_SWING;
	public static SoundEffect AXE_EQUIP;
	public static SoundEffect AXE_SWING;
	public static SoundEffect BOW_EQUIP;
	public static SoundEffect BOW_PULL;
	public static SoundEffect TOOL_EQUIP;
	public static SoundEffect TOOL_SWING;
	public static SoundEffect UTILITY_EQUIP;

	public static SoundEffect SHIELD_EQUIP = TOOL_EQUIP;

	public static SoundEffect LIGHT_ARMOR_EQUIP;
	public static SoundEffect MEDIUM_ARMOR_EQUIP;
	public static SoundEffect HEAVY_ARMOR_EQUIP;
	public static SoundEffect CRYSTAL_ARMOR_EQUIP;

	public static SoundEffect THUNDER;
	public static SoundEffect RAINFALL;

	public static SoundEffect WATER_DROP;
	public static SoundEffect WATER_DRIP;
	public static SoundEffect STEAM_HISS;

	public static SoundEffect FIRE;

	// Waterfalls
	public static SoundEffect WATERFALL0;
	public static SoundEffect WATERFALL1;
	public static SoundEffect WATERFALL2;
	public static SoundEffect WATERFALL3;
	public static SoundEffect WATERFALL4;
	public static SoundEffect WATERFALL5;

	// Weather stuff
	public static SoundEvent RAIN;
	public static SoundEvent DUST;

	// Quiet!
	public static SoundEvent SILENCE;

	private final static Map<ResourceLocation, SoundMetadata> soundMetadata = Maps.newHashMap();
	private final static Map<ResourceLocation, SoundEvent> myRegistry = Maps.newHashMap();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerSounds(final RegistryEvent.Register<SoundEvent> event) {
		DSurround.log().info("Registering sounds");

		final ParameterizedType TYPE = new ParameterizedType() {
			public Type[] getActualTypeArguments() {
				return new Type[] { String.class, SoundMetadataConfig.class };
			}

			public Type getRawType() {
				return Map.class;
			}

			public Type getOwnerType() {
				return null;
			}
		};

		final IForgeRegistry<SoundEvent> registry = event.getRegistry();
		try (final InputStream stream = SoundRegistry.class.getResourceAsStream("/assets/dsurround/sounds.json")) {
			if (stream != null) {
				@SuppressWarnings("unchecked")
				final Map<String, SoundMetadataConfig> sounds = (Map<String, SoundMetadataConfig>) new Gson()
						.fromJson(new InputStreamReader(stream), TYPE);
				for (final Entry<String, SoundMetadataConfig> e : sounds.entrySet()) {
					final String soundName = e.getKey();
					final SoundMetadata data = new SoundMetadata(e.getValue());
					final ResourceLocation resource = new ResourceLocation(DSurround.RESOURCE_ID, soundName);
					final SoundEvent sound = new SoundEvent(resource).setRegistryName(resource);
					registry.register(sound);
					soundMetadata.put(resource, data);
				}
			}
		} catch (final Throwable t) {
			DSurround.log().error("Unable to read the mod sound file!", t);
		}

		// Scan the "public" registries making a private one. The entries are
		// based on what the client
		// sees and disregards what the server wants. Not entirely sure why the
		// server has to dictate
		// what is client data.
		Iterator<SoundEvent> itr = SoundEvent.REGISTRY.iterator();
		while (itr.hasNext()) {
			final SoundEvent se = itr.next();
			myRegistry.put(se.getRegistryName(), se);
		}

		itr = registry.iterator();
		while (itr.hasNext()) {
			final SoundEvent se = itr.next();
			myRegistry.put(se.getRegistryName(), se);
		}

		SILENCE = Sounds.getSound(new ResourceLocation(DSurround.RESOURCE_ID, "silence"));

		// Weather

		RAIN = Sounds.getSound(new ResourceLocation(DSurround.RESOURCE_ID, "rain"));
		DUST = Sounds.getSound(new ResourceLocation(DSurround.RESOURCE_ID, "dust"));

		// SoundEffects

		JUMP = new SoundEffect.Builder("jump", SoundCategory.PLAYERS).setVariablePitch(true).build();
		CRAFTING = new SoundEffect.Builder("crafting", SoundCategory.PLAYERS).build();

		SWORD_EQUIP = new SoundEffect.Builder("sword.equip", SoundCategory.PLAYERS).setVolume(0.5F).build();
		SWORD_SWING = new SoundEffect.Builder("sword.swing", SoundCategory.PLAYERS).build();
		AXE_EQUIP = new SoundEffect.Builder("blunt.equip", SoundCategory.PLAYERS).setVolume(0.35F).build();
		AXE_SWING = new SoundEffect.Builder("blunt.swing", SoundCategory.PLAYERS).build();
		BOW_EQUIP = new SoundEffect.Builder("bow.equip", SoundCategory.PLAYERS).setVolume(0.30F).build();
		BOW_PULL = new SoundEffect.Builder("bow.pull", SoundCategory.PLAYERS).build();
		TOOL_EQUIP = new SoundEffect.Builder("tool.equip", SoundCategory.PLAYERS).setVolume(0.30F).build();
		TOOL_SWING = new SoundEffect.Builder("tool.swing", SoundCategory.PLAYERS).build();
		UTILITY_EQUIP = new SoundEffect.Builder("utility.equip", SoundCategory.PLAYERS).setVolume(0.35F).build();

		SHIELD_EQUIP = TOOL_EQUIP;

		LIGHT_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.light_walk", SoundCategory.PLAYERS).build();
		MEDIUM_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.medium_walk", SoundCategory.PLAYERS).build();
		HEAVY_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.heavy_walk", SoundCategory.PLAYERS).build();
		CRYSTAL_ARMOR_EQUIP = new SoundEffect.Builder("fs.armor.crystal_walk", SoundCategory.PLAYERS).build();

		THUNDER = new SoundEffect.Builder("thunder", SoundCategory.WEATHER).setVolume(10000F).build();
		RAINFALL = new SoundEffect.Builder("rain", SoundCategory.WEATHER).build();

		WATER_DROP = new SoundEffect.Builder("waterdrops", SoundCategory.AMBIENT).build();
		WATER_DRIP = new SoundEffect.Builder("waterdrips", SoundCategory.AMBIENT).build();
		STEAM_HISS = new SoundEffect.Builder(new ResourceLocation("block.fire.extinguish"), SoundCategory.AMBIENT)
				.setVolume(0.1F).setPitch(1.0F).build();

		FIRE = new SoundEffect.Builder(new ResourceLocation("minecraft:block.fire.ambient"), SoundCategory.BLOCKS)
				.build();

		// Waterfalls
		WATERFALL0 = new SoundEffect.Builder("waterfall.0", SoundCategory.AMBIENT).build();
		WATERFALL1 = new SoundEffect.Builder("waterfall.1", SoundCategory.AMBIENT).build();
		WATERFALL2 = new SoundEffect.Builder("waterfall.2", SoundCategory.AMBIENT).build();
		WATERFALL3 = new SoundEffect.Builder("waterfall.3", SoundCategory.AMBIENT).build();
		WATERFALL4 = new SoundEffect.Builder("waterfall.4", SoundCategory.AMBIENT).build();
		WATERFALL5 = new SoundEffect.Builder("waterfall.5", SoundCategory.AMBIENT).build();

	}

	@Nullable
	public static SoundMetadata getSoundMetadata(@Nonnull final ResourceLocation resource) {
		return soundMetadata.get(resource);
	}

	public static SoundEvent getSound(final ResourceLocation sound) {
		final SoundEvent evt = myRegistry.get(sound);
		if (evt == null) {
			DSurround.log().warn("Cannot find sound that should be registered [%s]", sound.toString());
			return SILENCE;
		}
		return evt;
	}

}
