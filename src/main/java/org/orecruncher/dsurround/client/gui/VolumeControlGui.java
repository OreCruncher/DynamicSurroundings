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

package org.orecruncher.dsurround.client.gui;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.lib.Color;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.gui.StandardPanel;
import org.orecruncher.lib.gui.Panel.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiSlider.FormatHelper;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VolumeControlGui extends GuiScreen implements GuiResponder {

	private static final FormatHelper FORMAT = (id, name, value) -> Localization.format("dsurround.dlg.format.Display",
			name, (int) (value * 100));

	public static final int SLIDER_WIDTH = 200;

	private static final int ID_MASTER_SOUND = 1;
	private static final int ID_BIOME_SOUND = 2;
	private static final int ID_FOOTSTEP_SOUND = 3;
	private static final int ID_LABEL = 10;

	protected final Configuration config = ModBase.config();
	protected final Minecraft mc = Minecraft.getMinecraft();

	protected float master = this.mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
	protected float biome = ModOptions.sound.masterSoundScaleFactor;
	protected float footstep = ModOptions.sound.footstepsSoundFactor;

	protected StandardPanel panel = new StandardPanel();

	private void addSlider(final GuiSlider slider) {
		slider.setWidth(SLIDER_WIDTH);
		addButton(slider);
	}

	@Override
	public void initGui() {
		final int drawX = (this.width + 1) / 2 - SLIDER_WIDTH / 2;
		final int drawY = 40;

		addSlider(new GuiSlider(this, ID_MASTER_SOUND, drawX, drawY, "dsurround.dlg.name.MasterSound", 0F, 1F,
				this.master, FORMAT));
		addSlider(new GuiSlider(this, ID_BIOME_SOUND, drawX, drawY + 25, "dsurround.dlg.name.BiomeSound", 0F, 1F,
				this.biome, FORMAT));
		addSlider(new GuiSlider(this, ID_FOOTSTEP_SOUND, drawX, drawY + 50, "dsurround.dlg.name.FootstepSound", 0F, 1F,
				this.footstep, FORMAT));

		final GuiLabel label = new GuiLabel(this.mc.fontRenderer, ID_LABEL, drawX, drawY + 75, SLIDER_WIDTH, 10,
				Color.MC_WHITE.rgb());
		label.setCentered().addLine(Localization.format("dsurround.dlg.name.Close"));
		this.labelList.add(label);

		this.panel.setMinimumWidth(SLIDER_WIDTH + this.mc.fontRenderer.FONT_HEIGHT * 2);
		this.panel.setMinimumHeight(4 * 25);
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		drawDefaultBackground();
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
		}
	}

	@Override
	public void setEntryValue(final int id, final String value) {

	}

	@Override
	public void onGuiClosed() {
		this.mc.gameSettings.setSoundLevel(SoundCategory.MASTER, this.master);
		this.mc.gameSettings.saveOptions();

		ModOptions.sound.masterSoundScaleFactor = this.biome;
		ModOptions.sound.footstepsSoundFactor = this.footstep;

		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_MASTER_SOUND_FACTOR)
				.set(ModOptions.sound.masterSoundScaleFactor);
		this.config.getCategory(ModOptions.CATEGORY_SOUND).get(ModOptions.CONFIG_FOOTSTEPS_SOUND_FACTOR)
				.set(ModOptions.sound.footstepsSoundFactor);
		this.config.save();
	}

}
