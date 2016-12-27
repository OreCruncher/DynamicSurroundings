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

package org.blockartistry.mod.DynSurround.api.events;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event raised when on the client when speech data is received.
 * This event will only fire client side.
 * 
 * Can be canceled.
 */
@Cancelable
public class SpeechTextEvent extends Event {
	
	/**
	 * Persistent ID of the entity this event is associated with.
	 */
	public final UUID entityId;
	
	/**
	 * The message to be displayed, or the message ID to be translated.
	 */
	public final String message;
	
	/**
	 * Indicates whether the message should be translated prior to
	 * display.
	 */
	public final boolean translate;

	/**
	 * Creates an event for a message to be displayed without modification.
	 * 
	 * @param id Entity UUID that this message is associated with
	 * @param message The text message to display
	 */
	public SpeechTextEvent(@Nonnull final UUID id, @Nonnull final String message) {
		this(id, message, false);
	}
	
	/**
	 * Creates an event that will permit translation.  The message ID would be
	 * provided in the message variable.  Translation occurs client side.
	 * 
	 * @param id Entity UUID that the message is associated with
	 * @param message The message ID/message to be displayed
	 * @param translate Indicates that the message is a message ID that needs translation
	 */
	public SpeechTextEvent(@Nonnull final UUID id, @Nonnull final String message, final boolean translate) {
		this.entityId = id;
		this.message = message;
		this.translate = translate;
	}

}
