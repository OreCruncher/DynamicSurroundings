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

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import org.blockartistry.Presets.Presets;
import org.blockartistry.Presets.data.PresetConfig;
import org.blockartistry.Presets.data.PresetInfo;
import org.blockartistry.lib.Color;
import org.blockartistry.lib.Localization;
import org.blockartistry.lib.gui.Panel;
import org.blockartistry.lib.gui.Panel.Reference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PresetsConfigGui extends GuiScreen implements GuiYesNoCallback {

	protected static final String PREV_BUTTON_TEXT = "<<<";
	protected static final String NEXT_BUTTON_TEXT = ">>>";
	protected static final String TITLE = Localization.format("msg.presets");
	protected static final String DONE_BUTTON_LABEL = Localization.format("presets.button.Done");
	protected static final String REFRESH_BUTTON_LABEL = Localization.format("presets.button.Refresh");
	protected static final String CREATE_BUTTON_LABEL = Localization.format("presets.button.Create");
	protected static final String APPLY_BUTTON_LABEL = Localization.format("presets.button.Apply");
	protected static final String SAVE_BUTTON_LABEL = Localization.format("presets.button.Save");
	protected static final String DELETE_BUTTON_LABEL = Localization.format("presets.button.Delete");
	protected static final String EDIT_BUTTON_LABEL = Localization.format("presets.button.Edit");

	protected static final String APPLY_WARNING_TEXT = Localization.format("presets.dlg.ApplyWarning");
	protected static final String SAVE_WARNING_TEXT = Localization.format("presets.dlg.SaveWarning");
	protected static final String DELETE_WARNING_TEXT = Localization.format("presets.dlg.DeleteWarning");
	protected static final String RESTART_REQUIRED_TEXT = TextFormatting.RED
			+ Localization.format("presets.dlg.RestartRequired");

	protected static final String TOOLTIP_RESTART_REQUIRED = TextFormatting.RED
			+ Localization.format("presets.dlg.RestartRequired.tooltip");

	protected static final int ID_TITLE = 500;

	protected static final int ID_DONE = 1000;
	protected static final int ID_APPLY = 1001;
	protected static final int ID_REFRESH = 1002;
	protected static final int ID_PREV_PAGE = 1003;
	protected static final int ID_NEXT_PAGE = 1004;
	protected static final int ID_CREATE = 1005;
	protected static final int ID_SAVE = 1006;
	protected static final int ID_DELETE = 1007;
	protected static final int ID_EDIT = 1008;

	protected static final int ID_PRESET_BASE = 1101;

	protected static final int REGION_WIDTH = 335;
	protected static final int REGION_HEIGHT = 195;

	protected static final int MARGIN = 10;
	protected static final int INSET = 5;
	protected static final int MAX_PRESETS_PAGE = 5;
	protected static final int BUTTON_WIDTH = 100;
	protected static final int BUTTON_HEIGHT = 20;
	protected static final int PRESET_BUTTON_WIDTH = 200;
	protected static final int PRESET_BUTTON_HEIGHT = 20;
	protected static final int NAV_BUTTON_WIDTH = 25;
	protected static final int NAV_BUTTON_INSET = 5;

	protected final GuiScreen parentScreen;
	protected final PresetConfig config;

	protected int anchorX;
	protected int anchorY;
	protected int regionWidth;
	protected int regionHeight;

	protected int currentPage = 0;
	protected int selectedPreset = -1;
	protected int maxPage;
	protected List<PresetInfo> presets;

	protected final List<GuiButtonExt> presetButtons = Lists.newArrayList();
	protected final List<HoverChecker> tooltipChecker = Lists.newArrayList();
	protected final List<List<String>> tooltips = Lists.newArrayList();
	protected GuiButtonExt previousButton;
	protected GuiButtonExt nextButton;
	protected GuiButtonExt refreshButton;
	protected GuiButtonExt applyButton;
	protected GuiButtonExt createButton;
	protected GuiButtonExt editButton;
	protected GuiButtonExt saveButton;
	protected GuiButtonExt deleteButton;

	protected final Panel backgroundPanel = new Panel();
	protected final Panel presetPanel = new Panel(0, 0, Color.GOLD, Color.BLACK, Color.WHITE);

	public PresetsConfigGui(@Nonnull final GuiScreen parent) {
		this.mc = Minecraft.getMinecraft();
		this.parentScreen = parent;
		this.config = new PresetConfig(Presets.dataDirectory());
	}

	@Override
	public void initGui() {

		this.presetButtons.clear();
		this.tooltipChecker.clear();
		this.tooltips.clear();
		this.labelList.clear();
		this.buttonList.clear();

		this.anchorX = (this.width - REGION_WIDTH) / 2;
		this.anchorY = (this.height - REGION_HEIGHT) / 2;

		final int titleWidth = this.fontRendererObj.getStringWidth(TITLE);
		final int titleX = this.anchorX + (REGION_WIDTH - titleWidth) / 2;
		int Y = this.anchorY + INSET;
		GuiLabel title = new GuiLabel(this.fontRendererObj, ID_TITLE, titleX, Y, REGION_WIDTH, BUTTON_HEIGHT,
				Color.MC_GOLD.rgb());
		title.addLine(TITLE);
		this.labelList.add(title);

		Y += INSET + BUTTON_HEIGHT;

		final int presetWidth = PRESET_BUTTON_WIDTH + INSET * 2;
		final int presetHeight = (int) (PRESET_BUTTON_HEIGHT * (MAX_PRESETS_PAGE + 1.5F)) + INSET;
		this.presetPanel.setWidth(presetWidth);
		this.presetPanel.setHeight(presetHeight);

		for (int i = 0; i < MAX_PRESETS_PAGE; i++) {
			final int id = ID_PRESET_BASE + i;
			final int x = this.anchorX + MARGIN + INSET;
			final int y = Y + i * BUTTON_HEIGHT;
			final GuiButtonExt button = new GuiButtonExt(id, x, y, PRESET_BUTTON_WIDTH, PRESET_BUTTON_HEIGHT,
					"<NOT SET>");
			button.visible = false;
			this.presetButtons.add(button);

			final HoverChecker checker = new HoverChecker(button, 800);
			this.tooltipChecker.add(checker);

			this.tooltips.add(ImmutableList.<String>of());
		}

		Y += PRESET_BUTTON_HEIGHT * MAX_PRESETS_PAGE;

		this.buttonList.addAll(this.presetButtons);

		// Do the previous and next buttons
		Y += INSET;
		int navButtonX = this.anchorX + MARGIN + NAV_BUTTON_INSET + INSET;
		this.previousButton = new GuiButtonExt(ID_PREV_PAGE, navButtonX, Y, NAV_BUTTON_WIDTH, PRESET_BUTTON_HEIGHT,
				PREV_BUTTON_TEXT);
		this.buttonList.add(this.previousButton);
		navButtonX = this.anchorX + MARGIN + presetWidth - INSET - NAV_BUTTON_WIDTH - NAV_BUTTON_INSET;
		this.nextButton = new GuiButtonExt(ID_NEXT_PAGE, navButtonX, Y, NAV_BUTTON_WIDTH, PRESET_BUTTON_HEIGHT,
				NEXT_BUTTON_TEXT);
		this.buttonList.add(this.nextButton);

		// Buttons to the side
		final int offset = (presetHeight - 6 * BUTTON_HEIGHT) / 7;
		final int buttonsX = this.anchorX + MARGIN + presetWidth + INSET;

		int buttonsY = this.anchorY + MARGIN * 2 + INSET + offset;
		this.refreshButton = new GuiButtonExt(ID_REFRESH, buttonsX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT,
				REFRESH_BUTTON_LABEL);
		this.buttonList.add(this.refreshButton);

		buttonsY += BUTTON_HEIGHT + offset;
		this.createButton = new GuiButtonExt(ID_CREATE, buttonsX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT,
				CREATE_BUTTON_LABEL);
		this.buttonList.add(this.createButton);

		buttonsY += BUTTON_HEIGHT + offset;
		this.editButton = new GuiButtonExt(ID_EDIT, buttonsX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT, EDIT_BUTTON_LABEL);
		this.buttonList.add(this.editButton);

		buttonsY += BUTTON_HEIGHT + offset;
		this.applyButton = new GuiButtonExt(ID_APPLY, buttonsX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT,
				APPLY_BUTTON_LABEL);
		this.buttonList.add(this.applyButton);

		buttonsY += BUTTON_HEIGHT + offset;
		this.saveButton = new GuiButtonExt(ID_SAVE, buttonsX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT, SAVE_BUTTON_LABEL);
		this.buttonList.add(this.saveButton);

		buttonsY += BUTTON_HEIGHT + offset;
		this.deleteButton = new GuiButtonExt(ID_DELETE, buttonsX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT,
				DELETE_BUTTON_LABEL);
		this.buttonList.add(this.deleteButton);

		// Set the final size of the background panel;
		this.regionWidth = MARGIN * 2 + presetWidth + INSET + BUTTON_WIDTH;
		this.regionHeight = MARGIN * 2 + presetHeight + BUTTON_HEIGHT * 2;
		this.backgroundPanel.setWidth(this.regionWidth);
		this.backgroundPanel.setHeight(this.regionHeight);

		// Done button
		final int doneX = (this.regionWidth - BUTTON_WIDTH) / 2 + this.anchorX;
		final int doneY = this.anchorY + this.regionHeight - (int) (BUTTON_HEIGHT * 1.5F);
		GuiButtonExt button = new GuiButtonExt(ID_DONE, doneX, doneY, BUTTON_WIDTH, BUTTON_HEIGHT, DONE_BUTTON_LABEL);
		this.buttonList.add(button);

		this.reload();
	}

	public void reload() {
		this.currentPage = 0;
		this.selectedPreset = -1;
		this.config.scan();
		this.presets = this.config.getPresets();
		this.maxPage = (this.presets.size() - 1) / MAX_PRESETS_PAGE;
		this.setPresetButtonText();
	}

	public void setPresetButtonText() {
		final int start = this.currentPage * MAX_PRESETS_PAGE;
		for (int i = 0; i < MAX_PRESETS_PAGE; i++) {
			final GuiButtonExt button = this.presetButtons.get(i);
			final int idx = start + i;
			if (idx >= this.presets.size()) {
				button.visible = false;
			} else {
				final PresetInfo info = this.presets.get(idx);
				button.displayString = info.getTitle();
				button.enabled = this.selectedPreset != idx;
				button.visible = true;

				final List<String> tipText = Lists.newArrayList();
				tipText.add(TextFormatting.GOLD + info.getFilename());
				if (info.isRestartRequired())
					tipText.add(TOOLTIP_RESTART_REQUIRED);
				final List<String> moreText = this.fontRendererObj.listFormattedStringToWidth(info.getDescription(),
						200);
				if (moreText.size() > 0) {
					tipText.add("");
					tipText.addAll(moreText);
				}
				this.tooltips.set(i, tipText);
			}
		}

		this.previousButton.enabled = this.currentPage > 0;
		this.nextButton.enabled = this.currentPage < this.maxPage;
		this.applyButton.enabled = this.selectedPreset != -1;
		this.saveButton.enabled = this.selectedPreset != -1;
		this.deleteButton.enabled = this.selectedPreset != -1;
		this.editButton.enabled = this.selectedPreset != -1;
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		this.drawDefaultBackground();
		this.backgroundPanel.render(this.anchorX, this.anchorY, Reference.UPPER_LEFT);
		this.presetPanel.render(this.anchorX + MARGIN, this.anchorY + MARGIN + INSET * 3, Reference.UPPER_LEFT);
		super.drawScreen(mouseX, mouseY, partialTicks);

		for (int i = 0; i < MAX_PRESETS_PAGE; i++) {
			final GuiButtonExt button = this.presetButtons.get(i);
			if (button.visible) {
				final HoverChecker check = this.tooltipChecker.get(i);
				if (check.checkHover(mouseX, mouseY)) {
					final List<String> textLines = this.tooltips.get(i);
					this.drawHoveringText(textLines, mouseX, mouseY);
				}
			}
		}
	}

	@Override
	protected void actionPerformed(@Nonnull final GuiButton button) throws IOException {

		switch (button.id) {
		case ID_DONE:
			this.mc.displayGuiScreen(this.parentScreen);
			break;
		case ID_APPLY: {
			final PresetInfo pi = this.presets.get(this.selectedPreset);
			final StringBuilder builder = new StringBuilder();
			builder.append(pi.getTitle());
			if (pi.isRestartRequired())
				builder.append("\n\n").append(RESTART_REQUIRED_TEXT);
			final GuiYesNo yn = new GuiYesNo(this, APPLY_WARNING_TEXT, builder.toString(), ID_APPLY);
			this.mc.displayGuiScreen(yn);
		}
			break;
		case ID_CREATE: {
			final PresetInfo pi = new PresetInfo();
			final PresetInfoModifyGui gui = new PresetInfoModifyGui(this, pi, false, ID_CREATE);
			this.mc.displayGuiScreen(gui);
		}
			break;
		case ID_EDIT: {
			final PresetInfo pi = this.presets.get(this.selectedPreset);
			final PresetInfoModifyGui gui = new PresetInfoModifyGui(this, pi, true, ID_EDIT);
			this.mc.displayGuiScreen(gui);
		}
			break;
		case ID_SAVE: {
			final PresetInfo pi = this.presets.get(this.selectedPreset);
			final GuiYesNo yn = new GuiYesNo(this, SAVE_WARNING_TEXT, pi.getTitle(), ID_SAVE);
			this.mc.displayGuiScreen(yn);
		}
			break;
		case ID_DELETE: {
			final PresetInfo pi = this.presets.get(this.selectedPreset);
			final GuiYesNo yn = new GuiYesNo(this, DELETE_WARNING_TEXT, pi.getTitle(), ID_DELETE);
			this.mc.displayGuiScreen(yn);
		}
			break;
		case ID_REFRESH:
			this.reload();
			break;
		case ID_PREV_PAGE:
			if (this.currentPage > 0) {
				this.currentPage--;
				this.setPresetButtonText();
			}
			break;
		case ID_NEXT_PAGE:
			if (this.currentPage < this.maxPage) {
				this.currentPage++;
				this.setPresetButtonText();
			}
			break;
		default:
			// Handle selection of a preset button
			final int buttonMashed = button.id - ID_PRESET_BASE;
			if (buttonMashed < 0 || buttonMashed >= MAX_PRESETS_PAGE) {
				super.actionPerformed(button);
			} else {
				// A preset was hit!
				if (this.selectedPreset != -1) {
					// We have a preset to clear. If it's in the current range
					// we need to toggle.
					final int low = this.currentPage * MAX_PRESETS_PAGE;
					final int high = low + MAX_PRESETS_PAGE;
					if (this.selectedPreset >= low && this.selectedPreset < high) {
						// It's in range
						final int idx = this.selectedPreset % MAX_PRESETS_PAGE;
						this.presetButtons.get(idx).enabled = true;
					}
				}
				this.selectedPreset = this.currentPage * MAX_PRESETS_PAGE + buttonMashed;
				this.presetButtons.get(buttonMashed).enabled = false;
				this.applyButton.enabled = true;
				this.saveButton.enabled = true;
				this.deleteButton.enabled = true;
				this.editButton.enabled = true;
			}
		}
	}

	@Override
	public void confirmClicked(final boolean answer, final int buttonId) {

		final PresetInfo pi = this.presets.get(this.selectedPreset);
		switch (buttonId) {
		case ID_SAVE:
			if (answer) {
				this.config.collectPreset(pi);
				this.config.save(pi);
			}
			break;
		case ID_DELETE:
			if (answer) {
				this.config.delete(pi);
				this.reload();
			}
			break;
		case ID_APPLY:
			if (answer) {
				this.config.applyPreset(pi);
			}
			break;
		}

		this.mc.displayGuiScreen(this);

	}

	public void confirmUpdate(final boolean answer, final int buttonId, final PresetInfo info) {
		switch (buttonId) {
		case ID_CREATE:
			if (answer) {
				this.config.collectPreset(info);
				this.config.save(info);
				this.reload();
			}
		case ID_EDIT:
			if (answer) {
				this.config.save(info);
				this.reload();
			}
		default:
			;
		}

		this.mc.displayGuiScreen(this);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if (this.parentScreen instanceof GuiConfig) {
			final GuiConfig parentGuiConfig = (GuiConfig) this.parentScreen;
			parentGuiConfig.needsRefresh = true;
			parentGuiConfig.initGui();
		}
	}
}
