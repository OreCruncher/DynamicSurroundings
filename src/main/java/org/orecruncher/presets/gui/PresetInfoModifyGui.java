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

package org.orecruncher.presets.gui;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.orecruncher.lib.Color;
import org.orecruncher.lib.Localization;
import org.orecruncher.lib.gui.StandardPanel;
import org.orecruncher.lib.gui.Panel.Reference;
import org.orecruncher.presets.data.PresetInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PresetInfoModifyGui extends GuiScreen {

	protected static final String CREATE_TITLE = Localization.format("presets.dlg.CreateTitle");
	protected static final String EDIT_TITLE = Localization.format("presets.dlg.EditTitle");
	protected static final String FILENAME_LABEL = Localization.format("presets.dlg.FileName");
	protected static final String PRESET_TITLE_LABEL = Localization.format("presets.dlg.Title");
	protected static final String PRESET_DESCRIPTION_LABEL = Localization.format("presets.dlg.Description");
	protected static final String DONE_BUTTON_LABEL = Localization.format("presets.button.Done");
	protected static final String CANCEL_BUTTON_LABEL = Localization.format("presets.button.Cancel");
	protected static final String REQUIRED_TEXT = Localization.format("presets.dlg.Required");

	protected static final String FILENAME_VALIDATION_REGEX = ".*[^\\w -.].*";

	// Labels
	protected static final int ID_TITLE = 500;
	protected static final int ID_PRESET_FILENAME = 501;
	protected static final int ID_PRESET_TITLE = 502;
	protected static final int ID_PRESET_DESCRIPTION = 503;
	protected static final int ID_FILENAME_REQUIRED = 504;
	protected static final int ID_TITLE_REQUIRED = 505;

	// Text fields
	protected static final int ID_PRESET_FILENAME_TEXT = 700;
	protected static final int ID_PRESET_TITLE_TEXT = 701;
	protected static final int ID_PRESET_DESCRIPTION_TEXT = 703;

	// Buttons
	protected static final int ID_DONE = 1000;
	protected static final int ID_CANCEL = 1001;

	// Geometry
	protected static final int REGION_WIDTH = 335;
	protected static final int REGION_HEIGHT = 200;
	protected static final int MARGIN = 10;
	protected static final int INSET = 5;
	protected static final int BUTTON_WIDTH = 100;
	protected static final int BUTTON_HEIGHT = 20;
	protected static final int LABEL_WIDTH = 300;

	protected final PresetsConfigGui parentScreen;

	protected int anchorX;
	protected int anchorY;
	protected int regionWidth;
	protected int regionHeight;

	protected GuiTextField fileName;
	protected GuiTextField presetTitle;
	protected GuiTextField presetDescription;
	protected GuiButtonExt doneButton;
	protected GuiLabel fileNameRequired;
	protected GuiLabel presetTitleRequired;

	protected final StandardPanel backgroundPanel = new StandardPanel();

	protected final int buttonId;
	protected final boolean editMode;
	protected final PresetInfo info;

	public PresetInfoModifyGui(@Nonnull final PresetsConfigGui parent, final PresetInfo info, final boolean editMode,
			final int buttonId) {
		this.mc = Minecraft.getMinecraft();
		this.parentScreen = parent;
		this.buttonId = buttonId;
		this.editMode = editMode;
		this.info = info;
	}

	@Override
	public void initGui() {

		this.labelList.clear();
		this.buttonList.clear();

		this.anchorX = (this.width - REGION_WIDTH) / 2;
		this.anchorY = (this.height - REGION_HEIGHT) / 2;

		final String theTitle = this.editMode ? EDIT_TITLE : CREATE_TITLE;
		final int titleWidth = this.fontRenderer.getStringWidth(theTitle);
		int X = this.anchorX + (REGION_WIDTH - titleWidth) / 2;
		int Y = this.anchorY + MARGIN;
		GuiLabel label = new GuiLabel(this.fontRenderer, ID_TITLE, X, Y, REGION_WIDTH, BUTTON_HEIGHT,
				Color.MC_GOLD.rgb());
		label.addLine(theTitle);
		this.labelList.add(label);

		X = this.anchorX + MARGIN;
		Y += BUTTON_HEIGHT;

		final int entryWidth = REGION_WIDTH - MARGIN * 2;
		final int labelColor = Color.WHITE.rgb();
		final int requiredWidth = this.fontRenderer.getStringWidth(REQUIRED_TEXT);

		label = new GuiLabel(this.fontRenderer, ID_PRESET_FILENAME, X, Y, LABEL_WIDTH, BUTTON_HEIGHT, labelColor);
		label.addLine(FILENAME_LABEL);
		this.labelList.add(label);

		this.fileNameRequired = new GuiLabel(this.fontRenderer, ID_FILENAME_REQUIRED, X + entryWidth - requiredWidth, Y,
				LABEL_WIDTH, BUTTON_HEIGHT, Color.RED.rgb());
		this.fileNameRequired.addLine(REQUIRED_TEXT);
		this.fileNameRequired.visible = false;
		this.labelList.add(this.fileNameRequired);

		Y += BUTTON_HEIGHT;
		this.fileName = new GuiTextField(ID_PRESET_FILENAME_TEXT, this.fontRenderer, X, Y, entryWidth, BUTTON_HEIGHT);
		this.fileName.setMaxStringLength(32);
		this.fileName.setText(this.info.getFilename());
		this.fileName.setValidator(input -> !input.matches(FILENAME_VALIDATION_REGEX));

		Y += BUTTON_HEIGHT + INSET;
		label = new GuiLabel(this.fontRenderer, ID_PRESET_FILENAME, X, Y, LABEL_WIDTH, BUTTON_HEIGHT, labelColor);
		label.addLine(PRESET_TITLE_LABEL);
		this.labelList.add(label);

		this.presetTitleRequired = new GuiLabel(this.fontRenderer, ID_TITLE_REQUIRED, X + entryWidth - requiredWidth, Y,
				LABEL_WIDTH, BUTTON_HEIGHT, Color.RED.rgb());
		this.presetTitleRequired.addLine(REQUIRED_TEXT);
		this.presetTitleRequired.visible = false;
		this.labelList.add(this.presetTitleRequired);

		Y += BUTTON_HEIGHT;
		this.presetTitle = new GuiTextField(ID_PRESET_TITLE_TEXT, this.fontRenderer, X, Y, entryWidth, BUTTON_HEIGHT);
		this.presetTitle.setMaxStringLength(48);
		this.presetTitle.setText(this.info.getTitle());

		Y += BUTTON_HEIGHT + INSET;
		label = new GuiLabel(this.fontRenderer, ID_PRESET_FILENAME, X, Y, LABEL_WIDTH, BUTTON_HEIGHT, labelColor);
		label.addLine(PRESET_DESCRIPTION_LABEL);
		this.labelList.add(label);

		Y += BUTTON_HEIGHT;
		this.presetDescription = new GuiTextField(ID_PRESET_DESCRIPTION_TEXT, this.fontRenderer, X, Y, entryWidth,
				BUTTON_HEIGHT);
		this.presetDescription.setMaxStringLength(255);
		this.presetDescription.setText(this.info.getDescription());

		Y += BUTTON_HEIGHT * 2 + INSET * 2 + MARGIN;
		// Set the final size of the background panel;
		this.regionWidth = REGION_WIDTH;
		this.regionHeight = Y - this.anchorY;
		this.backgroundPanel.setWidth(this.regionWidth);
		this.backgroundPanel.setHeight(this.regionHeight);

		// Done button
		int doneX = this.anchorX + (this.regionWidth - BUTTON_WIDTH * 3) / 2;
		final int doneY = this.anchorY + this.regionHeight - (int) (BUTTON_HEIGHT * 1.5F);
		this.doneButton = new GuiButtonExt(ID_DONE, doneX, doneY, BUTTON_WIDTH, BUTTON_HEIGHT, DONE_BUTTON_LABEL);
		this.buttonList.add(this.doneButton);

		// Cancel button
		doneX += BUTTON_WIDTH * 2;
		final GuiButtonExt button = new GuiButtonExt(ID_CANCEL, doneX, doneY, BUTTON_WIDTH, BUTTON_HEIGHT,
				CANCEL_BUTTON_LABEL);
		this.buttonList.add(button);

		// Filename field has first focus
		this.fileName.setFocused(true);
		doneEnableCheck();
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		drawDefaultBackground();
		this.backgroundPanel.render(this.anchorX, this.anchorY, Reference.UPPER_LEFT);
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.fileName.drawTextBox();
		this.presetTitle.drawTextBox();
		this.presetDescription.drawTextBox();
	}

	@Override
	protected void actionPerformed(@Nonnull final GuiButton button) throws IOException {
		switch (button.id) {
		case ID_DONE:
			this.info.setDescription(this.presetDescription.getText().trim());
			this.info.setTitle(this.presetTitle.getText().trim());
			this.info.setFilename(this.fileName.getText());
			this.parentScreen.confirmUpdate(true, this.buttonId, this.info);
			break;
		case ID_CANCEL:
			this.parentScreen.confirmUpdate(false, this.buttonId, this.info);
			break;
		default:
			super.actionPerformed(button);
		}
	}

	@Override
	public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.fileName.mouseClicked(mouseX, mouseY, mouseButton);
		this.presetTitle.mouseClicked(mouseX, mouseY, mouseButton);
		this.presetDescription.mouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void doneEnableCheck() {
		final boolean fName = this.fileName.getText().trim().length() > 0;
		final boolean pTitle = this.presetTitle.getText().trim().length() > 0;
		this.fileNameRequired.visible = !fName;
		this.presetTitleRequired.visible = !pTitle;
		this.doneButton.enabled = fName && pTitle;
	}

	@Override
	protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
		if (this.fileName.isFocused()) {
			this.fileName.textboxKeyTyped(typedChar, keyCode);
		} else if (this.presetTitle.isFocused()) {
			this.presetTitle.textboxKeyTyped(typedChar, keyCode);
		} else if (this.presetDescription.isFocused()) {
			this.presetDescription.textboxKeyTyped(typedChar, keyCode);
		}
		doneEnableCheck();
	}
}
