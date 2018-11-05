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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;

public class Translations {

	/**
	 * Default language that the mod has for translation strings. Typically American
	 * English (en_us).
	 */
	public static final String DEFAULT_LANGUAGE = "en_us";

	private Map<String, String> lookup = new Object2ObjectOpenHashMap<>();

	protected void merge(@Nonnull final InputStream stream) throws IOException {
		net.minecraftforge.fml.common.FMLCommonHandler.instance().loadLanguage(this.lookup, stream);
	}

	private static List<String> getLanguageList(@Nullable String... lang) {
		final List<String> result = new ArrayList<>();
		if (lang != null && lang.length > 0) {
			for (final String s : lang)
				if (result.contains(s))
					result.add(s);
		} else {
			final String mcLang = Minecraft.getMinecraft().gameSettings.language;
			result.add(DEFAULT_LANGUAGE);
			if (!result.contains(mcLang))
				result.add(mcLang);
		}
		return result;
	}

	public void load(@Nonnull final String assetRoot, @Nullable final String... lang) {
		final List<String> langList = getLanguageList(lang);
		for (final String l : langList) {
			final String assetName = StringUtils.appendIfMissing(assetRoot, "/") + l + ".lang";
			try (final InputStream stream = Translations.class.getResourceAsStream(assetName)) {
				if (stream != null)
					merge(stream);
			} catch (final Throwable t) {
				LibLog.log().error("Error merging language " + assetName, t);
			}
		}
	}

	@Nonnull
	public String format(@Nonnull final String translateKey, @Nullable final Object... parameters) {
		final String xlated = this.lookup.get(translateKey);
		return xlated == null ? translateKey : String.format(xlated, parameters);
	}

	@Nonnull
	public String loadString(@Nonnull final String translateKey) {
		final String xlated = this.lookup.get(translateKey);
		return xlated == null ? translateKey : xlated;
	}

	public void put(@Nonnull final String key, @Nonnull final String value) {
		this.lookup.put(key, value);
	}

	public void forAll(@Nonnull final Predicate<Entry<String, String>> pred) {
		for (final Entry<String, String> e : this.lookup.entrySet())
			pred.apply(e);
	}

	public void transform(@Nonnull final Function<Entry<String, String>, String> func) {
		final Map<String, String> old = this.lookup;
		this.lookup = new Object2ObjectOpenHashMap<>();
		for (final Entry<String, String> e : old.entrySet()) {
			this.lookup.put(e.getKey(), func.apply(e));
		}
	}
}
