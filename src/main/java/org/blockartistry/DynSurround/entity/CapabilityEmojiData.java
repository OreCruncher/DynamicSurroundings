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

package org.blockartistry.DynSurround.entity;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.api.entity.ActionState;
import org.blockartistry.DynSurround.api.entity.EmojiType;
import org.blockartistry.DynSurround.api.entity.EmotionalState;
import org.blockartistry.DynSurround.api.entity.IEmojiData;
import org.blockartistry.DynSurround.network.Network;
import org.blockartistry.DynSurround.network.PacketEntityEmote;
import org.blockartistry.lib.capability.CapabilityProviderSerializable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityEmojiData {

	@CapabilityInject(IEmojiData.class)
	public static final Capability<IEmojiData> EMOJI = null;
	public static final EnumFacing DEFAULT_FACING = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(DSurround.MOD_ID, "emojiData");

	public static void register() {
		CapabilityManager.INSTANCE.register(IEmojiData.class, new Capability.IStorage<IEmojiData>() {
			@Override
			public NBTBase writeNBT(@Nonnull final Capability<IEmojiData> capability,
					@Nonnull final IEmojiData instance, @Nullable final EnumFacing side) {
				final NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("as", instance.getActionState().ordinal());
				nbt.setInteger("et", instance.getEmojiType().ordinal());
				nbt.setInteger("es", instance.getEmotionalState().ordinal());
				return nbt;
			}

			@Override
			public void readNBT(@Nonnull final Capability<IEmojiData> capability, @Nonnull final IEmojiData instance,
					@Nullable final EnumFacing side, @Nonnull final NBTBase nbt) {
				final NBTTagCompound data = (NBTTagCompound) nbt;
				final IEmojiDataSettable settable = (IEmojiDataSettable) instance;
				settable.setActionState(ActionState.get(data.getInteger("as")));
				settable.setEmojiType(EmojiType.get(data.getInteger("et")));
				settable.setEmotionalState(EmotionalState.get(data.getInteger("es")));
				settable.clearDirty();
			}
		}, new Callable<IEmojiData>() {
			@Override
			public IEmojiData call() throws Exception {
				return new EmojiData(null);
			}
		});
	}

	@Nonnull
	public static ICapabilityProvider createProvider(final IEmojiData data) {
		return new CapabilityProviderSerializable<IEmojiData>(EMOJI, DEFAULT_FACING, data);
	}

	public static class EventHandler {
		
		/*
		 * Attach the capability to the Entity when it is created.
		 */
		@SubscribeEvent
		public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof EntityLivingBase) {
				final EmojiData emojiData = new EmojiData(event.getObject());
				event.addCapability(CAPABILITY_ID, createProvider(emojiData));
			}
		}

		/*
		 * Event generated when a player starts tracking an Entity. Need to send
		 * an initial sync to the player.
		 */
		@SubscribeEvent
		public static void trackingEvent(@Nonnull final PlayerEvent.StartTracking event) {
			final IEmojiData data = event.getTarget().getCapability(EMOJI, DEFAULT_FACING);
			if (data != null) {
				Network.sendToPlayer((EntityPlayerMP) event.getEntityPlayer(), new PacketEntityEmote(data));
			}
		}
	}

}
