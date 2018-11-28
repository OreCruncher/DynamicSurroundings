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

package org.orecruncher.dsurround.capabilities.speech;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.collections.ObjectArray;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpeechData implements ISpeechData {

	private final List<SpeechBubbleData> data = new ArrayList<>();
	private final ObjectArray<String> preppedList = new ObjectArray<>();
	
	private RenderContext ctx;

	@Override
	public void addMessage(@Nonnull final String string, final int ticksTTL) {
		final SpeechBubbleData data = new SpeechBubbleData(string, EnvironState.getTickCounter() + ticksTTL);
		this.data.add(data);
		generateTextForRender();
	}

	@Override
	public void onUpdate(final int currentTick) {
		final int oldSize = this.data.size();
		this.data.removeIf(d -> d.isExpired(currentTick));
		if (oldSize != this.data.size())
			generateTextForRender();
	}

	@Override
	public ObjectArray<String> getText() {
		return this.preppedList;
	}
	
	@Nullable
	public RenderContext getRenderContext() {
		return this.ctx;
	}

	@Override
	@Nonnull
	public NBTTagCompound serializeNBT() {
		// This shouldn't be needed, but just in case....
		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(@Nonnull final NBTTagCompound nbt) {
		// Do nothing - since this is 100% client side there is
		// no persistence.
	}
	
	protected void generateTextForRender() {
		this.preppedList.clear();
		for (final SpeechBubbleData entry : this.data)
			this.preppedList.addAll(entry.getText());
		this.ctx = new RenderContext(this.preppedList);
	}

}
