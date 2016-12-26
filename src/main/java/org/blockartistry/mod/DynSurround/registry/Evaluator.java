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

package org.blockartistry.mod.DynSurround.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;
import org.blockartistry.mod.DynSurround.util.Expression;
import org.blockartistry.mod.DynSurround.util.Expression.Variant;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;

import net.minecraft.world.biome.Biome.TempCategory;

public class Evaluator {

	public static void initialize() {
		Expression.addBuiltInVariable("isRaining", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.getWorld().getRainStrength(1.0F) > 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isNotRaining", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.getWorld().getRainStrength(1.0F) <= 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isDay", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return DiurnalUtils.isDaytime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isNight", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return DiurnalUtils.isNighttime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isSunrise", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return DiurnalUtils.isSunrise(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isSunset", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return DiurnalUtils.isSunset(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isHurt", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerHurt() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isHungry", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerHungry() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isBurning", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerBurning() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isSuffocating", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerSuffocating() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isFlying", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerFlying() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isSprinting", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerSprinting() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isInLava", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerInLava() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isInvisible", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerInvisible() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isBlind", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerBlind() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isInWater", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerInWater() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isRiding", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerRiding() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isOnGround", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerOnGround() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isMoving", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerMoving() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isInside", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerInside() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isUnderground", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerUnderground() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isInSpace", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerInSpace() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isInClouds", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isPlayerInClouds() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isFreezing", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				final boolean flag = PlayerUtils.getPlayerBiome(EnvironState.getPlayer(), true)
						.getFloatTemperature(EnvironState.getPlayerPosition()) < 0.15F;
				return flag ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isCold", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				final boolean flag = PlayerUtils.getPlayerBiome(EnvironState.getPlayer(), true)
						.getTempCategory() == TempCategory.COLD;
				return flag ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.isWarm", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				final boolean flag = PlayerUtils.getPlayerBiome(EnvironState.getPlayer(), true)
						.getTempCategory() == TempCategory.WARM;
				return flag ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("player.dimension", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return new Variant(EnvironState.getDimensionId());
			}
		});
		Expression.addBuiltInVariable("player.Y", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return new Variant(EnvironState.getPlayerPosition().getY());
			}
		});
		Expression.addBuiltInVariable("player.biome", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return new Variant(EnvironState.getBiomeName());
			}
		});
		Expression.addBuiltInVariable("isFoggy", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isFoggy() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isHumid", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isHumid() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isDry", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return EnvironState.isDry() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isAuroraVisible", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return DiurnalUtils.isAuroraVisible(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("moonPhaseFactor", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return new Variant(DiurnalUtils.getMoonPhaseFactor(EnvironState.getWorld()));
			}
		});
		Expression.addBuiltInVariable("hasSky", new Expression.LazyNumber() {
			@Override
			public Variant eval() {
				return !EnvironState.getWorld().provider.getHasNoSky() ? Expression.ONE : Expression.ZERO;
			}
		});
	}

	private static final List<String> naughtyList = new ArrayList<String>();

	public static List<String> getNaughtyList() {
		return naughtyList;
	}
	
	public static boolean check(@Nonnull final String conditions) {
		// Existing default regex - short circuit to make it faster
		if (StringUtils.isEmpty(conditions) || conditions.startsWith(".*"))
			return true;

		// Older regex supplied so it needs to be processed old school
		if (conditions.startsWith("(?i)")) {
			// Old school
			final String ev = EnvironState.getConditions();
			return Pattern.matches(conditions, ev);
		}

		// If it was bad the first time around it is doubtful it
		// changed it's ways.
		if (naughtyList.contains(conditions))
			return false;

		// New stuff. Compile the expression and evaluate
		try {
			final Variant result = Expression.compile(conditions).eval();
			return result.asFloat() != 0.0F;
		} catch (final Throwable t) {
			ModLog.error("Unable to execute check: " + conditions, t);
			naughtyList.add(conditions);
		}

		// Something bad happened, so return no match
		return false;
	}
}
