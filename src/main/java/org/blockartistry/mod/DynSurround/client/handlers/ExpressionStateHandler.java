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
import java.util.List;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
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

	private abstract static class DynamicVariable implements Expression.LazyVariant {

		protected Variant value;

		@Override
		public Variant eval() {
			return this.value;
		}

		public abstract void update();
	}

	private static final List<DynamicVariable> variables = new ArrayList<DynamicVariable>();

	private static void register(@Nonnull final String name, @Nonnull final DynamicVariable variable) {
		variables.add(variable);
		Expression.addBuiltInVariable(name, variable);
	}

	static {
		register("isRaining", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.getWorld().getRainStrength(1.0F) > 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		register("isNotRaining", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.getWorld().getRainStrength(1.0F) <= 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		register("isDay", new DynamicVariable() {
			@Override
			public void update() {
				this.value = DiurnalUtils.isDaytime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register("isNight", new DynamicVariable() {
			@Override
			public void update() {
				this.value = DiurnalUtils.isNighttime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register("isSunrise", new DynamicVariable() {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunrise(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register("isSunset", new DynamicVariable() {
			@Override
			public void update() {
				this.value = DiurnalUtils.isSunset(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register("isAuroraVisible", new DynamicVariable() {
			@Override
			public void update() {
				this.value = DiurnalUtils.isAuroraVisible(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		register("moonPhaseFactor", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(DiurnalUtils.getMoonPhaseFactor(EnvironState.getWorld()));
			}
		});
		register("hasSky", new DynamicVariable() {
			@Override
			public void update() {
				this.value = !EnvironState.getWorld().provider.getHasNoSky() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("season", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getSeason().getValue());
			}
		});

		// Biome variables
		register("biome.name", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getBiomeName());
			}
		});
		register("biome.temperature", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getBiomeTemperature().getValue());
			}
		});
		register("biome.isFoggy", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isFoggy() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("biome.isHumid", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isHumid() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("biome.isDry", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isDry() ? Expression.ONE : Expression.ZERO;
			}
		});

		// Player variables
		register("player.isHurt", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHurt() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isHungry", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerHungry() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isBurning", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBurning() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isSuffocating", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSuffocating() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isFlying", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerFlying() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isSprinting", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerSprinting() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isInLava", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInLava() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isInvisible", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInvisible() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isBlind", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerBlind() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isInWater", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInWater() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isRiding", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerRiding() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isOnGround", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerOnGround() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isMoving", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerMoving() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isInside", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInside() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isUnderground", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerUnderground() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isInSpace", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInSpace() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.isInClouds", new DynamicVariable() {
			@Override
			public void update() {
				this.value = EnvironState.isPlayerInClouds() ? Expression.ONE : Expression.ZERO;
			}
		});
		register("player.temperature", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayerTemperature().getValue());
			}
		});
		register("player.dimension", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getDimensionId());
			}
		});
		register("player.dimensionName", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getDimensionName());
			}
		});
		register("player.Y", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayerPosition().getY());
			}
		});
		register("player.health", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getHealth());
			}
		});
		register("player.maxHealth", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getMaxHealth());
			}
		});
		register("player.luck", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getLuck());
			}
		});
		register("player.food.saturation", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getFoodStats().getSaturationLevel());
			}
		});
		register("player.food.level", new DynamicVariable() {
			@Override
			public void update() {
				this.value = new Variant(EnvironState.getPlayer().getFoodStats().getFoodLevel());
			}
		});
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
	}

}
