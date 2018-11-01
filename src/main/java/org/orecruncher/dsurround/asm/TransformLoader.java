/*
 * This file is part of ModBase, licensed under the MIT License (MIT).
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

package org.orecruncher.dsurround.asm;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions({ "org.orecruncher.dsurround.asm."})
@IFMLLoadingPlugin.SortingIndex(10001)
@IFMLLoadingPlugin.Name("DynamicSurroundingsCore")
public class TransformLoader implements IFMLLoadingPlugin {

	public static final Logger logger = LogManager.getLogger("dsurroundcore");
	public static Configuration config = null;
	
	@Override
	public String getModContainerClass() {
		return "org.orecruncher.dsurround.asm.TransformLoader$Container";
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { Transformer.class.getName() };
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(final Map<String, Object> map) {
		// Tickle the configuration so we can get some options initialized
		final File configFile = new File((File) map.get("mcLocation"), "/config/dsurround/dsurround.cfg");
		config = new Configuration(configFile);
	}

	public static class Container extends DummyModContainer {
		
		public Container() {
			super(new ModMetadata());
			ModMetadata meta = getMetadata();
			meta.modId = "dsurroundcore";
			meta.name = "DynamicSurroundingsCore";
		}

		@Override
		public boolean registerBus(final EventBus bus, final LoadController controller) {
			bus.register(this);
			return true;
		}

//		@Subscribe
		//public void postInit(final FMLPostInitializationEvent event) {
//			TransformLoader.config = null;
		//}
	}
}
