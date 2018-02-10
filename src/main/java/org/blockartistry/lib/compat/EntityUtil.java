/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
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
package org.blockartistry.lib.compat;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public final class EntityUtil {
	
	private static Field nextStepDistance = ReflectionHelper.findField(Entity.class, "nextStepDistance",
			"field_70150_b");

	private EntityUtil() {
		
	}

	public static int getNextStepDistance(@Nonnull final Entity entity) {
		try {
			return nextStepDistance.getInt(entity);
		} catch (@Nonnull final Throwable t) {
			;
		}
		return 0;
	}

	public static void setNextStepDistance(@Nonnull final Entity entity, final int dist) {
		try {
			nextStepDistance.setInt(entity, dist);
		} catch (@Nonnull final Throwable t) {
			;
		}
	}

}
