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

package org.blockartistry.mod.DynSurround.server.services;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.data.HealthData;
import org.blockartistry.mod.DynSurround.network.Network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class HealthEffectService {

	protected HealthEffectService() {
	}
	
	public static void initialize() {
		if (ModOptions.enableDamagePopoffs)
			MinecraftForge.EVENT_BUS.register(new HealthEffectService());
	}

	// From the Minecraft code for damage
	// EntityPlayer.attackTargetEntityWithCurrentItem()
	private static boolean isCritical(final EntityPlayer player, final Entity target) {
		return player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater()
				&& !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding()
				&& target instanceof EntityLivingBase && !player.isSprinting();
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHurt(final LivingHurtEvent event) {
		if (event == null || event.getEntity() == null || event.getEntity().worldObj == null
				|| event.getEntity().worldObj.isRemote)
			return;

		// Living heal should handle heals - I think..
		if (event.getAmount() <= 0 || event.getEntityLiving() == null)
			return;

		// A bit hokey - may work.
		boolean isCrit = false;
		if (event.getSource() instanceof EntityDamageSourceIndirect) {
			final EntityDamageSourceIndirect dmgSource = (EntityDamageSourceIndirect) event.getSource();
			if (dmgSource.getSourceOfDamage() instanceof EntityArrow) {
				final EntityArrow arrow = (EntityArrow) dmgSource.getSourceOfDamage();
				isCrit = arrow.getIsCritical();
			}
		} else if (event.getSource() instanceof EntityDamageSource) {
			final EntityDamageSource dmgSource = (EntityDamageSource) event.getSource();
			if (dmgSource.getSourceOfDamage() instanceof EntityPlayer) {
				final EntityPlayer player = (EntityPlayer) dmgSource.getSourceOfDamage();
				isCrit = isCritical(player, event.getEntityLiving());
			}
		}

		final HealthData data = new HealthData(event.getEntityLiving(), isCrit, (int) event.getAmount());
		Network.sendHealthUpdate(data, event.getEntity().worldObj.provider.getDimension());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onLivingHeal(final LivingHealEvent event) {
		if (event == null || event.getEntity() == null || event.getEntity().worldObj == null
				|| event.getEntity().worldObj.isRemote)
			return;

		// Just in case
		if (event.getAmount() <= 0 || event.getEntityLiving() == null
				|| event.getEntityLiving().getHealth() == event.getEntityLiving().getMaxHealth())
			return;

		final HealthData data = new HealthData(event.getEntityLiving(), false, -(int) event.getAmount());
		Network.sendHealthUpdate(data, event.getEntity().worldObj.provider.getDimension());
	}
	
}
