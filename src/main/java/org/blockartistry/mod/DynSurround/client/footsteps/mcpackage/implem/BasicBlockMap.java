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

package org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.implem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.mcpackage.interfaces.IBlockMap;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicBlockMap implements IBlockMap {
	private static final Pattern pattern = Pattern.compile("([^:]+:[^^+]+)\\^?(\\d+)?\\+?(\\w+)?");

	private final Map<Block, TIntObjectHashMap<String>> metaMap = new TCustomHashMap<Block, TIntObjectHashMap<String>>(
			IdentityHashingStrategy.INSTANCE);
	private final Map<Block, Map<String, String>> substrateMap = new TCustomHashMap<Block, Map<String, String>>(
			IdentityHashingStrategy.INSTANCE);

	private static class MacroEntry {
		public final int meta;
		public final String substrate;
		public final String value;

		public MacroEntry(final String substrate, final String value) {
			this(-1, substrate, value);
		}

		public MacroEntry(final int meta, final String substrate, final String value) {
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
		entries.add(new MacroEntry("bigger", "bluntwood"));
		macros.put("#fence", entries);
	}

	public BasicBlockMap() {
	}

	@Override
	public String getBlockMap(final IBlockState state) {
		final TIntObjectHashMap<String> metas = this.metaMap.get(state.getBlock());
		if (metas != null) {
			String result = metas.get(state.getBlock().getMetaFromState(state));
			if (result == null)
				result = metas.get(-1);
			return result;
		}
		return null;
	}

	@Override
	public String getBlockMapSubstrate(final IBlockState state, final String substrate) {
		final Map<String, String> sub = this.substrateMap.get(state.getBlock());
		if (sub != null) {
			String result = sub.get(substrate + "." + state.getBlock().getMetaFromState(state));
			if (result == null)
				result = sub.get(substrate + ".-1");
			return result;
		}
		return null;
	}

	private void put(final Block block, final int meta, final String substrate, final String value) {
		if (StringUtils.isEmpty(substrate)) {
			TIntObjectHashMap<String> metas = this.metaMap.get(block);
			if (metas == null)
				this.metaMap.put(block, metas = new TIntObjectHashMap<String>());
			metas.put(meta, value);
		} else {
			Map<String, String> sub = this.substrateMap.get(block);
			if (sub == null)
				this.substrateMap.put(block, sub = new HashMap<String, String>());
			sub.put(substrate + "." + meta, value);
		}
	}

	private void expand(final Block block, final String value) {
		final List<MacroEntry> macro = macros.get(value);
		if (macro != null) {
			for (final MacroEntry entry : macro)
				put(block, entry.meta, entry.substrate, entry.value);
		} else {
			ModLog.debug("Unknown macro '%s'", value);
		}
	}

	@Override
	public void register(final String key, final String value) {
		final Matcher matcher = pattern.matcher(key);
		if (matcher.matches()) {
			final String blockName = matcher.group(1);
			final Block block = MCHelper.getBlockByName(blockName);
			if (block != null) {
				final int meta = matcher.group(2) == null ? -1 : Integer.parseInt(matcher.group(2));
				final String substrate = matcher.group(3);
				if (value.startsWith("#"))
					expand(block, value);
				else
					put(block, meta, substrate, value);
			} else {
				ModLog.debug("Unable to locate block for blockmap '%s'", blockName);
			}
		} else {
			ModLog.debug("Malformed key in blockmap '%s'", key);
		}
	}

	@Override
	public void collectData(final IBlockState state, final List<String> data) {
		String temp = this.getBlockMap(state);
		if (temp != null)
			data.add(temp);

		final Map<String, String> subs = this.substrateMap.get(state.getBlock());
		if (subs != null) {
			final int len = data.size();
			temp = "." + state.getBlock().getMetaFromState(state);
			for (final Entry<String, String> entry : subs.entrySet())
				if (entry.getKey().endsWith(temp))
					data.add(entry.getValue());
			if (data.size() == len) {
				temp = ".-1";
				for (final Entry<String, String> entry : subs.entrySet())
					if (entry.getKey().endsWith(temp))
						data.add(entry.getKey() + ":" + entry.getValue());
			}
		}
	}

	@Override
	public void clear() {
		this.metaMap.clear();
		this.substrateMap.clear();
	}
}
