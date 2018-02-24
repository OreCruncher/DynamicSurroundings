/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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
package org.blockartistry.lib.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiMessageDialog;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiConfigBase extends GuiConfig {

	public GuiConfigBase(GuiScreen parentScreen, List<IConfigElement> configElements, String modID,
			boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
		super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title);

	}

	protected void doFixups() {

	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for
	 * buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 2000) {
			boolean flag = true;
			try {
				if ((this.configID != null || this.parentScreen == null || !(this.parentScreen instanceof GuiConfig))
						&& (this.entryList.hasChangedEntry(true))) {
					final boolean requiresMcRestart = this.entryList.saveConfigElements();

					// My hook for massaging the results
					doFixups();

					if (Loader.isModLoaded(this.modID)) {
						final ConfigChangedEvent event = new OnConfigChangedEvent(this.modID, this.configID,
								this.isWorldRunning, requiresMcRestart);
						MinecraftForge.EVENT_BUS.post(event);
						if (!event.getResult().equals(Result.DENY))
							MinecraftForge.EVENT_BUS.post(new PostConfigChangedEvent(this.modID, this.configID,
									this.isWorldRunning, requiresMcRestart));

						if (requiresMcRestart) {
							flag = false;
							this.mc.displayGuiScreen(
									new GuiMessageDialog(this.parentScreen, "fml.configgui.gameRestartTitle",
											new TextComponentString(I18n.format("fml.configgui.gameRestartRequired")),
											"fml.configgui.confirmRestartMessage"));
						}

						if (this.parentScreen instanceof GuiConfig)
							((GuiConfig) this.parentScreen).needsRefresh = true;
					}
				}
			} catch (final Throwable e) {
				FMLLog.getLogger().error("Error performing GuiConfig action:", e);
			}

			if (flag)
				this.mc.displayGuiScreen(this.parentScreen);
		} else if (button.id == 2001) {
			this.entryList.setAllToDefault(this.chkApplyGlobally.isChecked());
		} else if (button.id == 2002) {
			this.entryList.undoAllChanges(this.chkApplyGlobally.isChecked());
		}
	}

}
