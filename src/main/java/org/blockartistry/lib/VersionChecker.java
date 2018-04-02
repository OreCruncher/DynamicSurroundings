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

package org.blockartistry.lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public final class VersionChecker {

	private final String modId;
	private final String messageId;

	public VersionChecker(@Nonnull final String id, @Nonnull final String messageId) {
		this.modId = id;
		this.messageId = messageId;
	}

	private static boolean dontPrintMessage(@Nonnull final CheckResult result) {
		return result == null || result.status == null || result.target == null || result.url == null
				|| result.status == Status.UP_TO_DATE || result.status == Status.AHEAD
				|| result.status == Status.PENDING || result.status == Status.FAILED;
	}

	@Nullable
	private String getUpdateMessage(@Nonnull final String modId) {
		final ModContainer mod = ForgeUtils.findModContainer(modId);
		if (mod == null)
			return null;
		final CheckResult result = ForgeVersion.getResult(mod);
		if (dontPrintMessage(result))
			return null;
		final String t = result.target.toString();
		final String u = result.url.toString();
		return Localization.format(this.messageId, mod.getName(), t, u);
	}

	public void playerLogin(@Nonnull final PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayer) {
			final String updateMessage = getUpdateMessage(this.modId);
			if (updateMessage != null) {
				try {
					final ITextComponent component = ITextComponent.Serializer.jsonToComponent(updateMessage);
					event.player.addChatMessage(component);
				} catch(@Nonnull final Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}
