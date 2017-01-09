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

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import net.minecraft.block.Block;

public final class BlockInfo {

	private static final Pattern pattern = Pattern.compile("^([^:]+:[^:]+)(?::?)([[\\d]+|[\\*]]*)");
	
	public static final int GENERIC = -1;
	private static final int NO_SUBTYPE = -100;

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

	@Override
	public boolean equals(final Object obj) {
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
		if (this.meta == GENERIC)
			builder.append(":*");
		else if (this.meta != NO_SUBTYPE)
			builder.append(':').append(this.meta);
		return builder.toString();
	}
}
