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

package org.blockartistry.mod.DynSurround.client.footsteps.implem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.mod.DynSurround.client.footsteps.system.Isolator;
import org.blockartistry.mod.DynSurround.registry.BlockInfo;
import org.blockartistry.mod.DynSurround.registry.BlockInfo.BlockInfoMutable;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockMap {
	private static final Pattern pattern = Pattern.compile("([^:]+:[^^+]+)\\^?(\\d+)?\\+?(\\w+)?");

	private final Isolator isolator;
	private final Map<BlockInfo, List<IAcoustic>> metaMap = new HashMap<BlockInfo, List<IAcoustic>>();
	private final Map<BlockInfo, Map<String, List<IAcoustic>>> substrateMap = new HashMap<BlockInfo, Map<String, List<IAcoustic>>>();

	private final BlockInfoMutable key = new BlockInfoMutable();

	private static class MacroEntry {
		public final int meta;
		public final String substrate;
		public final String value;

		public MacroEntry(@Nonnull final String substrate, final @Nonnull String value) {
			this(-1, substrate, value);
		}

		public MacroEntry(final int meta, @Nonnull final String substrate, @Nonnull final String value) {
			this.meta = meta;
			this.substrate = substrate;
			this.value = value;
		}
	}

	private static final Map<String, List<MacroEntry>> macros = new LinkedHashMap<String, List<MacroEntry>>();

	static {
		List<MacroEntry> entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry("foliage", "straw"));
		macros.put("#sapling", entries);
		macros.put("#reed", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "brush"));
		entries.add(new MacroEntry(3, "foliage", "brush"));
		entries.add(new MacroEntry(4, "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry(5, "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry(6, "foliage", "straw"));
		entries.add(new MacroEntry(7, "foliage", "straw"));
		macros.put("#wheat", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(3, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(4, "foliage", "brush"));
		entries.add(new MacroEntry(5, "foliage", "brush"));
		entries.add(new MacroEntry(6, "foliage", "brush"));
		entries.add(new MacroEntry(7, "foliage", "brush"));
		macros.put("#crop", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("messy", "MESSY_GROUND"));
		entries.add(new MacroEntry(0, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(1, "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry(2, "foliage", "brush"));
		entries.add(new MacroEntry(3, "foliage", "brush"));
		macros.put("#beets", entries);

		entries = new ArrayList<MacroEntry>();
		entries.add(new MacroEntry("bigger", "bluntwood"));
		macros.put("#fence", entries);
	}

	public BlockMap(@Nonnull final Isolator isolator) {
		this.isolator = isolator;
	}

	@Nullable
	public List<IAcoustic> getBlockMap(@Nonnull final IBlockState state) {
		this.key.set(state);
		List<IAcoustic> result = this.metaMap.get(this.key);
		if (result == null && this.key.hasSubTypes()) {
			result = this.metaMap.get(this.key.asGeneric());
		}
		return result;
	}

	private Map<String, List<IAcoustic>> getBlockSubstrate(@Nonnull final IBlockState state) {
		this.key.set(state);
		Map<String, List<IAcoustic>> sub = this.substrateMap.get(this.key);
		if (sub == null) {
			if (this.key.hasSubTypes())
				sub = this.substrateMap.get(this.key.asGeneric());
			else if (this.key.hasSpecialMeta())
				sub = this.substrateMap.get(this.key.asSpecial());
		}
		return sub;
	}

	@Nullable
	public List<IAcoustic> getBlockMapSubstrate(@Nonnull final IBlockState state, @Nonnull final String substrate) {
		final Map<String, List<IAcoustic>> sub = getBlockSubstrate(state);
		return sub != null ? sub.get(substrate) : null;
	}

	private void put(@Nonnull final Block block, final int meta, @Nonnull final String substrate,
			@Nonnull final String value) {

		final List<IAcoustic> acoustics = this.isolator.getAcoustics().compileAcoustics(value);

		if (StringUtils.isEmpty(substrate)) {
			this.metaMap.put(new BlockInfo(block, meta), acoustics);
		} else {
			final BlockInfo bi = new BlockInfo(block, meta);
			Map<String, List<IAcoustic>> sub = this.substrateMap.get(bi);
			if (sub == null)
				this.substrateMap.put(bi, sub = new HashMap<String, List<IAcoustic>>());
			sub.put(substrate.intern(), acoustics);
		}
	}

	private void expand(@Nonnull final Block block, @Nonnull final String value) {
		final List<MacroEntry> macro = macros.get(value);
		if (macro != null) {
			for (final MacroEntry entry : macro)
				put(block, entry.meta, entry.substrate, entry.value);
		} else {
			ModLog.debug("Unknown macro '%s'", value);
		}
	}

	public void register(@Nonnull final String key, @Nonnull final String value) {
		final Matcher matcher = pattern.matcher(key);
		if (matcher.matches()) {
			final String blockName = matcher.group(1);
			final Block block = MCHelper.getBlockByName(blockName);
			if (block != null) {
				if (value.startsWith("#")) {
					expand(block, value);
				} else {
					final int meta = matcher.group(2) == null
							? (MCHelper.hasVariants(block) ? BlockInfo.GENERIC : BlockInfo.NO_SUBTYPE)
							: Integer.parseInt(matcher.group(2));
					final String substrate = matcher.group(3);
					put(block, meta, substrate, value);
				}
			} else {
				ModLog.debug("Unable to locate block for blockmap '%s'", blockName);
			}
		} else {
			ModLog.debug("Malformed key in blockmap '%s'", key);
		}
	}

	@Nonnull
	private static String combine(@Nonnull final List<IAcoustic> acoustics) {
		final StringBuilder builder = new StringBuilder();
		boolean addComma = false;
		for (final IAcoustic a : acoustics) {
			if (addComma)
				builder.append(",");
			else
				addComma = true;
			builder.append(a.getAcousticName());
		}
		return builder.toString();
	}

	public void collectData(@Nonnull final IBlockState state, @Nonnull final List<String> data) {

		final List<IAcoustic> temp = getBlockMap(state);
		if (temp != null)
			data.add(combine(temp));

		final Map<String, List<IAcoustic>> subs = getBlockSubstrate(state);
		if (subs != null) {
			for (final Entry<String, List<IAcoustic>> entry : subs.entrySet())
				data.add(entry.getKey() + ":" + combine(entry.getValue()));
		}
	}

	public void clear() {
		this.metaMap.clear();
		this.substrateMap.clear();
	}
}
