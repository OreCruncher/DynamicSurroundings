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

package org.orecruncher.dsurround.commands;

import java.text.DecimalFormat;
import java.util.List;

import org.orecruncher.dsurround.ModOptions;
import org.orecruncher.dsurround.capabilities.CapabilityDimensionInfo;
import org.orecruncher.dsurround.capabilities.dimension.IDimensionInfoEx;
import org.orecruncher.dsurround.registry.RegistryManager;
import org.orecruncher.lib.Localization;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public final class CommandDS extends CommandBase {

	private static final String COMMAND = ModOptions.commands.ds.commandNameDS;
	private static final String COMMAND_OPTION_RAIN = "rain";
	private static final String COMMAND_OPTION_THUNDER = "thunder";
	private static final String COMMAND_OPTION_STATUS = "status";
	private static final String COMMAND_OPTION_RESET = "reset";
	private static final String COMMAND_OPTION_RELOAD = "reload";
	private static final String COMMAND_OPTION_SETMIN = "setmin";
	private static final String COMMAND_OPTION_SETMAX = "setmax";
	private static final String COMMAND_OPTION_SETSTRENGTH = "setstr";
	private static final String COMMAND_OPTION_SETTIME = "settime";
	private static final String COMMAND_OPTION_CONFIG = "config";

	private static final List<String> ALIAS = ImmutableList.<String>builder()
			.add(ModOptions.commands.ds.commandAliasDS.split(" ")).build();
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.0");

	private static final List<String> HELP = ImmutableList.<String>builder()
			.add(TextFormatting.GOLD + "Dynamic Surroundings command help:")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " reset")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " reload")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " config")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " status <rain|thunder|aurora>")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " settime <rain|thunder> 0-1000")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " setstr rain 0-100")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " setmin rain 0-100")
			.add(TextFormatting.YELLOW + "/" + COMMAND + " setmax rain 0-100").build();

	public static String rainStatusOutput(final World world, final IDimensionInfoEx data) {
		final StringBuilder builder = new StringBuilder();
		final float minutes = (world.getWorldInfo().getRainTime() / 20.0F) / 60.0F;
		builder.append(data.toString());
		builder.append("; isRaining: ").append(world.isRaining());
		builder.append("; isSurface: ").append(world.provider.isSurfaceWorld());
		builder.append("; strength: ").append(FORMATTER.format(world.getRainStrength(1.0F) * 100));
		builder.append("; timer: ").append(FORMATTER.format(minutes)).append(" minutes");
		return builder.toString();
	}

	public static String thunderStatusOutput(final World world, final IDimensionInfoEx data) {
		final StringBuilder builder = new StringBuilder();
		final float minutes = (world.getWorldInfo().getThunderTime() / 20.0F) / 60.0F;
		builder.append("dim ").append(data.getId());
		builder.append("; isThundering: ").append(world.isThundering());
		builder.append("; isSurface: ").append(world.provider.isSurfaceWorld());
		builder.append("; strength: ").append(FORMATTER.format(world.getThunderStrength(1.0F) * 100));
		builder.append("; timer: ").append(FORMATTER.format(minutes)).append(" minutes");
		return builder.toString();
	}

	public static String config(final IDimensionInfoEx data) {
		return data.configString();
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Nonnull
	@Override
	public String getName() {
		return COMMAND;
	}

	@Nonnull
	@Override
	public List<String> getAliases() {
		return ALIAS;
	}

	@Override
	public String getUsage(@Nonnull final ICommandSender sender) {
		return TextFormatting.GOLD + "/" + COMMAND + " help" + TextFormatting.BLUE
				+ " -- Help for Dynamic Surroundings";
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] parms) {

		try {
			final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
			final World world = player.world;
			final IDimensionInfoEx data = (IDimensionInfoEx) CapabilityDimensionInfo.getCapability(world);

			boolean showHelp = false;
			TextComponentString feedback = null;

			if (parms.length == 0) {
				showHelp = true;
			} else if (COMMAND_OPTION_RESET.compareToIgnoreCase(parms[0]) == 0) {
				world.provider.resetRainAndThunder();
				feedback = new TextComponentString(Localization.format("dsurround.msg.RainReset"));
			} else if (COMMAND_OPTION_RELOAD.compareToIgnoreCase(parms[0]) == 0) {
				RegistryManager.doReload();
				feedback = new TextComponentString(Localization.format("dsurround.msg.BiomeReload"));
			} else if (COMMAND_OPTION_CONFIG.compareToIgnoreCase(parms[0]) == 0) {
				if (data != null)
					feedback = new TextComponentString(config(data));
			} else if (COMMAND_OPTION_STATUS.compareToIgnoreCase(parms[0]) == 0) {
				if (parms.length < 2) {
					showHelp = true;
				} else if (COMMAND_OPTION_RAIN.compareToIgnoreCase(parms[1]) == 0) {
					if (data != null)
						feedback = new TextComponentString(rainStatusOutput(world, data));
				} else if (COMMAND_OPTION_THUNDER.compareToIgnoreCase(parms[1]) == 0) {
					if (data != null)
						feedback = new TextComponentString(thunderStatusOutput(world, data));
				}
			} else if (COMMAND_OPTION_SETTIME.compareToIgnoreCase(parms[0]) == 0) {
				if (parms.length < 3) {
					showHelp = true;
				} else {
					final double d = parseDouble(parms[2], 0.0D, 1000.0D) * 20.0D * 60.0D;
					if (COMMAND_OPTION_RAIN.compareToIgnoreCase(parms[1]) == 0) {
						world.getWorldInfo().setRainTime((int) d);
						feedback = new TextComponentString(
								Localization.format("dsurround.msg.RainTimeSet", FORMATTER.format(d)));
					} else if (COMMAND_OPTION_THUNDER.compareToIgnoreCase(parms[1]) == 0) {
						world.getWorldInfo().setThunderTime((int) d);
						feedback = new TextComponentString(
								Localization.format("dsurround.msg.ThunderTimeSet", FORMATTER.format(d)));
					} else {
						showHelp = true;
					}
				}
			} else if (COMMAND_OPTION_SETSTRENGTH.compareToIgnoreCase(parms[0]) == 0) {
				if (parms.length < 3) {
					showHelp = true;
				} else {
					final double d = parseDouble(parms[2], 0.0D, 100.0D) / 100.0D;
					if (COMMAND_OPTION_RAIN.compareToIgnoreCase(parms[1]) == 0) {
						if (data != null) {
							data.setRainIntensity((float) d);
							feedback = new TextComponentString(Localization.format("dsurround.msg.RainIntensitySet",
									FORMATTER.format(data.getRainIntensity() * 100)));
						}
					} else {
						showHelp = true;
					}
				}
			} else if (COMMAND_OPTION_SETMIN.compareToIgnoreCase(parms[0]) == 0) {
				if (parms.length < 3) {
					showHelp = true;
				} else {
					final double d = parseDouble(parms[2], 0.0D, 100.0D) / 100.0D;
					if (COMMAND_OPTION_RAIN.compareToIgnoreCase(parms[1]) == 0) {
						if (data != null) {
							data.setMinRainIntensity((float) d);
							feedback = new TextComponentString(Localization.format("dsurround.msg.MinRainIntensitySet",
									FORMATTER.format(data.getMinRainIntensity() * 100)));
						}
					} else {
						showHelp = true;
					}
				}

			} else if (COMMAND_OPTION_SETMAX.compareToIgnoreCase(parms[0]) == 0) {
				if (parms.length < 3) {
					showHelp = true;
				} else {
					final double d = parseDouble(parms[2], 0.0D, 100.0D) / 100.0D;
					if (COMMAND_OPTION_RAIN.compareToIgnoreCase(parms[1]) == 0) {
						if (data != null) {
							data.setMaxRainIntensity((float) d);
							feedback = new TextComponentString(Localization.format("dsurround.msg.MaxRainIntensitySet",
									FORMATTER.format(data.getMaxRainIntensity() * 100)));
						}
					} else {
						showHelp = true;
					}
				}

			} else {
				showHelp = true;
			}

			if (showHelp) {
				for (final String line : HELP)
					sender.sendMessage(new TextComponentString(line));
			} else if (feedback != null) {
				player.sendMessage(feedback);
			}

		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

}
