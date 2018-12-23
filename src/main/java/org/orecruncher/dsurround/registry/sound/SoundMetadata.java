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

package org.orecruncher.dsurround.registry.sound;

import java.util.List;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.registry.config.SoundMetadataConfig;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import net.minecraft.util.SoundCategory;

public class SoundMetadata {

	protected final String title;
	protected final SoundCategory category;
	protected final List<String> credits;

	protected SoundMetadata(@Nonnull final SoundMetadataConfig cfg) {
		this.title = cfg.title;
		this.category = MoreObjects.firstNonNull(translate(cfg.category), SoundCategory.NEUTRAL);
		this.credits = ImmutableList.copyOf(cfg.credits);
	}

	protected SoundCategory translate(@Nonnull final String catName) {
		if ("ds_biome".equals(catName))
			return SoundRegistry.BIOME;
		if ("ds_footsteps".equals(catName))
			return SoundRegistry.FOOTSTEPS;
		return SoundCategory.getByName(catName);
	}

	public String getTitle() {
		return this.title;
	}

	public SoundCategory getCategory() {
		return this.category;
	}

	public List<String> getCredits() {
		return this.credits;
	}

}
