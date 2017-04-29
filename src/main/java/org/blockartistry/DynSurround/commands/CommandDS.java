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

import java.text.DecimalFormat;
import java.util.List;

import org.blockartistry.DynSurround.ModOptions;
import org.blockartistry.DynSurround.data.DimensionEffectData;
import org.blockartistry.DynSurround.registry.RegistryManager;
import org.blockartistry.DynSurround.server.services.AuroraService;
import org.blockartistry.lib.Localization;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public final class CommandDS extends CommandBase {

	private static final String COMMAND = ModOptions.commandNameDS;
	private static final String COMMAND_OPTION_RAIN = "rain";
	private static final String COMMAND_OPTION_THUNDER = "thunder";
	private static final String COMMAND_OPTION_AURORA = "aurora";
	private static final String COMMAND_OPTION_STATUS = "status";
	private static final String COMMAND_OPTION_RESET = "reset";
	private static final String COMMAND_OPTION_RELOAD = "reload";
	private static final String COMMAND_OPTION_SETMIN = "setmin";
	private static final String COMMAND_OPTION_SETMAX = "setmax";
	private static final String COMMAND_OPTION_SETSTRENGTH = "setstr";
	private static final String COMMAND_OPTION_SETTIME = "settime";
	private static final String COMMAND_OPTION_CONFIG = "config";

	private static final List<String> ALIAS = ImmutableList.<String>builder().add(ModOptions.commandAliasDS.split(" "))
			.build();
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

	public static String rainStatusOutput(final World world, final DimensionEffectData data) {
		final StringBuilder builder = new StringBuilder();
		final float minutes = (world.getWorldInfo().getRainTime() / 20.0F) / 60.0F;
		builder.append(data.toString());
		builder.append("; isRaining: ").append(Boolean.toString(world.isRaining()));
		builder.append("; isSurface: ").append(Boolean.toString(world.provider.isSurfaceWorld()));
		builder.append("; strength: ").append(FORMATTER.format(world.getRainStrength(1.0F) * 100));
		builder.append("; timer: ").append(FORMATTER.format(minutes)).append(" minutes");
		return builder.toString();
	}

	public static String thunderStatusOutput(final World world, final DimensionEffectData data) {
		final StringBuilder builder = new StringBuilder();
		final float minutes = (world.getWorldInfo().getThunderTime() / 20.0F) / 60.0F;
		builder.append("dim ").append(data.getDimensionId());
		builder.append("; isThundering: ").append(Boolean.toString(world.isThundering()));
		builder.append("; isSurface: ").append(Boolean.toString(world.provider.isSurfaceWorld()));
		builder.append("; strength: ").append(FORMATTER.format(world.getThunderStrength(1.0F) * 100));
		builder.append("; timer: ").append(FORMATTER.format(minutes)).append(" minutes");
		return builder.toString();
	}

	public static String config(final World world, final DimensionEffectData data) {
		return data.configString();
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getName() {
		return COMMAND;
	}

	@Override
	public List<String> getAliases() {
		return ALIAS;
	}

	@Override
	public String getUsage(final ICommandSender sender) {
		return TextFormatting.GOLD + "/" + COMMAND + " help" + TextFormatting.BLUE
				+ " -- Help for Dynamic Surroundings";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] parms) throws CommandException {

		try {
			final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
			final World world = player.world;
			final DimensionEffectData data = DimensionEffectData.get(world);

			boolean showHelp = false;
			TextComponentString feedback = null;

			if (parms.length == 0) {
				showHelp = true;
			} else if (COMMAND_OPTION_RESET.compareToIgnoreCase(parms[0]) == 0) {
				world.provider.resetRainAndThunder();
				feedback = new TextComponentString(Localization.format("msg.RainReset"));
			} else if (COMMAND_OPTION_RELOAD.compareToIgnoreCase(parms[0]) == 0) {
				RegistryManager.reloadResources(null);
				feedback = new TextComponentString(Localization.format("msg.BiomeReload"));
			} else if (COMMAND_OPTION_CONFIG.compareToIgnoreCase(parms[0]) == 0) {
				feedback = new TextComponentString(config(world, data));
			} else if (COMMAND_OPTION_STATUS.compareToIgnoreCase(parms[0]) == 0) {
				if (parms.length < 2) {
					showHelp = true;
				} else if (COMMAND_OPTION_RAIN.compareToIgnoreCase(parms[1]) == 0) {
					feedback = new TextComponentString(rainStatusOutput(world, data));
				} else if (COMMAND_OPTION_THUNDER.compareToIgnoreCase(parms[1]) == 0) {
					feedback = new TextComponentString(thunderStatusOutput(world, data));
				} else if (COMMAND_OPTION_AURORA.compareToIgnoreCase(parms[1]) == 0) {
					feedback = new TextComponentString(AuroraService.getAuroraData(player));
				}
			} else if (COMMAND_OPTION_SETTIME.compareToIgnoreCase(parms[0]) == 0) {
				if (parms.length < 3) {
					showHelp = true;
				} else {
					final double d = parseDouble(parms[2], 0.0D, 1000.0D) * 20.0D * 60.0D;
					if (COMMAND_OPTION_RAIN.compareToIgnoreCase(parms[1]) == 0) {
						world.getWorldInfo().setRainTime((int) d);
						feedback = new TextComponentString(Localization.format("msg.RainTimeSet", FORMATTER.format(d)));
					} else if (COMMAND_OPTION_THUNDER.compareToIgnoreCase(parms[1]) == 0) {
						world.getWorldInfo().setThunderTime((int) d);
						feedback = new TextComponentString(
								Localization.format("msg.ThunderTimeSet", FORMATTER.format(d)));
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
						data.setRainIntensity((float) d);
						feedback = new TextComponentString(Localization.format("msg.RainIntensitySet",
								FORMATTER.format(data.getRainIntensity() * 100)));
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
						data.setMinRainIntensity((float) d);
						feedback = new TextComponentString(Localization.format("msg.MinRainIntensitySet",
								FORMATTER.format(data.getMinRainIntensity() * 100)));
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
						data.setMaxRainIntensity((float) d);
						feedback = new TextComponentString(Localization.format("msg.MaxRainIntensitySet",
								FORMATTER.format(data.getMaxRainIntensity() * 100)));
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
