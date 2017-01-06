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
import org.blockartistry.mod.DynSurround.util.script.BooleanValue;
import org.blockartistry.mod.DynSurround.util.script.Expression;
import org.blockartistry.mod.DynSurround.util.script.NumberValue;
import org.blockartistry.mod.DynSurround.util.script.StringValue;
import org.blockartistry.mod.DynSurround.util.script.Variant;

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

	public interface IDynamicVariable extends Comparable<IDynamicVariable> {
		String getName();

		void update();

		String asString();
	}

	public abstract static class DynamicNumber extends NumberValue implements IDynamicVariable {

		private final String name;

		public DynamicNumber(@Nonnull final String name) {
			this.name = name;
		}

		@Override
		@Nonnull
		public String getName() {
			return this.name;
		}

		@Override
		public int compareTo(@Nonnull final IDynamicVariable o) {
			return this.name.compareTo(o.getName());
		}

	}

	public abstract static class DynamicString extends StringValue implements IDynamicVariable {

		private final String name;

		public DynamicString(@Nonnull final String name) {
			this.name = name;
		}

		@Override
		@Nonnull
		public String getName() {
			return this.name;
		}

		@Override
		public int compareTo(@Nonnull final IDynamicVariable o) {
			return this.name.compareTo(o.getName());
		}
	}

	public abstract static class DynamicBoolean extends BooleanValue implements IDynamicVariable {
		private final String name;

		public DynamicBoolean(@Nonnull final String name) {
			this.name = name;
		}

		@Override
		@Nonnull
		public String getName() {
			return this.name;
		}

		@Override
		public int compareTo(@Nonnull final IDynamicVariable o) {
			return this.name.compareTo(o.getName());
		}
	}

	private static final List<IDynamicVariable> variables = new ArrayList<IDynamicVariable>();

	public static List<IDynamicVariable> getVariables() {
		return variables;
	}

	private static void register(@Nonnull final Variant variable) {
		final IDynamicVariable dv = (IDynamicVariable) variable;
		variables.add(dv);
		Expression.addBuiltInVariable(dv.getName(), variable);
	}

	static {
		register(new DynamicBoolean("isDay") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isDaytime(EnvironState.getWorld());
			}
		});
		register(new DynamicBoolean("isNight") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isNighttime(EnvironState.getWorld());
			}
		});
		register(new DynamicBoolean("isSunrise") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunrise(EnvironState.getWorld());
			}
		});
		register(new DynamicBoolean("isSunset") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunset(EnvironState.getWorld());
			}
		});
		register(new DynamicBoolean("isAuroraVisible") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isAuroraVisible(EnvironState.getWorld());
			}
		});
		register(new DynamicNumber("moonPhaseFactor") {
			@Override
			public void update() {
				this.value = DiurnalUtils.getMoonPhaseFactor(EnvironState.getWorld());
			}
		});
		register(new DynamicBoolean("hasSky") {
			@Override
			public void update() {
				this.value = !EnvironState.getWorld().provider.getHasNoSky();
			}
		});
		register(new DynamicString("season") {
			@Override
			public void update() {
				this.value = EnvironState.getSeason().getValue();
			}
		});

		// Biome variables
		register(new DynamicString("biome.name") {
			@Override
			public void update() {
				this.value = EnvironState.getBiomeName();
			}
		});
		register(new DynamicString("biome.temperature") {
			@Override
			public void update() {
				this.value = EnvironState.getBiomeTemperature().getValue();
			}
		});
		register(new DynamicNumber("biome.rainfall") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerBiome().getRainfall();
			}
		});
		register(new DynamicNumber("biome.temperatureValue") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerBiome().getTemperature();
			}
		});
		register(new DynamicBoolean("biome.isFoggy") {
			@Override
			public void update() {
				this.value = EnvironState.isFoggy();
			}
		});
		register(new DynamicBoolean("biome.isHumid") {
			@Override
			public void update() {
				this.value = EnvironState.isHumid();
			}
		});
		register(new DynamicBoolean("biome.isDry") {
			@Override
			public void update() {
				this.value = EnvironState.isDry();
			}
		});

		// Player variables
		register(new DynamicBoolean("player.isHurt") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHurt();
			}
		});
		register(new DynamicBoolean("player.isHungry") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHungry();
			}
		});
		register(new DynamicBoolean("player.isBurning") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBurning();
			}
		});
		register(new DynamicBoolean("player.isSuffocating") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSuffocating();
			}
		});
		register(new DynamicBoolean("player.isFlying") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerFlying();
			}
		});
		register(new DynamicBoolean("player.isSprinting") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSprinting();
			}
		});
		register(new DynamicBoolean("player.isInLava") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInLava();
			}
		});
		register(new DynamicBoolean("player.isInvisible") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInvisible();
			}
		});
		register(new DynamicBoolean("player.isBlind") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBlind();
			}
		});
		register(new DynamicBoolean("player.isInWater") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInWater();
			}
		});
		register(new DynamicBoolean("player.isWet") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().isWet();
			}
		});
		register(new DynamicBoolean("player.isUnderwater") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().isInsideOfMaterial(Material.WATER);
			}
		});
		register(new DynamicBoolean("player.isRiding") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().isRiding();
			}
		});
		register(new DynamicBoolean("player.isOnGround") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().onGround;
			}
		});
		register(new DynamicBoolean("player.isMoving") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player.distanceWalkedModified != player.prevDistanceWalkedModified;
			}
		});
		register(new DynamicBoolean("player.isInside") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInside();
			}
		});
		register(new DynamicBoolean("player.isUnderground") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerUnderground();
			}
		});
		register(new DynamicBoolean("player.isInSpace") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInSpace();
			}
		});
		register(new DynamicBoolean("player.isInClouds") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInClouds();
			}
		});
		register(new DynamicString("player.temperature") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerTemperature().getValue();
			}
		});
		register(new DynamicNumber("player.dimension") {
			@Override
			public void update() {
				this.value = EnvironState.getDimensionId();
			}
		});
		register(new DynamicString("player.dimensionName") {
			@Override
			public void update() {
				this.value = EnvironState.getDimensionName();
			}
		});
		register(new DynamicNumber("player.X") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getX();
			}
		});
		register(new DynamicNumber("player.Y") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getY();
			}
		});
		register(new DynamicNumber("player.Z") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getZ();
			}
		});
		register(new DynamicNumber("player.health") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().getHealth();
			}
		});
		register(new DynamicNumber("player.maxHealth") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().getMaxHealth();
			}
		});
		register(new DynamicNumber("player.luck") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().getLuck();
			}
		});
		register(new DynamicNumber("player.food.saturation") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().getFoodStats().getSaturationLevel();
			}
		});
		register(new DynamicNumber("player.food.level") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().getFoodStats().getFoodLevel();
			}
		});
		register(new DynamicBoolean("player.canRainOn") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
				this.value = world.canBlockSeeSky(pos) && !(world.getTopSolidOrLiquidBlock(pos).getY() > pos.getY());
			}
		});
		register(new DynamicBoolean("player.canSeeSky") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				final BlockPos pos = EnvironState.getPlayerPosition().add(0, 2, 0);
				this.value = world.canBlockSeeSky(pos);
			}
		});
		register(new DynamicBoolean("player.inBoat") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayer().getRidingEntity() instanceof EntityBoat;
			}
		});
		register(new DynamicNumber("player.lightLevel") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				final BlockPos pos = EnvironState.getPlayerPosition();
				final int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, pos);
				final int skyLight = world.getLightFor(EnumSkyBlock.SKY, pos) - world.calculateSkylightSubtracted(1.0F);
				this.value = Math.max(blockLight, skyLight);
			}
		});

		// Weather variables
		register(new DynamicBoolean("weather.isRaining") {
			@Override
			public void update() {
				this.value = WeatherProperties.getIntensityLevel() > 0.0F;
			}
		});
		register(new DynamicBoolean("weather.isThundering") {
			@Override
			public void update() {
				this.value = EnvironState.getWorld().isThundering();
			}
		});
		register(new DynamicBoolean("weather.isNotRaining") {
			@Override
			public void update() {
				this.value = WeatherProperties.getIntensityLevel() <= 0.0F;
			}
		});
		register(new DynamicBoolean("weather.isNotThundering") {
			@Override
			public void update() {
				this.value = !EnvironState.getWorld().isThundering();
			}
		});
		register(new DynamicNumber("weather.rainfall") {
			@Override
			public void update() {
				this.value = WeatherProperties.getIntensityLevel();
			}
		});
		register(new DynamicNumber("weather.temperatureValue") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition());
			}
		});
		register(new DynamicString("weather.temperature") {
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
		for (final IDynamicVariable dv : variables)
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
