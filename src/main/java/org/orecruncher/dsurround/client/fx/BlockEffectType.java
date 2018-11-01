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

package org.orecruncher.dsurround.client.fx;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModOptions;

/**
 * Describes the various types of block effects that can be generated.
 */
public enum BlockEffectType {

	/**
	 * Generic UNKNOWN effect
	 */
	UNKNOWN("UNKNOWN", null) {
		@Override
		public boolean isEnabled() {
			return false;
		}
	},

	/**
	 * Not used currently
	 */
	SOUND("sound", null) {
		@Override
		public boolean isEnabled() {
			return false;
		}
	},

	/**
	 * Firefly mote effect
	 */
	FIREFLY("firefly", FireFlyEffect.class) {
		@Override
		public boolean isEnabled() {
			return ModOptions.block.effects.enableFireflies;
		}
	},

	/**
	 * Various jet like particle effects
	 */
	STEAM_JET("steam", SteamJetEffect.class) {
		@Override
		public boolean isEnabled() {
			return ModOptions.block.effects.enableSteamJets;
		}
	},
	FIRE_JET("fire", FireJetEffect.class) {
		@Override
		public boolean isEnabled() {
			return ModOptions.block.effects.enableFireJets;
		}
	},
	BUBBLE_JET("bubble", BubbleJetEffect.class) {
		@Override
		public boolean isEnabled() {
			return ModOptions.block.effects.enableBubbleJets;
		}
	},
	DUST_JET("dust", DustJetEffect.class) {
		@Override
		public boolean isEnabled() {
			return ModOptions.block.effects.enableDustJets;
		}
	},
	FOUNTAIN_JET("fountain", FountainJetEffect.class) {
		@Override
		public boolean isEnabled() {
			return ModOptions.block.effects.enableFountainJets;
		}
	},
	SPLASH_JET("splash", WaterSplashJetEffect.class) {
		@Override
		public boolean isEnabled() {
			return ModOptions.block.effects.enableWaterSplash;
		}
	};

	private static final Map<String, BlockEffectType> typeMap = new HashMap<>();
	static {
		for (final BlockEffectType effect : BlockEffectType.values())
			typeMap.put(effect.getName(), effect);
	}

	@Nonnull
	public static BlockEffectType get(@Nonnull final String name) {
		final BlockEffectType result = typeMap.get(name);
		return result == null ? BlockEffectType.UNKNOWN : result;
	}

	protected final String name;
	protected Constructor<?> factory;

	private BlockEffectType(@Nonnull final String name, @Nullable final Class<?> clazz) {
		this.name = name;

		try {

			this.factory = clazz.getConstructor(int.class);

		} catch (@Nonnull final Throwable t) {
			;
		}
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	public abstract boolean isEnabled();

	@Nullable
	public BlockEffect getInstance(final int chance) {
		if (this.factory != null) {
			try {
				return (BlockEffect) this.factory.newInstance(chance);
			} catch (final Throwable t) {
				;
			}
		}

		return null;
	}
}
