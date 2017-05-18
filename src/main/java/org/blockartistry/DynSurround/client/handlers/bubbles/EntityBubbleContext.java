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

package org.blockartistry.DynSurround.client.handlers.bubbles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.fx.particle.ParticleBillboard;
import org.blockartistry.DynSurround.client.fx.particle.ParticleHelper;

import com.google.common.base.Supplier;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityBubbleContext implements Supplier<List<String>> {

	private final List<SpeechBubbleData> data = new ArrayList<SpeechBubbleData>();
	private List<String> preppedList;
	private ParticleBillboard bubble;

	public void add(@Nonnull final SpeechBubbleData d) {
		this.data.add(d);
		this.preppedList = null;
	}

	public boolean clean(final int currentTick) {
		boolean reset = false;
		final Iterator<SpeechBubbleData> itr = this.data.iterator();
		while (itr.hasNext()) {
			final SpeechBubbleData d = itr.next();
			if (d.isExpired(currentTick)) {
				itr.remove();
				reset = true;
			}
		}

		if (reset)
			this.preppedList = null;

		return this.data.isEmpty();
	}

	public void handleBubble(@Nonnull final Entity entity) {
		if (this.bubble == null || this.bubble.shouldExpire()) {
			this.bubble = new ParticleBillboard(entity, this);
			ParticleHelper.addParticle(this.bubble);
		}
	}

	@Override
	public List<String> get() {
		if (this.preppedList == null && this.data.size() > 0) {
			this.preppedList = new ArrayList<String>();
			for (final SpeechBubbleData entry : this.data)
				this.preppedList.addAll(entry.getText());
		}
		return this.preppedList;
	}
}