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

package org.orecruncher.dsurround.registry.acoustics;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EventSelectorAcoustics implements IAcoustic {
	private final String name;

	private final Map<EventType, IAcoustic> pairs = new EnumMap<>(EventType.class);

	public EventSelectorAcoustics(@Nonnull final String acousticName) {
		this.name = acousticName;
	}

	@Nonnull
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void playSound(@Nonnull final ISoundPlayer player, @Nonnull final Vec3d location,
			@Nullable final EventType event, @Nullable final IOptions inputOptions) {
		final IAcoustic acoustic = this.pairs.get(event);
		if (acoustic != null)
			acoustic.playSound(player, location, event, inputOptions);
		else if (event != null && event.canTransition())
			playSound(player, location, event.getTransitionDestination(), inputOptions);
	}

	public void setAcousticPair(@Nonnull final EventType type, @Nonnull final IAcoustic acoustic) {
		this.pairs.put(type, acoustic);
	}

	@Override
	public String toString() {
		return getName();
	}

}
