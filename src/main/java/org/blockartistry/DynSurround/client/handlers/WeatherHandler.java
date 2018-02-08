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

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.api.events.ThunderEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.sound.Sounds;
import org.blockartistry.DynSurround.client.weather.Weather;
import org.blockartistry.DynSurround.event.DiagnosticEvent;
import org.blockartistry.lib.sound.BasicSound;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WeatherHandler extends EffectHandlerBase {
 
	private int timer = 0;

	public WeatherHandler() {
		super("WeatherHandler");
	}
	
	@Override
	public void process(@Nonnull final EntityPlayer player) {
		if (this.timer > 0) {
			this.timer--;
			EnvironState.getWorld().setLastLightningBolt(2);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onThunderEvent(@Nonnull final ThunderEvent event) {
		if (!ModOptions.rain.allowBackgroundThunder)
			return;

		final BasicSound<?> thunder = Sounds.THUNDER.createSound(event.location).setVolume(ModOptions.sound.thunderVolume);
		SoundEffectHandler.INSTANCE.playSound(thunder);

		if (event.doFlash)
			this.timer = 2;

	}
	
	@SubscribeEvent
	public void diagnostic(@Nonnull final DiagnosticEvent.Gather event) {
		event.output.add(Weather.diagnostic());
	}

}
