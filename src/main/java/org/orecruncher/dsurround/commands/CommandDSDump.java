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

package org.orecruncher.dsurround.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.registry.biome.BiomeInfo;
import org.orecruncher.dsurround.registry.biome.BiomeUtil;
import org.orecruncher.dsurround.registry.biome.traits.BiomeTraits;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * /dsdump biomes writes every biome to config/dsurround/dumps/biomes-(time).csv, for seeing how well the biomes in a
 * pack are classified: each biome's mod, its Forge biome dictionary types and whether Forge had to guess them (a
 * biome registered with none: Forge guesses from its climate and logs a warning, read from this session's log), its
 * climate and what it grows, and what the mod's config gives it (background and spot sounds, fog, the config entries
 * that matched). A summary goes to chat. A client command: the mod's biome information is the client's.
 */
@SideOnly(Side.CLIENT)
public final class CommandDSDump extends CommandBase {

	private static final String COMMAND = "dsdump";
	private static final String BIOMES = "biomes";
	// Forge's warning for a biome registered without types
	private static final Pattern GUESSED = Pattern.compile("No types have been added to Biome (\\S+?),");

	@Override
	@Nonnull
	public String getName() {
		return COMMAND;
	}

	@Override
	@Nonnull
	public String getUsage(@Nonnull final ICommandSender sender) {
		return "/" + COMMAND + " " + BIOMES;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public boolean checkPermission(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender) {
		return true;
	}

	@Override
	@Nonnull
	public List<String> getTabCompletions(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender,
			@Nonnull final String[] args, @Nullable final net.minecraft.util.math.BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, BIOMES) : Collections.emptyList();
	}

	@Override
	public void execute(@Nullable final MinecraftServer server, @Nonnull final ICommandSender sender,
			@Nonnull final String[] args) throws CommandException {
		if (args.length != 1 || !BIOMES.equalsIgnoreCase(args[0]))
			throw new WrongUsageException(getUsage(sender));
		try {
			final File file = dumpBiomes();
			sender.sendMessage(new TextComponentString("Biomes written to " + file.getPath()));
		} catch (final Throwable t) {
			ModBase.log().error("Unable to dump the biomes", t);
			sender.sendMessage(new TextComponentString("Unable to dump the biomes: " + t.getMessage()));
		}
	}

	@Nonnull
	private static File dumpBiomes() throws IOException {
		final Set<String> guessed = guessedBiomes();
		final List<Biome> biomes = new ArrayList<>(ForgeRegistries.BIOMES.getValuesCollection());
		biomes.sort((a, b) -> String.valueOf(a.getRegistryName()).compareTo(String.valueOf(b.getRegistryName())));

		final File folder = new File(ModBase.dataDirectory(), "dumps");
		folder.mkdirs();
		final String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
		final File file = new File(folder, "biomes-" + stamp + ".csv");

		// Per mod: biomes, without types, guessed, with types worked out, without any sound
		final Map<String, int[]> byMod = new TreeMap<>();
		int total = 0;
		try (final PrintWriter out = new PrintWriter(Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8))) {
			out.println("biome,mod,name,types,typesGuessed,typesAdded,typeSources,temperature,rainfall,canRain,snow,highHumidity,topBlock,"
					+ "fillerBlock,trees,extraTreeChance,grass,flowers,deadBushes,cacti,waterlilies,mushrooms,"
					+ "backgroundSounds,spotSounds,fog,dust,aurora,configEntries");
			for (final Biome biome : biomes) {
				final ResourceLocation key = biome.getRegistryName();
				final String id = key == null ? "?" : key.toString();
				final String mod = key == null ? "?" : key.getResourceDomain();
				final BiomeInfo info = BiomeUtil.getBiomeData(biome);
				final Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
				final boolean wasGuessed = guessed.contains(id);
				final int sounds = info.getSoundCount();
				final int spots = info.getSpotSoundCount();
				final BiomeDecorator d = biome.decorator;
				final List<Object> row = new ArrayList<>();
				row.add(id);
				row.add(mod);
				row.add(name(biome));
				row.add(types.stream().map(BiomeDictionary.Type::getName).sorted().collect(Collectors.joining(" ")));
				row.add(wasGuessed);
				final BiomeTraits traits = info.getTraits();
				final Set<String> added = traits == null ? Collections.<String>emptySet() : traits.added();
				row.add(String.join(" ", added));
				row.add(traits == null ? "" : traits.describe());
				row.add(biome.getDefaultTemperature());
				row.add(biome.getRainfall());
				row.add(biome.canRain());
				row.add(biome.getEnableSnow());
				row.add(biome.isHighHumidity());
				row.add(block(biome.topBlock));
				row.add(block(biome.fillerBlock));
				row.add(d == null ? "" : d.treesPerChunk);
				row.add(d == null ? "" : d.extraTreeChance);
				row.add(d == null ? "" : d.grassPerChunk);
				row.add(d == null ? "" : d.flowersPerChunk);
				row.add(d == null ? "" : d.deadBushPerChunk);
				row.add(d == null ? "" : d.cactiPerChunk);
				row.add(d == null ? "" : d.waterlilyPerChunk);
				row.add(d == null ? "" : d.mushroomsPerChunk);
				row.add(sounds);
				row.add(spots);
				row.add(info.getHasFog());
				row.add(info.getHasDust());
				row.add(info.getHasAurora());
				row.add(String.join("; ", info.getComments()));
				out.println(row.stream().map(CommandDSDump::csv).collect(Collectors.joining(",")));

				final int[] counts = byMod.computeIfAbsent(mod, k -> new int[5]);
				counts[0]++;
				if (types.isEmpty())
					counts[1]++;
				if (wasGuessed)
					counts[2]++;
				if (!added.isEmpty())
					counts[3]++;
				if (sounds == 0 && spots == 0)
					counts[4]++;
				total++;
			}
		}

		ModBase.log().info("/dsdump biomes: %d biomes written to %s", total, file.getPath());
		ModBase.log().info("  mod: biomes / no types / types guessed by Forge / types worked out by DS / no sounds from DS");
		for (final Map.Entry<String, int[]> e : byMod.entrySet()) {
			final int[] c = e.getValue();
			ModBase.log().info("  %s: %d / %d / %d / %d / %d", e.getKey(), c[0], c[1], c[2], c[3], c[4]);
		}
		return file;
	}

	/**
	 * The biomes Forge had to guess the types of, by registry name, from this session's log
	 */
	@Nonnull
	private static Set<String> guessedBiomes() {
		final Set<String> result = new HashSet<>();
		final File log = new File(Minecraft.getMinecraft().mcDataDir, "logs/latest.log");
		if (!log.exists())
			return result;
		try {
			for (final String line : Files.readAllLines(log.toPath(), StandardCharsets.ISO_8859_1)) {
				final Matcher m = GUESSED.matcher(line);
				if (m.find())
					result.add(m.group(1));
			}
		} catch (final IOException e) {
			ModBase.log().warn("Unable to read %s for the biomes Forge guessed: %s", log, e.getMessage());
		}
		return result;
	}

	@Nonnull
	private static String name(@Nonnull final Biome biome) {
		try {
			return biome.getBiomeName();
		} catch (final Throwable t) {
			return "";
		}
	}

	@Nonnull
	private static String block(@Nullable final IBlockState state) {
		if (state == null)
			return "";
		final ResourceLocation key = state.getBlock().getRegistryName();
		return key == null ? state.toString() : key.toString();
	}

	@Nonnull
	private static String csv(@Nullable final Object value) {
		final String s = String.valueOf(value == null ? "" : value);
		if (s.indexOf(',') < 0 && s.indexOf('"') < 0 && s.indexOf('\n') < 0)
			return s;
		return '"' + s.replace("\"", "\"\"") + '"';
	}
}
