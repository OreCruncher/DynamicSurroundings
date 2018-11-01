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

package org.orecruncher.dsurround.client.handlers;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.dsurround.client.sound.BasicSound;
import org.orecruncher.dsurround.client.sound.Sounds;
import org.orecruncher.dsurround.client.weather.Weather;
import org.orecruncher.dsurround.event.DiagnosticEvent;
import org.orecruncher.dsurround.event.ThunderEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WeatherHandler extends EffectHandlerBase {

	private int timer = 0;

	public WeatherHandler() {
		super("Weather");
	}

	@Override
	public boolean doTick(final int tick) {
		return this.timer > 0;
	}

	@Override
	public void onConnect() {
		this.timer = 0;
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		this.timer--;
		player.getEntityWorld().setLastLightningBolt(2);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onThunderEvent(@Nonnull final ThunderEvent event) {
		if (!ModOptions.rain.allowBackgroundThunder || EnvironState.getDimensionId() != event.dimId)
			return;

		final BasicSound<?> thunder = Sounds.THUNDER.createSoundAt(event.location)
				.setVolume(ModOptions.sound.thunderVolume);
		SoundEffectHandler.INSTANCE.playSound(thunder);

		if (event.doFlash)
			this.timer = 2;

	}

	@SubscribeEvent
	public void diagnostic(@Nonnull final DiagnosticEvent.Gather event) {
		event.output.add(Weather.diagnostic());
	}

}
