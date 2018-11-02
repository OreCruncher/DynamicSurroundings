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

package org.orecruncher.dsurround.registry.config.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.lib.JsonUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public final class ResourcePacks {

	private ResourcePacks() {

	}

	public static final ResourceLocation MANIFEST_RESOURCE = new ResourceLocation(ModBase.RESOURCE_ID,
			"manifest.json");
	public static final ResourceLocation ACOUSTICS_RESOURCE = new ResourceLocation(ModBase.RESOURCE_ID,
			"acoustics.json");
	public static final ResourceLocation PRIMITIVEMAP_RESOURCE = new ResourceLocation(ModBase.RESOURCE_ID,
			"primitivemap.json");
	public static final ResourceLocation CONFIGURE_RESOURCE = new ResourceLocation(ModBase.RESOURCE_ID,
			"configure.json");

	public static class Pack {

		protected final String modName;
		protected final String packPath;
		protected Manifest manifest;

		public Pack(@Nonnull final String modName) {
			this.modName = modName;
			this.packPath = "/assets/" + modName + "/";
		}

		public boolean hasManifest() {
			if (this.manifest == null) {
				try (final InputStream stream = getInputStream(MANIFEST_RESOURCE)) {
					if (stream != null)
						this.manifest = JsonUtils.load(stream, Manifest.class);
				} catch (final Throwable t) {
				}
			}

			return this.manifest != null;
		}

		@Nonnull
		public String getModName() {
			return this.modName;
		}

		@Nullable
		public Manifest getManifest() {
			return this.manifest;
		}

		public InputStream getInputStream(@Nonnull final ResourceLocation loc) throws IOException {
			final StringBuilder builder = new StringBuilder();
			builder.append(this.packPath);
			builder.append(loc.getNamespace()).append('/').append(loc.getPath());
			return ModBase.class.getResourceAsStream(builder.toString());
		}

		public boolean resourceExists(@Nonnull final ResourceLocation loc) {
			try (final InputStream stream = getInputStream(loc)) {
				return stream != null;
			} catch (@Nonnull final Throwable t) {

			}
			return false;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("Resource pack ").append(getModName()).append(": ");
			if (this.manifest != null)
				builder.append(String.format("%s by %s (%s)", this.manifest.getName(), this.manifest.getAuthor(),
						this.manifest.getWebsite()));
			else
				builder.append("No manifest");
			return builder.toString();
		}
	}

	private static class ResourcePack extends Pack {

		protected final IResourcePack pack;

		public ResourcePack(@Nonnull IResourcePack resource) {
			super(resource.getPackName());
			this.pack = resource;
		}

		@Override
		public InputStream getInputStream(@Nonnull final ResourceLocation loc) throws IOException {
			return this.pack.getInputStream(loc);
		}

		@Override
		public boolean resourceExists(@Nonnull final ResourceLocation loc) {
			return this.pack.resourceExists(loc);
		}
	}

	@Nonnull
	public static List<Pack> findResourcePacks() {

		final List<Pack> foundEntries = new ArrayList<>();

		// Add ourselves to the list as the first entry
		Pack p = new Pack(ModBase.MOD_ID);
		if (!p.hasManifest())
			throw new RuntimeException("Missing configuration!");
		foundEntries.add(p);

		// Scan the mods that are loaded to see if they have a
		// configuration we are interested in.
		for (final ModContainer mod : Loader.instance().getActiveModList()) {
			// DS is already added so we have to skip
			if (!mod.getModId().equals(ModBase.MOD_ID)) {
				p = new Pack(mod.getModId());
				if (p.hasManifest())
					foundEntries.add(p);
			}
		}

		// Look in other resource packs for more configuration data. Only do
		// this if NOT running as a dedicated server.
		if (!ModBase.proxy().isRunningAsServer()) {
			final List<ResourcePackRepository.Entry> repo = Minecraft.getMinecraft().getResourcePackRepository()
					.getRepositoryEntries();

			for (final ResourcePackRepository.Entry pack : repo) {
				p = new ResourcePack(pack.getResourcePack());
				if (p.hasManifest())
					foundEntries.add(p);
			}
		}

		return foundEntries;
	}

}
