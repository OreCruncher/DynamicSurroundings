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
package org.blockartistry.lib.effects;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.client.sound.SoundEffect;
import org.blockartistry.lib.sound.BasicSound;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The EventEffectLibrary is the focal point of EventEffect management. It is
 * responsible for registration and tear down of associated events as needed.
 *
 */
@SideOnly(Side.CLIENT)
public class EventEffectLibrary extends EffectStateBase implements IEventEffectLibraryState {

	protected final List<EventEffect> effects = new ArrayList<EventEffect>();

	public EventEffectLibrary(@Nonnull final IParticleHelper ph, @Nonnull final ISoundHelper sh) {
		super(ph, sh);
	}

	/**
	 * Registers the EventEffect with the EventEffectLibrary. The reference will
	 * automatically be registered with Forge, and will be tracked.
	 * 
	 * @param effect
	 *            EventEffect instance to register
	 */
	public void register(@Nonnull final EventEffect effect) {
		this.effects.add(effect);
		MinecraftForge.EVENT_BUS.register(effect);
	}

	/**
	 * Unregisters all EventEffects that have been registered prior to going out of
	 * scope.
	 */
	public void cleanup() {
		for (final EventEffect e : this.effects)
			MinecraftForge.EVENT_BUS.unregister(e);
		this.effects.clear();
	}

	/**
	 * Indicates if the specified player is the one sitting behind the screen.
	 * 
	 * @param player
	 *            The EntityPlayer to check
	 * @return true if it is the local player, false otherwise
	 */
	@Override
	public boolean isActivePlayer(@Nonnull final Entity player) {
		final EntityPlayer ep = Minecraft.getMinecraft().player;
		return ep != null && ep.getEntityId() == player.getEntityId();
	}

	/**
	 * Creates a BasicSound<> object for the specified SoundEffect centered at the
	 * Entity. If the Entity is the current active player the sound will be
	 * non-attenuated.
	 * 
	 * @param se
	 *            SoundEffect to use as the basis of the sound
	 * @param player
	 *            The player location of where the sound will be generated
	 * @return A BasicSound<?> with applicable properties set
	 */
	@Override
	@Nonnull
	public BasicSound<?> createSound(@Nonnull final SoundEffect se, @Nonnull final Entity player) {
		if (this.isActivePlayer(player))
			return se.createSound(player, false);
		return se.createSound(player);
	}

}
