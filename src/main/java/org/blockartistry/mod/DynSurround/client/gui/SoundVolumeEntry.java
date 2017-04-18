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

import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.IConfigElement;

@SideOnly(Side.CLIENT)
public class SoundVolumeEntry extends NumberSliderEntry {

	private static final int ID_PLAY = 10000;
	
	private final PlaySoundButton play;

	public SoundVolumeEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
		super(owningScreen, owningEntryList, configElement);

		this.play = new PlaySoundButton(ID_PLAY, configElement.getName());
		this.btnValue.width = this.owningEntryList.controlWidth - 82;
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
			boolean isSelected) {
		this.owningEntryList.controlWidth -= 36;
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
		this.owningEntryList.controlWidth += 36;

		this.play.xPosition = this.owningEntryList.scrollBarX - 82;
		this.play.yPosition = y;
		this.play.enabled = enabled();
		this.play.drawButton(this.mc, mouseX, mouseY);
	}

	@Override
	public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		if (this.play.mousePressed(this.mc, x, y)) {
			final float volume = ((Integer)this.getCurrentValue()) / 100F;
			this.play.playSound(this.mc, volume);
			return true;
		} else
			return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
	}

}
