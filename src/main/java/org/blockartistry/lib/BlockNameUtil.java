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
package org.blockartistry.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

/*
 * Utility functions for manipulating block names.
 */
public final class BlockNameUtil {

	private BlockNameUtil() {

	}

	/*
	 * Sentinal value for metaData that indicates the name contained a wildcard
	 * token for the metadata slot ('*').
	 */
	private static final int GENERIC = -1;

	/*
	 * Sentinal value for metaData that indicates that the name did not contain any
	 * metaData specification.
	 */
	private static final int META_NOT_SPECIFIED = -100;

	/*
	 * Value for the control field that indicates no control code was specified.
	 */
	private static final char NO_CONTROL_CODE = '\0';

	// https://www.regexplanet.com/advanced/java/index.html
	private static final Pattern pattern = Pattern.compile("(\\W)?(\\w+:[\\w\\.]+)[\\^|:]?(\\d+|\\*)?\\+?(\\w+)?");

	public final static class NameResult {

		/*
		 * Control code at the beginning of the string, if any. A control code is a
		 * character that matches a regex \W. It is up to the calling code to decide
		 * what to do if anything about the code.
		 */
		private final char control;

		/*
		 * Name of the block in standard domain:path form.
		 */
		private final String block;

		/*
		 * Metadata specified in the name. If no metadata was specified it will be set
		 * to META_NOT_SPECIFIED. If a wildcard generic was specified it will be
		 * GENERIC. Otherwise it will be the integer value specified. It is up to the
		 * calling routine to ensure that the information is valid.
		 */
		private final int metaData;

		/*
		 * Extra tokens tacked onto the end of the string. It is up to the calling
		 * routine to make sense of the data.
		 */
		private final String extras;

		NameResult(final Matcher matcher) {
			String temp = matcher.group(1);
			if (!StringUtils.isEmpty(temp))
				this.control = temp.charAt(0);
			else
				this.control = NO_CONTROL_CODE;

			this.block = matcher.group(2);

			temp = matcher.group(3);
			if (StringUtils.isEmpty(temp))
				this.metaData = META_NOT_SPECIFIED;
			else if ("*".equals(temp))
				this.metaData = GENERIC;
			else
				this.metaData = Integer.parseInt(temp);

			this.extras = matcher.group(4);
		}

		public boolean hasControlCode() {
			return this.control != NO_CONTROL_CODE;
		}

		public boolean isGeneric() {
			return this.metaData == GENERIC;
		}

		public boolean noMetadataSpecified() {
			return this.metaData == META_NOT_SPECIFIED;
		}

		public boolean hasExtras() {
			return !StringUtils.isEmpty(this.extras);
		}

		public char getControlCode() {
			return this.control;
		}

		@Nonnull
		public String getBlockName() {
			return this.block;
		}

		public int getMetadata() {
			return this.metaData;
		}

		@Nullable
		public String getExtras() {
			return this.extras;
		}

		@Override
		@Nonnull
		public String toString() {
			final String t;
			if (this.metaData == GENERIC)
				t = "GENERIC";
			else if (this.metaData == META_NOT_SPECIFIED)
				t = "META_NOT_SPECIFIED";
			else
				t = Integer.toString(this.metaData);

			return String.format("['%c', %s, %s, '%s']", this.control, this.block, t, this.extras);
		}
	}

	/*
	 * Parses the block name passed in and returns the result of that parsing. If
	 * null is returned it means there was some sort of error.
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
