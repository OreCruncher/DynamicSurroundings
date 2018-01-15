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

import javax.annotation.Nonnull;

import org.blockartistry.Presets.Presets;
import org.blockartistry.lib.Localization;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

public class Proxy {
	
	protected void registerLanguage() {
		Localization.initialize(Side.SERVER);
	}
	
	protected static void register(final Class<?> clazz) {
		Presets.log().info("Registering for Forge events: %s", clazz.getName());
		MinecraftForge.EVENT_BUS.register(clazz);
	}

	protected void eventBusRegistrations() {

	}

	public boolean isRunningAsServer() {
		return true;
	}
	
	public Side effectiveSide() {
		return Side.SERVER;
	}

	public void preInit(@Nonnull final FMLPreInitializationEvent event) {
		registerLanguage();
		eventBusRegistrations();
	}

	public void init(@Nonnull final FMLInitializationEvent event) {
	}

	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
	}
	
	public void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
	}

}
