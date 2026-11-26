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

package org.orecruncher.dsurround.profiling;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.lib.profiling.JfrSupport;
import org.orecruncher.dsurround.lib.profiling.ProfileFiles;
import org.orecruncher.dsurround.lib.profiling.RenderTimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * /dsprofile: sets up a scene ({@link ProfileScene}), records the game with Java Flight Recorder while it shows, and
 * zips the recording into the mod's profiles folder with a short summary (see {@link ProfileFiles}), nothing private in
 * it (see {@code FlightRecording}). When it is done, the player is told where it is, the summary is copied to the
 * clipboard (1.12.2's chat can't copy on a click), and the log gets the render timers and the summary.
 * <p>
 * Java Flight Recorder needs Java 8u262 or newer: without it, the player is told, and nothing that uses it is loaded.
 * Ported from the 26.x line.
 */
@SideOnly(Side.CLIENT)
public final class Profiler {

	/**
	 * The start of the recordings' file names
	 */
	public static final String FILE_PREFIX = "dsurround-";

	private static final String KEY = "dsurround.command.dsprofile.";

	// Added to the scene's settling the first time since the game started, for JFR to warm up in: the first
	// recording's slowdown lasted seconds
	static final int WARM_UP_SECONDS = 10;

	private static final AtomicBoolean BUSY = new AtomicBoolean();

	private Profiler() {
	}

	/**
	 * Where recordings go
	 */
	@Nonnull
	public static Path folder() {
		return new File(ModBase.dataDirectory(), "profiles").toPath();
	}

	/**
	 * Whether Java Flight Recorder can record here
	 */
	public static boolean isAvailable() {
		return JfrSupport.isAvailable();
	}

	/**
	 * Sets the scene up and starts recording, on the render thread. The recording runs on a thread of its own.
	 *
	 * @return what to tell the player
	 */
	@Nonnull
	public static List<ITextComponent> start(@Nonnull final ProfileScene scene, final int seconds) {
		return start(scene, seconds, path -> {
		});
	}

	/**
	 * As {@link #start(ProfileScene, int)}, telling {@code done} on the render thread where the zipped recording is
	 * once it is finished, or null if it couldn't be made. Not told if it didn't start (the result says why).
	 */
	@Nonnull
	public static List<ITextComponent> start(@Nonnull final ProfileScene scene, final int seconds,
			@Nonnull final Consumer<Path> done) {
		final List<ITextComponent> lines = new ArrayList<>();
		if (!isAvailable()) {
			lines.add(new TextComponentTranslation(KEY + "unavailable", System.getProperty("java.version")));
			return lines;
		}
		if (!BUSY.compareAndSet(false, true)) {
			lines.add(new TextComponentTranslation(KEY + "busy"));
			return lines;
		}

		final Minecraft mc = Minecraft.getMinecraft();
		final ProfileEnvironment environment;
		try {
			environment = ProfileEnvironment.capture();
		} catch (final RuntimeException e) {
			BUSY.set(false);
			ModBase.log().error("Unable to capture the profile's environment", e);
			lines.add(new TextComponentTranslation(KEY + "failed", e.toString()));
			return lines;
		}

		final int settle = scene.settle() + (ProfileSources.isWarm() ? 0 : WARM_UP_SECONDS);
		lines.add(new TextComponentTranslation(KEY + "started", scene.commandName(),
				new TextComponentTranslation(scene.translationKey() + ".what"), settle, seconds,
				new TextComponentTranslation(scene.translationKey() + ".tip")));
		if (!scene.commands().isEmpty()) {
			final String list = "/" + String.join(", /", scene.commands());
			final EntityPlayerSP player = mc.player;
			if (mc.isIntegratedServerRunning() && player != null) {
				for (final String command : scene.commands())
					player.sendChatMessage("/" + command);
				lines.add(new TextComponentTranslation(KEY + "sent", list));
			} else {
				lines.add(new TextComponentTranslation(KEY + "notsent", list));
			}
		}

		final Path file = folder().resolve(ProfileFiles.name(FILE_PREFIX, scene.commandName(), LocalDateTime.now()));
		final Thread thread = new Thread(() -> {
			Path zip = null;
			try {
				zip = record(scene, settle, file, seconds, environment);
			} finally {
				final Path result = zip;
				mc.addScheduledTask(() -> {
					BUSY.set(false);
					done.accept(result);
				});
			}
		}, "Dynamic Surroundings profile");
		thread.setDaemon(true);
		thread.start();
		return lines;
	}

	/**
	 * @return the zipped recording, or null if it couldn't be made
	 */
	@Nullable
	private static Path record(@Nonnull final ProfileScene scene, final int settle, @Nonnull final Path file,
			final int seconds, @Nonnull final ProfileEnvironment environment) {
		try {
			java.nio.file.Files.createDirectories(file.getParent());
			ProfileSources.warmUp(Duration.ofSeconds(settle));
			tell(new TextComponentTranslation(KEY + "recording", seconds));
			ProfileSources.record(file, Duration.ofSeconds(seconds), environment);
			// A summary to paste where a player asks for help; the recording is still made without one
			String text = null;
			ProfileHeadline headline = null;
			try {
				final ProfileSummary summary = ProfileSummary.of(file);
				text = summary.text(scene.commandName());
				headline = summary.headline();
			} catch (final Exception e) {
				ModBase.log().warn("Unable to summarize the profile %s: %s", file.getFileName(), e);
			}
			final Path zip = ProfileFiles.zip(file, text);
			final String summaryText = text;
			final ProfileHeadline summaryHeadline = headline;
			Minecraft.getMinecraft().addScheduledTask(() -> report(zip, summaryText, summaryHeadline));
			return zip;
		} catch (final Throwable t) {
			ModBase.log().error("Unable to record a profile", t);
			tell(new TextComponentTranslation(KEY + "failed", t.toString()));
			return null;
		}
	}

	/**
	 * Says where the recording is, the name opening it when clicked, with the frame rate and the costliest effect, and
	 * copies the summary to the clipboard; and logs it, with the summary and the render timers that measured lately. On
	 * the render thread.
	 */
	private static void report(@Nonnull final Path file, @Nullable final String summary,
			@Nullable final ProfileHeadline headline) {
		final ITextComponent name = new TextComponentString(file.getFileName().toString());
		name.setStyle(new Style().setColor(TextFormatting.AQUA).setUnderlined(true)
				.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.toAbsolutePath().toString()))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(file.toString()))));
		final int fps = Minecraft.getDebugFPS();
		tell(new TextComponentTranslation(KEY + "done", name, fps));
		ModBase.log().info("Profile recorded to %s (%d FPS)", file, fps);
		for (final RenderTimer timer : RenderTimers.all()) {
			if (timer.isStale())
				continue;
			ModBase.log().info("  %s: GPU %.3f ms (peak %.3f), CPU %.3f ms", timer.name(), timer.getMSecs(),
					timer.getPeakMSecs(), timer.getCpuMSecs());
		}
		if (headline != null)
			tell(new TextComponentTranslation(KEY + "headline", headline.fps, headline.effect,
					percent(headline.gpuShare), percent(headline.cpuShare)));
		if (summary != null) {
			ModBase.log().info("Profile summary:%n%s", summary);
			GuiScreen.setClipboardString(summary);
			tell(new TextComponentTranslation(KEY + "copied"));
		}
	}

	@Nonnull
	private static String percent(final double share) {
		return String.format(Locale.ROOT, "%.0f%%", share * 100D);
	}

	private static void tell(@Nonnull final ITextComponent message) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			final EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (player != null)
				player.sendMessage(message);
		});
	}
}
