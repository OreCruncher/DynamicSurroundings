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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.registry.SoundMetadata;
import org.blockartistry.lib.sound.SoundConfigProcessor;
import org.blockartistry.lib.streams.StreamUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = DSurround.MOD_ID)
public class SoundLoader {

	private final static Map<ResourceLocation, SoundMetadata> soundMetadata = new HashMap<>();
	private final static Map<ResourceLocation, SoundEvent> myRegistry = new HashMap<>();
	private static SoundEvent SILENCE;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerSounds(@Nonnull final RegistryEvent.Register<SoundEvent> event) {
		DSurround.log().info("Registering sounds");

		final ResourceLocation soundFile = new ResourceLocation(DSurround.MOD_ID, "sounds.json");
		final IForgeRegistry<SoundEvent> registry = event.getRegistry();
		try (final SoundConfigProcessor proc = new SoundConfigProcessor(soundFile)) {
			proc.forEach((sound, meta) -> {
				final SoundMetadata data = new SoundMetadata(meta);
				final ResourceLocation resource = new ResourceLocation(DSurround.RESOURCE_ID, sound);
				final SoundEvent se = new SoundEvent(resource).setRegistryName(resource);
				registry.register(se);
				soundMetadata.put(resource, data);
			});
		} catch (@Nonnull final Exception ex) {
			ex.printStackTrace();
		}

		// Scan the "public" registries making a private one. The entries are
		// based on what the client sees and disregards what the server wants.
		// Not entirely sure why the server has to dictate what is client data.
		StreamUtil.asStream(SoundEvent.REGISTRY.iterator()).forEach(se -> myRegistry.put(se.getRegistryName(), se));
		StreamUtil.asStream(registry.iterator()).forEach(se -> myRegistry.put(se.getRegistryName(), se));

		SILENCE = myRegistry.get(new ResourceLocation(DSurround.MOD_ID, "silence"));

		DSurround.log().info("%d sound events in registry", myRegistry.size());
	}

	@Nullable
	public static SoundMetadata getSoundMetadata(@Nonnull final ResourceLocation resource) {
		return soundMetadata.get(resource);
	}

	@Nonnull
	public static SoundEvent getSound(final ResourceLocation sound) {
		final SoundEvent evt = myRegistry.get(sound);
		if (evt == null) {
			DSurround.log().warn("Cannot find sound that should be registered [%s]", sound.toString());
			return SILENCE;
		}
		return evt;
	}

}
