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
import org.blockartistry.mod.DynSurround.client.storm.StormProperties;
import org.blockartistry.mod.DynSurround.client.swing.DiagnosticPanel;
import org.blockartistry.mod.DynSurround.registry.TemperatureRating;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;
import org.blockartistry.mod.DynSurround.util.Expression;
import org.blockartistry.mod.DynSurround.util.Expression.Variant;

import net.minecraft.entity.player.EntityPlayer;
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

	public abstract static class DynamicVariable implements Comparable<DynamicVariable>, Expression.LazyVariant {

		protected final String name;
		protected Variant value;

		public DynamicVariable(@Nonnull final String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public Variant eval() {
			return this.value;
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
				this.value = DiurnalUtils.isDaytime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("isNight") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isNighttime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("isSunrise") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunrise(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("isSunset") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunset(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("isAuroraVisible") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isAuroraVisible(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("moonPhaseFactor") {
			@Override
			public void update() {
				this.value = new Variant(DiurnalUtils.getMoonPhaseFactor(EnvironState.getWorld()));
			}
		});
		register(new DynamicVariable("hasSky") {
			@Override
			public void update() {
				this.value = !EnvironState.getWorld().provider.getHasNoSky() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("season") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getSeason().getValue());
			}
		});

		// Biome variables
		register(new DynamicVariable("biome.name") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getBiomeName());
			}
		});
		register(new DynamicVariable("biome.temperature") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getBiomeTemperature().getValue());
			}
		});
		register(new DynamicVariable("biome.rainfall") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayerBiome().getRainfall());
			}
		});
		register(new DynamicVariable("biome.temperatureValue") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayerBiome().getTemperature());
			}
		});
		register(new DynamicVariable("biome.isFoggy") {
			@Override
			public void update() {
				this.value = EnvironState.isFoggy() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("biome.isHumid") {
			@Override
			public void update() {
				this.value = EnvironState.isHumid() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("biome.isDry") {
			@Override
			public void update() {
				this.value = EnvironState.isDry() ? Expression.ONE : Expression.ZERO;
			}
		});

		// Player variables
		register(new DynamicVariable("player.isHurt") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHurt() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isHungry") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHungry() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isBurning") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBurning() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isSuffocating") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSuffocating() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isFlying") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerFlying() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isSprinting") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSprinting() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isInLava") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInLava() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isInvisible") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInvisible() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isBlind") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBlind() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isInWater") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInWater() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isRiding") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerRiding() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isOnGround") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerOnGround() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isMoving") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerMoving() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isInside") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInside() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isUnderground") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerUnderground() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isInSpace") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInSpace() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.isInClouds") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInClouds() ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("player.temperature") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayerTemperature().getValue());
			}
		});
		register(new DynamicVariable("player.dimension") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getDimensionId());
			}
		});
		register(new DynamicVariable("player.dimensionName") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getDimensionName());
			}
		});
		register(new DynamicVariable("player.Y") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayerPosition().getY());
			}
		});
		register(new DynamicVariable("player.health") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getHealth());
			}
		});
		register(new DynamicVariable("player.maxHealth") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getMaxHealth());
			}
		});
		register(new DynamicVariable("player.luck") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getLuck());
			}
		});
		register(new DynamicVariable("player.food.saturation") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getFoodStats().getSaturationLevel());
			}
		});
		register(new DynamicVariable("player.food.level") {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getFoodStats().getFoodLevel());
			}
		});

		// Weather variables
		register(new DynamicVariable("weather.isRaining") {
			@Override
			public void update() {
				this.value = StormProperties.getIntensityLevel() > 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("weather.isNotRaining") {
			@Override
			public void update() {
				this.value = StormProperties.getIntensityLevel() <= 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		register(new DynamicVariable("weather.rainfall") {
			@Override
			public void update() {
				this.value = new Variant(StormProperties.getIntensityLevel());
			}
		});
		register(new DynamicVariable("weather.temperature") {
			@Override
			public void update() {
				this.value = new Variant(
						EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition()));
			}
		});
		register(new DynamicVariable("weather.temperatureValue") {
			@Override
			public void update() {
				this.value = new Variant(TemperatureRating
						.fromTemp(EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition()))
						.getValue());
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
