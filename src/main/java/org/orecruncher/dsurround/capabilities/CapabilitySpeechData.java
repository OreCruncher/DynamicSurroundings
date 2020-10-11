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

package org.orecruncher.dsurround.capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.capabilities.speech.ISpeechData;
import org.orecruncher.dsurround.capabilities.speech.SpeechData;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.capability.CapabilityProviderSerializable;
import org.orecruncher.lib.capability.CapabilityUtils;
import org.orecruncher.lib.capability.NullStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class CapabilitySpeechData {

	@CapabilityInject(ISpeechData.class)
	public static final Capability<ISpeechData> SPEECH_DATA = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ModInfo.MOD_ID, "speech");

	@SideOnly(Side.CLIENT)
	public static void register() {
		CapabilityManager.INSTANCE.register(ISpeechData.class, new NullStorage<>(), SpeechData::new);
	}

	@SideOnly(Side.CLIENT)
	@Nullable
	public static ISpeechData getCapability(@Nonnull final Entity entity) {
		return CapabilityUtils.getCapability(entity, SPEECH_DATA, null);
	}

	@SideOnly(Side.CLIENT)
	@Nonnull
	public static ICapabilityProvider createProvider(final ISpeechData data) {
		return new CapabilityProviderSerializable<>(SPEECH_DATA, null, data);
	}

	@SideOnly(Side.CLIENT)
	public static boolean isCandidate(@Nonnull final Entity entity) {
		return entity instanceof EntityPlayer || entity instanceof EntityLiving;
	}

	@SideOnly(Side.CLIENT)
	public static boolean isCandidate(@Nullable final Class<? extends Entity> clazz) {
		return clazz != null
				&& (EntityPlayer.class.isAssignableFrom(clazz) || EntityLiving.class.isAssignableFrom(clazz));
	}

	@EventBusSubscriber(modid = ModInfo.MOD_ID, value = Side.CLIENT)
	public static class EventHandler {

		/*
		 * Attach the capability to the Entity when it is created. This only applies
		 * client side. We are using the capability system to attach client data to an
		 * entity.
		 */
		@SubscribeEvent
		public static void attachCapabilities(@Nonnull final AttachCapabilitiesEvent<Entity> event) {
			final World world = event.getObject().getEntityWorld();
			if (world != null && world.isRemote && isCandidate(event.getObject())) {
				final SpeechData speechData = new SpeechData();
				event.addCapability(CAPABILITY_ID, createProvider(speechData));
			}
		}

		/*
		 * Called when an entity is being updated. Need to update the state of the
		 * speech data.
		 */
		@SubscribeEvent()
		public static void livingUpdate(@Nonnull final LivingUpdateEvent event) {
			final Entity entity = event.getEntity();
			final World world = entity.getEntityWorld();
			// Don't tick if this is the client thread. We only check 4 times a
			// second as if that is enough :)
			if (!world.isRemote || (entity.ticksExisted % 5) != 0)
				return;
			final ISpeechData data = getCapability(event.getEntity());
			if (data != null) {
				data.onUpdate(EnvironState.getTickCounter());
			}
		}
	}
}
