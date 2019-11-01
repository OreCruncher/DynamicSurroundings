/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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
package org.orecruncher.dsurround.expression;

import org.orecruncher.dsurround.client.handlers.EnvironStateHandler.EnvironState;
import org.orecruncher.lib.expression.Dynamic;
import org.orecruncher.lib.expression.DynamicVariantList;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlayerVariables extends DynamicVariantList {

	public PlayerVariables() {
		add(new Dynamic.DynamicBoolean("player.isHurt", () -> EnvironState.isPlayerHurt()));
		add(new Dynamic.DynamicBoolean("player.isHungry", () -> EnvironState.isPlayerHungry()));
		add(new Dynamic.DynamicBoolean("player.isBurning", () -> EnvironState.isPlayerBurning()));
		add(new Dynamic.DynamicBoolean("player.isSuffocating", () -> EnvironState.isPlayerSuffocating()));
		add(new Dynamic.DynamicBoolean("player.isFlying", () -> EnvironState.isPlayerFlying()));
		add(new Dynamic.DynamicBoolean("player.isSprinting", () -> EnvironState.isPlayerSprinting()));
		add(new Dynamic.DynamicBoolean("player.isInLava", () -> EnvironState.isPlayerInLava()));
		add(new Dynamic.DynamicBoolean("player.isInvisible", () -> EnvironState.isPlayerInvisible()));
		add(new Dynamic.DynamicBoolean("player.isBlind", () -> EnvironState.isPlayerBlind()));
		add(new Dynamic.DynamicBoolean("player.isInWater", () -> EnvironState.isPlayerInWater()));
		add(new Dynamic.DynamicBoolean("player.isMoving", () -> EnvironState.isPlayerMoving()));
		add(new Dynamic.DynamicBoolean("player.isInside", () -> EnvironState.isPlayerInside()));
		add(new Dynamic.DynamicBoolean("player.isUnderground", () -> EnvironState.isPlayerUnderground()));
		add(new Dynamic.DynamicBoolean("player.isInSpace", () -> EnvironState.isPlayerInSpace()));
		add(new Dynamic.DynamicBoolean("player.isInClouds", () -> EnvironState.isPlayerInClouds()));
		add(new Dynamic.DynamicString("player.temperature", () -> EnvironState.getPlayerTemperature().getValue()));
		add(new Dynamic.DynamicBoolean("player.inVillage", () -> EnvironState.inVillage()));
		add(new Dynamic.DynamicNumber("player.X", () -> (float) EnvironState.getPlayerPosition().getX()));
		add(new Dynamic.DynamicNumber("player.Y", () -> (float) EnvironState.getPlayerPosition().getY()));
		add(new Dynamic.DynamicNumber("player.Z", () -> (float) EnvironState.getPlayerPosition().getZ()));
		add(new Dynamic.DynamicNumber("player.lightLevel", () -> (float) EnvironState.getLightLevel()));

		add(new Dynamic.DynamicBoolean("player.isDead", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null && player.isDead;
		}));
		add(new Dynamic.DynamicBoolean("player.isWet", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null && player.isWet();
		}));
		add(new Dynamic.DynamicBoolean("player.isUnderwater", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null && player.isInsideOfMaterial(Material.WATER);
		}));
		add(new Dynamic.DynamicBoolean("player.isRiding", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null && player.isRiding();
		}));
		add(new Dynamic.DynamicBoolean("player.isOnGround", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null && player.onGround;
		}));
		add(new Dynamic.DynamicNumber("player.health", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null ? player.getHealth() : Integer.MAX_VALUE;
		}));
		add(new Dynamic.DynamicNumber("player.maxHealth", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null ? player.getMaxHealth() : Integer.MAX_VALUE;
		}));
		add(new Dynamic.DynamicNumber("player.luck", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null ? player.getLuck() : 0;
		}));
		add(new Dynamic.DynamicNumber("player.food.saturation", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null ? player.getFoodStats().getSaturationLevel() : 0;
		}));
		add(new Dynamic.DynamicNumber("player.food.level", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null ? (float) player.getFoodStats().getFoodLevel() : 0;
		}));

		add(new Dynamic.DynamicBoolean("player.canRainOn", () -> {
			final World world = EnvironState.getWorld();
			if (world != null) {
				final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
				return world.canBlockSeeSky(pos) && !(world.getTopSolidOrLiquidBlock(pos).getY() > pos.getY());
			}
			return false;
		}));
		add(new Dynamic.DynamicBoolean("player.canSeeSky", () -> {
			final World world = EnvironState.getWorld();
			if (world != null) {
				final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
				return world.canBlockSeeSky(pos);
			}
			return false;
		}));
		add(new Dynamic.DynamicBoolean("player.inBoat", () -> {
			final EntityPlayer player = EnvironState.getPlayer();
			return player != null && player.getRidingEntity() instanceof EntityBoat;
		}));
	}
}
