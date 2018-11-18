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

package org.orecruncher.dsurround.client.footsteps.implem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.registry.blockstate.BlockStateMatcher;
import org.orecruncher.lib.BlockNameUtil;
import org.orecruncher.lib.BlockNameUtil.NameResult;

import com.google.common.base.Optional;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockMap {

	private final AcousticsManager acousticsManager;
	private final BlockAcousticMap metaMap;
	private final Map<Substrate, BlockAcousticMap> substrateMap = new EnumMap<>(Substrate.class);

	private static class MacroEntry {
		public final String propertyName;
		public final String propertyValue;
		public final String substrate;
		public final String value;

		public MacroEntry(@Nonnull final String substrate, @Nonnull final String value) {
			this(null, null, substrate, value);
		}

		public MacroEntry(@Nullable final String propertyName, @Nullable final String propertyValue,
				@Nonnull final String substrate, @Nonnull final String value) {
			this.propertyName = propertyName;
			this.propertyValue = propertyValue;
			this.substrate = substrate;
			this.value = value;
		}

		@Nonnull
		public IBlockState withProperty(@Nonnull final IBlockState state) {
			if (this.propertyName == null)
				return state;
			final IProperty<?> iProp = state.getBlock().getBlockState().getProperty(this.propertyName);
			if (iProp != null)
				return setValueHelper(state, iProp, this.propertyValue);
			return state;
		}

		private static <T extends Comparable<T>> IBlockState setValueHelper(IBlockState state, IProperty<T> prop,
				String value) {
			final Optional<T> optional = prop.parseValue(value);
			if (optional.isPresent())
				return state.withProperty(prop, optional.get());
			return state;
		}

	}

	private static final Map<String, List<MacroEntry>> macros = new Object2ObjectOpenHashMap<>();

	static {

		final MacroEntry MESSY = new MacroEntry("messy", "MESSY_GROUND");

		List<MacroEntry> entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(MESSY);
		entries.add(new MacroEntry("foliage", "straw"));
		macros.put("#sapling", entries);
		macros.put("#reed", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "leaves"));
		entries.add(MESSY);
		entries.add(new MacroEntry("foliage", "brush"));
		macros.put("#plant", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "leaves"));
		entries.add(MESSY);
		entries.add(new MacroEntry("foliage", "brush_straw_transition"));
		macros.put("#bush", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("bigger", "bluntwood"));
		macros.put("#fence", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "straw"));
		entries.add(MESSY);
		entries.add(new MacroEntry("foliage", "straw"));
		macros.put("#vine", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(new MacroEntry("carpet", "rug"));
		macros.put("#moss", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(MESSY);
		entries.add(new MacroEntry("age", "0", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "1", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "2", "foliage", "brush"));
		entries.add(new MacroEntry("age", "3", "foliage", "brush"));
		entries.add(new MacroEntry("age", "4", "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry("age", "5", "foliage", "brush_straw_transition"));
		entries.add(new MacroEntry("age", "6", "foliage", "straw"));
		entries.add(new MacroEntry("age", "7", "foliage", "straw"));
		macros.put("#wheat", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(MESSY);
		entries.add(new MacroEntry("age", "0", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "1", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "2", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "3", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "4", "foliage", "brush"));
		entries.add(new MacroEntry("age", "5", "foliage", "brush"));
		entries.add(new MacroEntry("age", "6", "foliage", "brush"));
		entries.add(new MacroEntry("age", "7", "foliage", "brush"));
		macros.put("#crop", entries);

		entries = new ArrayList<>();
		entries.add(new MacroEntry(null, "NOT_EMITTER"));
		entries.add(MESSY);
		entries.add(new MacroEntry("age", "0", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "1", "foliage", "NOT_EMITTER"));
		entries.add(new MacroEntry("age", "2", "foliage", "brush"));
		entries.add(new MacroEntry("age", "3", "foliage", "brush"));
		macros.put("#beets", entries);
	}

	public BlockMap(@Nonnull final AcousticsManager manager) {
		this.acousticsManager = manager;
		this.metaMap = new BlockAcousticMap(bs -> ClientRegistry.FOOTSTEPS.resolve(bs));
	}

	public boolean hasAcoustics(@Nonnull final IBlockState state) {
		return this.metaMap.getBlockAcoustics(state) != null;
	}

	@Nullable
	public IAcoustic[] getBlockAcoustics(@Nonnull final IBlockState state) {
		return this.getBlockAcoustics(state, null);
	}

	@Nullable
	public IAcoustic[] getBlockAcoustics(@Nonnull final IBlockState state, @Nullable final Substrate substrate) {
		// Walking an edge of a block can produce this
		if (state == Blocks.AIR.getDefaultState())
			return AcousticsManager.NOT_EMITTER;
		if (substrate != null) {
			final BlockAcousticMap sub = this.substrateMap.get(substrate);
			return sub != null ? sub.getBlockAcoustics(state) : null;
		}
		return this.metaMap.getBlockAcoustics(state);
	}

	private void put(@Nonnull final BlockStateMatcher info, @Nonnull final String substrate,
			@Nonnull final String value) {

		final Substrate s = Substrate.get(substrate);
		final IAcoustic[] acoustics = this.acousticsManager.compileAcoustics(value);

		if (s == null) {
			this.metaMap.put(info, acoustics);
		} else {
			BlockAcousticMap sub = this.substrateMap.get(s);
			if (sub == null)
				this.substrateMap.put(s, sub = new BlockAcousticMap());
			sub.put(info, acoustics);
		}
	}

	private void expand(@Nonnull final BlockStateMatcher info, @Nonnull final String value) {
		final List<MacroEntry> macro = macros.get(value);
		if (macro != null) {
			for (final MacroEntry entry : macro) {
				final IBlockState state = entry.withProperty(info.getBlock().getDefaultState());
				put(new BlockStateMatcher(state), entry.substrate, entry.value);
			}
		} else {
			ModBase.log().debug("Unknown macro '%s'", value);
		}
	}

	public void register(@Nonnull final String key, @Nonnull final String value) {
		final NameResult name = BlockNameUtil.parseBlockName(key);
		if (name != null) {
			final String blockName = name.getBlockName();
			final Block block = name.getBlock();
			if (block == null) {
				ModBase.log().debug("Unable to locate block for blockMap '%s'", blockName);
			} else {
				final BlockStateMatcher matcher = BlockStateMatcher.create(name);
				if (value.startsWith("#")) {
					expand(matcher, value);
				} else {
					final String substrate = name.getExtras();
					put(matcher, substrate, value);
				}
			}
		} else {
			ModBase.log().warn("Malformed key in blockMap '%s'", key);
		}
	}

	@Nonnull
	private static String combine(@Nonnull final IAcoustic[] acoustics) {
		return String.join(",", Arrays.stream(acoustics).map(IAcoustic::getAcousticName).toArray(String[]::new));
	}

	public void collectData(@Nonnull final World world, @Nonnull final IBlockState state, @Nonnull final BlockPos pos,
			@Nonnull final List<String> data) {

		final IAcoustic[] temp = getBlockAcoustics(state);
		if (temp != null)
			data.add(combine(temp));

		for (final Entry<Substrate, BlockAcousticMap> e : this.substrateMap.entrySet()) {
			final IAcoustic[] acoustics = e.getValue().getBlockAcoustics(state);
			if (acoustics != null)
				data.add(e.getKey() + ":" + combine(acoustics));
		}
	}

	public void clear() {
		this.metaMap.clear();
		this.substrateMap.clear();
	}

}
