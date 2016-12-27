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

package org.blockartistry.mod.DynSurround;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.mod.DynSurround.util.Localization;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

// Modeled after the BuildCraft version check system.
public final class VersionCheck implements Runnable {

	private static final String REMOTE_VERSION_FILE = "https://raw.githubusercontent.com/OreCruncher/DynamicSurroundings/master/versions.txt";
	private static final int VERSION_CHECK_RETRIES = 3;
	private static final int VERSION_CHECK_INTERVAL = 10000;

	public static enum UpdateStatus {
		UNKNOWN, CURRENT, OUTDATED, COMM_ERROR
	}

	public static final SoftwareVersion modVersion = new SoftwareVersion(DSurround.VERSION);
	public static SoftwareVersion currentVersion = new SoftwareVersion();
	public static UpdateStatus status = UpdateStatus.UNKNOWN;

	private static final String mcVersion = Loader.instance().getMinecraftModContainer().getVersion();

	public static class SoftwareVersion implements Comparable<SoftwareVersion> {

		public final int major;
		public final int minor;
		public final int revision;
		public final int patch;
		public final boolean isAlpha;
		public final boolean isBeta;

		public SoftwareVersion() {
			this.major = 0;
			this.minor = 0;
			this.revision = 0;
			this.patch = 0;
			this.isAlpha = false;
			this.isBeta = false;
		}

		public SoftwareVersion(String versionString) {

			// This can happen when running in debug
			if (versionString.charAt(0) == '@') {
				this.major = 0;
				this.minor = 0;
				this.revision = 0;
				this.patch = 0;
				this.isAlpha = false;
				this.isBeta = false;
				return;
			}

			assert versionString != null;
			assert versionString.length() > 0;

			isAlpha = StringUtils.containsIgnoreCase(versionString, "ALPHA");
			if (isAlpha)
				versionString = StringUtils.remove(versionString, "ALPHA");

			isBeta = StringUtils.containsIgnoreCase(versionString, "BETA");
			if(isBeta)
				versionString = StringUtils.remove(versionString, "BETA");

			final String[] parts = StringUtils.split(versionString, ".");
			final int numComponents = parts.length;

			assert numComponents >= 3;

			this.major = Integer.parseInt(parts[0]);
			this.minor = Integer.parseInt(parts[1]);
			this.revision = Integer.parseInt(parts[2]);
			if (numComponents == 4) {
				this.patch = Integer.parseInt(parts[3]);
			} else {
				this.patch = 0;
			}
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append(this.major).append('.').append(this.minor).append('.').append(this.revision);
			if (this.patch != 0)
				builder.append('.').append(this.patch);
			if (this.isAlpha)
				builder.append("ALPHA");
			if (this.isBeta)
				builder.append("BETA");
			return builder.toString();
		}

		@Override
		public int compareTo(final SoftwareVersion obj) {

			if (this.major != obj.major)
				return this.major - obj.major;

			if (this.minor != obj.minor)
				return this.minor - obj.minor;

			if (this.revision != obj.revision)
				return this.revision - obj.revision;

			return this.patch - obj.patch;
		}
	}

	private VersionCheck() {
	}

	private static final String CURSE_PROJECT_NAME = "238891";
	private static final String MOD_NAME_TEMPLATE = "DynamicSurroundings-1.10.2-[].jar";

	public static void register() {

		if (Loader.isModLoaded("VersionChecker")) {
			final NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("curseProjectName", CURSE_PROJECT_NAME);
			nbt.setString("curseFilenameParser", MOD_NAME_TEMPLATE);
			FMLInterModComms.sendRuntimeMessage(DSurround.MOD_ID, "VersionChecker", "addVersionCheck", nbt);
		}

		if (ModOptions.enableVersionChecking) {
			final VersionCheck test = new VersionCheck();
			MinecraftForge.EVENT_BUS.register(test);
			new Thread(test).start();
		}
	}

	@SubscribeEvent
	public void playerLogin(final PlayerLoggedInEvent event) {

		if (event.player instanceof EntityPlayer) {
			if (status == UpdateStatus.OUTDATED) {
				final String msg = Localization.format("msg.NewVersionAvailable.dsurround",
						DSurround.MOD_NAME, currentVersion, CURSE_PROJECT_NAME);
				final ITextComponent component = ITextComponent.Serializer.jsonToComponent(msg);
				event.player.addChatMessage(component);
			}
		}
	}

	private static void versionCheck() {
		try {

			String location = REMOTE_VERSION_FILE;
			HttpURLConnection conn = null;
			while (location != null && !location.isEmpty()) {
				URL url = new URL(location);

				if (conn != null) {
					conn.disconnect();
				}

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)");
				conn.connect();
				location = conn.getHeaderField("Coordinates");
			}

			if (conn == null) {
				throw new NullPointerException();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(":");
				if (mcVersion.matches(tokens[0])) {
					currentVersion = new SoftwareVersion(tokens[1]);
					break;
				}
			}

			status = UpdateStatus.CURRENT;
			if (modVersion.compareTo(currentVersion) < 0)
				status = UpdateStatus.OUTDATED;

			conn.disconnect();
			reader.close();

		} catch (Exception e) {
			ModLog.warn("Unable to read remote version data", e);
			status = UpdateStatus.COMM_ERROR;
		}
	}

	@Override
	public void run() {

		int count = 0;

		ModLog.info("Checking for newer mod version");

		try {

			do {
				if (count > 0) {
					ModLog.info("Awaiting attempt %d", count);
					Thread.sleep(VERSION_CHECK_INTERVAL);
				}
				versionCheck();
				count++;
			} while (count < VERSION_CHECK_RETRIES && status == UpdateStatus.COMM_ERROR);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		switch (status) {
		case COMM_ERROR:
			ModLog.warn("Version check failed");
			break;
		case CURRENT:
			ModLog.info("Dynamic Surroundings version [%s] is the same or newer than the current version [%s]", modVersion,
					currentVersion);
			break;
		case OUTDATED:
			ModLog.warn("Using outdated version [" + modVersion + "] for Minecraft " + mcVersion
					+ ". Consider updating to " + currentVersion + ".");
			break;
		case UNKNOWN:
			ModLog.warn("Unknown version check status!");
			break;
		default:
			break;

		}
	}
}
