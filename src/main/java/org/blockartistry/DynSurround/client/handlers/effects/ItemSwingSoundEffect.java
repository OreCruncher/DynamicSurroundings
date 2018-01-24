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
package org.blockartistry.DynSurround.client.handlers.effects;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.sound.BasicSound;
import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.DynSurround.registry.ItemRegistry;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.effects.EventEffect;
import org.blockartistry.lib.effects.IEventEffectLibraryState;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemSwingSoundEffect extends EventEffect {

	protected final ItemRegistry itemRegistry = RegistryManager.get(RegistryType.ITEMS);

	public ItemSwingSoundEffect(@Nonnull final IEventEffectLibraryState state) {
		super(state);
	}

	// TODO: Investigate to see if there is another way we can detect player swings
	// without needing to route the sound through the server.
	@SubscribeEvent
	public void onItemSwing(@Nonnull final PlayerInteractEvent.LeftClickEmpty event) {
		if (!isClientValid(event))
			return;

		final ItemStack currentItem = event.getEntityPlayer().getHeldItem(event.getHand());
		final SoundEffect soundEffect = this.itemRegistry.getSwingSound(currentItem);
		if (soundEffect != null) {
			final BasicSound<?> fx = this.library.createSound(soundEffect, event.getEntityPlayer());
			fx.setRoutable(true);
			this.library.playSound(fx);
		}
	}

}
