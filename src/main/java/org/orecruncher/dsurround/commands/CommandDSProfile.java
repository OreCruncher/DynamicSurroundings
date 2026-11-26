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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.profiling.ProfileScene;
import org.orecruncher.dsurround.profiling.Profiler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * /dsprofile [scene] [seconds]: sets up a scene and records the game with Java Flight Recorder while it shows (see
 * {@link Profiler}), for the player to send to the mod's authors when something is slow, and for finding what is.
 * With no scene, the game as it is ({@link ProfileScene#GENERAL}); with no length, two minutes. A client command.
 */
@SideOnly(Side.CLIENT)
public final class CommandDSProfile extends CommandBase {

	private static final String COMMAND = "dsprofile";
	private static final int DEFAULT_SECONDS = 120;
	private static final int LEAST_SECONDS = 10;
	private static final int MOST_SECONDS = 600;

	@Override
	@Nonnull
	public String getName() {
		return COMMAND;
	}

	@Override
	@Nonnull
	public String getUsage(@Nonnull final ICommandSender sender) {
		return "/" + COMMAND + " [" + String.join("|", sceneNames()) + "] [" + LEAST_SECONDS + "-" + MOST_SECONDS
				+ " seconds]";
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
			@Nonnull final String[] args) throws CommandException {
		if (args.length > 2)
			throw new WrongUsageException(getUsage(sender));
		ProfileScene scene = ProfileScene.GENERAL;
		int seconds = DEFAULT_SECONDS;
		if (args.length > 0) {
			scene = sceneOf(args[0]);
			if (scene == null)
				throw new WrongUsageException(getUsage(sender));
		}
		if (args.length > 1)
			seconds = parseInt(args[1], LEAST_SECONDS, MOST_SECONDS);
		for (final ITextComponent line : Profiler.start(scene, seconds))
			sender.sendMessage(line);
	}

	@Override
	@Nonnull
	public List<String> getTabCompletions(@Nonnull final MinecraftServer server, @Nonnull final ICommandSender sender,
			@Nonnull final String[] args, @Nullable final BlockPos targetPos) {
		if (args.length == 1)
			return getListOfStringsMatchingLastWord(args, sceneNames());
		return Collections.emptyList();
	}

	@Nullable
	private static ProfileScene sceneOf(@Nonnull final String name) {
		for (final ProfileScene scene : ProfileScene.values())
			if (scene.commandName().equals(name.toLowerCase(Locale.ROOT)))
				return scene;
		return null;
	}

	@Nonnull
	private static List<String> sceneNames() {
		return Arrays.asList(Arrays.stream(ProfileScene.values()).map(ProfileScene::commandName)
				.toArray(String[]::new));
	}
}
