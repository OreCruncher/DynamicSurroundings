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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.lib.math.MathStuff;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class ConfigProcessor {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface Parameter {
		String category();

		String property();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface LangKey {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface Comment {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface DefaultValue {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface RangeInt {
		int min() default Integer.MIN_VALUE;

		int max() default Integer.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface RangeFloat {
		float min() default Float.MIN_VALUE;

		float max() default Float.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface RestartRequired {
		boolean world() default true;

		boolean server() default false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public static @interface Hidden {

	}

	public static void process(@Nonnull final Configuration config, @Nonnull final Class<?> clazz) {
		process(config, clazz, null);
	}

	public static void process(@Nonnull final Configuration config, @Nonnull final Class<?> clazz,
			@Nullable final Object parameters) {
		for (final Field field : clazz.getFields()) {
			final Parameter annotation = field.getAnnotation(Parameter.class);
			if (annotation != null) {
				final String category = annotation.category();
				final String property = annotation.property();
				final String language = field.getAnnotation(LangKey.class) != null
						? field.getAnnotation(LangKey.class).value()
						: null;
				final String comment = field.getAnnotation(Comment.class) != null
						? field.getAnnotation(Comment.class).value()
						: null;
				final String defaultValue = field.getAnnotation(DefaultValue.class) != null
						? field.getAnnotation(DefaultValue.class).value()
						: null;

				try {
					final Object fieldValue = field.get(parameters);

					if (fieldValue instanceof Boolean) {
						final boolean dv = StringUtils.isEmpty(defaultValue) ? false : Boolean.valueOf(defaultValue);
						field.set(parameters, config.getBoolean(property, category, dv, comment));
					} else if (fieldValue instanceof Integer) {
						int minInt = Integer.MIN_VALUE;
						int maxInt = Integer.MAX_VALUE;
						final RangeInt mmi = field.getAnnotation(RangeInt.class);
						if (mmi != null) {
							minInt = mmi.min();
							maxInt = mmi.max();
						}
						final int dv = StringUtils.isEmpty(defaultValue) ? MathStuff.clamp(0, minInt, maxInt)
								: Integer.valueOf(defaultValue);
						field.set(parameters, config.getInt(property, category, dv, minInt, maxInt, comment));
					} else if (fieldValue instanceof Float) {
						float minFloat = Float.MIN_VALUE;
						float maxFloat = Float.MAX_VALUE;
						final RangeFloat mmf = field.getAnnotation(RangeFloat.class);
						if (mmf != null) {
							minFloat = mmf.min();
							maxFloat = mmf.max();
						}
						final float dv = StringUtils.isEmpty(defaultValue) ? MathStuff.clamp(0F, minFloat, maxFloat)
								: Float.valueOf(defaultValue);
						field.set(parameters, config.getFloat(property, category, dv, minFloat, maxFloat, comment));
					} else if (fieldValue instanceof String) {
						field.set(parameters, config.getString(property, category, defaultValue, comment));
					} else if (fieldValue instanceof String[]) {
						field.set(parameters, config.getStringList(property, category,
								StringUtils.split(defaultValue, ','), comment));
					}

					// Configure other settings
					final Property prop = config.getCategory(category).get(property);
					if (!StringUtils.isEmpty(language))
						prop.setLanguageKey(language);
					if (field.getAnnotation(RestartRequired.class) != null) {
						final RestartRequired restart = field.getAnnotation(RestartRequired.class);
						prop.setRequiresMcRestart(restart.server());
						prop.setRequiresWorldRestart(restart.world());
					} else {
						prop.setRequiresMcRestart(false);
						prop.setRequiresWorldRestart(false);
					}

					prop.setShowInGui(field.getAnnotation(Hidden.class) == null);

				} catch (final Throwable t) {
					LibLog.log().error("Unable to parse configuration", t);
				}
			}
		}
	}
}
