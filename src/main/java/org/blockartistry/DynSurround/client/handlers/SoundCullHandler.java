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

package org.blockartistry.DynSurround.client.handlers;

import java.util.ArrayList;
import java.util.List;

import org.blockartistry.DynSurround.ModLog;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.event.RegistryEvent;
import org.blockartistry.DynSurround.client.gui.ConfigSound;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class SoundCullHandler extends EffectHandlerBase {

	private final List<String> soundsToBlock = new ArrayList<String>();
	private final TObjectIntHashMap<String> soundCull = new TObjectIntHashMap<String>();

	public SoundCullHandler() {
	}

	@Override
	public String getHandlerName() {
		return "SoundCullHandler";
	}

	@Override
	public void process(final World world, final EntityPlayer player) {

	}

	@Override
	public void onConnect() {
		this.soundsToBlock.clear();
		this.soundCull.clear();
		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		for (final Object resource : handler.soundRegistry.getKeys()) {
			final String rs = resource.toString();
			if (getSoundRegistry().isSoundBlocked(rs)) {
				ModLog.debug("Blocking sound '%s'", rs);
				this.soundsToBlock.add(rs);
			} else if (getSoundRegistry().isSoundCulled(rs)) {
				ModLog.debug("Culling sound '%s'", rs);
				this.soundCull.put(rs, -ModOptions.soundCullingThreshold);
			}
		}
	}

	@SubscribeEvent
	public void soundConfigReload(final RegistryEvent.Reload event) {
		if (event.getSide() == Side.CLIENT && getSoundRegistry() != null)
			onConnect();
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void soundEvent(final PlaySoundEvent event) {
		if (event.getSound() == null || event.getSound() instanceof ConfigSound)
			return;
		
		final String resource = event.getSound().getSoundLocation().toString();
		if (this.soundsToBlock.contains(resource)) {
			event.setResultSound(null);
			return;
		}

		if (ModOptions.soundCullingThreshold <= 0)
			return;

		// Get the last time the sound was seen
		final int lastOccurance = this.soundCull.get(resource);
		if (lastOccurance == 0)
			return;

		final int currentTick = EnvironState.getTickCounter();
		if ((currentTick - lastOccurance) < ModOptions.soundCullingThreshold) {
			event.setResultSound(null);
		} else {
			this.soundCull.put(resource, currentTick);
		}
	}
}
