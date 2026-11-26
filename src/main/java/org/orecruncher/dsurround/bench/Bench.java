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

package org.orecruncher.dsurround.bench;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ClientCommandHandler;

/**
 * The development bench: scenario runs (see {@link ScenarioRunner}) and /dsbench. Only in development runs, started by
 * {@code profiling.BenchHook}; this package isn't in the published jar.
 * <p>
 * /dsbench mark &lt;name&gt; remembers where the player stands and looks as a scene of that name in this world, for
 * scenes that aren't a biome (a waterfall, a lava lake); /dsbench list lists the world's scenes.
 */
public final class Bench {

	private Bench() {
	}

	/**
	 * Called by reflection from BenchHook
	 *
	 * @param scenario the scenario file to run, or empty for none (only /dsbench)
	 */
	public static void start(@Nonnull final String scenario) {
		ClientCommandHandler.instance.registerCommand(new BenchCommand());
		if (!scenario.trim().isEmpty())
			ScenarioRunner.start(Paths.get(scenario));
	}

	private static final class BenchCommand extends CommandBase {

		@Override
		@Nonnull
		public String getName() {
			return "dsbench";
		}

		@Override
		@Nonnull
		public String getUsage(@Nonnull final ICommandSender sender) {
			return "/dsbench mark <name> | list";
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
		public void execute(@Nullable final MinecraftServer server, @Nonnull final ICommandSender sender,
				@Nonnull final String[] args) throws WrongUsageException {
			String message;
			try {
				if (args.length == 2 && "mark".equals(args[0]))
					message = mark(args[1]);
				else if (args.length == 1 && "list".equals(args[0]))
					message = list();
				else
					throw new WrongUsageException(getUsage(sender));
			} catch (final WrongUsageException e) {
				throw e;
			} catch (final Exception e) {
				ModBase.log().error("/dsbench failed", e);
				message = "Failed: " + e;
			}
			for (final String line : message.split("\n"))
				sender.sendMessage(new TextComponentString(line));
		}

		@Override
		@Nonnull
		public List<String> getTabCompletions(@Nonnull final MinecraftServer server,
				@Nonnull final ICommandSender sender, @Nonnull final String[] args, @Nullable final BlockPos targetPos) {
			return args.length == 1 ? getListOfStringsMatchingLastWord(args, Arrays.asList("mark", "list"))
					: Collections.emptyList();
		}
	}

	@Nullable
	private static Path worldFolder() {
		final MinecraftServer server = Minecraft.getMinecraft().getIntegratedServer();
		return server == null ? null : server.getWorld(0).getSaveHandler().getWorldDirectory().toPath();
	}

	@Nonnull
	private static String mark(@Nonnull final String name) throws Exception {
		final Minecraft mc = Minecraft.getMinecraft();
		final Path folder = worldFolder();
		final EntityPlayerSP player = mc.player;
		if (folder == null || player == null || mc.world == null)
			return "Scenes are kept in single player worlds only";
		final Biome biome = mc.world.getBiome(player.getPosition());
		final ResourceLocation key = Biome.REGISTRY.getNameForObject(biome);
		final String biomeName = key == null ? "?" : key.toString();
		final SceneLocations.Location location = new SceneLocations.Location(biomeName, player.posX, player.posY,
				player.posZ, player.rotationYaw, player.rotationPitch, 1D, true);
		SceneLocations.load(folder).put(name, location,
				mc.getIntegratedServer().getWorld(0).getSeed());
		return String.format("Scene '%s' marked at %.1f %.1f %.1f (yaw %.1f, pitch %.1f) in %s", name, location.x,
				location.y, location.z, location.yaw, location.pitch, biomeName);
	}

	@Nonnull
	private static String list() throws Exception {
		final Path folder = worldFolder();
		if (folder == null)
			return "Scenes are kept in single player worlds only";
		final Map<String, SceneLocations.Location> all = SceneLocations.load(folder).all();
		if (all.isEmpty())
			return "No scenes in this world yet";
		final StringBuilder text = new StringBuilder("Scenes in this world:");
		all.forEach((name, l) -> text.append(String.format("%n  %s: %s at %.0f %.0f %.0f%s", name, l.biome, l.x, l.y,
				l.z, l.marked ? " (marked)" : String.format(" (found, %.0f%% biome)", 100 * l.biomeShare))));
		return text.toString();
	}
}
