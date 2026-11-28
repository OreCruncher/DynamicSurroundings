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
package org.orecruncher.dsurround.client.handlers.effects;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.effects.EventEffect;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.sound.ISoundInstance;
import org.orecruncher.dsurround.client.sound.Sounds;

import com.google.common.collect.ImmutableSet;

import net.minecraft.inventory.Container;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CraftingSoundEffect extends EventEffect {

	// Crafting screens of other mods that play a sound of their own when something is made (Tinkers' tool station
	// and tool forge, whose crafting also fires the crafting event); a subclass counts too
	private static final Set<String> OWN_SOUND = ImmutableSet.of(
			"slimeknights.tconstruct.tools.common.inventory.ContainerToolStation");

	private int craftSoundThrottle = 0;

	static boolean playsItsOwnSound(@Nullable final Container container) {
		for (Class<?> c = container == null ? null : container.getClass(); c != null; c = c.getSuperclass())
			if (OWN_SOUND.contains(c.getName()))
				return true;
		return false;
	}

	@SubscribeEvent
	public void onEvent(@Nonnull final ItemCraftedEvent event) {
		if (!ModOptions.sound.enableCraftingSound || !isClientValid(event))
			return;

		if (this.craftSoundThrottle >= (EnvironState.getTickCounter() - 30))
			return;

		if (playsItsOwnSound(event.player.openContainer))
			return;

		this.craftSoundThrottle = EnvironState.getTickCounter();
		final ISoundInstance fx = getState().createSound(Sounds.CRAFTING, event.player);
		getState().playSound(fx);
	}

}
