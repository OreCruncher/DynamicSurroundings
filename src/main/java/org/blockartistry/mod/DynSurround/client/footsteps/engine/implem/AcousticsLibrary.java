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

package org.blockartistry.mod.DynSurround.client.footsteps.engine.implem;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ILibrary;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.INamedAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.IOptions;
import org.blockartistry.mod.DynSurround.client.footsteps.engine.interfaces.ISoundPlayer;
import org.blockartistry.mod.DynSurround.client.footsteps.game.system.Association;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AcousticsLibrary implements ILibrary {
	private Map<String, IAcoustic> acoustics = new LinkedHashMap<String, IAcoustic>();

	public AcousticsLibrary() {
	}

	@Override
	public void addAcoustic(final INamedAcoustic acoustic) {
		this.acoustics.put(acoustic.getName(), acoustic);
	}

	@Override
	public void playAcoustic(final Object location, final Association acousticName, final EventType event) {
		playAcoustic(location, acousticName.getData(), event, null);
	}

	@Override
	public void playAcoustic(final Object location, final String acousticName, final EventType event,
			final IOptions inputOptions) {
		if(StringUtils.isEmpty(acousticName)) {
			ModLog.debug("Attempt to play acoustic with no name");
			return;
		}
		
		final String fragments[] = acousticName.split(",");
		for (final String fragment : fragments) {
			final IAcoustic acoustic = this.acoustics.get(fragment);
			if (acoustic == null) {
				onAcousticNotFound(location, fragment, event, inputOptions);
			} else {
				if (ModLog.DEBUGGING)
					ModLog.debug("  Playing acoustic " + acousticName + " for event " + event.toString().toUpperCase());
				acoustic.playSound(mySoundPlayer(), location, event, inputOptions);
			}
		}
	}

	protected void onAcousticNotFound(final Object location, final String acousticName, final EventType event,
			final IOptions inputOptions) {
		ModLog.debug("Tried to play a missing acoustic: " + acousticName);
	}

	protected abstract ISoundPlayer mySoundPlayer();
}