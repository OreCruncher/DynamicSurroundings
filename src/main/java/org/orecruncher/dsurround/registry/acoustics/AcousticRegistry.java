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

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.client.footsteps.DelayedAcoustic;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.lib.MCHelper;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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

	private static final Map<Material, String> materialProfiles = new Reference2ObjectOpenHashMap<>();

	static {
		// No macros in this list!
		materialProfiles.put(Material.AIR, "NOT_EMITTER");
		materialProfiles.put(Material.GRASS, "grass");
		materialProfiles.put(Material.GROUND, "dirt");
		materialProfiles.put(Material.WOOD, "wood");
		materialProfiles.put(Material.ROCK, "stone");
		materialProfiles.put(Material.IRON, "hardmetal");
		materialProfiles.put(Material.ANVIL, "metalcompressed,hardmetal");
		materialProfiles.put(Material.WATER, "NOT_EMITTER");
		materialProfiles.put(Material.LAVA, "NOT_EMITTER");
		materialProfiles.put(Material.LEAVES, "leaves");
		materialProfiles.put(Material.PLANTS, "brush");
		materialProfiles.put(Material.VINE, "leaves");
		materialProfiles.put(Material.SPONGE, "organic_dry");
		materialProfiles.put(Material.CLOTH, "rug");
		materialProfiles.put(Material.FIRE, "NOT_EMITTER");
		materialProfiles.put(Material.SAND, "sand");
		materialProfiles.put(Material.CIRCUITS, "stoneutility");
		materialProfiles.put(Material.CARPET, "rug");
		materialProfiles.put(Material.GLASS, "glass");
		materialProfiles.put(Material.REDSTONE_LIGHT, "NOT_EMITTER");
		materialProfiles.put(Material.TNT, "equipment");
		materialProfiles.put(Material.CORAL, "NOT_EMITTER");
		materialProfiles.put(Material.ICE, "ice");
		materialProfiles.put(Material.PACKED_ICE, "ice");
		materialProfiles.put(Material.SNOW, "snow");
		materialProfiles.put(Material.CRAFTED_SNOW, "snow");
		materialProfiles.put(Material.CACTUS, "grass");
		materialProfiles.put(Material.CLAY, "dirt");
		materialProfiles.put(Material.GOURD, "organic_dry");
		materialProfiles.put(Material.DRAGON_EGG, "obsidian");
		materialProfiles.put(Material.PORTAL, "NOT_EMITTER");
		materialProfiles.put(Material.CAKE, "organic");
		materialProfiles.put(Material.WEB, "NOT_EMITTER");
		materialProfiles.put(Material.PISTON, "stonemachine");
		materialProfiles.put(Material.BARRIER, "glass");
		materialProfiles.put(Material.STRUCTURE_VOID, "NOT_EMITTER");
	}

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
	private int primitives;
	private int material;

	public AcousticRegistry() {
		super("Acoustic Registry");
	}

	@Override
	protected void preInit() {
		this.hits = 0;
		this.primitives = 0;
		this.material = 0;
		this.acoustics.clear();
		this.compiled.clear();
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
		ModBase.log().info("[%s] %d primitives by material generated", getName(), this.material);
		ModBase.log().info("[%s] %d primitives by sound generated", getName(), this.primitives);
	}

	/**
	 * Used to determine what acoustics to play based on the block's sound
	 * attributes. It's a fallback method in case there isn't a configuration
	 * defined acoustic profile for a block state.
	 *
	 * @param state BlockState for which the acoustic profile is being generated
	 * @return Acoustic profile for the BlockState, if any
	 */
	@Nullable
	public IAcoustic[] resolvePrimitive(@Nonnull final IBlockState state) {

		if (state == Blocks.AIR.getDefaultState())
			return NOT_EMITTER;

		// Get the step sound based on the block's SoundType
		final String soundName;
		final SoundType type = MCHelper.getSoundType(state);
		if (type != null && type.getStepSound() != null
				&& !type.getStepSound().getSoundName().getNamespace().isEmpty()) {
			soundName = type.getStepSound().getSoundName().toString();
		} else {
			soundName = null;
		}

		IAcoustic[] acoustics = null;

		// If we don't have a step sound, or the sound belongs to Minecraft,
		// resolve based on material. This lets modded sounds through, like
		// for Chisel's Laboratory blocks.
		//
		// The reason we do Material before SoundType is because Material
		// is more descriptive of the block. Also, modders have a tendency
		// to forget to set the SoundType which let's it default to stone.
		//
		// Note that the only Material that DS knows about are the ones
		// defined by Minecraft. Modded Material instances will not be
		// recognized, meaning that the SoundType will be used eventually.
		// I figure if the modder is savvy enough to make custom materials
		// they are savvy enough to set the SoundType.
		if (soundName == null || soundName.startsWith("minecraft"))
			acoustics = resolveByMaterial(state);

		if (acoustics != null)
			this.material++;

		// If we haven't resolved yet and have a sound name resolve it as
		// a primitive.
		if (acoustics == null && StringUtils.isNotEmpty(soundName))
			acoustics = getPrimitive(soundName);

		return MoreObjects.firstNonNull(acoustics, EMPTY);
	}

	@Nullable
	private IAcoustic[] getPrimitive(@Nonnull final String primitive) {
		IAcoustic[] result = this.compiled.get(primitive);
		if (result == null) {
			final IAcoustic a = generateAcoustic(primitive);
			if (a != null) {
				this.compiled.put(primitive, result = new IAcoustic[] { a });
				this.primitives++;
			}
		}
		return result;
	}

	@Nullable
	private IAcoustic[] resolveByMaterial(@Nonnull final IBlockState state) {
		IAcoustic[] result = null;
		final String profile = materialProfiles.get(state.getMaterial());
		if (StringUtils.isNotEmpty(profile))
			result = compileAcoustics(profile);
		return result == EMPTY ? null : result;
	}

	private void addAcoustic(@Nonnull final IAcoustic acoustic) {
		this.acoustics.put(acoustic.getName(), acoustic);
	}

	@Nullable
	public IAcoustic getAcoustic(@Nonnull final String name) {
		return this.acoustics.get(name);
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
		} else if (unsolved.isJsonPrimitive() && unsolved.getAsJsonPrimitive().isString()) {
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
				res = new ResourceLocation(ModInfo.RESOURCE_ID, soundName);
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