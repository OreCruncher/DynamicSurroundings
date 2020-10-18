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
package org.orecruncher.dsurround.registry.effect;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModInfo;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.config.EntityConfig;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.dsurround.registry.effect.theme.GloamwoodTheme;
import org.orecruncher.dsurround.registry.effect.theme.ThemeInfo;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EffectRegistry extends Registry {

	public static final EntityEffectInfo DEFAULT = new EntityEffectInfo();
	public static final ResourceLocation DEFAULT_THEME = new ResourceLocation(ModInfo.MOD_ID, "default");
	private static final ThemeInfo DEFAULT_THEME_INFO = new ThemeInfo();

	protected final Map<Class<? extends Entity>, EntityEffectInfo> effects = new Reference2ObjectOpenHashMap<>();
	protected EntityEffectInfo playerEffects = DEFAULT;

	protected final Map<ResourceLocation, ThemeInfo> themes = new Object2ObjectOpenHashMap<>();
	protected ThemeInfo activeTheme = DEFAULT_THEME_INFO;

	public EffectRegistry() {
		super("Effects Registry");
	}

	@Override
	protected void preInit() {
		this.playerEffects = DEFAULT;
		this.effects.clear();
		this.themes.clear();
		this.activeTheme = DEFAULT_THEME_INFO;
		this.themes.put(DEFAULT_THEME, DEFAULT_THEME_INFO);
		this.themes.put(new ResourceLocation(ModInfo.MOD_ID, "gloamwood"), new GloamwoodTheme());
	}

	@Override
	protected void init(@Nonnull final ModConfiguration cfg) {
		for (final Entry<String, EntityConfig> e : cfg.entities.entrySet()) {

			final String entityName = e.getKey();
			final EntityConfig entityEffects = e.getValue();

			if ("minecraft:player".equals(entityName)) {
				this.playerEffects = new EntityEffectInfo(entityEffects);
				continue;
			}

			final Class<? extends Entity> clazz = EntityList.getClassFromName(entityName);
			if (clazz != null) {
				this.effects.put(clazz, new EntityEffectInfo(entityEffects));
			} else {
				ModBase.log().warn("Unrecognized resource name for entity: %s", entityName);
			}
		}
	}

	@Override
	protected void postInit() {
		// Need to process all the entities registered with Forge to see if they
		// are in our list. If they aren't the list is scanned looking for
		// hereditary matches.
		for (final ResourceLocation r : EntityList.getEntityNameList()) {
			final Class<? extends Entity> clazz = EntityList.getClass(r);
			if (clazz != null) {
				if (!this.effects.containsKey(clazz)) {
					// Not found. Scan our list looking for those that can be assigned
					for (Entry<Class<? extends Entity>, EntityEffectInfo> e : this.effects.entrySet()) {
						if (e.getKey().isAssignableFrom(clazz)) {
							this.effects.put(clazz, e.getValue());
							break;
						}
					}
				}
			} else {
				// This is possible for entity lightening bolt. May show up for other
				// mods as well.
				ModBase.log().debug("Forge reported entity %s but not found in it's own registry!", r.toString());
			}
		}

	}

	@Override
	protected void complete() {
		if (ModOptions.logging.enableDebugLogging) {
			// Iterate through the registered entities to see how they match up against
			// the config.
			ModBase.log().info("Entity Effect Configuration");
			ModBase.log().info("===========================");
			for (final ResourceLocation r : EntityList.getEntityNameList()) {
				final Class<?> clazz = EntityList.getClass(r);
				if (clazz != null) {
					final EntityEffectInfo info = this.effects.getOrDefault(clazz, DEFAULT);
					ModBase.log().info("%s = %s", r.toString(), info.toString());
				}
			}
		}
	}

	@Nonnull
	public EntityEffectInfo getEffects(@Nonnull final Entity entity) {
		if (entity instanceof EntityPlayer)
			return this.playerEffects;
		return this.effects.getOrDefault(entity.getClass(), DEFAULT);
	}

	@Nonnull
	public ThemeInfo setTheme(@Nonnull final ResourceLocation theme) {
		this.activeTheme = this.themes.getOrDefault(theme, DEFAULT_THEME_INFO);
		return this.activeTheme;
	}

}
