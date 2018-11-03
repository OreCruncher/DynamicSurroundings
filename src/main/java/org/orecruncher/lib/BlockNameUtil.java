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
package org.orecruncher.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/*
 * Utility functions for manipulating blockName names.
 */
public final class BlockNameUtil {

	private BlockNameUtil() {

	}

	// https://www.regexplanet.com/advanced/java/index.html
	private static final Pattern pattern = Pattern.compile("([\\w\\-]+:[\\w\\.\\-/]+)(\\{.*\\})?\\+?(\\w+)?");

	public final static class NameResult {

		/*
		 * Name of the blockName in standard domain:path form.
		 */
		protected String blockName;

		/*
		 * The block from the registries
		 */
		protected Block block;

		/*
		 * The parsed NBTTagCompound after the blockName name, if present
		 */
		protected NBTTagCompound nbt;

		/*
		 * Extra information that may have been appended at the end
		 */
		private String extras;

		NameResult() {
			
		}
		
		NameResult(final Matcher matcher) throws NBTException {
			this.blockName = matcher.group(1);
			final Block proposed = Block.REGISTRY.getObject(new ResourceLocation(this.blockName));
			this.block = proposed == Blocks.AIR ? null : proposed;

			String temp = matcher.group(2);
			if (!StringUtils.isEmpty(temp)) {
				this.nbt = JsonToNBT.getTagFromJson(matcher.group(2));
			}

			this.extras = matcher.group(3);
		}

		@Nonnull
		public String getBlockName() {
			return this.blockName;
		}

		@Nullable
		public Block getBlock() {
			return this.block;
		}

		public boolean hasNBT() {
			return this.nbt != null;
		}

		@Nullable
		public NBTTagCompound getNBT() {
			return this.nbt;
		}

		public boolean hasExtras() {
			return this.extras != null;
		}

		@Nullable
		public String getExtras() {
			return this.extras;
		}

		@Override
		@Nonnull
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append('[');
			if (getBlock() == null)
				builder.append("*INVALID* ");
			builder.append(getBlockName());
			if (hasNBT())
				builder.append(getNBT().toString());
			builder.append(']');
			return builder.toString();
		}
	}

	/*
	 * Parses the blockName name passed in and returns the result of that parsing.
	 * If null is returned it means there was some sort of error.
	 */
	@Nullable
	public static NameResult parseBlockName(@Nonnull final String blockName) {
		try {
			final Matcher matcher = pattern.matcher(blockName);
			return matcher.matches() ? new NameResult(matcher) : null;
		} catch (final Exception ex) {
			LibLog.log().error(String.format("Unable to parse '%s'", blockName), ex);
		}
		return null;
	}

}
