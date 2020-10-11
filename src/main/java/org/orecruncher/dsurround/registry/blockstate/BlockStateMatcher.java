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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.lib.BlockNameUtil;
import org.orecruncher.lib.BlockNameUtil.NameResult;

import com.google.common.base.Optional;

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
public final class BlockStateMatcher {

	private static final Reference2ObjectOpenHashMap<IProperty<?>, Object> EMPTY = new Reference2ObjectOpenHashMap<>(0);

	// We all know about air...
	public static final BlockStateMatcher AIR = BlockStateMatcher.create(Blocks.AIR.getDefaultState());

	// All instances will have this defined
	protected final Block block;

	// Sometimes an exact match of state is needed. The state being compared
	// would have to match all these properties.
	protected final Reference2ObjectOpenHashMap<IProperty<?>, Object> props;

	protected BlockStateMatcher(@Nonnull final IBlockState state) {
		this.block = state.getBlock();
		this.props = getPropsFromState(state);
	}

	protected BlockStateMatcher(@Nonnull final Block block) {
		this(block, EMPTY);
	}

	protected BlockStateMatcher(@Nonnull final Block block,
			@Nonnull final Reference2ObjectOpenHashMap<IProperty<?>, Object> props) {
		this.block = block;
		this.props = props;
	}

	@Nonnull
	public Block getBlock() {
		return this.block;
	}

	@Nonnull
	public List<IBlockState> asBlockStates() {
		//@formatter:off
		return this.block.getBlockState().getValidStates().stream()
			.filter(this::matchProps)
			.collect(Collectors.toList());
		//@formatter:on
	}

	protected boolean matchProps(@Nonnull final IBlockState state) {
		if (this.props.isEmpty())
			return true;
		for (final Entry<IProperty<?>, Object> entry : this.props.reference2ObjectEntrySet()) {
			final Object result = state.getValue(entry.getKey());
			if (!entry.getValue().equals(result))
				return false;
		}

		return true;
	}

	public boolean hasSubtypes() {
		return this.block.getBlockState().getValidStates().size() > 1;
	}

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

			// If both lists are empty its a match
			if (this.props.isEmpty() && m.props.isEmpty())
				return true;

			// If the other list is larger there isn't a way it's going
			// to match us.
			if (this.props.size() < m.props.size())
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

		final Reference2ObjectOpenHashMap<IProperty<?>, Object> result = new Reference2ObjectOpenHashMap<>(1);

		for (final IProperty<?> prop : state.getPropertyKeys()) {
			final Object o = state.getValue(prop);
			result.put(prop, o);
		}

		return result.size() == 0 ? EMPTY : result;
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
	public static BlockStateMatcher create(@Nullable final NameResult result) {
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

				if (!result.hasProperties()) {
					// No NBT specification so this is a generic
					return new BlockStateMatcher(block);
				}

				final Map<String, String> properties = result.getProperties();
				final Reference2ObjectOpenHashMap<IProperty<?>, Object> props = new Reference2ObjectOpenHashMap<>(
						properties.size());

				// Blow out the property list
				for (final java.util.Map.Entry<String, String> entry : properties.entrySet()) {
					// Stuff in our meta property if requested
					final String s = entry.getKey();
					final IProperty<?> prop = container.getProperty(s);
					if (prop != null) {
						final Optional<?> optional = prop.parseValue(entry.getValue());
						if (optional.isPresent()) {
							props.put(prop, optional.get());
						} else {
							final String allowed = getAllowedValues(block, s);
							ModBase.log().warn("Property value '%s' for property '%s' not found for block '%s'",
									entry.getValue(), s, result.getBlockName());
							ModBase.log().warn("Allowed values: %s", allowed);
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

	@Override
	@Nonnull
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(Block.REGISTRY.getNameForObject(this.block));
		if (!this.props.isEmpty()) {
			final String txt = this.props.reference2ObjectEntrySet().stream()
					.map(e -> e.getKey().getName() + "=" + getValue(this.block, e.getKey().getName(), e.getValue()))
					.collect(Collectors.joining(","));
			builder.append('[').append(txt).append(']');
		}

		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	protected static <T extends Comparable<T>> String getValue(@Nonnull final Block block,
			@Nonnull final String propName, @Nonnull final Object val) {
		final BlockStateContainer container = block.getBlockState();
		final IProperty<T> prop = (IProperty<T>) container.getProperty(propName);
		return prop.getName((T) val);
	}

	@SuppressWarnings("unchecked")
	protected static <T extends Comparable<T>> String getAllowedValues(final Block block, final String propName) {
		final BlockStateContainer container = block.getBlockState();
		final IProperty<T> prop = (IProperty<T>) container.getProperty(propName);
		final List<String> result = new ArrayList<>();
		for (final T v : prop.getAllowedValues()) {
			result.add(prop.getName(v));
		}
		return String.join(",", result);
	}

}
