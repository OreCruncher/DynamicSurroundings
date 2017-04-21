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

package org.blockartistry.mod.DynSurround.client.gui;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.util.Color;
import org.blockartistry.mod.DynSurround.util.Localization;
import org.blockartistry.mod.DynSurround.util.gui.Panel;
import org.blockartistry.mod.DynSurround.util.gui.Panel.Reference;

import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiSlider.FormatHelper;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VolumeControlGui extends GuiScreen implements GuiResponder {

	private static final FormatHelper FORMAT = new FormatHelper() {
		@Override
		public String getText(int id, String name, float value) {
			return Localization.format("dlg.format.Display", name, (int) (value * 100));
		}
	};
	
	public static final int SLIDER_WIDTH = 200;
	
	private static final int ID_MASTER_SOUND = 1;
	private static final int ID_BIOME_SOUND = 2;
	private static final int ID_FOOTSTEP_SOUND = 3;
	private static final int ID_RAIN_SOUND = 4;
	private static final int ID_LABEL = 10;

	protected final Configuration config = DSurround.config();
	protected final Minecraft mc = Minecraft.getMinecraft();

	protected float master = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
	protected float biome = ModOptions.masterSoundScaleFactor;
	protected float footstep = ModOptions.footstepsSoundFactor;
	protected float rain = ModOptions.soundLevel;

	protected Panel panel = new Panel();
	
	private void addSlider(final GuiSlider slider) {
		slider.setWidth(SLIDER_WIDTH);
		addButton(slider);
	}
	
	@Override
	public void initGui() {
		final int drawX = (this.width + 1) / 2 - SLIDER_WIDTH / 2;
		final int drawY = 40;
		
		addSlider(new GuiSlider(this, ID_MASTER_SOUND, drawX, drawY, "dlg.name.MasterSound", 0F, 1F, this.master, FORMAT));
		addSlider(new GuiSlider(this, ID_RAIN_SOUND, drawX, drawY + 25, "dlg.name.RainSound", 0F, 1F, this.rain, FORMAT));
		addSlider(new GuiSlider(this, ID_BIOME_SOUND, drawX, drawY + 50, "dlg.name.BiomeSound", 0F, 1F, this.biome, FORMAT));
		addSlider(new GuiSlider(this, ID_FOOTSTEP_SOUND, drawX, drawY + 75, "dlg.name.FootstepSound", 0F, 1F, this.footstep, FORMAT));

		final GuiLabel label = new GuiLabel(mc.fontRendererObj, ID_LABEL, drawX, drawY + 100, SLIDER_WIDTH, 10, Color.MC_WHITE.rgb());
		label.setCentered().addLine(Localization.format("dlg.name.Close"));
		this.labelList.add(label);
		
		this.panel.setMinimumWidth(SLIDER_WIDTH + mc.fontRendererObj.FONT_HEIGHT * 2);
		this.panel.setMinimumHeight(5 * 25);
	}
	
	@Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
		final int drawX = (this.width + 1) / 2;
		final int drawY = 30;
		this.panel.render(drawX, drawY, Reference.TOP_CENTER);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void setEntryValue(final int id, final boolean value) {
	}

	@Override
	public void setEntryValue(final int id, final float value) {
		switch (id) {
		case ID_MASTER_SOUND:
			this.master = value;
			break;
		case ID_BIOME_SOUND:
			this.biome = value;
			break;
		case ID_FOOTSTEP_SOUND:
			this.footstep = value;
			break;
		case ID_RAIN_SOUND:
			this.rain = value;
			break;
		}
	}

	@Override
	public void setEntryValue(final int id, final String value) {

	}

	@Override
	public void onGuiClosed() {
		this.mc.gameSettings.setSoundLevel(SoundCategory.MASTER, this.master);
		this.mc.gameSettings.saveOptions();

		ModOptions.masterSoundScaleFactor = this.biome;
		ModOptions.footstepsSoundFactor = this.footstep;
		ModOptions.soundLevel = this.rain;
		this.config.save();
	}

}
