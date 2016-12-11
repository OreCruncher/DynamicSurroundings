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

package org.blockartistry.mod.DynSurround.client.speech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.IClientEffectHandler;
import org.blockartistry.mod.DynSurround.client.speech.SpeechBubbleRenderer.RenderingInfo;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpeechBubbleHandler implements IClientEffectHandler {

	private static final Map<UUID, List<SpeechBubbleData>> messages = new HashMap<UUID, List<SpeechBubbleData>>();

	protected static class SpeechBubbleData {
		public final long expires = System.currentTimeMillis() + (long) (ModOptions.speechBubbleDuration * 1000F);
		public final RenderingInfo messages;

		public SpeechBubbleData(final String message) {
			this.messages = SpeechBubbleRenderer.generateRenderInfo(message);
		}
	}

	public static void addSpeechBubble(final UUID entityId, final String message) {
		if (!ModOptions.enableSpeechBubbles || entityId == null || StringUtils.isEmpty(message))
			return;

		List<SpeechBubbleData> list = messages.get(entityId);
		if (list == null) {
			messages.put(entityId, list = new ArrayList<SpeechBubbleData>());
		}
		list.add(new SpeechBubbleData(message));
	}

	// Used to retrieve messages that are to be displayed
	// above the players head.
	public static List<RenderingInfo> getMessagesForPlayer(final EntityPlayer player) {
		final List<SpeechBubbleData> data = messages.get(player.getUniqueID());
		if (data == null || data.isEmpty())
			return null;
		final List<RenderingInfo> result = new ArrayList<RenderingInfo>();
		for (final SpeechBubbleData entry : data)
			result.add(entry.messages);
		return result;
	}

	@Override
	public void process(final World world, final EntityPlayer player) {
		final long timeStamp = System.currentTimeMillis();
		for (final List<SpeechBubbleData> list : messages.values()) {
			final Iterator<SpeechBubbleData> itr = list.iterator();
			while (itr.hasNext()) {
				final SpeechBubbleData data = itr.next();
				if (timeStamp > data.expires)
					itr.remove();
				else
					break;
			}
		}
	}

	@Override
	public boolean hasEvents() {
		return false;
	}
}
