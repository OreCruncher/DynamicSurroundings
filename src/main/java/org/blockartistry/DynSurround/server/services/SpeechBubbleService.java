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

package org.blockartistry.DynSurround.server.services;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.network.Network;
import org.blockartistry.DynSurround.network.PacketSpeechBubble;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public final class SpeechBubbleService extends Service {

	SpeechBubbleService() {
		super("SpeechBubbleService");
	}

	// Received when the server is processing a regular chat
	// message - not a command, etc.
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onChatMessageEvent(@Nonnull final ServerChatEvent event) {
		if (!ModOptions.enableSpeechBubbles)
			return;

		final TargetPoint point = Network.getTargetPoint(event.getPlayer(), ModOptions.speechBubbleRange);
		final PacketSpeechBubble packet = new PacketSpeechBubble(event.getPlayer().getUniqueID(), event.getMessage(),
				false);
		Network.sendToAllAround(point, packet);
	}
}
