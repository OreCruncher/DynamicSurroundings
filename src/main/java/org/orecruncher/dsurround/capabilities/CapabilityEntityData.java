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
import org.orecruncher.dsurround.capabilities.entitydata.EntityData;
import org.orecruncher.dsurround.capabilities.entitydata.EntityDataTables;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityData;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityDataSettable;
import org.orecruncher.dsurround.network.Network;
import org.orecruncher.dsurround.network.PacketEntityData;
import org.orecruncher.lib.capability.CapabilityProviderSerializable;
import org.orecruncher.lib.capability.CapabilityUtils;
import org.orecruncher.lib.capability.SimpleStorage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CapabilityEntityData {

	@CapabilityInject(IEntityData.class)
	public static final Capability<IEntityData> ENTITY_DATA = null;
	public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(ModInfo.MOD_ID, "data");

	public static void register() {
		CapabilityManager.INSTANCE.register(IEntityData.class, new SimpleStorage<>(), EntityData::new);
	}

	@Nullable
	public static IEntityData getCapability(@Nonnull final Entity entity) {
		return CapabilityUtils.getCapability(entity, CapabilityEntityData.ENTITY_DATA, null);
	}

	@Nonnull
	public static ICapabilityProvider createProvider(final IEntityData data) {
		return new CapabilityProviderSerializable<>(ENTITY_DATA, null, data);
	}

	@EventBusSubscriber(modid = ModInfo.MOD_ID)
	public static class EventHandler {

		/*
		 * Attach the capability to the Entity when it is created.
		 */
		@SubscribeEvent
		public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof EntityLiving) {
				final EntityData emojiData = new EntityData((EntityLiving) event.getObject());
				event.addCapability(CAPABILITY_ID, createProvider(emojiData));
			}
		}

		/*
		 * Event generated when a player starts tracking an Entity. Need to send an
		 * initial sync to the player.
		 */
		@SubscribeEvent
		public static void trackingEvent(@Nonnull final PlayerEvent.StartTracking event) {
			if (event.getTarget() instanceof EntityLiving) {
				final IEntityData data = event.getTarget().getCapability(ENTITY_DATA, null);
				if (data != null) {
					Network.sendToPlayer((EntityPlayerMP) event.getEntityPlayer(), new PacketEntityData(data));
				}
			}
		}

		/*
		 * Called when an entity is being updated. Need to evaluate new states.
		 */
		@SubscribeEvent()
		public static void livingUpdate(@Nonnull final LivingUpdateEvent event) {
			final Entity entity = event.getEntity();
			final World world = entity.getEntityWorld();
			// Don't tick if this is the client thread. We only check 4 times a
			// second as if that is enough :)
			if (world.isRemote || (entity.ticksExisted % 5) != 0)
				return;
			final IEntityDataSettable data = (IEntityDataSettable) getCapability(event.getEntity());
			if (data != null) {
				EntityDataTables.assess(data);
				data.sync();
			}
		}
	}

}
