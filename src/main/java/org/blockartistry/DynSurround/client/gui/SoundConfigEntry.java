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

package org.blockartistry.DynSurround.client.gui;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.DynSurround.registry.SoundMetadata;
import org.blockartistry.DynSurround.registry.SoundRegistry;
import org.blockartistry.lib.ForgeUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
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

	private final CheckBoxButton cull;
	private final CheckBoxButton block;
	private final PlaySoundButton play;

	private final HoverChecker cullHover;
	private final HoverChecker blockHover;
	private final HoverChecker playHover;
	private final HoverChecker sliderHover;

	private final IConfigElement realConfig;

	public SoundConfigEntry(@Nonnull final GuiConfig owningScreen, @Nonnull final GuiConfigEntries owningEntryList,
			@Nonnull final IConfigElement configElement) {
		super(owningScreen, owningEntryList, new ConfigElementSliderAdapter(configElement));

		final String soundName = configElement.getName();

		// Parse out our parameter string
		String parms = (String) configElement.get();
		final boolean culled = parms.contains(GuiConstants.TOKEN_CULL);
		final boolean blocked = parms.contains(GuiConstants.TOKEN_BLOCK);

		this.cull = new CheckBoxButton(ID_CULLED, GuiConstants.TEXT_CULL, culled, false);
		this.block = new CheckBoxButton(ID_BLOCKED, GuiConstants.TEXT_BLOCK, blocked, false);
		this.play = new PlaySoundButton(ID_PLAY, soundName);

		this.cullHover = new HoverChecker(this.cull, 800);
		this.blockHover = new HoverChecker(this.block, 800);
		this.playHover = new HoverChecker(this.play, 800);
		this.sliderHover = new HoverChecker(null, 800);

		// Replace the slider tooltip with our own version
		final List<String> text = Lists.newArrayList();
		final ResourceLocation soundResource = new ResourceLocation(soundName);
		text.add(TextFormatting.GREEN + ForgeUtils.getModName(soundResource));
		text.add(TextFormatting.GOLD + soundName);

		final SoundMetadata data = SoundRegistry.getSoundMetadata(soundResource);
		if (data != null) {
			boolean spaceAdded = false;
			final String title = data.getTitle();
			if (!StringUtils.isEmpty(title)) {
				spaceAdded = true;
				text.add("");
				text.add(TextFormatting.YELLOW + title);
			}

			final List<String> credits = data.getCredits();
			if (credits != null && credits.size() > 0) {
				if (!spaceAdded)
					text.add("");
				for (final String s : credits)
					text.add(s);
			}
		}

		this.toolTip = ImmutableList.copyOf(text);

		this.realConfig = configElement;

	}

	@Override
	public void drawToolTip(final int mouseX, final int mouseY) {
		super.drawToolTip(mouseX, mouseY);

		final boolean canHover = mouseY < this.owningScreen.entryList.bottom
				&& mouseY > this.owningScreen.entryList.top;

		//if (this.sliderHover.checkHover(mouseX, mouseY))
		//	this.owningScreen.drawToolTip(this.toolTip, mouseX, mouseY);

		if (this.cullHover.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(this.toolTip, mouseX, mouseY);

		if (this.blockHover.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(this.toolTip, mouseX, mouseY);

		if (this.playHover.checkHover(mouseX, mouseY, canHover))
			this.owningScreen.drawToolTip(this.toolTip, mouseX, mouseY);
	}

	@Override
	public void drawEntry(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
			final int mouseX, final int mouseY, final boolean isSelected, final float partial) {

		this.owningEntryList.controlWidth -= 205;
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
		this.owningEntryList.controlWidth += 205;

		this.sliderHover.updateBounds(y, y + slotHeight, x, this.owningEntryList.scrollBarX - 196);

		final int buttonWidth = 68;
		this.play.x = this.owningEntryList.scrollBarX - 115;
		this.play.y = y;
		this.play.enabled = enabled();
		this.play.drawButton(this.mc, mouseX, mouseY, partial);

		this.cull.x = this.owningEntryList.scrollBarX - 115 - (buttonWidth);
		this.cull.y = y;
		this.cull.enabled = enabled();
		this.cull.drawButton(this.mc, mouseX, mouseY, partial);

		this.block.x = this.owningEntryList.scrollBarX - 115 - (buttonWidth * 2);
		this.block.y = y;
		this.block.enabled = enabled();
		this.block.drawButton(this.mc, mouseX, mouseY, partial);

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
	public boolean mousePressed(final int index, final int x, final int y, final int mouseEvent, final int relativeX,
			final int relativeY) {
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
	public void onGuiClosed() {
		this.play.stopSound();
	}

	@Override
	public boolean saveConfigElement() {
		if (this.enabled() && this.isChanged()) {
			final StringBuilder builder = new StringBuilder();
			if (this.cull.getValue())
				builder.append(GuiConstants.TOKEN_CULL).append(' ');
			if (this.block.getValue())
				builder.append(GuiConstants.TOKEN_BLOCK).append(' ');
			final int volume = ((GuiSlider) this.btnValue).getValueInt();
			if (volume != 100)
				builder.append(volume);
			this.realConfig.set(builder.toString().trim());
			return this.realConfig.requiresMcRestart();
		}
		return false;
	}

	/**
	 * The NumberSliderEntry super class assumes a single source value as a
	 * value to manipulate. Since the SoundConfigEntry is a composite of 3
	 * different config values, we need to process the parameter string to
	 * extract out the volume scale information. This adapter is used because
	 * the NumberSliderEntry CTOR is invoked before any other data processing
	 * can take place.
	 */
	private static class ConfigElementSliderAdapter implements IConfigElement {

		private final IConfigElement element;
		private int initialValue;

		public ConfigElementSliderAdapter(@Nonnull final IConfigElement element) {
			this.element = element;

			final String n = ((String) element.get()).replace(GuiConstants.TOKEN_CULL, "")
					.replace(GuiConstants.TOKEN_BLOCK, "").trim();
			if (StringUtils.isEmpty(n))
				this.initialValue = (int) (SoundRegistry.DEFAULT_SOUNDFACTOR * 100F);
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
			return this.initialValue == (int) (SoundRegistry.DEFAULT_SOUNDFACTOR * 100F);
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
			return (int) (SoundRegistry.MIN_SOUNDFACTOR);
		}

		@Override
		public Object getMaxValue() {
			return (int) (SoundRegistry.MAX_SOUNDFACTOR * 100F);
		}

		@Override
		public Pattern getValidationPattern() {
			return this.element.getValidationPattern();
		}

	}
}
