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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.client.swing.DiagnosticPanel;
import org.blockartistry.mod.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.mod.DynSurround.registry.TemperatureRating;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;
import org.blockartistry.mod.DynSurround.util.Expression;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Calculates and caches predefined sets of data points for script evaluation
 * during a tick. Goal is to minimize script evaluation overhead as much as
 * possible.
 */
@SideOnly(Side.CLIENT)
public class ExpressionStateHandler extends EffectHandlerBase {

	public abstract static class DynamicVariable extends Expression.Variant implements Comparable<DynamicVariable> {

		protected static final float TRUE = Expression.TRUE.asNumber();
		protected static final float FALSE = Expression.FALSE.asNumber();

		protected final String name;

		public DynamicVariable(@Nonnull final String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public int compareTo(@Nonnull final DynamicVariable var) {
			return this.name.compareTo(var.name);
		}

		public abstract void update();
	}

	private static final List<DynamicVariable> variables = new ArrayList<DynamicVariable>();

	public static List<DynamicVariable> getVariables() {
		return variables;
	}

	private static void register(@Nonnull final DynamicVariable variable) {
		variables.add(variable);
		Expression.addBuiltInVariable(variable.getName(), variable);
	}

	static {
		register(new DynamicVariable("isDay") {
			@Override
			public void update() {
				this.floatVal = DiurnalUtils.isDaytime(EnvironState.getWorld()) ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("isNight") {
			@Override
			public void update() {
				this.floatVal = DiurnalUtils.isNighttime(EnvironState.getWorld()) ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("isSunrise") {
			@Override
			public void update() {
				this.floatVal = DiurnalUtils.isSunrise(EnvironState.getWorld()) ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("isSunset") {
			@Override
			public void update() {
				this.floatVal = DiurnalUtils.isSunset(EnvironState.getWorld()) ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("isAuroraVisible") {
			@Override
			public void update() {
				this.floatVal = DiurnalUtils.isAuroraVisible(EnvironState.getWorld()) ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("moonPhaseFactor") {
			@Override
			public void update() {
				this.floatVal = DiurnalUtils.getMoonPhaseFactor(EnvironState.getWorld());
			}
		});
		register(new DynamicVariable("hasSky") {
			@Override
			public void update() {
				this.floatVal = !EnvironState.getWorld().provider.getHasNoSky() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("season") {
			@Override
			public void update() {
				this.value = EnvironState.getSeason().getValue();
			}
		});

		// Biome variables
		register(new DynamicVariable("biome.name") {
			@Override
			public void update() {
				this.value = EnvironState.getBiomeName();
			}
		});
		register(new DynamicVariable("biome.temperature") {
			@Override
			public void update() {
				this.value = EnvironState.getBiomeTemperature().getValue();
			}
		});
		register(new DynamicVariable("biome.rainfall") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayerBiome().getRainfall();
			}
		});
		register(new DynamicVariable("biome.temperatureValue") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayerBiome().getTemperature();
			}
		});
		register(new DynamicVariable("biome.isFoggy") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isFoggy() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("biome.isHumid") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isHumid() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("biome.isDry") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isDry() ? TRUE : FALSE;
			}
		});

		// Player variables
		register(new DynamicVariable("player.isHurt") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerHurt() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isHungry") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerHungry() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isBurning") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerBurning() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isSuffocating") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerSuffocating() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isFlying") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerFlying() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isSprinting") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerSprinting() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isInLava") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerInLava() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isInvisible") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerInvisible() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isBlind") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerBlind() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isInWater") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerInWater() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isWet") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.floatVal = player.isWet() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isUnderwater") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.floatVal = player.isInsideOfMaterial(Material.WATER) ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isRiding") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.floatVal = player.isRiding() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isOnGround") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.floatVal = player.onGround ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isMoving") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.floatVal = player.distanceWalkedModified != player.prevDistanceWalkedModified ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isInside") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerInside() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isUnderground") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerUnderground() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isInSpace") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerInSpace() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.isInClouds") {
			@Override
			public void update() {
				this.floatVal = EnvironState.isPlayerInClouds() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.temperature") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerTemperature().getValue();
			}
		});
		register(new DynamicVariable("player.dimension") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getDimensionId();
			}
		});
		register(new DynamicVariable("player.dimensionName") {
			@Override
			public void update() {
				this.value = EnvironState.getDimensionName();
			}
		});
		register(new DynamicVariable("player.X") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayerPosition().getX();
			}
		});
		register(new DynamicVariable("player.Y") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayerPosition().getY();
			}
		});
		register(new DynamicVariable("player.Z") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayerPosition().getZ();
			}
		});
		register(new DynamicVariable("player.health") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayer().getHealth();
			}
		});
		register(new DynamicVariable("player.maxHealth") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayer().getMaxHealth();
			}
		});
		register(new DynamicVariable("player.luck") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayer().getLuck();
			}
		});
		register(new DynamicVariable("player.food.saturation") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayer().getFoodStats().getSaturationLevel();
			}
		});
		register(new DynamicVariable("player.food.level") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayer().getFoodStats().getFoodLevel();
			}
		});
		register(new DynamicVariable("player.canRainOn") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
				this.floatVal = world.canBlockSeeSky(pos) && !(world.getTopSolidOrLiquidBlock(pos).getY() > pos.getY())
						? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.canSeeSky") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
				this.floatVal = world.canBlockSeeSky(pos) ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.inBoat") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayer().getRidingEntity() instanceof EntityBoat ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("player.lightLevel") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				final BlockPos pos = EnvironState.getPlayerPosition();
				final int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, pos);
				final int skyLight = world.getLightFor(EnumSkyBlock.SKY, pos) - world.calculateSkylightSubtracted(1.0F);
				this.floatVal = Math.max(blockLight, skyLight);
			}
		});

		// Weather variables
		register(new DynamicVariable("weather.isRaining") {
			@Override
			public void update() {
				this.floatVal = WeatherProperties.getIntensityLevel() > 0.0F ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("weather.isThundering") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getWorld().isThundering() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("weather.isNotRaining") {
			@Override
			public void update() {
				this.floatVal = WeatherProperties.getIntensityLevel() <= 0.0F ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("weather.isNotThundering") {
			@Override
			public void update() {
				this.floatVal = !EnvironState.getWorld().isThundering() ? TRUE : FALSE;
			}
		});
		register(new DynamicVariable("weather.rainfall") {
			@Override
			public void update() {
				this.floatVal = WeatherProperties.getIntensityLevel();
			}
		});
		register(new DynamicVariable("weather.temperatureValue") {
			@Override
			public void update() {
				this.floatVal = EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition());
			}
		});
		register(new DynamicVariable("weather.temperature") {
			@Override
			public void update() {
				this.value = TemperatureRating
						.fromTemp(EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition()))
						.getValue();
			}
		});

		// Sort them for easy display
		Collections.sort(variables);
	}

	@Override
	@Nonnull
	public String getHandlerName() {
		return "ExpressionStateHandler";
	}

	@Override
	public void process(@Nonnull final World world, @Nonnull final EntityPlayer player) {

		// Iterate through the variables and get the data cached for this ticks
		// expression evaluations.
		for (final DynamicVariable dv : variables)
			dv.update();

		if (ModOptions.showDebugDialog)
			DiagnosticPanel.refresh();
	}

	@Override
	public void onConnect() {
		if (ModOptions.showDebugDialog)
			DiagnosticPanel.create();
	}

	@Override
	public void onDisconnect() {
		if (ModOptions.showDebugDialog)
			DiagnosticPanel.destroy();
	}

}
