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

package org.blockartistry.DynSurround.registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.lib.BlockNameUtil;
import org.blockartistry.lib.BlockNameUtil.NameResult;
import org.blockartistry.lib.MCHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockInfo {

	public static final int GENERIC = -1;
	public static final int NO_SUBTYPE = -100;

	protected Block block;
	protected int meta;
	protected int specialMeta;

	public BlockInfo(@Nonnull final Block block, final int meta) {
		this.block = block;
		this.meta = meta;
		this.specialMeta = NO_SUBTYPE;
	}

	public BlockInfo(@Nonnull final Block block) {
		this(block, NO_SUBTYPE);
	}

	public BlockInfo(@Nonnull final IBlockState state) {
		configure(state);
	}

	protected void configure(@Nonnull final IBlockState state) {
		this.block = state.getBlock();
		if (this.block == Blocks.AIR) {
			this.meta = NO_SUBTYPE;
			this.specialMeta = NO_SUBTYPE;
		} else {
			this.meta = MCHelper.hasVariants(this.block) ? state.getBlock().getMetaFromState(state) : NO_SUBTYPE;
			this.specialMeta = MCHelper.hasSpecialMeta(this.block) ? state.getBlock().getMetaFromState(state)
					: NO_SUBTYPE;
		}
	}

	@Nonnull
	public Block getBlock() {
		return this.block;
	}

	public int getMeta() {
		return this.meta;
	}

	public int getSpecialMeta() {
		return this.specialMeta;
	}

	public boolean isGeneric() {
		return this.meta == GENERIC;
	}

	public boolean hasSubTypes() {
		return this.meta != NO_SUBTYPE;
	}

	public boolean hasSpecialMeta() {
		return this.specialMeta != NO_SUBTYPE;
	}

	private final static int TERM = 769;

	@Override
	public int hashCode() {
		return this.block.hashCode() ^ (this.meta * TERM);
	}

	@Override
	public boolean equals(final Object obj) {
		return this.block == ((BlockInfo)obj).block && this.meta == ((BlockInfo)obj).meta;
	}

	@Nullable
	public static BlockInfo create(@Nonnull final String blockId) {
		String workingName = blockId;
		int subType = NO_SUBTYPE;

		final NameResult result = BlockNameUtil.parseBlockName(blockId);
		if (result != null) {

			workingName = result.getBlockName();
			
			if(result.isGeneric())
				subType = GENERIC;
			else if(result.noMetadataSpecified())
				subType = NO_SUBTYPE;
			else
				subType = result.getMetadata();

		} else {
			DSurround.log().warn("Unkown block id [%s]", blockId);
		}

		final Block block = MCHelper.getBlockByName(workingName);
		if (subType == NO_SUBTYPE && MCHelper.hasVariants(block))
			subType = GENERIC;
		return block != null ? new BlockInfo(block, subType) : null;
	}

	@Override
	public String toString() {
		if (this.block == null)
			return "UNKNOWN";
		final StringBuilder builder = new StringBuilder();
		builder.append(MCHelper.nameOf(this.block));
		if (isGeneric())
			builder.append(":*");
		else if (hasSubTypes())
			builder.append(':').append(this.meta);
		else if (hasSpecialMeta())
			builder.append('[').append(this.specialMeta).append(']');
		return builder.toString();
	}

	public static class BlockInfoMutable extends BlockInfo {

		protected IBlockState lastState;
		protected int originalMeta;

		public BlockInfoMutable() {
			super((Block) null);
			
			this.originalMeta = this.meta;
		}

		public BlockInfoMutable set(@Nonnull final IBlockState state) {
			if(this.lastState == state) {
				this.meta = this.originalMeta;
			} else {
				configure(state);
				this.originalMeta = this.meta;
			}
			return this;
		}

		public BlockInfoMutable set(@Nonnull final BlockInfo info) {
			this.block = info.block;
			this.meta = info.meta;
			this.specialMeta = info.specialMeta;
			this.originalMeta = this.meta;
			return this;
		}

		public BlockInfoMutable asGeneric() {
			this.meta = GENERIC;
			return this;
		}

		public BlockInfoMutable asSpecial() {
			this.meta = this.specialMeta;
			return this;
		}

	}
}
