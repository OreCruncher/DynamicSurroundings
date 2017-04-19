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

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.util.Localization;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries.IArrayEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.IConfigElement;

@SideOnly(Side.CLIENT)
public class SoundConfigEntry extends NumberSliderEntry {

	private static final int ID_CULLED = 10000;
	private static final int ID_BLOCKED = 10001;
	private static final int ID_PLAY = 10002;

	private static final String CULL = Localization.format("format.Cull");
	private static final String BLOCK = Localization.format("format.Block");
	private static final String PLAY = Localization.format("format.Play");

	private static final String TOOLTIP_FORMAT = TextFormatting.GREEN + "%s " + TextFormatting.GOLD + "%s";
	private final CheckBoxButton cull;
	private final CheckBoxButton block;
	private final PlaySoundButton play;

	private final HoverChecker cullHover;
	private final HoverChecker blockHover;
	private final HoverChecker playHover;

	private final List<String> cullHoverText;
	private final List<String> blockHoverText;
	private final List<String> playHoverText;

	private final IConfigElement realConfig;

	public SoundConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
		super(owningScreen, owningEntryList, new ConfigElementSliderAdapter(configElement));

		// Parse out our parameter string
		String parms = (String) configElement.get();
		final boolean culled = parms.contains("cull");
		final boolean blocked = parms.contains("block");

		this.cull = new CheckBoxButton(ID_CULLED, CULL, culled, false);
		this.block = new CheckBoxButton(ID_BLOCKED, BLOCK, blocked, false);
		this.play = new PlaySoundButton(ID_PLAY, configElement.getName());

		this.cullHover = new HoverChecker(this.cull, 800);
		this.blockHover = new HoverChecker(this.block, 800);
		this.playHover = new HoverChecker(this.play, 800);

		this.cullHoverText = ImmutableList.of(String.format(TOOLTIP_FORMAT, CULL, configElement.getName()));
		this.blockHoverText = ImmutableList.of(String.format(TOOLTIP_FORMAT, BLOCK, configElement.getName()));
		this.playHoverText = ImmutableList.of(String.format(TOOLTIP_FORMAT, PLAY, configElement.getName()));

		this.realConfig = configElement;
	}

	@Override
	public void drawToolTip(int mouseX, int mouseY) {
		super.drawToolTip(mouseX, mouseY);

		final boolean canHover = mouseY < this.owningScreen.entryList.bottom
				&& mouseY > this.owningScreen.entryList.top;

		if (this.cullHover.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(this.cullHoverText, mouseX, mouseY);

		if (this.blockHover.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(this.blockHoverText, mouseX, mouseY);

		if (this.playHover.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(this.playHoverText, mouseX, mouseY);
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
			boolean isSelected) {

		this.owningEntryList.controlWidth -= 148;
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
		this.owningEntryList.controlWidth += 148;

		this.play.xPosition = this.owningEntryList.scrollBarX - 82;
		this.play.yPosition = y;
		this.play.enabled = enabled();
		this.play.drawButton(this.mc, mouseX, mouseY);

		this.cull.xPosition = this.owningEntryList.scrollBarX - 138;
		this.cull.yPosition = y;
		this.cull.enabled = enabled();
		this.cull.drawButton(this.mc, mouseX, mouseY);

		this.block.xPosition = this.owningEntryList.scrollBarX - 194;
		this.block.yPosition = y;
		this.block.enabled = enabled();
		this.block.drawButton(this.mc, mouseX, mouseY);

	}

	@Override
	public boolean isDefault() {
		return super.isDefault() && this.cull.isDefault() && this.block.isDefault();
	}

	@Override
	public boolean isChanged() {
		return super.isChanged() || this.cull.isChanged() || this.block.isChanged();
	}

	@Override
	public void undoChanges() {
		super.undoChanges();
		this.cull.undoChanges();
		this.block.undoChanges();
	}

	@Override
	public void setToDefault() {
		super.setToDefault();
		this.cull.setToDefault();
		this.block.setToDefault();
	}

	@Override
	public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		if (this.cull.mousePressed(this.mc, x, y)) {
			this.cull.toggleState();
		} else if (this.block.mousePressed(this.mc, x, y)) {
			this.block.toggleState();
		} else if (this.play.mousePressed(this.mc, x, y)) {
			final float volume = ((Integer) this.getCurrentValue()) / 100F;
			this.play.playSound(this.mc, volume);
		} else
			return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);

		return true;
	}

	@Override
	public boolean saveConfigElement() {
		if (this.enabled() && this.isChanged()) {
			final StringBuilder builder = new StringBuilder();
			if (this.cull.getValue())
				builder.append("cull ");
			if (this.block.getValue())
				builder.append("block ");
			final int volume = ((GuiSlider) this.btnValue).getValueInt();
			if (volume != 100)
				builder.append(volume);
			this.realConfig.set(builder.toString().trim());
			return this.realConfig.requiresMcRestart();
		}
		return false;
	}

	private static class ConfigElementSliderAdapter implements IConfigElement {

		private final IConfigElement element;
		private int initialValue;

		public ConfigElementSliderAdapter(@Nonnull final IConfigElement element) {
			this.element = element;

			final String n = ((String) element.get()).replace("cull", "").replaceAll("block", "").trim();
			if (StringUtils.isEmpty(n))
				this.initialValue = 100;
			else
				this.initialValue = Integer.parseInt(n);
		}

		@Override
		public boolean isProperty() {
			return true;
		}

		@Override
		public Class<? extends IConfigEntry> getConfigEntryClass() {
			return this.element.getConfigEntryClass();
		}

		@Override
		public Class<? extends IArrayEntry> getArrayEntryClass() {
			return this.element.getArrayEntryClass();
		}

		@Override
		public String getName() {
			return this.element.getName();
		}

		@Override
		public String getQualifiedName() {
			return this.element.getQualifiedName();
		}

		@Override
		public String getLanguageKey() {
			return this.element.getQualifiedName();
		}

		@Override
		public String getComment() {
			return this.element.getQualifiedName();
		}

		@Override
		public List<IConfigElement> getChildElements() {
			return this.element.getChildElements();
		}

		@Override
		public ConfigGuiType getType() {
			return ConfigGuiType.INTEGER;
		}

		@Override
		public boolean isList() {
			return false;
		}

		@Override
		public boolean isListLengthFixed() {
			return true;
		}

		@Override
		public int getMaxListLength() {
			return this.element.getMaxListLength();
		}

		@Override
		public boolean isDefault() {
			return this.initialValue == 100;
		}

		@Override
		public Object getDefault() {
			return 100;
		}

		@Override
		public Object[] getDefaults() {
			return new Object[] { 100 };
		}

		@Override
		public void setToDefault() {
			this.element.setToDefault();
		}

		@Override
		public boolean requiresWorldRestart() {
			return false;
		}

		@Override
		public boolean showInGui() {
			return this.element.showInGui();
		}

		@Override
		public boolean requiresMcRestart() {
			return false;
		}

		@Override
		public Object get() {
			return this.initialValue;
		}

		@Override
		public Object[] getList() {
			return new Object[] { this.initialValue };
		}

		@Override
		public void set(Object value) {
			this.initialValue = Integer.parseInt((String) value);
		}

		@Override
		public void set(Object[] aVal) {
			set(aVal[0]);
		}

		@Override
		public String[] getValidValues() {
			return this.element.getValidValues();
		}

		@Override
		public Object getMinValue() {
			return 0;
		}

		@Override
		public Object getMaxValue() {
			return 200;
		}

		@Override
		public Pattern getValidationPattern() {
			return this.element.getValidationPattern();
		}

	}
}
