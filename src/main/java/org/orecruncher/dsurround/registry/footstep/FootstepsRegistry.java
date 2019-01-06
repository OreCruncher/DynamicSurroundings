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

package org.orecruncher.dsurround.registry.footstep;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.footsteps.BlockMap;
import org.orecruncher.dsurround.client.footsteps.Generator;
import org.orecruncher.dsurround.client.footsteps.GeneratorQP;
import org.orecruncher.dsurround.registry.Registry;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.dsurround.registry.acoustics.AcousticRegistry;
import org.orecruncher.dsurround.registry.acoustics.IAcoustic;
import org.orecruncher.dsurround.registry.acoustics.RainSplashAcoustic;
import org.orecruncher.dsurround.registry.blockstate.BlockStateMatcher;
import org.orecruncher.dsurround.registry.config.ModConfiguration;
import org.orecruncher.dsurround.registry.config.ModConfiguration.ForgeEntry;
import org.orecruncher.dsurround.registry.effect.EntityEffectInfo;
import org.orecruncher.lib.ItemStackUtil;
import org.orecruncher.lib.MCHelper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockVine;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@SideOnly(Side.CLIENT)
public final class FootstepsRegistry extends Registry {

	//@formatter:off
	private static final List<String> FOOTPRINT_SOUND_PROFILE =
		Arrays.asList(
			"minecraft:block.sand.step",
			"minecraft:block.gravel.step",
			"minecraft:block.snow.step"
		);
	//@formatter:on

	private BlockMap blockMap;

	private Set<Material> FOOTPRINT_MATERIAL;
	private Set<IBlockState> FOOTPRINT_STATES;

	private Map<String, Variator> variators;
	private Variator childVariator;
	private Variator playerVariator;
	private Variator playerQuadrupedVariator;

	private Set<IBlockState> missingAcoustics;

	public IAcoustic[] SWIM;
	public IAcoustic[] JUMP;
	public IAcoustic[] SPLASH;

	public FootstepsRegistry() {
		super("Footsteps Registry");
	}

	@Override
	protected void preInit() {

		final AcousticRegistry acoustics = RegistryManager.ACOUSTICS;
		this.blockMap = new BlockMap(acoustics);
		this.FOOTPRINT_MATERIAL = new ReferenceOpenHashSet<>();
		this.FOOTPRINT_STATES = new ReferenceOpenHashSet<>();
		this.variators = new Object2ObjectOpenHashMap<>();

		// Initialize the known materials that leave footprints
		this.FOOTPRINT_MATERIAL.add(Material.CLAY);
		this.FOOTPRINT_MATERIAL.add(Material.GRASS);
		this.FOOTPRINT_MATERIAL.add(Material.GROUND);
		this.FOOTPRINT_MATERIAL.add(Material.ICE);
		this.FOOTPRINT_MATERIAL.add(Material.PACKED_ICE);
		this.FOOTPRINT_MATERIAL.add(Material.SAND);
		this.FOOTPRINT_MATERIAL.add(Material.CRAFTED_SNOW);
		this.FOOTPRINT_MATERIAL.add(Material.SNOW);

		// It's a hack - needs refactor
		this.SWIM = null;
		this.JUMP = null;
		this.SPLASH = null;

		seedMap();
	}

	@Override
	protected void init(@Nonnull final ModConfiguration cfg) {
		for (final ForgeEntry entry : cfg.forgeMappings) {
			for (final String name : entry.dictionaryEntries)
				registerForgeEntries(entry.acousticProfile, name);
		}

		//@formatter:off
		cfg.footsteps.forEach((k, v) -> registerBlocks(v, k));
		cfg.footprints.forEach(f -> registerFootprint(f));
		this.variators.putAll(
			cfg.variators.entrySet().stream()
				.collect(
					Collectors.toMap(Map.Entry::getKey, e -> new Variator(e.getValue()))
				)
		);
		//@formatter:on
	}

	@Override
	protected void postInit() {
		this.SWIM = RegistryManager.ACOUSTICS.compileAcoustics("_SWIM");
		this.JUMP = RegistryManager.ACOUSTICS.compileAcoustics("_JUMP");
		this.SPLASH = new IAcoustic[] {
				new RainSplashAcoustic(RegistryManager.ACOUSTICS.compileAcoustics("waterfine")) };

		this.childVariator = getVariator("child");
		this.playerVariator = getVariator(ModOptions.sound.firstPersonFootstepCadence ? "playerSlow" : "player");
		this.playerQuadrupedVariator = getVariator(
				ModOptions.sound.firstPersonFootstepCadence ? "quadrupedSlow" : "quadruped");

		// Generate a list of IBlockState objects for all blocks registered
		// with Forge.
		//@formatter:off
		final Set<IBlockState> blockStates =
			StreamSupport.stream(ForgeRegistries.BLOCKS.spliterator(), false)
				.map(block -> block.getBlockState().getValidStates())
				.flatMap(l -> l.stream())
				.collect(Collectors.toSet());
		//@formatter:on

		// Scan the block list looking for any block states that do not have sounds
		// definitions supplied by configuration files or by primitives. This scan
		// has the side effect of priming the caches.
		//@formatter:off
		this.missingAcoustics =
			blockStates.stream()
				.filter(bs -> !getBlockMap().hasAcoustics(bs))
				.collect(Collectors.toSet());
		//@formatter:on

		// Identify any IBlockStates that could have footprints associated and
		// register them if necessary.
		//@formatter:off
		blockStates
			.stream()
			.filter(bs -> bs.getMaterial().blocksMovement()	&& !hasFootprint(bs))
			.filter(bs -> {
				final SoundType sound = MCHelper.getSoundType(bs);
				if (sound != null) {
					final SoundEvent event = sound.getStepSound();
					if (event != null) {
						final ResourceLocation resource = event.getSoundName();
						if (resource != null) {
							final String soundName = resource.toString();
							return FOOTPRINT_SOUND_PROFILE.contains(soundName);
						}
					}
				}
				return false;
			})
			.forEach(bs -> this.FOOTPRINT_STATES.add(bs));
		//@formatter:on
	}

	@Override
	protected void complete() {
		if (ModOptions.logging.enableDebugLogging) {
			if (this.missingAcoustics.size() > 0) {
				ModBase.log().info("          >>>> MISSING ACOUSTIC ENTRIES <<<< ");
				ModBase.log().info("Sounds for these states will default to their step sound");
				ModBase.log().info("========================================================");
				this.missingAcoustics.stream().map(IBlockState::toString).sorted().forEach(ModBase.log()::info);
			}
		}
		this.missingAcoustics = null;
	}

	private void seedMap() {
		// Iterate through the blockmap looking for known pattern types.
		// Though they probably should all be registered with Forge
		// dictionary it's not a requirement.
		final Iterator<Block> itr = Block.REGISTRY.iterator();
		while (itr.hasNext()) {
			final Block block = itr.next();
			final String blockName = MCHelper.nameOf(block);
			if (blockName != null) {
				if (block instanceof BlockCrops) {
					final BlockCrops crop = (BlockCrops) block;
					if (crop.getMaxAge() == 3) {
						// Like beets
						registerBlocks("#beets", blockName);
					} else if (blockName.equals("minecraft:wheat")) {
						// Wheat is special because it is straw like
						registerBlocks("#wheat", blockName);
					} else if (crop.getMaxAge() == 7) {
						// Like carrots and potatoes
						registerBlocks("#crop", blockName);
					}
				} else if (block instanceof BlockSapling) {
					registerBlocks("#sapling", blockName);
				} else if (block instanceof BlockReed) {
					registerBlocks("#reed", blockName);
				} else if (block instanceof BlockFence) {
					registerBlocks("#fence", blockName);
				} else if (block instanceof BlockVine) {
					registerBlocks("#vine", blockName);
				} else if (block instanceof BlockFlower || block instanceof BlockMushroom) {
					registerBlocks("NOT_EMITTER", blockName);
				} else if (block instanceof BlockPlanks) {
					registerBlocks("wood", blockName);
				} else if (block instanceof BlockLog) {
					registerBlocks("log", blockName);
				} else if (block instanceof BlockDoor) {
					registerBlocks("bluntwood", blockName);
				} else if (block instanceof BlockLeaves) {
					registerBlocks("leaves", blockName);
				} else if (block instanceof BlockOre) {
					registerBlocks("ore", blockName);
				} else if (block instanceof BlockIce) {
					registerBlocks("ice", blockName);
				} else if (block instanceof BlockChest) {
					registerBlocks("squeakywood", blockName);
				} else if (block instanceof BlockGlass) {
					registerBlocks("glass", blockName);
				}
			}
		}
	}

	public Generator createGenerator(@Nonnull final EntityLivingBase entity) {
		final EntityEffectInfo info = RegistryManager.EFFECTS.getEffects(entity);
		Variator var = getVariator(info.variator);
		if (entity.isChild()) {
			var = this.childVariator;
		} else if (entity instanceof EntityPlayer) {
			var = ModOptions.sound.foostepsQuadruped ? this.playerQuadrupedVariator : this.playerVariator;
		}
		return var.QUADRUPED ? new GeneratorQP(var) : new Generator(var);
	}

	@Nonnull
	private Variator getVariator(@Nonnull final String varName) {
		return this.variators.getOrDefault(varName, Variator.DEFAULT);
	}

	@Nonnull
	public BlockMap getBlockMap() {
		return this.blockMap;
	}

	/**
	 * Used by the cache routines to resolve a block state that it cannot be solved
	 * using configuration data. Typically it involves deeper analysis of a block
	 * state to determine the correct acoustic profile.
	 *
	 * @param state The state for which an AcousticProfile needs to be resolved
	 * @return AcousticProfile for the state, null otherwise
	 */
	@Nonnull
	public IAcoustic[] resolve(@Nonnull final IBlockState state) {
		return RegistryManager.ACOUSTICS.resolvePrimitive(state);
	}

	public boolean hasFootprint(@Nonnull final IBlockState state) {
		return this.FOOTPRINT_MATERIAL.contains(state.getMaterial()) || this.FOOTPRINT_STATES.contains(state);
	}

	private static Block resolveToBlock(@Nonnull final ItemStack stack) {
		if (!ItemStackUtil.isValidItemStack(stack))
			return null;
		final Item item = stack.getItem();
		if (item instanceof ItemBlock)
			return ((ItemBlock) item).getBlock();
		if (item instanceof ItemBlockSpecial)
			return ((ItemBlockSpecial) item).getBlock();
		return null;
	}

	private void registerFootprint(@Nonnull final String... blocks) {
		for (String b : blocks) {
			boolean materialMatch = false;
			if (b.startsWith("@")) {
				materialMatch = true;
				b = b.substring(1);
			}

			final BlockStateMatcher bi = BlockStateMatcher.create(b);
			if (materialMatch) {
				final IBlockState state = bi.getBlock().getDefaultState();
				this.FOOTPRINT_MATERIAL.add(state.getMaterial());
			} else {
				this.FOOTPRINT_STATES.addAll(bi.asBlockStates());
			}
		}
	}

	private void registerForgeEntries(@Nonnull final String blockClass, @Nonnull final String... entries) {
		for (final String dictionaryName : entries) {
			final List<ItemStack> stacks = OreDictionary.getOres(dictionaryName, false);
			for (final ItemStack stack : stacks) {
				final Block block = resolveToBlock(stack);
				if (block != null) {
					String blockName = null;
					if (stack.getHasSubtypes() && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
						try {
							// TODO: Need to sort out with tagging in 1.13
							final int meta = stack.getItem().getMetadata(stack);
							@SuppressWarnings("deprecation")
							final IBlockState state = block.getStateFromMeta(meta);
							blockName = state.toString();
						} catch (@Nonnull final Throwable t) {
							ModBase.log().warn("Unable to resolve blockstate for [%s]",
									ItemStackUtil.getItemName(stack));
						}
					} else {
						blockName = MCHelper.nameOf(block);
					}
					if (blockName != null)
						getBlockMap().register(blockName, blockClass);
					else
						ModBase.log().warn("Unable to obtain block name for ItemStack [%s]", stack.toString());
				}
			}
		}
	}

	private void registerBlocks(@Nonnull final String blockClass, @Nonnull final String... blocks) {
		for (final String s : blocks)
			getBlockMap().register(s, blockClass);
	}
}
