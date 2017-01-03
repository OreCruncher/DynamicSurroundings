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

package org.blockartistry.mod.DynSurround.client.handlers;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.DSurround;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.api.events.ThunderEvent;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.SoundUtils;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WeatherHandler extends EffectHandlerBase {

	private static final SoundEvent THUNDER = SoundUtils
			.getOrRegisterSound(new ResourceLocation(DSurround.RESOURCE_ID, "thunder"));

	private int timer = 0;

	@Override
	public String getHandlerName() {
		return "WeatherHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {
		if (this.timer > 0) {
			this.timer--;
			EnvironState.getWorld().setLastLightningBolt(2);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public void onThunderEvent(@Nonnull final ThunderEvent event) {
		if (ModOptions.stormThunderThreshold > 1.0F)
			return;

		final ISound thunder = new PositionedSoundRecord(THUNDER, SoundCategory.WEATHER, 10000.0F, 1.0F,
				event.location.getX(), event.location.getY(), event.location.getZ());
		SoundEffectHandler.INSTANCE.playSound(thunder);

		if (event.doFlash)
			this.timer = 2;

	}

}
