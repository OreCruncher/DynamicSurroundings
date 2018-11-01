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

package org.orecruncher.lib.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RecordTitleEmitter implements ITickable {

	public static interface ITimeKeeper {
		int getTickMark();
	}

	// The number of ticks Vanilla will display after
	// the record name is set.
	private static final int VANILLA_DISPLAY_TIME = 60;

	// Default amount of ticks to display the title
	private static final int DEFAULT_DISPLAY_TIME = 200;

	protected final Minecraft mc = Minecraft.getMinecraft();
	protected final String title;
	protected final ITimeKeeper time;
	protected int expiry;

	public RecordTitleEmitter(@Nonnull final String title, @Nonnull final ITimeKeeper time) {
		this(title, time, DEFAULT_DISPLAY_TIME);
	}

	public RecordTitleEmitter(@Nonnull final String title, @Nonnull final ITimeKeeper time, final int ticksToDisplay) {
		this.title = TextFormatting.GOLD + title;
		this.time = time;
		this.expiry = this.time.getTickMark() + ticksToDisplay - VANILLA_DISPLAY_TIME;
	}

	@Override
	public void update() {
		if (this.time.getTickMark() <= this.expiry) {
			this.mc.ingameGUI.setOverlayMessage(this.title, false);
		} else {
			// Depending on ticking there could be some lag where the timer
			// expires, unexpires, and expires again. Setting to 0 makes
			// sure it stays expires when it does.
			this.expiry = 0;
		}
	}
}
