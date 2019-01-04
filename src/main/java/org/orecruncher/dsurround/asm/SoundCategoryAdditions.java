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

package org.orecruncher.dsurround.asm;

import java.lang.reflect.Field;
import java.util.Map;

import javax.annotation.Nonnull;

import org.objectweb.asm.tree.ClassNode;

import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@SuppressWarnings("deprecation")
public class SoundCategoryAdditions extends Transmorgrifier {

	public SoundCategoryAdditions() {
		super("net.minecraft.client.settings.GameSettings");
	}

	@Override
	public String name() {
		return "SoundCategory Additions";
	}

	@Override
	public boolean transmorgrify(final ClassNode cn) {

		if (SoundCategory.getByName("ds_footsteps") == null) {
			// Add our new sound categories
			final SoundCategory fs = EnumHelper.addEnum(SoundCategory.class, "DS_FOOTSTEPS",
					new Class<?>[] { String.class }, "ds_footsteps");
			final SoundCategory b = EnumHelper.addEnum(SoundCategory.class, "DS_BIOME", new Class<?>[] { String.class },
					"ds_biome");

			// Update the internal cached list
			try {
				final Field f = ReflectionHelper.findField(SoundCategory.class, "SOUND_CATEGORIES", "field_187961_k");
				@SuppressWarnings("unchecked")
				final Map<String, SoundCategory> theMap = (Map<String, SoundCategory>) f.get(null);
				theMap.put(fs.getName(), fs);
				theMap.put(b.getName(), b);
			} catch (@Nonnull final Throwable t) {
				Transformer.log().error("Unable to update SoundCategory map: {}", t.toString());
				return false;
			}
		} else {
			Transformer.log().warn("Attempt to transmorgrify SoundCategory a second time");
		}

		return true;
	}

}
