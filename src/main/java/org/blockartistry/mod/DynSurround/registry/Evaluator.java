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

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.handlers.EnvironStateHandler.EnvironState;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;
import org.blockartistry.mod.DynSurround.util.Expression;
import org.blockartistry.mod.DynSurround.util.PlayerUtils;

import net.minecraft.world.biome.Biome.TempCategory;

public class Evaluator {

	public static void initialize() {
		Expression.addBuiltInVariable("isRaining", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return EnvironState.getWorld().getRainStrength(1.0F) > 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isNotRaining", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return EnvironState.getWorld().getRainStrength(1.0F) <= 0.0F ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isDay", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return DiurnalUtils.isDaytime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isNight", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return DiurnalUtils.isNighttime(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isSunrise", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return DiurnalUtils.isSunrise(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isSunset", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return DiurnalUtils.isSunset(EnvironState.getWorld()) ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isPlayerHurt", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return EnvironState.isPlayerHurt() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isPlayerHungry", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return EnvironState.isPlayerHungry() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isPlayerInside", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return EnvironState.isPlayerInside() ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isPlayerFreezing", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				final boolean flag = PlayerUtils.getPlayerBiome(EnvironState.getPlayer(), true)
						.getFloatTemperature(EnvironState.getPlayerPosition()) < 0.15F;
				return flag ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isPlayerCold", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				final boolean flag = PlayerUtils.getPlayerBiome(EnvironState.getPlayer(), true).getTempCategory() == TempCategory.COLD;
				return flag ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("isPlayerWarm", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				final boolean flag = PlayerUtils.getPlayerBiome(EnvironState.getPlayer(), true).getTempCategory() == TempCategory.WARM;
				return flag ? Expression.ONE : Expression.ZERO;
			}
		});
		Expression.addBuiltInVariable("playerDimension", new Expression.LazyNumber() {
			@Override
			public Float eval() {
				return new Float(EnvironState.getDimensionId());
			}
		});
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

		// New stuff. Compile the expression and evaluate
		try {
			final Float result = Expression.compile(conditions).eval();
			return result.floatValue() != 0.0F;
		} catch (final Throwable t) {
			ModLog.error("Unable to execute check: " + conditions, t);
		}

		// Something bad happened, so return no match
		return false;
	}
}
