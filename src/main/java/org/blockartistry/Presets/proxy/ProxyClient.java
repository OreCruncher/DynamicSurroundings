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

package org.blockartistry.Presets.proxy;

import java.io.File;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.blockartistry.Presets.Presets;
import org.blockartistry.lib.Localization;
import org.blockartistry.lib.io.Streams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyClient extends Proxy {
	
	// List of preset files to copy.  Temporary until I can figure a way to
	// automagically enumerate the files in a jar
	private static final String[] presetFiles = new String[] {
		"presets_level0",
		"presets_level1",
		"presets_level2",
		"presets_level3",
		"dsurround_skyblock",
		"dsurround_emojis"
	};
	
	@Override
	protected void registerLanguage() {
		Localization.initialize(Side.CLIENT);
	}

	@Override
	public boolean isRunningAsServer() {
		return false;
	}

	@Override
	public Side effectiveSide() {
		return FMLCommonHandler.instance().getEffectiveSide();
	}

	@Override
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		super.preInit(event);
		
		// Extract the preset config files present in the JAR
		final IResourceManager manager = Minecraft.getMinecraft().getResourceManager();

		for(final String preset : presetFiles) {
			final String name = preset + ".presets";
			try {
				final IResource r = manager.getResource(new ResourceLocation(Presets.MOD_ID, "data/" + name));
				try(final InputStream stream = r.getInputStream()) {
					Streams.copy(stream, new File(Presets.dataDirectory(), name));
				}
			} catch(final Throwable t) {
				Presets.log().error("Unable to extract preset file " + name, t);
			}
		}
	}

	@Override
	public void init(@Nonnull final FMLInitializationEvent event) {
		super.init(event);
	}

}
