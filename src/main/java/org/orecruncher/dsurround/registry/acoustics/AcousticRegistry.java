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

package org.orecruncher.dsurround.registry.acoustics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.footsteps.implem.DelayedAcoustic;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.config.ModConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A ILibrary that can also play sounds and default footsteps.
 */
@SideOnly(Side.CLIENT)
public class AcousticRegistry extends Registry {

	private static final float default_volMin = 0.9F;
	private static final float default_volMax = 1F;
	private static final float default_pitchMin = 0.95F;
	private static final float default_pitchMax = 1.05F;

	private static final float DIVIDE = 100f;

	/*
	 * The piece parts that are used to make more complicated sound effects
	 */
	private final Map<String, IAcoustic> acoustics = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/*
	 * The compiled acoustics using one or more acoustic entries
	 */
	private final Map<String, IAcoustic[]> compiled = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);

	// Special sentinels for equating
	public static final IAcoustic[] EMPTY = {};
	public static final IAcoustic[] NOT_EMITTER = { new NullAcoustic("NOT_EMITTER") };
	public static final IAcoustic[] MESSY_GROUND = { new NullAcoustic("MESSY_GROUND") };

	private int hits;

	public AcousticRegistry() {
		super("Acoustic Registry");
	}

	@Override
	protected void preInit() {
		this.hits = 0;
		this.acoustics.clear();
		this.compiled.put("EMPTY", EMPTY);
		this.compiled.put("NOT_EMITTER", NOT_EMITTER);
		this.compiled.put("MESSY_GROUND", MESSY_GROUND);
	}

	@Override
	protected void init(@Nonnull final ModConfiguration cfg) {
		// Process our acoustic entries
		for (final Entry<String, JsonElement> preAcoustics : cfg.acoustics.entrySet()) {
			final String acousticsName = preAcoustics.getKey();
			try {
				final JsonObject acousticsDefinition = preAcoustics.getValue().getAsJsonObject();
				final EventSelectorAcoustics selector = new EventSelectorAcoustics(acousticsName);
				parseSelector(selector, acousticsDefinition);
				addAcoustic(selector);
			} catch (@Nonnull final Throwable t) {
				final String msg = String.format("Unable to parse Json entry [%s]!", acousticsName);
				ModBase.log().error(msg, t);
			}
		}

		// Load up the primitive map. Do this after the acoustic entries because
		// they may reference.
		for (final Entry<String, String> prims : cfg.primitiveAcoustics.entrySet()) {
			this.compiled.put(prims.getKey(), compileAcoustics(prims.getValue()));
		}
	}

	@Override
	protected void complete() {
		ModBase.log().info("[%s] %d cache hits during initialization", getName(), this.hits);
	}

	@Nullable
	public IAcoustic[] getPrimitive(@Nonnull final String primitive) {
		IAcoustic[] result = this.compiled.get(primitive);
		if (result == null) {
			final IAcoustic a = generateAcoustic(primitive);
			if (a != null)
				this.compiled.put(primitive, result = new IAcoustic[] { a });
		}
		return result;
	}

	@Nullable
	public IAcoustic[] getPrimitiveSubstrate(@Nonnull final String primitive, @Nonnull final String substrate) {
		return this.compiled.get(primitive + "@" + substrate);
	}

	public void addAcoustic(@Nonnull final IAcoustic acoustic) {
		this.acoustics.put(acoustic.getName(), acoustic);
	}

	@Nullable
	public IAcoustic getAcoustic(@Nonnull final String name) {
		return this.acoustics.get(name);
	}

	@Nonnull
	public IAcoustic[] compileAcoustics(@Nonnull final SoundEvent evt) {
		IAcoustic[] result = this.compiled.get(evt.getSoundName().toString());
		if (result == null) {
			final IAcoustic a = generateAcoustic(evt);
			this.compiled.put(a.getName(), result = new IAcoustic[] { a });
		} else {
			this.hits++;
		}
		return result;
	}

	@Nonnull
	public IAcoustic[] compileAcoustics(@Nonnull final String acousticName) {
		IAcoustic[] result = this.compiled.get(acousticName);
		if (result == null) {
			result = Arrays.stream(acousticName.split(",")).map(fragment -> {
				// See if we have an acoustic for this fragment
				final IAcoustic a = generateAcoustic(fragment);
				if (a == null)
					ModBase.log().warn("Acoustic '%s' not found!", fragment);
				return a;
			}).filter(Objects::nonNull).toArray(IAcoustic[]::new);

			if (result == null || result.length == 0)
				result = EMPTY;
			this.compiled.put(acousticName, result);
		} else {
			this.hits++;
		}

		return result;
	}

	@Nullable
	private IAcoustic generateAcoustic(@Nonnull final String name) {
		IAcoustic a = this.acoustics.get(name);
		if (a == null) {
			// Nope. Doesn't exist yet. It could be a sound name based on location.
			final ResourceLocation loc = new ResourceLocation(name);
			final SoundEvent evt = RegistryManager.SOUND.getSound(loc);
			if (evt != null)
				a = generateAcoustic(evt);
		}
		return a;
	}

	@Nonnull
	private IAcoustic generateAcoustic(@Nonnull final SoundEvent evt) {
		IAcoustic result = this.acoustics.get(evt.getSoundName().toString());
		if (result == null) {
			result = new SimpleAcoustic(evt);
			this.acoustics.put(result.getName(), result);
		}
		return result;
	}

	private void parseSelector(final EventSelectorAcoustics selector, final JsonObject acousticsDefinition) {
		for (final EventType i : EventType.values()) {
			final String eventName = i.jsonName();
			if (acousticsDefinition.has(eventName)) {
				final JsonElement unsolved = acousticsDefinition.get(eventName);
				final IAcoustic acoustic = solveAcoustic(unsolved);
				selector.setAcousticPair(i, acoustic);
			}
		}
	}

	private IAcoustic solveAcoustic(final JsonElement unsolved) {
		IAcoustic ret = null;

		if (unsolved.isJsonObject()) {
			ret = solveAcousticsCompound(unsolved.getAsJsonObject());
		} else if (unsolved.isJsonPrimitive() && unsolved.getAsJsonPrimitive().isString()) { // Is
																								// a
																								// sound
																								// name
			final SimpleAcoustic a = new SimpleAcoustic();
			prepareDefaults(a);
			setupSoundName(a, unsolved.getAsString());
			ret = a;
		}

		if (ret == null)
			throw new IllegalStateException("Unresolved Json element: \r\n" + unsolved.toString());
		return ret;
	}

	private IAcoustic solveAcousticsCompound(final JsonObject unsolved) {
		IAcoustic ret = null;

		if (!unsolved.has("type") || unsolved.get("type").getAsString().equals("basic")) {
			final SimpleAcoustic a = new SimpleAcoustic();
			prepareDefaults(a);
			setupClassics(a, unsolved);
			ret = a;
		} else {
			final String type = unsolved.get("type").getAsString();
			if (type.equals("simultaneous")) {
				final List<IAcoustic> acoustics = new ArrayList<>();
				final JsonArray sim = unsolved.getAsJsonArray("array");
				final Iterator<JsonElement> iter = sim.iterator();
				while (iter.hasNext()) {
					final JsonElement subElement = iter.next();
					acoustics.add(solveAcoustic(subElement));
				}

				final SimultaneousAcoustic a = new SimultaneousAcoustic(acoustics);
				ret = a;
			} else if (type.equals("delayed")) {
				final DelayedAcoustic a = new DelayedAcoustic();
				prepareDefaults(a);
				setupClassics(a, unsolved);

				if (unsolved.has("delay")) {
					a.setDelayMin(unsolved.get("delay").getAsInt());
					a.setDelayMax(unsolved.get("delay").getAsInt());
				} else {
					a.setDelayMin(unsolved.get("delay_min").getAsInt());
					a.setDelayMax(unsolved.get("delay_max").getAsInt());
				}

				ret = a;
			} else if (type.equals("probability")) {
				final List<Integer> weights = new ArrayList<>();
				final List<IAcoustic> acoustics = new ArrayList<>();

				final JsonArray sim = unsolved.getAsJsonArray("array");
				final Iterator<JsonElement> iter = sim.iterator();
				while (iter.hasNext()) {
					JsonElement subElement = iter.next();
					weights.add(subElement.getAsInt());

					if (!iter.hasNext())
						throw new IllegalStateException("Probability has odd number of children!");

					subElement = iter.next();
					acoustics.add(solveAcoustic(subElement));
				}

				final ProbabilityWeightsAcoustic a = new ProbabilityWeightsAcoustic(acoustics, weights);

				ret = a;
			}
		}

		return ret;
	}

	private void prepareDefaults(@Nonnull final SimpleAcoustic a) {
		a.setVolMin(default_volMin);
		a.setVolMax(default_volMax);
		a.setPitchMin(default_pitchMin);
		a.setPitchMax(default_pitchMax);
	}

	private void setupSoundName(@Nonnull final SimpleAcoustic a, @Nonnull final String soundName) {
		try {
			final ResourceLocation res;
			if ("@".equals(soundName)) {
				res = null;
			} else if (soundName.contains(":")) {
				res = new ResourceLocation(soundName);
			} else if (soundName.charAt(0) != '@') {
				res = new ResourceLocation(ModBase.RESOURCE_ID, soundName);
			} else {
				res = new ResourceLocation("minecraft", soundName.substring(1));
			}
			if (res == null)
				a.setSound(null);
			else
				a.setSound(RegistryManager.SOUND.getSound(res));
		} catch (final Throwable t) {
			ModBase.log().warn("Unable to locate sound [%s]", soundName);
			a.setSound(null);
		}
	}

	private void setupClassics(final SimpleAcoustic a, final JsonObject solved) {
		setupSoundName(a, solved.get("name").getAsString());
		if (solved.has("vol_min")) {
			a.setVolMin(processPitchOrVolume(solved, "vol_min"));
		}
		if (solved.has("vol_max")) {
			a.setVolMax(processPitchOrVolume(solved, "vol_max"));
		}
		if (solved.has("pitch_min")) {
			a.setPitchMin(processPitchOrVolume(solved, "pitch_min"));
		}
		if (solved.has("pitch_max")) {
			a.setPitchMax(processPitchOrVolume(solved, "pitch_max"));
		}
	}

	private float processPitchOrVolume(@Nonnull final JsonObject object, @Nonnull final String param) {
		return object.get(param).getAsFloat() / DIVIDE;
	}

}