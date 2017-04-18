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

import javax.annotation.Nonnull;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CheckBoxButton extends GuiButtonExt {

	private static final String CHECKED = " [" + TextFormatting.RED + TextFormatting.BOLD + "X" + TextFormatting.RESET
			+ "]";
	private static final String UNCHECKED = " [  ]";

	private final String prefix;
	private final boolean initialState;
	private final boolean defaultState;
	private boolean currentState;

	public CheckBoxButton(final int id, @Nonnull final String prefix, final boolean initialState,
			final boolean defaultState) {
		super(id, 0, 0, 52, 18, "");

		this.prefix = prefix;
		this.initialState = initialState;
		this.defaultState = defaultState;
		this.currentState = initialState;

		updateDisplayString();
	}

	protected void updateDisplayString() {
		this.displayString = this.prefix + (this.currentState ? CHECKED : UNCHECKED);
	}

	public boolean isDefault() {
		return this.currentState == this.defaultState;
	}

	public boolean isChanged() {
		return this.currentState != this.initialState;
	}

	public void setToDefault() {
		this.currentState = this.defaultState;
		updateDisplayString();
	}

	public void undoChanges() {
		this.currentState = this.initialState;
		updateDisplayString();
	}

	public void toggleState() {
		this.currentState = !this.currentState;
		updateDisplayString();
	}

	public boolean getValue() {
		return this.currentState;
	}
}
