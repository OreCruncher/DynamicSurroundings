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

package org.blockartistry.Presets.gui;

import java.util.ArrayList;
import javax.annotation.Nonnull;

import org.blockartistry.Presets.ModOptions;
import org.blockartistry.Presets.Presets;
import org.blockartistry.lib.ConfigProcessor;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class PresetsConfigGui extends GuiConfig {

	private final Configuration config = Presets.config();

	public PresetsConfigGui(final GuiScreen parentScreen) {
		
		super(parentScreen, new ArrayList<IConfigElement>(), Presets.MOD_ID, false, false, Presets.MOD_NAME);
		this.titleLine2 = this.config.getConfigFile().getAbsolutePath();

		// Tack on the rest of the categories for configuration
		addConfigCategory(ModOptions.CATEGORY_LOGGING_CONTROL);
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
			this.config.save();
			ConfigProcessor.process(this.config, ModOptions.class);
		}
	}

}
