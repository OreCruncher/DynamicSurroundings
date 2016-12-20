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

package org.blockartistry.mod.DynSurround.client.footsteps;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import javax.annotation.Nonnull;

import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.Manifest;
import org.blockartistry.mod.DynSurround.client.footsteps.implem.PrimitiveMap;
import org.blockartistry.mod.DynSurround.client.footsteps.parsers.AcousticsJsonReader;
import org.blockartistry.mod.DynSurround.client.footsteps.system.Generator;
import org.blockartistry.mod.DynSurround.client.footsteps.system.Isolator;
import org.blockartistry.mod.DynSurround.client.footsteps.system.ResourcePacks;
import org.blockartistry.mod.DynSurround.client.footsteps.system.Solver;
import org.blockartistry.mod.DynSurround.client.footsteps.system.UserConfigSoundPlayerWrapper;
import org.blockartistry.mod.DynSurround.client.footsteps.util.ConfigProperty;
import org.blockartistry.mod.DynSurround.registry.DataScripts;
import org.blockartistry.mod.DynSurround.registry.DataScripts.IDependent;
import org.blockartistry.mod.DynSurround.util.JsonUtils;
import org.blockartistry.mod.DynSurround.util.MCHelper;

import net.minecraft.block.Block;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class Footsteps implements IDependent {

	public static Footsteps INSTANCE = null;

	// System
	private ResourcePacks dealer = new ResourcePacks();
	private final Isolator isolator;

	public Footsteps() {
		this.isolator = new Isolator();
		DataScripts.registerDependent(this);
	}

	public static void initialize() {
		INSTANCE = new Footsteps();
	}

	public void preInit() {

		// It's a hack - needs refactor
		AcousticsManager.SWIM = null;
		this.getBlockMap().clear();
		final List<IResourcePack> repo = this.dealer.findResourcePacks();

		reloadManifests(repo);
		reloadAcoustics(repo);
		reloadPrimitiveMap(repo);

		this.isolator.setSolver(new Solver(this.isolator));
		this.isolator.setGenerator(new Generator(this.isolator));
		/*
		 * this.isolator.setGenerator(getConfig().getInteger("custom.stance") ==
		 * 0 ? new Generator(this.isolator) : new GeneratorQP(this.isolator));
		 */
	}

	private void reloadManifests(@Nonnull final List<IResourcePack> repo) {
		for (final IResourcePack pack : repo) {
			InputStream stream = null;
			try {
				stream = this.dealer.openPackDescriptor(pack);
				if (stream != null) {
					final Manifest manifest = JsonUtils.load(stream, Manifest.class);
					if (manifest != null) {
						ModLog.info("Resource pack %s: %s by %s (%s)", pack.getPackName(), manifest.getName(),
								manifest.getAuthor(), manifest.getWebsite());
					}
				}
			} catch (final Exception e) {
				ModLog.debug("Unable to load variator data from pack %s", pack.getPackName());
			} finally {
				if (stream != null)
					try {
						stream.close();
					} catch (final IOException e) {
						;
					}
			}
		}
	}

	private void reloadPrimitiveMap(@Nonnull final List<IResourcePack> repo) {
		final PrimitiveMap primitiveMap = new PrimitiveMap(this.isolator);

		for (final IResourcePack pack : repo) {
			InputStream stream = null;
			try {
				stream = this.dealer.openPrimitiveMap(pack);
				if (stream != null)
					primitiveMap.setup(ConfigProperty.fromStream(stream));
			} catch (final IOException e) {
				ModLog.debug("Unable to load primitive map data from pack %s", pack.getPackName());
			} finally {
				if (stream != null)
					try {
						stream.close();
					} catch (final IOException e) {
						;
					}
			}
		}

		this.isolator.setPrimitiveMap(primitiveMap);
	}

	private void reloadAcoustics(@Nonnull final List<IResourcePack> repo) {
		AcousticsManager acoustics = new AcousticsManager(this.isolator);
		Scanner scanner = null;
		InputStream stream = null;

		for (final IResourcePack pack : repo) {
			try {
				stream = this.dealer.openAcoustics(pack);
				if (stream != null) {
					scanner = new Scanner(stream);
					final String jasonString = scanner.useDelimiter("\\Z").next();

					new AcousticsJsonReader("").parseJSON(jasonString, acoustics);
				}
			} catch (final IOException e) {
				ModLog.debug("Unable to load acoustic data from pack %s", pack.getPackName());
			} finally {
				try {
					if (scanner != null)
						scanner.close();
					if (stream != null)
						stream.close();
				} catch (final IOException e) {
					;
				}
			}
		}

		this.isolator.setAcoustics(acoustics);
		this.isolator.setSoundPlayer(new UserConfigSoundPlayerWrapper(acoustics));
		this.isolator.setDefaultStepPlayer(acoustics);
	}

	public void process(@Nonnull World world, @Nonnull EntityPlayer player) {
		if (this.isolator == null)
			preInit();
		this.isolator.onFrame();
		player.nextStepDistance = Integer.MAX_VALUE;
	}

	@Nonnull
	public BlockMap getBlockMap() {
		return this.isolator.getBlockMap();
	}

	@Override
	public void postInit() {
		// TODO Implement dumpState()
	}

	public static void registerForgeEntries(@Nonnull final String blockClass, @Nonnull final String... entries) {
		for (final String dictionaryName : entries) {
			final List<ItemStack> stacks = OreDictionary.getOres(dictionaryName, false);
			for (final ItemStack stack : stacks) {
				final Block block = Block.getBlockFromItem(stack.getItem());
				if (block != null) {
					String blockName = MCHelper.nameOf(block);
					if (stack.getHasSubtypes() && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE)
						blockName += "^" + stack.getItemDamage();
					INSTANCE.getBlockMap().register(blockName, blockClass);
				}
			}
		}
	}

	public static void registerBlocks(@Nonnull final String blockClass, @Nonnull final String... blocks) {
		for (final String s : blocks)
			INSTANCE.getBlockMap().register(s, blockClass);
	}
}
