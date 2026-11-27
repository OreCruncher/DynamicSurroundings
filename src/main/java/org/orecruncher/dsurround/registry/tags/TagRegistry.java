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

package org.orecruncher.dsurround.registry.tags;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.blockstate.BlockStateMatcher;
import org.orecruncher.dsurround.registry.config.ModConfiguration;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Block tags, the mod's own, as the 26.x line has (1.12.2 has none of the game's). Laid out as 26.x's:
 * {@code dsurround:effects/floor_squeaks} is defined by every
 * {@code assets/<namespace>/dsconfigs/tags/block/effects/floor_squeaks.json}, in mod jars (the mod's own carries
 * other mods' namespaces) and resource packs, read through the game's resource manager. A namespace that isn't an
 * installed mod's is skipped, but for files a player's resource pack brings. 1.12.2's resource manager can't list a
 * folder, so the files looked for are those of the tags the mod uses ({@link BlockTags}) and of the tags they name.
 * A config file's {@code tags} section ({@code "effects/floor_squeaks": [...]}) adds to them, for players' external
 * config files. Entries:
 * <ul>
 * <li>a block, {@code minecraft:planks}, or some of its states, {@code minecraft:planks[variant=oak]} (as block
 * names are written elsewhere in the config);</li>
 * <li>{@code ore:plankWood}: the blocks of the ore dictionary's items by that name, which picks up mods' blocks
 * (only those with an item, placed as that item places them);</li>
 * <li>{@code #floor_squeaks}: another tag's members.</li>
 * </ul>
 * A tag in several files has all their entries. Members are the block states themselves, so a check is a map and a
 * set lookup. Client only.
 */
@SideOnly(Side.CLIENT)
public final class TagRegistry extends Registry {

	private final Map<String, List<String>> definitions = new TreeMap<>();
	private Map<String, Set<IBlockState>> tags = Collections.emptyMap();

	public TagRegistry() {
		super("Tag Registry");
	}

	@Override
	protected void preInit() {
		this.definitions.clear();
		this.tags = Collections.emptyMap();
	}

	@Override
	protected void init(@Nonnull final ModConfiguration cfg) {
		TagResolver.merge(this.definitions, cfg.tags);
	}

	@Override
	protected void postInit() {
		TagResolver.load(this.definitions, BlockTags.ALL, new TagFiles());
		final Map<String, Set<IBlockState>> resolved = TagResolver.resolve(this.definitions, TagRegistry::statesOf,
				ReferenceOpenHashSet::new, msg -> ModBase.log().warn(msg));
		this.tags = new Object2ObjectOpenHashMap<>(resolved);
	}

	@Override
	protected void complete() {
		this.tags.forEach((name, members) -> ModBase.log().info("[%s] %s: %d block states", getName(), name,
				members.size()));
	}

	/**
	 * Whether a block state is in a tag (a name from {@link BlockTags})
	 */
	public boolean is(@Nonnull final String tag, @Nonnull final IBlockState state) {
		final Set<IBlockState> members = this.tags.get(tag);
		return members != null && members.contains(state);
	}

	/**
	 * The tags a block state is in, by name, for the inspection overlay
	 */
	@Nonnull
	public List<String> tagsOf(@Nonnull final IBlockState state) {
		final List<String> result = new ArrayList<>();
		this.tags.forEach((name, members) -> {
			if (members.contains(state))
				result.add(name);
		});
		Collections.sort(result);
		return result;
	}

	/**
	 * Reads a tag's files from every namespace of the resource manager
	 */
	private static final class TagFiles implements java.util.function.Function<String, List<String>> {

		private final IResourceManager resources = Minecraft.getMinecraft().getResourceManager();
		// The player's resource packs, by name, and the namespaces they hold
		private final Set<String> playerPacks = new HashSet<>();
		private final Set<String> playerNamespaces = new HashSet<>();

		TagFiles() {
			final ResourcePackRepository repository = Minecraft.getMinecraft().getResourcePackRepository();
			for (final ResourcePackRepository.Entry entry : repository.getRepositoryEntries()) {
				final IResourcePack pack = entry.getResourcePack();
				this.playerPacks.add(pack.getPackName());
				this.playerNamespaces.addAll(pack.getResourceDomains());
			}
		}

		@Override
		@Nonnull
		public List<String> apply(@Nonnull final String tag) {
			final String path = TagResolver.filePath(tag);
			final List<String> entries = new ArrayList<>();
			for (final String namespace : new TreeSet<>(this.resources.getResourceDomains())) {
				final boolean installed = "minecraft".equals(namespace) || Loader.isModLoaded(namespace);
				if (!installed && !this.playerNamespaces.contains(namespace))
					continue;
				final List<IResource> found;
				try {
					found = this.resources.getAllResources(new ResourceLocation(namespace, path));
				} catch (final FileNotFoundException none) {
					continue;
				} catch (final Throwable t) {
					ModBase.log().warn("Unable to look for %s:%s: %s", namespace, path, t.getMessage());
					continue;
				}
				for (final IResource resource : found) {
					try (final IResource r = resource;
							final Reader reader = new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8)) {
						if (!installed && !this.playerPacks.contains(r.getResourcePackName()))
							continue;
						entries.addAll(TagResolver.parseFile(reader));
					} catch (final Throwable t) {
						ModBase.log().warn("Tag file %s from %s is not read: %s", resource.getResourceLocation(),
								resource.getResourcePackName(), t.getMessage());
					}
				}
			}
			return entries;
		}
	}

	@Nonnull
	private static Collection<IBlockState> statesOf(@Nonnull final String entry) {
		if (entry.startsWith("ore:"))
			return oreStates(entry.substring(4));
		final BlockStateMatcher matcher = BlockStateMatcher.create(entry);
		if (matcher == null) {
			// The matcher has said why
			return Collections.emptyList();
		}
		return matcher.asBlockStates();
	}

	/**
	 * The block states the ore dictionary's items by a name place: every state of a block whose item has no
	 * subtypes, or is listed for all of them; otherwise those that drop the listed one
	 */
	@Nonnull
	private static Collection<IBlockState> oreStates(@Nonnull final String name) {
		final List<IBlockState> result = new ArrayList<>();
		for (final ItemStack stack : OreDictionary.getOres(name, false)) {
			final Block block = Block.getBlockFromItem(stack.getItem());
			if (block == Blocks.AIR)
				continue;
			final List<IBlockState> states = block.getBlockState().getValidStates();
			if (!stack.getHasSubtypes() || stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
				result.addAll(states);
				continue;
			}
			final int meta = stack.getItem().getMetadata(stack);
			for (final IBlockState state : states) {
				try {
					if (block.damageDropped(state) == meta)
						result.add(state);
				} catch (final Throwable t) {
					ModBase.log().warn("Unable to match the states of %s to ore dictionary entry %s", block, name);
					break;
				}
			}
		}
		return result;
	}
}
