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

package org.orecruncher.dsurround.registry.blockstate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.lib.BlockNameUtil;
import org.orecruncher.lib.BlockNameUtil.NameResult;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockStateMatcher {

	// We all know about air...
	public static final BlockStateMatcher AIR = new BlockStateMatcher(Blocks.AIR.getDefaultState());

	public static final String META_NAME = "_meta_";

	// Our contrived metadata property to make things go...
	private static final IProperty<Integer> META_PROP = new IProperty<Integer>() {
		@Override
		public String getName() {
			return META_NAME;
		}

		@Override
		public Collection<Integer> getAllowedValues() {
			return IntStream.rangeClosed(0, 15).boxed().collect(Collectors.toCollection(HashSet::new));
		}

		@Override
		public Class<Integer> getValueClass() {
			return Integer.class;
		}

		@Override
		public Optional<Integer> parseValue(String value) {
			try {
				final Integer i = Integer.parseInt(value);
				return Optional.of(i);
			} catch (@Nonnull final Throwable t) {
				// Can't parse
			}
			return Optional.absent();
		}

		@Override
		public String getName(Integer value) {
			return value.toString();
		}

	};

	// All instances will have this defined
	protected final Block block;

	// Item represenation of the block
	protected final Item item;

	// Sometimes an exact match of state is needed. The state being compared
	// would have to match all these properties.
	protected Reference2ObjectOpenHashMap<IProperty<?>, Object> props;

	public BlockStateMatcher(@Nonnull final IBlockState state) {
		this.block = state.getBlock();
		this.props = getPropsFromState(state);
		this.item = Item.getItemFromBlock(this.block);
	}

	protected BlockStateMatcher(@Nonnull final Block block) {
		this(block, null);
	}

	protected BlockStateMatcher(@Nonnull final Block block,
			@Nullable final Reference2ObjectOpenHashMap<IProperty<?>, Object> props) {
		this.block = block;
		this.props = props;
		this.item = Item.getItemFromBlock(this.block);
	}

	@Nonnull
	public Block getBlock() {
		return this.block;
	}

	@Nullable
	public List<IBlockState> asBlockStates() {
		final List<IBlockState> states = new ArrayList<>();
		final ImmutableList<IBlockState> valid = this.block.getBlockState().getValidStates();
		for (final IBlockState bs : valid) {
			if (matchProps(bs))
				states.add(bs);
		}
		return states;
	}

	protected boolean matchProps(@Nonnull final IBlockState state) {
		if (this.props == null)
			return true;
		for (final Entry<IProperty<?>, Object> entry : this.props.reference2ObjectEntrySet()) {
			final Object result;
			if (entry.getKey() == META_PROP) {
				result = state.getBlock().getMetaFromState(state);
			} else {
				result = state.getValue(entry.getKey());
			}
			if (!entry.getValue().equals(result))
				return false;
		}

		return true;
	}

	public boolean hasSubtypes() {
		return this.block.getBlockState().getValidStates().size() > 1;
	}

	@Nonnull
	public String getBlockName() {
		return Block.REGISTRY.getNameForObject(this.block).toString();
	}

	// private final static int TERM = 769;

	@Override
	public int hashCode() {
		return this.block.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof BlockStateMatcher) {
			final BlockStateMatcher m = (BlockStateMatcher) obj;
			// If the block types don't match, there will be no match
			if (this.block != m.block)
				return false;
			// If they both are null then they are equal
			if (this.props == null && m.props == null)
				return true;
			// If there are no props in this one then it is a real generic match
			if (this.props == null || this.props.size() == 0)
				return true;
			// This one has props. If the other doesn't then they don't match
			if (m.props == null || m.props.size() == 0)
				return false;

			// Should this should have the same or less properties than the other
			if (this.props.size() > m.props.size())
				return false;

			// Run 'em down doing compares
			for (final Entry<IProperty<?>, Object> entry : m.props.reference2ObjectEntrySet()) {
				final Object v = this.props.get(entry.getKey());
				if (v == null || !v.equals(entry.getValue()))
					return false;
			}

			return true;
		}
		return false;
	}

	@Nullable
	protected Reference2ObjectOpenHashMap<IProperty<?>, Object> getPropsFromState(@Nonnull final IBlockState state) {
		final Reference2ObjectOpenHashMap<IProperty<?>, Object> result = new Reference2ObjectOpenHashMap<>();

		if (state.getBlock().getBlockState().getValidStates().size() > 1) {
			final int m = state.getBlock().getMetaFromState(state);
			result.put(META_PROP, Integer.valueOf(m));
		}

		for (final IProperty<?> prop : state.getPropertyKeys()) {
			final Object o = state.getValue(prop);
			if (o != null) {
				result.put(prop, o);
			}
		}

		return result.size() > 0 ? result : null;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		builder.append(Block.REGISTRY.getNameForObject(this.block));
		if (this.props != null) {
			boolean doComma = false;
			builder.append('{');
			for (final Entry<IProperty<?>, Object> entry : this.props.reference2ObjectEntrySet()) {
				if (doComma)
					builder.append(',');
				else
					doComma = true;
				builder.append(entry.getKey().getName()).append('=').append(entry.getValue().toString());
			}
			builder.append('}');
		}

		return builder.toString();
	}

	@Nonnull
	public static BlockStateMatcher asGeneric(@Nonnull final IBlockState state) {
		return new BlockStateMatcher(state.getBlock());
	}

	@Nonnull
	public static BlockStateMatcher create(@Nonnull final IBlockState state) {
		return new BlockStateMatcher(state);
	}

	@Nullable
	public static BlockStateMatcher create(@Nonnull final String blockId) {
		return create(BlockNameUtil.parseBlockName(blockId));
	}

	@Nullable
	public static BlockStateMatcher create(@Nonnull final NameResult result) {
		if (result != null) {
			final Block block = result.getBlock();
			if (block != null) {
				final IBlockState defaultState = block.getDefaultState();
				final BlockStateContainer container = block.getBlockState();
				if (container.getValidStates().size() == 1) {
					// Easy case - it's always an identical match because there are no other
					// properties
					return new BlockStateMatcher(defaultState);
				}

				if (!result.hasNBT()) {
					// No NBT specification so this is a generic
					return new BlockStateMatcher(block);
				}

				final Reference2ObjectOpenHashMap<IProperty<?>, Object> props = new Reference2ObjectOpenHashMap<>();

				// Blow out the property list
				for (final String s : result.getNBT().getKeySet()) {
					// Stuff in our meta property if requested
					final IProperty<?> prop = s.equals(META_NAME) ? META_PROP : container.getProperty(s);
					if (prop != null) {
						final Optional<?> optional = prop.parseValue(result.getNBT().getString(s));
						if (optional.isPresent()) {
							props.put(prop, optional.get());
						} else {
							ModBase.log().warn("Property value %s not found for block %s", s, result.getBlockName());
						}
					} else {
						ModBase.log().warn("Property %s not found for block %s", s, result.getBlockName());
					}
				}

				// If we have properties it will be a partial generic type
				// match. Otherwise it will be an exact match on the default
				// state.
				if (props.size() > 0) {
					return new BlockStateMatcher(defaultState.getBlock(), props);
				} else {
					return new BlockStateMatcher(defaultState);
				}

			} else {
				ModBase.log().warn("Unable to locate block '%s' in the Forge registry", result.getBlockName());
			}

		}

		return null;
	}
}
