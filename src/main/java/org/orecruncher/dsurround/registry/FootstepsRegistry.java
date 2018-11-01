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

package org.orecruncher.dsurround.registry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.client.ClientRegistry;
import org.orecruncher.dsurround.client.footsteps.implem.AcousticProfile;
import org.orecruncher.dsurround.client.footsteps.implem.AcousticsManager;
import org.orecruncher.dsurround.client.footsteps.implem.BlockMap;
import org.orecruncher.dsurround.client.footsteps.implem.PrimitiveMap;
import org.orecruncher.dsurround.client.footsteps.implem.RainSplashAcoustic;
import org.orecruncher.dsurround.client.footsteps.interfaces.IAcoustic;
import org.orecruncher.dsurround.client.footsteps.parsers.AcousticsJsonReader;
import org.orecruncher.dsurround.client.footsteps.system.Generator;
import org.orecruncher.dsurround.client.footsteps.system.GeneratorQP;
import org.orecruncher.dsurround.client.footsteps.util.ConfigProperty;
import org.orecruncher.dsurround.data.xface.ModConfigurationFile;
import org.orecruncher.dsurround.data.xface.VariatorConfig;
import org.orecruncher.dsurround.data.xface.ModConfigurationFile.ForgeEntry;
import org.orecruncher.dsurround.packs.ResourcePacks;
import org.orecruncher.dsurround.packs.ResourcePacks.Pack;
import org.orecruncher.lib.ItemStackUtil;
import org.orecruncher.lib.MCHelper;
import org.orecruncher.lib.collections.IdentityHashSet;

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
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

	private static final List<String> FOOTPRINT_SOUND_PROFILE = Arrays.asList("minecraft:block.sand.step",
			"minecraft:block.gravel.step", "minecraft:block.snow.step");

	private AcousticsManager acousticsManager;
	private PrimitiveMap primitiveMap;
	private BlockMap blockMap;

	private Set<Material> FOOTPRINT_MATERIAL;
	private Set<IBlockState> FOOTPRINT_STATES;

	private Map<ArmorClass, IAcoustic> ARMOR_SOUND;
	private Map<ArmorClass, IAcoustic> ARMOR_SOUND_FOOT;

	private Map<String, Variator> variators;
	private Variator childVariator;
	private Variator playerVariator;
	private Variator playerQuadrupedVariator;

	public FootstepsRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {

		this.acousticsManager = new AcousticsManager();
		this.primitiveMap = new PrimitiveMap(this.acousticsManager);
		this.blockMap = new BlockMap(this.acousticsManager);
		this.FOOTPRINT_MATERIAL = new IdentityHashSet<>();
		this.FOOTPRINT_STATES = new IdentityHashSet<>();
		this.ARMOR_SOUND = new EnumMap<>(ArmorClass.class);
		this.ARMOR_SOUND_FOOT = new EnumMap<>(ArmorClass.class);
		this.variators = new HashMap<>();

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
		AcousticsManager.SWIM = null;
		AcousticsManager.JUMP = null;
		AcousticsManager.SPLASH = null;

		final List<Pack> repo = ResourcePacks.findResourcePacks();
		reloadAcoustics(repo);
		reloadPrimitiveMap(repo);

		seedMap();

	}

	@Override
	public void configure(@Nonnull final ModConfigurationFile cfg) {
		for (final ForgeEntry entry : cfg.forgeMappings) {
			for (final String name : entry.dictionaryEntries)
				registerForgeEntries(entry.acousticProfile, name);
		}

		for (final Entry<String, String> entry : cfg.footsteps.entrySet()) {
			registerBlocks(entry.getValue(), entry.getKey());
		}

		for (final String fp : cfg.footprints) {
			registerFootrint(fp);
		}

		for (final Entry<String, VariatorConfig> entry : cfg.variators.entrySet())
			this.variators.put(entry.getKey(), new Variator(entry.getValue()));
	}

	@Override
	public void initComplete() {
		AcousticsManager.SWIM = this.acousticsManager.compileAcoustics("_SWIM");
		AcousticsManager.JUMP = this.acousticsManager.compileAcoustics("_JUMP");
		AcousticsManager.SPLASH = new IAcoustic[] {
				new RainSplashAcoustic(this.acousticsManager.compileAcoustics("waterfine")) };

		final AcousticsManager am = this.acousticsManager;
		this.ARMOR_SOUND.put(ArmorClass.NONE, am.getAcoustic("NOT_EMITTER"));
		this.ARMOR_SOUND.put(ArmorClass.LIGHT, am.getAcoustic("armor_light"));
		this.ARMOR_SOUND.put(ArmorClass.MEDIUM, am.getAcoustic("armor_medium"));
		this.ARMOR_SOUND.put(ArmorClass.CRYSTAL, am.getAcoustic("armor_crystal"));
		this.ARMOR_SOUND.put(ArmorClass.HEAVY, am.getAcoustic("armor_heavy"));
		this.ARMOR_SOUND_FOOT.put(ArmorClass.NONE, am.getAcoustic("NOT_EMITTER"));
		this.ARMOR_SOUND_FOOT.put(ArmorClass.LIGHT, am.getAcoustic("armor_light"));
		this.ARMOR_SOUND_FOOT.put(ArmorClass.MEDIUM, am.getAcoustic("medium_foot"));
		this.ARMOR_SOUND_FOOT.put(ArmorClass.CRYSTAL, am.getAcoustic("crystal_foot"));
		this.ARMOR_SOUND_FOOT.put(ArmorClass.HEAVY, am.getAcoustic("heavy_foot"));

		this.childVariator = getVariator("child");
		this.playerVariator = getVariator(ModOptions.sound.firstPersonFootstepCadence ? "playerSlow" : "player");
		this.playerQuadrupedVariator = getVariator(
				ModOptions.sound.firstPersonFootstepCadence ? "quadrupedSlow" : "quadruped");

		// Generate a list of IBlockState objects for all blocks registered
		// with Forge.
		final Set<IBlockState> blockStates = StreamSupport.stream(ForgeRegistries.BLOCKS.spliterator(), false)
				.map(block -> block.getBlockState().getValidStates()).flatMap(l -> l.stream())
				.collect(Collectors.toSet());

		// Scan the block list looking for any block states that do not have sounds
		// definitions supplied by configuration files or by primitives. This scan
		// has the side effect of priming the caches.
		final Set<IBlockState> missingAcoustics = blockStates.stream()
				.filter(bs -> !FootstepsRegistry.this.getBlockMap().hasAcoustics(bs)).collect(Collectors.toSet());

		if (ModOptions.logging.enableDebugLogging) {
			if (missingAcoustics.size() > 0) {
				ModBase.log().info("          >>>> MISSING ACOUSTIC ENTRIES <<<< ");
				ModBase.log().info("Sounds for these states will default to their step sound");
				ModBase.log().info("========================================================");
				missingAcoustics.stream().map(IBlockState::toString).sorted().forEach(ModBase.log()::info);
			}
		}

		// Identify any IBlockStates that could have footprints associated and
		// register them if necessary.
		blockStates
				.stream().filter(bs -> bs.getMaterial().blocksMovement()
						&& !this.FOOTPRINT_MATERIAL.contains(bs.getMaterial()) && !this.FOOTPRINT_STATES.contains(bs))
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
				}).forEach(bs -> this.FOOTPRINT_STATES.add(bs));
	}

	@Override
	public void fini() {

	}

	private void reloadPrimitiveMap(@Nonnull final List<Pack> repo) {
		for (final Pack pack : repo) {
			try (final InputStream stream = pack.getInputStream(ResourcePacks.PRIMITIVEMAP_RESOURCE)) {
				if (stream != null)
					this.primitiveMap.setup(ConfigProperty.fromStream(stream));
			} catch (final IOException e) {
				ModBase.log().debug("Unable to load primitive map data from pack %s", pack.getModName());
			}
		}
	}

	private void reloadAcoustics(@Nonnull final List<Pack> repo) {
		for (final Pack pack : repo) {
			try (final InputStream stream = pack.getInputStream(ResourcePacks.ACOUSTICS_RESOURCE)) {
				if (stream != null)
					try (final Scanner scanner = new Scanner(stream)) {
						final String jasonString = scanner.useDelimiter("\\Z").next();

						new AcousticsJsonReader("").parseJSON(jasonString, this.acousticsManager);
					}
			} catch (final IOException e) {
				ModBase.log().debug("Unable to load acoustic data from pack %s", pack.getModName());
			}
		}
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
					registerBlocks("#beets", blockName);
				} else if (blockName.equals("minecraft:wheat")) {
					registerBlocks("#wheat", blockName);
				} else if (crop.getMaxAge() == 7) {
					registerBlocks("#crop", blockName);
				}
			} else if (block instanceof BlockSapling) {
				registerBlocks("#sapling", blockName);
			} else if (block instanceof BlockReed) {
				registerBlocks("#reed", blockName);
			} else if (block instanceof BlockFence) {
				registerBlocks("#fence", blockName);
			} else if (block instanceof BlockFlower || block instanceof BlockMushroom) {
				registerBlocks("NOT_EMITTER", blockName);
			} else if (block instanceof BlockLog || block instanceof BlockPlanks) {
				registerBlocks("wood", blockName);
			} else if (block instanceof BlockDoor) {
				registerBlocks("bluntwood", blockName);
			} else if (block instanceof BlockLeaves) {
				registerBlocks("leaves", blockName);
			} else if (block instanceof BlockOre) {
				registerBlocks("ore", blockName);
			} else if (block instanceof BlockIce) {
				registerBlocks("ice", blockName);
			}
		}
	}

	public Generator createGenerator(@Nonnull final EntityLivingBase entity) {
		final EntityEffectInfo info = ClientRegistry.EFFECTS.getEffects(entity);
		Variator var = getVariator(info.variator);
		if (entity.isChild()) {
			var = this.childVariator;
		} else if (entity instanceof EntityPlayer) {
			var = ModOptions.sound.foostepsQuadruped ? this.playerQuadrupedVariator : this.playerVariator;
		}
		return var.QUADRUPED ? new GeneratorQP(var) : new Generator(var);
	}

	@Nonnull
	public Variator getVariator(@Nonnull final String varName) {
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
	 * @param state
	 *            The state for which an AcousticProfile needs to be resolved
	 * @return AcousticProfile for the state, null otherwise
	 */
	@Nullable
	public AcousticProfile resolve(@Nonnull final IBlockState state) {
		// TODO: Change when dynamic states are put in place
		final IAcoustic[] acoustics = resolvePrimitive(state);
		return acoustics != null ? new AcousticProfile.Static(acoustics) : AcousticProfile.NO_PROFILE;
	}

	/**
	 * Used to determine what acoustics to play based on the block's sound
	 * attributes. It's a fallback method in case there isn't a configuration
	 * defined acoustic profile for a block state.
	 *
	 * @param state
	 *            BlockState for which the acoustic profile is being generated
	 * @return Acoustic profile for the BlockState, if any
	 */
	@Nullable
	public IAcoustic[] resolvePrimitive(@Nonnull final IBlockState state) {

		if (state == Blocks.AIR.getDefaultState())
			return AcousticsManager.NOT_EMITTER;

		final SoundType type = MCHelper.getSoundType(state);

		if (type == null)
			return AcousticsManager.NOT_EMITTER;

		final String soundName;
		boolean flag = false;

		if (type.getStepSound() == null || type.getStepSound().getSoundName().getNamespace().isEmpty()) {
			soundName = "UNDEFINED";
			flag = true;
		} else
			soundName = type.getStepSound().getSoundName().toString();

		final String substrate = String.format(Locale.ENGLISH, "%.2f_%.2f", type.getVolume(), type.getPitch());

		// Check for primitive in register
		IAcoustic[] primitive = this.primitiveMap.getPrimitiveMapSubstrate(soundName, substrate);
		if (primitive == null) {
			if (flag) {
				// Check sound
				primitive = this.primitiveMap.getPrimitiveMapSubstrate(soundName, "break_" + soundName);
			}
			if (primitive == null) {
				primitive = this.primitiveMap.getPrimitiveMap(soundName);
			}
		}

		if (primitive == null) {
			// TODO: Generate an acoustic profile on the fly and insert into the map?
		}

		return primitive;
	}

	public boolean hasFootprint(@Nonnull final IBlockState state) {
		return this.FOOTPRINT_MATERIAL.contains(state.getMaterial()) || this.FOOTPRINT_STATES.contains(state);
	}

	@Nullable
	public IAcoustic getArmorAcoustic(@Nonnull final ArmorClass ac) {
		return ac != null ? this.ARMOR_SOUND.get(ac) : null;
	}

	@Nullable
	public IAcoustic getFootArmorAcoustic(@Nonnull final ArmorClass ac) {
		return ac != null ? this.ARMOR_SOUND_FOOT.get(ac) : null;
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

	public void registerFootrint(@Nonnull final String... blocks) {
		for (String b : blocks) {
			boolean materialMatch = false;
			if (b.startsWith("@")) {
				materialMatch = true;
				b = b.substring(1);
			}

			final BlockInfo bi = BlockInfo.create(b);
			if (materialMatch) {
				final IBlockState state = bi.getBlock().getDefaultState();
				this.FOOTPRINT_MATERIAL.add(state.getMaterial());
			} else if (!bi.isGeneric()) {
				this.FOOTPRINT_STATES.addAll(bi.asBlockStates());
			} else {
				ModBase.log().warn("Generic matching is not supported for footprints: %s", b);
			}
		}
	}

	public void registerForgeEntries(@Nonnull final String blockClass, @Nonnull final String... entries) {
		for (final String dictionaryName : entries) {
			final List<ItemStack> stacks = OreDictionary.getOres(dictionaryName, false);
			for (final ItemStack stack : stacks) {
				final Block block = resolveToBlock(stack);
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
