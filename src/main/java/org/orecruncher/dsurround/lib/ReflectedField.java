/*
 * Licensed under the MIT License (MIT).
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
package org.orecruncher.dsurround.lib;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.orecruncher.dsurround.ModBase;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

public class ReflectedField {

	protected final String className;
	protected final String fieldName;
	protected final Field field;

	protected ReflectedField(@Nonnull final String className, @Nonnull final String fieldName,
			@Nullable final String obfName) {
		this.className = className;
		this.fieldName = fieldName;
		this.field = resolve(className, fieldName, obfName);
	}

	protected ReflectedField(@Nonnull final Class<?> clazz, @Nonnull final String fieldName,
			@Nullable final String obfName) {
		Preconditions.checkNotNull(clazz);
		Preconditions.checkArgument(StringUtils.isNotEmpty(fieldName), "Field name cannot be empty");
		this.className = clazz.getName();
		this.fieldName = fieldName;
		this.field = resolve(clazz, fieldName, obfName);
	}

	@Nullable
	private static Field resolve(@Nonnull final String className, @Nonnull final String fieldName,
			@Nullable final String obfName) {
		try {
			return resolve(Class.forName(className), fieldName, obfName);
		} catch (@Nonnull final Throwable t) {
			// Left intentionally blank
		}
		return null;
	}

	@Nullable
	private static Field resolve(@Nonnull final Class<?> clazz, @Nonnull final String fieldName,
			@Nullable final String obfName) {
		final String nameToFind = FMLLaunchHandler.isDeobfuscatedEnvironment() ? fieldName
				: MoreObjects.firstNonNull(obfName, fieldName);
		try {
			final Field f = clazz.getDeclaredField(nameToFind);
			f.setAccessible(true);
			return f;
		} catch (final Throwable t) {
			final String msg = String.format("Unable to locate field [%s::%s]", clazz.getName(), nameToFind);
			ModBase.log().error(msg, t);
		}

		return null;
	}

	public boolean isAvailable() {
		return this.field != null;
	}

	protected void check() {
		if (!isAvailable()) {
			final String msg = String.format("Uninitialized field [%s::%s]", this.className, this.fieldName);
			throw new IllegalStateException(msg);
		}
	}

	protected void report(@Nonnull final Throwable t) {
		final String msg = String.format("Unable to access field [%s::%s]", this.className, this.fieldName);
		ModBase.log().error(msg, t);
	}

	@Nullable
	public static Class<?> resolveClass(@Nonnull final String className) {
		try {
			return Class.forName(className);
		} catch (@Nonnull final Throwable t) {
			;
		}
		return null;
	}

	public static class ObjectField<T, R> extends ReflectedField {

		public final R defaultValue;

		public ObjectField(@Nonnull final String className, @Nonnull final String fieldName,
				@Nonnull final String obfName) {
			super(className, fieldName, obfName);
			this.defaultValue = null;
		}

		public ObjectField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName,
				@Nonnull final String obfName) {
			this(clazz, fieldName, obfName, null);
		}

		public ObjectField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName,
				@Nonnull final String obfName, @Nullable final R defaultValue) {
			super(clazz, fieldName, obfName);
			this.defaultValue = defaultValue;
		}

		@SuppressWarnings("unchecked")
		public R get(@Nullable final T obj) {
			check();
			R result = null;
			try {
				result = (R) this.field.get(obj);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
			return result == null ? this.defaultValue : result;
		}

		public void set(@Nullable final T obj, @Nullable final Object val) {
			check();
			try {
				this.field.set(obj, val);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
		}
	}

	public static class IntegerField<T> extends ReflectedField {

		public final int defaultValue;

		public IntegerField(@Nonnull final String className, @Nonnull final String fieldName,
				@Nonnull final String obfName) {
			super(className, fieldName, obfName);
			this.defaultValue = 0;
		}

		public IntegerField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName,
				@Nonnull final String obfName) {
			this(clazz, fieldName, obfName, 0);
		}

		public IntegerField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName,
				@Nonnull final String obfName, final int defaultValue) {
			super(clazz, fieldName, obfName);
			this.defaultValue = defaultValue;
		}

		public int get(@Nullable final T obj) {
			check();
			try {
				return this.field.getInt(obj);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
			return this.defaultValue;
		}

		public void set(@Nullable final T obj, final int val) {
			check();
			try {
				this.field.setInt(obj, val);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
		}
	}

	public static class FloatField<T> extends ReflectedField {

		public final float defaultValue;

		public FloatField(@Nonnull final String className, @Nonnull final String fieldName,
				@Nonnull final String obfName) {
			super(className, fieldName, obfName);
			this.defaultValue = 0F;
		}

		public FloatField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName,
				@Nonnull final String obfName) {
			this(clazz, fieldName, obfName, 0F);
		}

		public FloatField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName, @Nonnull final String obfName,
				final float defaultValue) {
			super(clazz, fieldName, obfName);
			this.defaultValue = defaultValue;
		}

		public float get(@Nullable final T obj) {
			check();
			try {
				return this.field.getFloat(obj);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
			return this.defaultValue;
		}

		public void set(@Nullable final T obj, final float val) {
			check();
			try {
				this.field.setFloat(obj, val);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
		}
	}

	public static class BooleanField<T> extends ReflectedField {

		public final boolean defaultValue;

		public BooleanField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName,
				@Nonnull final String obfName) {
			this(clazz, fieldName, obfName, false);
		}

		public BooleanField(@Nonnull final Class<T> clazz, @Nonnull final String fieldName,
				@Nonnull final String obfName, final boolean defaultValue) {
			super(clazz, fieldName, obfName);
			this.defaultValue = defaultValue;
		}

		public boolean get(@Nullable final T obj) {
			check();
			try {
				return this.field.getBoolean(obj);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
			return this.defaultValue;
		}

		public void set(@Nullable final T obj, final boolean val) {
			check();
			try {
				this.field.setBoolean(obj, val);
			} catch (@Nonnull final Throwable t) {
				report(t);
			}
		}
	}

}
