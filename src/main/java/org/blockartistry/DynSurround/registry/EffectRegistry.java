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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.data.xface.EntityConfig;
import org.blockartistry.DynSurround.data.xface.ModConfigurationFile;
import org.blockartistry.lib.effects.EntityEffectInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EffectRegistry extends Registry {

	public static final EntityEffectInfo DEFAULT = new EntityEffectInfo();
	public static final ResourceLocation DEFAULT_THEME = new ResourceLocation(DSurround.MOD_ID, "default");

	protected final Map<Class<? extends Entity>, EntityEffectInfo> effects = new IdentityHashMap<>();
	protected EntityEffectInfo playerEffects = DEFAULT;

	protected final Map<ResourceLocation, ThemeInfo> themes = new HashMap<>();
	protected ThemeInfo activeTheme = new ThemeInfo();

	public EffectRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {
		this.playerEffects = DEFAULT;
		this.effects.clear();
		this.themes.clear();
		this.activeTheme = new ThemeInfo();
	}

	@Override
	public void configure(@Nonnull final ModConfigurationFile cfg) {
		for (final Entry<String, EntityConfig> e : cfg.entities.entrySet()) {

			final String entityName = e.getKey();
			final EntityConfig entityEffects = e.getValue();

			if ("minecraft:player".equals(entityName)) {
				this.playerEffects = new EntityEffectInfo(entityEffects);
				continue;
			}

			final int id = EntityList.getIDFromString(entityName);
			final Class<? extends Entity> clazz = EntityList.getClassFromID(id);
			if (clazz != null) {
				this.effects.put(clazz, new EntityEffectInfo(entityEffects));
			} else {
				DSurround.log().warn("Unrecognized resource name for entity: %s", entityName);
			}
		}
	}

	@Override
	public void initComplete() {
		// Need to process all the entities registered with Forge to see if they
		// are in our list. If they aren't the list is scanned looking for
		// hereditary matches.
		for (final String r : EntityList.getEntityNameList()) {
			final int id = EntityList.getIDFromString(r);
			final Class<? extends Entity> clazz = EntityList.getClassFromID(id);
			if (clazz != null) {
				if (!this.effects.containsKey(clazz)) {
					// Not found. Scan our list looking for those that can be assigned
					final Iterator<Entry<Class<? extends Entity>, EntityEffectInfo>> itr = this.effects.entrySet()
							.iterator();
					while (itr.hasNext()) {
						final Entry<Class<? extends Entity>, EntityEffectInfo> e = itr.next();
						if (e.getKey().isAssignableFrom(clazz)) {
							this.effects.put(clazz, e.getValue());
							break;
						}
					}
				}
			} else {
				// This is possible for entity lightening bolt. May show up for other
				// mods as well.
				DSurround.log().debug("Forge reported entity %s but not found in it's own registry!", r.toString());
			}
		}

		DSurround.log().debug("Entity Effect Entries");
		DSurround.log().debug("=====================");
		for (final Entry<Class<? extends Entity>, EntityEffectInfo> e : this.effects.entrySet()) {
			String keyName = EntityList.getEntityStringFromClass(e.getKey());
			if (keyName == null)
				keyName = "No ID Found";
			DSurround.log().debug("%s = %s (%s)", keyName, e.getValue().toString(), e.getKey().getName());
		}

	}

	@Override
	public void fini() {

	}

	@Nonnull
	public EntityEffectInfo getEffects(@Nonnull final Entity entity) {
		if (entity instanceof EntityPlayer)
			return this.playerEffects;
		return this.effects.getOrDefault(entity.getClass(), DEFAULT);
	}

	public void setTheme(@Nonnull final ResourceLocation theme) {
		if (!theme.equals(DEFAULT_THEME)) {
			this.activeTheme = this.themes.get(theme);
			if (this.activeTheme == null)
				this.activeTheme = new ThemeInfo();
		} else {
			this.activeTheme = new ThemeInfo();
		}
	}

	@Nonnull
	public ThemeInfo getTheme() {
		return this.activeTheme;
	}
}
