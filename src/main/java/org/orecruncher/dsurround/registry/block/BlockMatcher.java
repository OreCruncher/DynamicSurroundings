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

package org.orecruncher.dsurround.registry.block;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockMatcher {

	// We all know about air...
	public static final BlockMatcher AIR = new BlockMatcher(Blocks.AIR.getDefaultState());

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

	// If it is a generic match this will be null, otherwise the state will be set
	protected final IBlockState state;

	// Sometimes an exact match of state is needed. The state being compared
	// would have to match all these properties.
	protected Reference2ObjectOpenHashMap<IProperty<?>, Object> props;

	protected BlockMatcher(@Nonnull final IBlockState state) {
		this.block = state.getBlock();
		this.state = state;
		this.props = null;
	}

	protected BlockMatcher(@Nonnull final Block block) {
		this.block = block;
		this.state = null;
		this.props = null;
	}

	protected BlockMatcher(@Nonnull final Block block,
			@Nonnull final Reference2ObjectOpenHashMap<IProperty<?>, Object> props) {
		this(block);
		this.props = props;
	}

	@Nonnull
	public Block getBlock() {
		return this.block;
	}

	@Nullable
	public List<IBlockState> asBlockStates() {
		final List<IBlockState> states = new ArrayList<>();
		if (this.state != null) {
			states.add(this.state);
		} else {
			final ImmutableList<IBlockState> valid = this.block.getBlockState().getValidStates();
			for (final IBlockState bs : valid) {
				if (matchProps(bs))
					states.add(bs);
			}
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

	public boolean isGeneric() {
		return this.state == null;
	}

	@Nonnull
	public String getBlockName() {
		return Block.REGISTRY.getNameForObject(this.block).toString();
	}

	@Nullable
	public IBlockState getBlockstate() {
		return this.state;
	}

	// private final static int TERM = 769;

	@Override
	public int hashCode() {
		if (this.state == null)
			return this.block.hashCode();
		return this.state.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		final BlockMatcher m = (BlockMatcher) obj;
		// If the block types don't match, there will be no match
		if (this.block != m.block)
			return false;
		// An exact state means its the same identical block state
		if (this.state == m.state && this.state != null)
			return true;
		// Both have block states set but they do not match
		if ((this.state != null && m.state != null) && (this.state != m.state))
			return false;
		// If one has no props and the other has state specified
		// it's a match. (The block type has already been determined
		// to match.)
		if ((this.props == null && m.state != null) || (this.state != null && m.props == null))
			return true;
		// If both of the props are not assigned its an identical
		// match
		if (this.props == null && m.props == null)
			return true;

		// Not sure what we have left - going by feel :)
		// TODO: Make sure this is complete
		if (this.state != null && m.matchProps(this.state) || m.state != null && matchProps(m.state))
			return true;

		// At this point I think all that is left is both having properties and needing
		// to make sure both sets are equal.
		if (this.props == null || m.props == null)
			return false;

		// Should this should have the same or less properties than the other
		if (this.props.size() > m.props.size())
			return false;

		// Run em down doing compares
		for (final Entry<IProperty<?>, Object> entry : m.props.reference2ObjectEntrySet()) {
			final Object v = this.props.get(entry.getKey());
			if (v == null || !v.equals(entry.getValue()))
				return false;
		}

		return true;
	}

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		if (this.state != null) {
			builder.append(this.state.toString());
		} else {
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
		}

		return builder.toString();
	}

	@Nonnull
	public static BlockMatcher asGeneric(@Nonnull final IBlockState state) {
		return new BlockMatcher(state.getBlock());
	}

	@Nonnull
	public static BlockMatcher create(@Nonnull final IBlockState state) {
		return new BlockMatcher(state);
	}

	@Nullable
	public static BlockMatcher create(@Nonnull final String blockId) {
		return create(BlockNameUtil.parseBlockName(blockId));
	}

	@Nullable
	public static BlockMatcher create(@Nonnull final NameResult result) {
		if (result != null) {
			final Block block = result.getBlock();
			if (block != null) {
				final IBlockState defaultState = block.getDefaultState();
				final BlockStateContainer container = block.getBlockState();
				if (container.getValidStates().size() == 1) {
					// Easy case - it's always an identical match because there are no other
					// properties
					return new BlockMatcher(defaultState);
				}

				if (!result.hasNBT()) {
					// No NBT specification so this is a generic
					return new BlockMatcher(block);
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
					return new BlockMatcher(defaultState.getBlock(), props);
				} else {
					return new BlockMatcher(defaultState);
				}

			} else {
				ModBase.log().warn("Unable to locate block '%s' in the Forge registry", result.getBlockName());
			}

		}

		return null;
	}
}
