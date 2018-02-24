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
package org.blockartistry.DynSurround.expression;

import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.lib.expression.Dynamic;
import org.blockartistry.lib.expression.DynamicVariantList;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlayerVariables extends DynamicVariantList {

	public PlayerVariables() {
		add(new Dynamic.DynamicBoolean("player.isDead") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isDead;
			}
		});
		add(new Dynamic.DynamicBoolean("player.isHurt") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHurt();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isHungry") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHungry();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isBurning") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBurning();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isSuffocating") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSuffocating();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isFlying") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerFlying();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isSprinting") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSprinting();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isInLava") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInLava();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isInvisible") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInvisible();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isBlind") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBlind();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isInWater") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInWater();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isWet") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isWet();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isUnderwater") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isInsideOfMaterial(Material.WATER);
			}
		});
		add(new Dynamic.DynamicBoolean("player.isRiding") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isRiding();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isOnGround") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.onGround;
			}
		});
		add(new Dynamic.DynamicBoolean("player.isMoving") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerMoving();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isInside") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInside();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isUnderground") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerUnderground();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isInSpace") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInSpace();
			}
		});
		add(new Dynamic.DynamicBoolean("player.isInClouds") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInClouds();
			}
		});
		add(new Dynamic.DynamicString("player.temperature") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerTemperature().getValue();
			}
		});
		add(new Dynamic.DynamicNumber("player.dimension") {
			@Override
			public void update() {
				this.value = EnvironState.getDimensionId();
			}
		});
		add(new Dynamic.DynamicString("player.dimensionName") {
			@Override
			public void update() {
				this.value = EnvironState.getDimensionName();
			}
		});
		add(new Dynamic.DynamicNumber("player.X") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getX();
			}
		});
		add(new Dynamic.DynamicNumber("player.Y") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getY();
			}
		});
		add(new Dynamic.DynamicNumber("player.Z") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getZ();
			}
		});
		add(new Dynamic.DynamicNumber("player.health") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getHealth() : Integer.MAX_VALUE;
			}
		});
		add(new Dynamic.DynamicNumber("player.maxHealth") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getMaxHealth() : Integer.MAX_VALUE;
			}
		});
		add(new Dynamic.DynamicNumber("player.luck") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getLuck() : 0;
			}
		});
		add(new Dynamic.DynamicNumber("player.food.saturation") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getFoodStats().getSaturationLevel() : 0;
			}
		});
		add(new Dynamic.DynamicNumber("player.food.level") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getFoodStats().getFoodLevel() : 0;
			}
		});
		add(new Dynamic.DynamicBoolean("player.canRainOn") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				if (world != null) {
					final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
					this.value = world.canBlockSeeSky(pos)
							&& !(world.getTopSolidOrLiquidBlock(pos).getY() > pos.getY());
				} else {
					this.value = false;
				}
			}
		});
		add(new Dynamic.DynamicBoolean("player.canSeeSky") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				if (world != null) {
					final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
					this.value = world.canBlockSeeSky(pos);
				} else {
					this.value = false;
				}
			}
		});
		add(new Dynamic.DynamicBoolean("player.inBoat") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.getRidingEntity() instanceof EntityBoat;
			}
		});
		add(new Dynamic.DynamicNumber("player.lightLevel") {
			@Override
			public void update() {
				this.value = EnvironState.getLightLevel();
			}
		});
		add(new Dynamic.DynamicString("player.armor") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerArmorClass().getClassName();
			}
		});
		add(new Dynamic.DynamicBoolean("player.inVillage") {
			@Override
			public void update() {
				this.value = EnvironState.inVillage();
			}
		});

	}
}
