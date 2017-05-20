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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.annotation.Nonnull;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.DynSurround.client.footsteps.implem.Manifest;
import org.blockartistry.DynSurround.client.footsteps.implem.PrimitiveMap;
import org.blockartistry.DynSurround.client.footsteps.parsers.AcousticsJsonReader;
import org.blockartistry.DynSurround.client.footsteps.system.Generator;
import org.blockartistry.DynSurround.client.footsteps.system.GeneratorQP;
import org.blockartistry.DynSurround.client.footsteps.system.Isolator;
import org.blockartistry.DynSurround.client.footsteps.system.ResourcePacks;
import org.blockartistry.DynSurround.client.footsteps.system.Solver;
import org.blockartistry.DynSurround.client.footsteps.util.ConfigProperty;
import org.blockartistry.lib.JsonUtils;
import org.blockartistry.lib.MCHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSapling;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;

public class FootstepsRegistry extends Registry {

	// System
	private ResourcePacks dealer = new ResourcePacks();
	private final Isolator isolator;

	public FootstepsRegistry(@Nonnull final Side side) {
		super(side);
		this.isolator = new Isolator();
	}

	@Override
	public void init() {

		// It's a hack - needs refactor
		AcousticsManager.SWIM = null;
		this.getBlockMap().clear();
		final List<IResourcePack> repo = this.dealer.findResourcePacks();

		reloadManifests(repo);
		reloadAcoustics(repo);
		reloadPrimitiveMap(repo);

		seedMap();

		this.isolator.setSolver(new Solver(this.isolator));
		this.isolator.setGenerator(
				ModOptions.foostepsQuadruped ? new GeneratorQP(this.isolator) : new Generator(this.isolator));
	}

	@Override
	public void initComplete() {
		this.getBlockMap().freeze();
		AcousticsManager.SWIM = this.isolator.getAcoustics().compileAcoustics("_SWIM");
	}

	@Override
	public void fini() {

	}

	private void reloadManifests(@Nonnull final List<IResourcePack> repo) {
		for (final IResourcePack pack : repo) {
			InputStream stream = null;
			try {
				stream = this.dealer.openPackDescriptor(pack);
				if (stream != null) {
					final Manifest manifest = JsonUtils.load(stream, Manifest.class);
					if (manifest != null) {
						DSurround.log().info("Resource pack %s: %s by %s (%s)", pack.getPackName(), manifest.getName(),
								manifest.getAuthor(), manifest.getWebsite());
					}
				}
			} catch (final Exception e) {
				DSurround.log().debug("Unable to load variator data from pack %s", pack.getPackName());
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
				DSurround.log().debug("Unable to load primitive map data from pack %s", pack.getPackName());
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
				DSurround.log().debug("Unable to load acoustic data from pack %s", pack.getPackName());
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
		this.isolator.setSoundPlayer(acoustics);
		this.isolator.setDefaultStepPlayer(acoustics);
	}

	private void seedMap() {
		// Iterate through the blockmap looking for known pattern types.
		// Though they probably should all be registered with Forge
		// dictionary it's not a requirement.
		final Iterator<Block> itr = Block.REGISTRY.iterator();
		while (itr.hasNext()) {
			final Block block = itr.next();
			final String blockName = MCHelper.nameOf(block);
			if (block instanceof BlockCrops) {
				final BlockCrops crop = (BlockCrops) block;
				if (crop.getMaxAge() == 3) {
					this.registerBlocks("#beets", blockName);
				} else if (blockName.equals("minecraft:wheat")) {
					this.registerBlocks("#wheat", blockName);
				} else if (crop.getMaxAge() == 7) {
					this.registerBlocks("#crop", blockName);
				}
			} else if (block instanceof BlockSapling) {
				this.registerBlocks("#sapling", blockName);
			} else if (block instanceof BlockReed) {
				this.registerBlocks("#reed", blockName);
			} else if (block instanceof BlockFence) {
				this.registerBlocks("#fence", blockName);
			} else if (block instanceof BlockFlower || block instanceof BlockMushroom) {
				this.registerBlocks("NOT_EMITTER", blockName);
			} else if (block instanceof BlockLog || block instanceof BlockPlanks) {
				this.registerBlocks("wood", blockName);
			} else if (block instanceof BlockDoor) {
				this.registerBlocks("bluntwood", blockName);
			} else if (block instanceof BlockLeaves) {
				this.registerBlocks("leaves", blockName);
			} else if (block instanceof BlockOre) {
				this.registerBlocks("ore", blockName);
			} else if (block instanceof BlockIce) {
				this.registerBlocks("ice", blockName);
			}
		}
	}

	public void process(@Nonnull World world, @Nonnull EntityPlayer player) {
		this.isolator.onFrame(player);
		player.nextStepDistance = Integer.MAX_VALUE;
	}

	@Nonnull
	public BlockMap getBlockMap() {
		return this.isolator.getBlockMap();
	}

	public void registerForgeEntries(@Nonnull final String blockClass, @Nonnull final String... entries) {
		for (final String dictionaryName : entries) {
			final List<ItemStack> stacks = OreDictionary.getOres(dictionaryName, false);
			for (final ItemStack stack : stacks) {
				final Block block = Block.getBlockFromItem(stack.getItem());
				if (block != null) {
					String blockName = MCHelper.nameOf(block);
					if (stack.getHasSubtypes() && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE)
						blockName += "^" + stack.getItemDamage();
					getBlockMap().register(blockName, blockClass);
				}
			}
		}
	}

	public void registerBlocks(@Nonnull final String blockClass, @Nonnull final String... blocks) {
		for (final String s : blocks)
			getBlockMap().register(s, blockClass);
	}
}
