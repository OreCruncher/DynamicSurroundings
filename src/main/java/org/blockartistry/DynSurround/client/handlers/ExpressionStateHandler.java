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

package org.blockartistry.DynSurround.client.handlers;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.event.ExpressionEvent;
import org.blockartistry.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.DynSurround.client.swing.DiagnosticPanel;
import org.blockartistry.DynSurround.client.weather.WeatherProperties;
import org.blockartistry.DynSurround.registry.TemperatureRating;
import org.blockartistry.lib.DiurnalUtils;
import org.blockartistry.lib.expression.Dynamic;
import org.blockartistry.lib.expression.DynamicVariantList;
import org.blockartistry.lib.expression.IDynamicVariant;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Calculates and caches predefined sets of data points for script evaluation
 * during a tick. Goal is to minimize script evaluation overhead as much as
 * possible.
 */
@SideOnly(Side.CLIENT)
public class ExpressionStateHandler extends EffectHandlerBase {

	private static final DynamicVariantList variables = new DynamicVariantList();

	public static List<IDynamicVariant<?>> getVariables() {
		return variables.getList();
	}

	private static void register(@Nonnull final IDynamicVariant<?> variable) {
		variables.add(variable);
	}

	public static void register() {
		register(new Dynamic.DynamicBoolean("isDay") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isDaytime(EnvironState.getWorld());
			}
		});
		register(new Dynamic.DynamicBoolean("isNight") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isNighttime(EnvironState.getWorld());
			}
		});
		register(new Dynamic.DynamicBoolean("isSunrise") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunrise(EnvironState.getWorld());
			}
		});
		register(new Dynamic.DynamicBoolean("isSunset") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunset(EnvironState.getWorld());
			}
		});
		register(new Dynamic.DynamicBoolean("isAuroraVisible") {
			@Override
			public void update() {
				this.value = DiurnalUtils.isAuroraVisible(EnvironState.getWorld());
			}
		});
		register(new Dynamic.DynamicNumber("moonPhaseFactor") {
			@Override
			public void update() {
				this.value = DiurnalUtils.getMoonPhaseFactor(EnvironState.getWorld());
			}
		});
		register(new Dynamic.DynamicBoolean("hasSky") {
			@Override
			public void update() {
				final World world = EnvironState.getWorld();
				this.value = world != null && !world.provider.hasNoSky;
			}
		});
		register(new Dynamic.DynamicString("season") {
			@Override
			public void update() {
				this.value = EnvironState.getSeason().getValue();
			}
		});

		// Biome variables
		register(new Dynamic.DynamicString("biome.name") {
			@Override
			public void update() {
				this.value = EnvironState.getBiomeName();
			}
		});
		register(new Dynamic.DynamicString("biome.temperature") {
			@Override
			public void update() {
				this.value = EnvironState.getBiomeTemperature().getValue();
			}
		});
		register(new Dynamic.DynamicNumber("biome.rainfall") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerBiome().getRainfall();
			}
		});
		register(new Dynamic.DynamicNumber("biome.temperatureValue") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerBiome().getTemperature();
			}
		});

		// Scan the BiomeDictionary adding the the types
		try {
			final Field accessor = ReflectionHelper.findField(BiomeDictionary.Type.class, "byName");
			if (accessor != null) {
				@SuppressWarnings("unchecked")
				final Map<String, BiomeDictionary.Type> stuff = (Map<String, BiomeDictionary.Type>) accessor.get(null);
				for (final Entry<String, Type> e : stuff.entrySet()) {
					register(new Dynamic.DynamicBoolean("biome.is" + e.getKey()) {
						@Override
						public void update() {
							this.value = EnvironState.getTruePlayerBiome().isBiomeType(e.getValue());
						}
					});
				}
			}

		} catch (final Throwable t) {
			throw new RuntimeException("Cannot locate BiomeDictionary.Type table!");
		}

		// Player variables
		register(new Dynamic.DynamicBoolean("player.isDead") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isDead;
			}
		});
		register(new Dynamic.DynamicBoolean("player.isHurt") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHurt();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isHungry") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHungry();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isBurning") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBurning();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isSuffocating") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSuffocating();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isFlying") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerFlying();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isSprinting") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSprinting();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isInLava") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInLava();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isInvisible") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInvisible();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isBlind") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBlind();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isInWater") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInWater();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isWet") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isWet();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isUnderwater") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isInsideOfMaterial(Material.WATER);
			}
		});
		register(new Dynamic.DynamicBoolean("player.isRiding") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.isRiding();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isOnGround") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.onGround;
			}
		});
		register(new Dynamic.DynamicBoolean("player.isMoving") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerMoving();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isInside") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInside();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isUnderground") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerUnderground();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isInSpace") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInSpace();
			}
		});
		register(new Dynamic.DynamicBoolean("player.isInClouds") {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInClouds();
			}
		});
		register(new Dynamic.DynamicString("player.temperature") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerTemperature().getValue();
			}
		});
		register(new Dynamic.DynamicNumber("player.dimension") {
			@Override
			public void update() {
				this.value = EnvironState.getDimensionId();
			}
		});
		register(new Dynamic.DynamicString("player.dimensionName") {
			@Override
			public void update() {
				this.value = EnvironState.getDimensionName();
			}
		});
		register(new Dynamic.DynamicNumber("player.X") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getX();
			}
		});
		register(new Dynamic.DynamicNumber("player.Y") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getY();
			}
		});
		register(new Dynamic.DynamicNumber("player.Z") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerPosition().getZ();
			}
		});
		register(new Dynamic.DynamicNumber("player.health") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getHealth() : Integer.MAX_VALUE;
			}
		});
		register(new Dynamic.DynamicNumber("player.maxHealth") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getMaxHealth() : Integer.MAX_VALUE;
			}
		});
		register(new Dynamic.DynamicNumber("player.luck") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getLuck() : 0;
			}
		});
		register(new Dynamic.DynamicNumber("player.food.saturation") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getFoodStats().getSaturationLevel() : 0;
			}
		});
		register(new Dynamic.DynamicNumber("player.food.level") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null ? player.getFoodStats().getFoodLevel() : 0;
			}
		});
		register(new Dynamic.DynamicBoolean("player.canRainOn") {
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
		register(new Dynamic.DynamicBoolean("player.canSeeSky") {
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
		register(new Dynamic.DynamicBoolean("player.inBoat") {
			@Override
			public void update() {
				final EntityPlayer player = EnvironState.getPlayer();
				this.value = player != null && player.getRidingEntity() instanceof EntityBoat;
			}
		});
		register(new Dynamic.DynamicNumber("player.lightLevel") {
			@Override
			public void update() {
				this.value = EnvironState.getLightLevel();
			}
		});
		register(new Dynamic.DynamicString("player.armor") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerArmorClass().getClassName();
			}
		});
		register(new Dynamic.DynamicBoolean("player.inVillage") {
			@Override
			public void update() {
				this.value = EnvironState.inVillage();
			}
		});

		// Weather variables
		register(new Dynamic.DynamicBoolean("weather.isRaining") {
			@Override
			public void update() {
				this.value = WeatherProperties.isRaining();
			}
		});
		register(new Dynamic.DynamicBoolean("weather.isThundering") {
			@Override
			public void update() {
				this.value = WeatherProperties.isThundering();
			}
		});
		register(new Dynamic.DynamicBoolean("weather.isNotRaining") {
			@Override
			public void update() {
				this.value = !WeatherProperties.isRaining();
			}
		});
		register(new Dynamic.DynamicBoolean("weather.isNotThundering") {
			@Override
			public void update() {
				this.value = !WeatherProperties.isThundering();
			}
		});
		register(new Dynamic.DynamicNumber("weather.rainfall") {
			@Override
			public void update() {
				this.value = WeatherProperties.getIntensityLevel();
			}
		});
		register(new Dynamic.DynamicNumber("weather.temperatureValue") {
			@Override
			public void update() {
				this.value = EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition());
			}
		});
		register(new Dynamic.DynamicString("weather.temperature") {
			@Override
			public void update() {
				this.value = TemperatureRating
						.fromTemp(EnvironState.getPlayerBiome().getFloatTemperature(EnvironState.getPlayerPosition()))
						.getValue();
			}
		});

		// Battle state
		register(new Dynamic.DynamicBoolean("battle.inBattle") {
			@Override
			public void update() {
				this.value = EnvironState.getBattleScanner().inBattle();
			}
		});
		register(new Dynamic.DynamicBoolean("battle.isBoss") {
			@Override
			public void update() {
				this.value = EnvironState.getBattleScanner().isBoss();
			}
		});
		register(new Dynamic.DynamicBoolean("battle.isWither") {
			@Override
			public void update() {
				this.value = EnvironState.getBattleScanner().isWither();
			}
		});
		register(new Dynamic.DynamicBoolean("battle.isDragon") {
			@Override
			public void update() {
				this.value = EnvironState.getBattleScanner().isDragon();
			}
		});

		/*
		 * // Sort them for easy display Collections.sort(variables, new
		 * Comparator<IDynamicVariant>() {
		 * 
		 * @Override public int compare(@Nonnull final IDynamicVariant o1, @Nonnull
		 * final IDynamicVariant o2) { return o1.getName().compareTo(o2.getName()); }
		 * });
		 */

	}

	public ExpressionStateHandler() {
		super("ExpressionStateHandler");
	}

	@Override
	public void process(@Nonnull final EntityPlayer player) {
		// Iterate through the variables and get the data cached for this ticks
		// expression evaluations.
		variables.update();

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

	@SubscribeEvent
	public void onExpressionCreate(@Nonnull final ExpressionEvent.Create event) {
		variables.attach(event.expression);
	}

}
