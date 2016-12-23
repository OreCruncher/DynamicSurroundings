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

package org.blockartistry.mod.DynSurround.server.services;

import java.util.ArrayList;
import java.util.List;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.registry.DimensionRegistry;
import org.blockartistry.mod.DynSurround.registry.RegistryManager;
import org.blockartistry.mod.DynSurround.registry.RegistryManager.RegistryType;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ServiceManager extends Service {
	
	private static final ServiceManager INSTANCE = new ServiceManager();
	
	private final List<Service> services = new ArrayList<Service>();
	private final DimensionRegistry dimensions = RegistryManager.get(RegistryType.DIMENSION);
	
	private ServiceManager() {
		super("ServiceManager");
	}
	
	private void addService(final Service service) {
		this.services.add(service);
	}
	
	private void clearServices() {
		this.services.clear();
	}
	
	private void dumpServices() {
		ModLog.info("Dynamic Surrounding Services");
		for(final Service s: this.services)
			ModLog.info("* %s", s.getServiceName());
	}

	private void init0() {
		for(final Service s: this.services) {
			s.init();
			MinecraftForge.EVENT_BUS.register(s);
		}
	}
	
	private void fini0() {
		for(final Service s: this.services) {
			s.fini();
			MinecraftForge.EVENT_BUS.unregister(s);
		}
	}
	
	public static void initialize() {
		INSTANCE.addService(INSTANCE);
		INSTANCE.addService(new AtmosphereService());
		if (ModOptions.auroraEnable)
			INSTANCE.addService(new AuroraService());
		if (ModOptions.enableEntityChat)
			INSTANCE.addService(new EntityEmojiService());
		if (ModOptions.enableDamagePopoffs)
			INSTANCE.addService(new HealthEffectService());
		if (ModOptions.enableSpeechBubbles)
			INSTANCE.addService(new SpeechBubbleService());
		
		INSTANCE.dumpServices();
		INSTANCE.init0();
	}
	
	public static void deinitialize() {
		INSTANCE.fini0();
		INSTANCE.clearServices();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWorldLoad(final WorldEvent.Load e) {
		// Tickle the Dimension Registry so it has the
		// latest info.
		this.dimensions.loading(e.getWorld());
	}

}
