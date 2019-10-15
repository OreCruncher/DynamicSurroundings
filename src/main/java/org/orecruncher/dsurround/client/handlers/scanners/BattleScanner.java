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

package org.orecruncher.dsurround.client.handlers.scanners;

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilityEntityData;
import org.orecruncher.dsurround.capabilities.entitydata.IEntityData;
import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*
 * Scans the entities in the immediate vicinity to determine if
 * a battle is taking place.  This does not mean the player is
 * being attacked - only that there are entities that are
 * fighting nearby.
 */
@SideOnly(Side.CLIENT)
public class BattleScanner implements ITickable {

	private static final String DEBUG_NOT_ENABLED = TextFormatting.RED + "<BATTLE MUSIC NOT ENABLED>";
	
	private static final int BOSS_RANGE = 65536; // 256 block range
	private static final int MINI_BOSS_RANGE = 16384; // 128 block range
	private static final int MOB_RANGE = 400; // 20 block range
	private static final int BATTLE_TIMER_EXPIRY = 10;

	protected int battleTimer;
	protected boolean inBattle;
	protected boolean isWither;
	protected boolean isDragon;
	protected boolean isBoss;
	
	public void reset() {
		this.inBattle = false;
		this.isWither = false;
		this.isDragon = false;
		this.isBoss = false;
	}

	public boolean inBattle() {
		return this.inBattle;
	}

	public boolean isWither() {
		return this.isWither;
	}

	public boolean isDragon() {
		return this.isDragon;
	}

	public boolean isBoss() {
		return this.isBoss;
	}
	
	private boolean isApplicableType(final Entity e) {
		if (e instanceof IMob)
			return true;
		if (e instanceof EntityPlayer)
			return true;
		if (e instanceof EntityGolem)
			return true;
		if (e instanceof EntityPolarBear)
			return true;
		return false;
	}

	@Override
	public void update() {

		if (!ModOptions.sound.enableBattleMusic) {
			this.inBattle = this.isBoss = this.isDragon = this.isWither = false;
			return;
		}

		final EntityPlayer player = EnvironState.getPlayer();
		final BlockPos playerPos = EnvironState.getPlayerPosition();
		final World world = EnvironState.getWorld();

		boolean inBattle = false;
		boolean isBoss = false;
		boolean isDragon = false;
		boolean isWither = false;

		List<Entity> entities = world.getLoadedEntityList();
		for (final Entity e : entities) {

			// The player isn't a candidate
			if (e == player)
				continue;
			
			// Gotta be the right type of entity. Do this first
			// to filter out all the animals.
			if (!isApplicableType(e))
				continue;

			// Has to be within a snowballs chance. Boss range is
			// the farthest range.
			final double dist = e.getDistanceSq(playerPos);
			if (dist > BOSS_RANGE)
				continue;

			// Invisible things do not trigger as well as the current
			// player and team members.
			if (e.isInvisible() || e.isOnSameTeam(player))
				continue;

			if (!e.isNonBoss()) {
				if (e instanceof EntityWither) {
					inBattle = isWither = isBoss = true;
					isDragon = false;
					// Wither will override *any* other mob
					// so terminate early.
					break;
				} else if (e instanceof EntityDragon) {
					inBattle = isDragon = isBoss = true;
				} else if (dist <= MINI_BOSS_RANGE) {
					inBattle = isBoss = true;
				}
			} else if (inBattle || dist > MOB_RANGE) {
				// If we are flagged to be in battle or if the normal
				// mob is outside of the mob range range it is
				// not a candidate.
				continue;
			} else {
				// Use entity data to determine if the mob is attacking
				final IEntityData data = e.getCapability(CapabilityEntityData.ENTITY_DATA, null);
				if (data != null && data.isAttacking()) {
					// Only in battle if the entity sees the player, or the
					// player sees the entity
					final EntityLiving living = (EntityLiving) e;
					if (living.getEntitySenses().canSee(player) || player.canEntityBeSeen(living))
						inBattle = true;
				}
			}
		}

		final int tickCounter = EnvironState.getTickCounter();

		if (inBattle) {
			this.inBattle = inBattle;
			this.isBoss = isBoss;
			this.isWither = isWither;
			this.isDragon = isDragon;
			this.battleTimer = tickCounter + BATTLE_TIMER_EXPIRY;
		} else if (this.inBattle && tickCounter > this.battleTimer) {
			reset();
		}
	}
	
	@Override
	@Nonnull
	public String toString() {
		
		if (!ModOptions.sound.enableBattleMusic) {
			return DEBUG_NOT_ENABLED;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatting.RED);
		builder.append("BattleScanner inBattle:").append(this.inBattle).append(';');
		builder.append(" isBoss:").append(this.isBoss).append(';');
		builder.append(" isWither:").append(this.isWither).append(';');
		builder.append(" isDragon:").append(this.isDragon).append(';');
		return builder.toString();
	}

}
