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

package org.blockartistry.DynSurround.client.footsteps.system;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class ResourcePacks {

	private final ResourceLocation manifest = new ResourceLocation(DSurround.RESOURCE_ID, "manifest.json");
	private final ResourceLocation acoustics = new ResourceLocation(DSurround.RESOURCE_ID, "acoustics.json");
	private final ResourceLocation primitivemap = new ResourceLocation(DSurround.RESOURCE_ID, "primitivemap.json");

	// Resource pack reference for the built in pack.
	private static class DefaultPack implements IResourcePack {

		private final String mod;

		public DefaultPack() {
			this.mod = null;
		}

		public DefaultPack(@Nonnull final String mod) {
			this.mod = mod;
		}

		@Override
		public InputStream getInputStream(@Nonnull final ResourceLocation loc) throws IOException {
			final StringBuilder builder = new StringBuilder();
			builder.append("/assets/dsurround/data/");
			builder.append(loc.getResourceDomain());
			builder.append('/');
			if (this.mod != null)
				builder.append(this.mod).append('_');
			builder.append(loc.getResourcePath());
			return DSurround.class.getResourceAsStream(builder.toString());
		}

		@Override
		public boolean resourceExists(@Nonnull final ResourceLocation loc) {
			return true;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		@Nullable
		public Set getResourceDomains() {
			return null;
		}

		@Override
		@Nullable
		public BufferedImage getPackImage() throws IOException {
			return null;
		}

		@Override
		@Nonnull
		public String getPackName() {
			if (this.mod == null)
				return "DEFAULT";
			return "DEFAULT: " + this.mod;
		}

		@Override
		@Nullable
		public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer,
				String metadataSectionName) throws IOException {
			return null;
		}

	}

	@Nonnull
	public List<IResourcePack> findResourcePacks() {
		final List<ResourcePackRepository.Entry> repo = Minecraft.getMinecraft().getResourcePackRepository()
				.getRepositoryEntries();

		final List<IResourcePack> foundEntries = new ArrayList<IResourcePack>();
		foundEntries.add(new DefaultPack());

		// Add a default back for mods that are loaded - there may be a default
		// configuration privided in the archive
		for (final ModContainer mod : Loader.instance().getActiveModList())
			foundEntries.add(new DefaultPack(mod.getModId()));

		// Look in other resource packs for more configuration data
		for (final ResourcePackRepository.Entry pack : repo) {
			DSurround.log().debug("Resource Pack: %s", pack.getResourcePackName());
			if (checkCompatible(pack)) {
				DSurround.log().debug("Found FootstepsRegistry resource pack: %s", pack.getResourcePackName());
				foundEntries.add(pack.getResourcePack());
			}
		}
		return foundEntries;
	}

	private boolean checkCompatible(@Nonnull final ResourcePackRepository.Entry pack) {
		return pack.getResourcePack().resourceExists(this.manifest);
	}

	@Nullable
	public InputStream openPackDescriptor(@Nonnull final IResourcePack pack) throws IOException {
		return pack.getInputStream(this.manifest);
	}

	@Nullable
	public InputStream openAcoustics(@Nonnull final IResourcePack pack) throws IOException {
		return pack.getInputStream(this.acoustics);
	}

	@Nullable
	public InputStream openPrimitiveMap(@Nonnull final IResourcePack pack) throws IOException {
		return pack.getInputStream(this.primitivemap);
	}
}
