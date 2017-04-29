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

package org.blockartistry.mod.DynSurround.client.footsteps.parsers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.blockartistry.lib.SoundUtils;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.BasicAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.DelayedAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.EventSelectorAcoustics;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.ProbabilityWeightsAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.SimultaneousAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.EventType;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IAcoustic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * JASON? JAAAASOOON?<br>
 * <a href="http://youtu.be/i7IE9gLwLUU?t=1m28s">http://youtu.
 * be/i7IE9gLwLUU?t=1m28s</a><br>
 * <br>
 * A JSON parser that creates a ILibrary of Acoustics.
 * 
 * @author Hurry
 */
@SideOnly(Side.CLIENT)
public class AcousticsJsonReader {
	private final int ENGINEVERSION = 0;
	
	private String soundRoot;
	
	private float default_volMin;
	private float default_volMax;
	private float default_pitchMin;
	private float default_pitchMax;
	
	private final float DIVIDE = 100f;
	
	public AcousticsJsonReader(String root) {
		soundRoot = root;
	}
	
	public void parseJSON(final String jasonString, final AcousticsManager lib) {
		try {
			parseJSONUnsafe(jasonString, lib);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseJSONUnsafe(final String jsonString, final AcousticsManager lib) throws JsonParseException {
		JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
		
		if (!json.get("type").getAsString().equals("library"))
			throw new JsonParseException("Invalid type: \"library\"");
		if (json.get("engineversion").getAsInt() != ENGINEVERSION)
			throw new JsonParseException("Unrecognised Engine version: " + ENGINEVERSION + " expected, got " + json.get("engineversion").getAsInt());
		if (!json.has("contents"))
			throw new JsonParseException("Empty contents");
		
		if (json.has("soundroot")) {
			soundRoot += json.get("soundroot").getAsString();
		}
		
		default_volMin = 1f;
		default_volMax = 1f;
		default_pitchMin = 1f;
		default_pitchMax = 1f;
		
		if (json.has("defaults")) {
			JsonObject defaults = json.getAsJsonObject("defaults");
			if (defaults.has("vol_min")) {
				default_volMin = processPitchOrVolume(defaults, "vol_min");
			}
			if (defaults.has("vol_max")) {
				default_volMax = processPitchOrVolume(defaults, "vol_max");
			}
			if (defaults.has("pitch_min")) {
				default_pitchMin = processPitchOrVolume(defaults, "pitch_min");
			}
			if (defaults.has("pitch_max")) {
				default_pitchMax = processPitchOrVolume(defaults, "pitch_max");
			}
		}
		
		final JsonObject contents = json.getAsJsonObject("contents");
		for (final Entry<String, JsonElement> preAcoustics : contents.entrySet()) {
			final String acousticsName = preAcoustics.getKey();
			final JsonObject acousticsDefinition = preAcoustics.getValue().getAsJsonObject();
			final EventSelectorAcoustics selector = new EventSelectorAcoustics(acousticsName);
			parseSelector(selector, acousticsDefinition);
			lib.addAcoustic(selector);
		}
	}
	
	private void parseSelector(final EventSelectorAcoustics selector, final JsonObject acousticsDefinition) throws JsonParseException {
		for (final EventType i : EventType.values()) {
			final String eventName = i.jsonName();
			if (acousticsDefinition.has(eventName)) {
				final JsonElement unsolved = acousticsDefinition.get(eventName);
				final IAcoustic acoustic = solveAcoustic(unsolved);
				selector.setAcousticPair(i, acoustic);
			}
		}
	}
	
	private IAcoustic solveAcoustic(final JsonElement unsolved) throws JsonParseException {
		IAcoustic ret = null;
		
		if (unsolved.isJsonObject()) {
			ret = solveAcousticsCompound(unsolved.getAsJsonObject());
		} else if (unsolved.isJsonPrimitive() && unsolved.getAsJsonPrimitive().isString()) { // Is a sound name
			final BasicAcoustic a = new BasicAcoustic();
			prepareDefaults(a);
			setupSoundName(a, unsolved.getAsString());
			ret = a;
		}
		
		if (ret == null)
			throw new JsonParseException("Unresolved Json element: \r\n" + unsolved.toString());
		return ret;
	}
	
	private IAcoustic solveAcousticsCompound(final JsonObject unsolved) throws JsonParseException {
		IAcoustic ret = null;
		
		if (!unsolved.has("type") || unsolved.get("type").getAsString().equals("basic")) {
			final BasicAcoustic a = new BasicAcoustic();
			prepareDefaults(a);
			setupClassics(a, unsolved);
			ret = a;
		} else {
			final String type = unsolved.get("type").getAsString();
			if (type.equals("simultaneous")) {
				final List<IAcoustic> acoustics = new ArrayList<IAcoustic>();
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
				final List<Integer> weights = new ArrayList<Integer>();
				final List<IAcoustic> acoustics = new ArrayList<IAcoustic>();
				
				final JsonArray sim = unsolved.getAsJsonArray("array");
				final Iterator<JsonElement> iter = sim.iterator();
				while (iter.hasNext()) {
					JsonElement subElement = iter.next();
					weights.add(subElement.getAsInt());
					
					if (!iter.hasNext())
						throw new JsonParseException("Probability has odd number of children!");
					
					subElement = iter.next();
					acoustics.add(solveAcoustic(subElement));
				}
				
				final ProbabilityWeightsAcoustic a = new ProbabilityWeightsAcoustic(acoustics, weights);
				
				ret = a;
			}
		}
		
		return ret;
	}
	
	private void prepareDefaults(final BasicAcoustic a) {
		a.setVolMin(this.default_volMin);
		a.setVolMax(this.default_volMax);
		a.setPitchMin(this.default_pitchMin);
		a.setPitchMax(this.default_pitchMax);
	}
	
	private void setupSoundName(final BasicAcoustic a, final String soundName) {
		if("@".equals(soundName)) {
			a.setSound(SoundUtils.getOrRegisterSound("MISSING"));
		} else if (soundName.charAt(0) != '@') {
			a.setSound(SoundUtils.getOrRegisterSound(this.soundRoot + soundName));
		} else {
			a.setSound(SoundUtils.getOrRegisterSound("minecraft:" + soundName.substring(1)));
		}
	}
	
	private void setupClassics(final BasicAcoustic a, final JsonObject solved) {
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
	
	private float processPitchOrVolume(final JsonObject object, final String param) {
		return object.get(param).getAsFloat() / DIVIDE;
	}
}
