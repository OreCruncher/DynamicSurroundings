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

package org.blockartistry.mod.DynSurround.registry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class BlockInfo {

	private static final Pattern pattern = Pattern.compile("^([^:]+:[^:]+)(?::?)([[\\d]+|[\\*]]*)");
	
	public static final int GENERIC = -1;
	protected static final int NO_SUBTYPE = -100;

	protected Block block;
	protected int meta;

	public BlockInfo(@Nonnull final Block block, final int meta) {
		this.block = block;
		this.meta = meta;
	}

	public BlockInfo(@Nonnull final Block block) {
		this(block, NO_SUBTYPE);
	}

	@Nonnull
	public Block getBlock() {
		return this.block;
	}

	public int getMeta() {
		return this.meta;
	}

	public boolean isGeneric() {
		return this.meta == GENERIC;
	}

	public boolean hasNoSubtypes() {
		return this.meta == NO_SUBTYPE;
	}

	private final static int TERM = 3079;
	
	@Override
	public int hashCode() {
		return this.block.hashCode() ^ (this.meta * TERM);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof BlockInfo))
			return false;
		final BlockInfo key = (BlockInfo) obj;
		return this.block == key.block && this.meta == key.meta;
	}

	@Nullable
	public static BlockInfo create(@Nonnull final String blockId) {
		String workingName = blockId;
		int subType = NO_SUBTYPE;

		// Parse out the possible subtype from the end of the string
		final Matcher m = pattern.matcher(blockId);
		if (m.matches()) {
			workingName = m.group(1);
			final String num = m.group(2);

			if (num != null && !num.isEmpty()) {
				if ("*".compareTo(num) == 0)
					subType = GENERIC;
				else {
					try {
						subType = Integer.parseInt(num);
					} catch (Exception e) {
						// It appears malformed - assume the incoming name
						// isthe real name and continue.
						;
					}
				}
			}
		} else {
			ModLog.warn("Unkown block id [%s]", blockId);
		}

		final Block block = MCHelper.getBlockByName(workingName);
		if (subType == NO_SUBTYPE && MCHelper.hasVariants(block))
			subType = GENERIC;
		return block != null ? new BlockInfo(block, subType) : null;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(MCHelper.nameOf(this.block));
		if (isGeneric())
			builder.append(":*");
		else if (!hasNoSubtypes())
			builder.append(':').append(this.meta);
		return builder.toString();
	}

	public static class BlockInfoMutable extends BlockInfo {

		public BlockInfoMutable() {
			super(null);
		}

		public void set(@Nonnull final IBlockState state) {
			this.block = state.getBlock();
			this.meta = MCHelper.hasVariants(this.block) ? state.getBlock().getMetaFromState(state) : NO_SUBTYPE;
		}
		
		public void set(@Nonnull final BlockInfo info) {
			this.block = info.block;
			this.meta = info.meta;
		}

		public void makeGeneric() {
			this.meta = GENERIC;
		}

	}
}
