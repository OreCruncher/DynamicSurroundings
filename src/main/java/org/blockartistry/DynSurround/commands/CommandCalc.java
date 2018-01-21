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

package org.blockartistry.DynSurround.commands;

import java.util.List;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.lib.expression.Expression;
import org.blockartistry.lib.expression.ExpressionException;
import org.blockartistry.lib.expression.Variant;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CommandCalc extends CommandBase {

	private final static String COMMAND = ModOptions.commandNameCalc;
	private final static String COMMAND_OPTION_HELP = "help";
	private final static String COMMAND_OPTION_FUNCS = "funcs";
	private final static String COMMAND_OPTION_VARS = "vars";
	private final static String COMMAND_OPTION_OPS = "ops";

	private static final List<String> ALIAS = ImmutableList.<String>builder()
			.add(ModOptions.commandAliasCalc.split(" ")).build();

	private static final List<String> HELP = ImmutableList.<String>builder()
			.add(TextFormatting.GOLD + "Calculator command help:")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " <expression>")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " funcs").add(TextFormatting.YELLOW + "/" + COMMAND + " vars")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " ops").build();

	@Override
	public String getName() {
		return COMMAND;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public List<String> getAliases() {
		return ALIAS;
	}

	@Override
	public String getUsage(final ICommandSender sender) {
		return TextFormatting.GOLD + "/" + COMMAND + " help" + TextFormatting.BLUE + " -- Help for Calculator";
	}

	@Override
	public void execute(final MinecraftServer server, final ICommandSender sender, final String[] parms)
			throws CommandException {
		try {
			boolean showHelp = false;

			if (parms.length == 0) {
				showHelp = true;
			} else if (COMMAND_OPTION_HELP.compareToIgnoreCase(parms[0]) == 0) {
				showHelp = true;
			} else if (COMMAND_OPTION_FUNCS.compareToIgnoreCase(parms[0]) == 0) {
				final Expression exp = new Expression("0");
				for (final String line : exp.getDeclaredFunctions())
					sender.sendMessage(new TextComponentString(line));
			} else if (COMMAND_OPTION_VARS.compareToIgnoreCase(parms[0]) == 0) {
				final Expression exp = new Expression("0");
				for (final String line : exp.getDeclaredVariables())
					sender.sendMessage(new TextComponentString(line));
			} else if (COMMAND_OPTION_OPS.compareToIgnoreCase(parms[0]) == 0) {
				final Expression exp = new Expression("0");
				for (final String line : exp.getDeclaredOperators())
					sender.sendMessage(new TextComponentString(line));
			} else {
				try {
					final Expression exp = new Expression(buildString(parms, 0));
					final Variant result = exp.eval();
					sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "-> " + result.asString()));
				} catch (final ExpressionException t) {
					sender.sendMessage(new TextComponentString(TextFormatting.RED + t.getMessage()));
				} catch (final Throwable t) {
					sender.sendMessage(new TextComponentString(TextFormatting.RED + "Internal error"));
					showHelp = true;
				}
			}

			if (showHelp) {
				for (final String line : HELP)
					sender.sendMessage(new TextComponentString(line));
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

}
