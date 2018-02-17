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
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.blockartistry.DynSurround.DSurround;
import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.client.ClientRegistry;
import org.blockartistry.DynSurround.client.footsteps.implem.AcousticsManager;
import org.blockartistry.DynSurround.client.footsteps.implem.BlockMap;
import org.blockartistry.DynSurround.client.footsteps.implem.PrimitiveMap;
import org.blockartistry.DynSurround.client.footsteps.implem.RainSplashAcoustic;
import org.blockartistry.DynSurround.client.footsteps.interfaces.IAcoustic;
import org.blockartistry.DynSurround.client.footsteps.parsers.AcousticsJsonReader;
import org.blockartistry.DynSurround.client.footsteps.system.Generator;
import org.blockartistry.DynSurround.client.footsteps.system.GeneratorQP;
import org.blockartistry.DynSurround.client.footsteps.util.ConfigProperty;
import org.blockartistry.DynSurround.data.xface.ModConfigurationFile;
import org.blockartistry.DynSurround.data.xface.VariatorConfig;
import org.blockartistry.DynSurround.data.xface.ModConfigurationFile.ForgeEntry;
import org.blockartistry.DynSurround.packs.ResourcePacks;
import org.blockartistry.DynSurround.packs.ResourcePacks.Pack;
import org.blockartistry.lib.ItemStackUtil;
import org.blockartistry.lib.MCHelper;
import org.blockartistry.lib.collections.IdentityHashSet;

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
	private Variator playerQuadruped;

	public FootstepsRegistry(@Nonnull final Side side) {
		super(side);
	}

	@Override
	public void init() {

		this.acousticsManager = new AcousticsManager();
		this.blockMap = new BlockMap(this.acousticsManager);
		this.primitiveMap = new PrimitiveMap(this.acousticsManager);
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
				this.registerForgeEntries(entry.acousticProfile, name);
		}

		for (final Entry<String, String> entry : cfg.footsteps.entrySet()) {
			this.registerBlocks(entry.getValue(), entry.getKey());
		}

		for (final String fp : cfg.footprints) {
			this.registerFootrint(fp);
		}

		for (final Entry<String, VariatorConfig> entry : cfg.variators.entrySet())
			this.variators.put(entry.getKey(), new Variator(entry.getValue()));
	}

	@Override
	public void initComplete() {
		this.getBlockMap().freeze();
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

		this.childVariator = this.getVariator("child");
		this.playerQuadruped = this.getVariator("quadruped");

		// Generate a list of IBlockState objects for all blocks registered
		// with Forge.
		final Set<IBlockState> blockStates = StreamSupport.stream(ForgeRegistries.BLOCKS.spliterator(), false)
				.map(block -> block.getBlockState().getValidStates()).flatMap(l -> l.stream())
				.collect(Collectors.toSet());

		// Collect the set of missing acoustics and print them out
		if (ModOptions.logging.enableDebugLogging) {
			final Set<String> missingAcoustics = blockStates.stream()
					.filter(bs -> !FootstepsRegistry.this.getBlockMap().hasAcoustics(bs)).map(IBlockState::toString)
					.collect(Collectors.toSet());

			if (missingAcoustics.size() > 0) {
				DSurround.log().info("MISSING ACOUSTIC ENTRIES");
				DSurround.log().info("========================");
				missingAcoustics.stream().sorted().forEach(DSurround.log()::info);
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
				}).forEach(bs -> {
					this.FOOTPRINT_STATES.add(bs);
					DSurround.log().debug("Added blockstate [%s] to footprint list", bs.toString());
				});
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
				DSurround.log().debug("Unable to load primitive map data from pack %s", pack.getModName());
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
				DSurround.log().debug("Unable to load acoustic data from pack %s", pack.getModName());
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

	public Generator createGenerator(@Nonnull final EntityLivingBase entity) {
		final EntityEffectInfo info = ClientRegistry.EFFECTS.getEffects(entity);
		Variator var = this.getVariator(info.variator);
		if (entity.isChild())
			var = this.childVariator;
		else if (entity instanceof EntityPlayer && ModOptions.sound.foostepsQuadruped)
			var = this.playerQuadruped;
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

	@Nonnull
	public PrimitiveMap getPrimitiveMap() {
		return this.primitiveMap;
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
				final IBlockState state = bi.getBlock().getDefaultState();
				this.FOOTPRINT_STATES.add(state);
			} else {
				DSurround.log().warn("Generic matching is not supported for footprints: %s", b);
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
