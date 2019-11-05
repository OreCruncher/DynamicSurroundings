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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFilteredConfigEntries extends GuiConfigEntries {


	private static final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
	private static final String searchLabel = I18n.format("fml.menu.mods.search");
	private static final int searchLabelWidth = fontRenderer.getStringWidth(searchLabel);
	private static final int renderColor = 0xFFDD00;

	private GuiTextField search;
	private String lastFilterText = StringUtils.EMPTY;
	private List<IConfigEntry> filteredList;

	private final GuiConfig parent;

	public GuiFilteredConfigEntries(GuiConfig parent, Minecraft mc) {
		super(parent, mc);

		this.parent = parent;
		this.filteredList = this.listEntries;
	}

	@Override
	public void initGui() {
		super.initGui();

		final int w = 240;
		final int y = (this.parent.width - w - searchLabelWidth - 10) / 2 + searchLabelWidth + 5;
		this.search = new GuiTextField(0, fontRenderer, y, this.top, w, 14);
		this.search.setText(this.lastFilterText);
		this.search.setFocused(false);
		this.search.setCanLoseFocus(true);

		this.top = this.owningScreen.titleLine2 != null ? 53 : 43;
	}

	protected void filterText() {
		this.lastFilterText = this.search.getText();

		if (StringUtils.isEmpty(this.lastFilterText)) {
			this.filteredList = this.listEntries;
		} else {
			this.filteredList = new ArrayList<>();
			final String filter = this.lastFilterText.toLowerCase();
			for (final IConfigEntry e : this.listEntries) {
				final IConfigElement ce = e.getConfigElement();
				String trans = I18n.format(ce.getLanguageKey());
				if (trans.equals(ce.getLanguageKey()))
					trans = ce.getName();
				if (trans.toLowerCase().contains(filter))
					this.filteredList.add(e);
			}
		}
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 *
	 * @return
	 */
	@Override
	public boolean mouseClicked(int x, int y, int button) {
		boolean result = super.mouseClicked(x, y, button);
		result |= this.search.mouseClicked(x, y, button);
		if (button == 1 && x >= this.search.x && x < this.search.x + this.search.width && y >= this.search.y
				&& y < this.search.y + this.search.height) {
			this.search.setText("");
		}

		return result;
	}

	@Override
	public void mouseClickedPassThru(int mouseX, int mouseY, int mouseEvent) {
		for (final IConfigEntry entry : this.filteredList)
			entry.mouseClicked(mouseX, mouseY, mouseEvent);
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the
	 * equivalent of KeyListener.keyTyped(KeyEvent e). Args : character (character
	 * on the key), keyCode (lwjgl Keyboard key code)
	 */
	@Override
	public void keyTyped(char c, int keyCode) {
		for (final IConfigEntry entry : this.filteredList)
			entry.keyTyped(c, keyCode);

		this.search.textboxKeyTyped(c, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		for (final IConfigEntry entry : this.filteredList)
			entry.updateCursorCounter();

		this.search.updateCursorCounter();

		if (!this.search.getText().equals(this.lastFilterText)) {
			filterText();
		}
	}

	@Override
	public int getSize() {
		return this.filteredList.size();
	}

	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	@Override
	public IConfigEntry getListEntry(int index) {
		return this.filteredList.get(index);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		final int w = 240;
		final int x = (this.parent.width - w - searchLabelWidth - 10) / 2;
		fontRenderer.drawString(searchLabel, x, this.top - 18, renderColor);
		this.search.drawTextBox();
	}

	@Override
	public void drawScreenPost(int mouseX, int mouseY, float partialTicks) {
		for (final IConfigEntry entry : this.filteredList)
			entry.drawToolTip(mouseX, mouseY);
	}

}
