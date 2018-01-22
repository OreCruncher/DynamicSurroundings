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

package org.blockartistry.DynSurround.registry;

import java.util.Set;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.data.xface.BiomeConfig;
import org.blockartistry.DynSurround.registry.RegistryManager.RegistryType;
import org.blockartistry.lib.BiomeUtils;
import org.blockartistry.lib.expression.BooleanValue;
import org.blockartistry.lib.expression.Expression;
import org.blockartistry.lib.expression.Function;
import org.blockartistry.lib.expression.NumberValue;
import org.blockartistry.lib.expression.StringValue;
import org.blockartistry.lib.expression.Variant;

import com.google.common.primitives.Booleans;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public abstract class BiomeMatcher {

	public abstract boolean match(@Nonnull final BiomeInfo info);

	public static BiomeMatcher getMatcher(@Nonnull final BiomeConfig cfg) {
		if (cfg.conditions != null)
			return new ConditionsImpl(cfg);
		return new LegacyImpl(RegistryManager.<BiomeRegistry>get(RegistryType.BIOME), cfg);
	}

	private static class LegacyImpl extends BiomeMatcher {

		protected final BiomeRegistry reg;
		protected final BiomeConfig config;

		public LegacyImpl(@Nonnull final BiomeRegistry reg, @Nonnull final BiomeConfig cfg) {
			this.reg = reg;
			this.config = cfg;
		}

		@Override
		public boolean match(@Nonnull final BiomeInfo info) {
			return this.reg.isBiomeMatch(this.config, info);
		}

	}

	private static class ConditionsImpl extends BiomeMatcher {

		private class BiomeTypeVariable extends Variant {

			private final BiomeDictionary.Type type;

			public BiomeTypeVariable(@Nonnull final BiomeDictionary.Type t) {
				super("biome.is" + t.getName());
				this.type = t;
			}

			@Override
			public int compareTo(Variant o) {
				return Booleans.compare(this.asBoolean(), o.asBoolean());
			}

			@Override
			public float asNumber() {
				return this.asBoolean() ? 1 : 0;
			}

			@Override
			public String asString() {
				return Boolean.toString(this.asBoolean());
			}

			@Override
			public boolean asBoolean() {
				return ConditionsImpl.this.current.isBiomeType(this.type);
			}

			@Override
			public Variant add(Variant term) {
				return new BooleanValue(this.asBoolean() || term.asBoolean());
			}
		}

		protected BiomeInfo current;
		protected final Expression exp;

		public ConditionsImpl(@Nonnull final BiomeConfig config) {
			this.exp = new Expression(config.conditions);

			// Biome name!
			this.exp.addVariable(new Variant("biome.name") {

				@Override
				public int compareTo(Variant o) {
					return this.asString().compareTo(o.asString());
				}

				@Override
				public float asNumber() {
					return 0;
				}

				@Override
				public String asString() {
					return ConditionsImpl.this.current.getBiomeName();
				}

				@Override
				public boolean asBoolean() {
					return false;
				}

				@Override
				public Variant add(Variant term) {
					return new StringValue(this.asString().concat(term.asString()));
				}

			});
			
			this.exp.addVariable(new Variant("biome.rainfall") {
				@Override
				public int compareTo(Variant o) {
					return Float.compare(this.asNumber(), o.asNumber());
				}

				@Override
				public float asNumber() {
					return ConditionsImpl.this.current.getRainfall();
				}

				@Override
				public String asString() {
					return Float.toString(this.asNumber());
				}

				@Override
				public boolean asBoolean() {
					return this.asNumber() != 0F;
				}

				@Override
				public Variant add(Variant term) {
					return new NumberValue(this.asNumber() + term.asNumber());
				}
				
			});

			// Fake biome
			this.exp.addVariable(new Variant("biome.isFake") {

				@Override
				public int compareTo(Variant o) {
					return this.asString().compareTo(o.asString());
				}

				@Override
				public float asNumber() {
					return this.asBoolean() ? 1 : 0;
				}

				@Override
				public String asString() {
					return Boolean.toString(this.asBoolean());
				}

				@Override
				public boolean asBoolean() {
					return ConditionsImpl.this.current.isFake();
				}

				@Override
				public Variant add(Variant term) {
					return new BooleanValue(this.asBoolean() || term.asBoolean());
				}

			});

			// Scan the BiomeDictionary adding the the types
			final Set<BiomeDictionary.Type> stuff = BiomeUtils.getBiomeTypes();
			for(final BiomeDictionary.Type t: stuff)
				this.exp.addVariable(new BiomeTypeVariable(t));

			// Add the biomes in the biome list
			for (final ResourceLocation b : Biome.REGISTRY.getKeys())
				if ("minecraft".equals(b.getResourceDomain()))
					this.exp.addVariable(new StringValue("biomeType." + b.getResourcePath(), b.toString()));

			// Add a function to do some biome comparisons
			this.exp.addFunction(new Function("biome.isLike", 1) {
				@Override
				public Variant eval(final Variant... params) {
					final String biomeName = params[0].asString();
					final Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeName));
					return biome != null && ConditionsImpl.this.current.areBiomesSameClass(biome) ? Expression.TRUE
							: Expression.FALSE;
				}
			});

			// Compile it
			this.exp.getRPN();

		}

		@Override
		public boolean match(@Nonnull final BiomeInfo info) {
			this.current = info;
			return this.exp.eval().asBoolean();
		}

	}
}
