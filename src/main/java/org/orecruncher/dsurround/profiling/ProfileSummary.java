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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jdk.jfr.ValueDescriptor;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

/**
 * A short summary of a recording, for players to paste where they ask for help (an issue, Discord) when sending the
 * recording itself is too much: what it was made on, the frames, what each effect cost, the client ticks and GC. In
 * English, for the mod's authors; kept to {@link #LIMIT} characters, a Discord message's worth, dropping the cheapest
 * effects if it must. Made from the finished recording, which holds nothing private, so neither can this. Uses
 * {@code jdk.jfr}: only loaded once a recording has been made. Ported from the 26.x line.
 */
final class ProfileSummary {

	/**
	 * The most characters a summary has: a Discord message holds 2000
	 */
	static final int LIMIT = 1900;
	// An effect that cost less than this, GPU and CPU together, in ms a frame, drew nothing to speak of
	private static final double TRIVIAL_MS = 0.01;
	private static final double TICKS_PER_SECOND = 20D;
	// Handlers named in the ticks' line
	private static final int TOP_HANDLERS = 3;

	// What the recording was made on, by the Environment event's field names
	private final Map<String, Object> environment = new LinkedHashMap<>();
	// Each second's frames: frames, mean, p95, max, gpu, cpu
	private final List<double[]> frames = new ArrayList<>();
	// Each effect's averages, as written once a second
	private final Map<String, List<double[]>> timers = new LinkedHashMap<>();
	// The effects timed once a tick rather than once a frame
	private final Set<String> perTick = new HashSet<>();
	// Each tick's handlers' time, ns
	private final Map<Long, Long> ticks = new TreeMap<>();
	private final Map<String, Long> handlerTotals = new HashMap<>();
	private final List<Double> pauses = new ArrayList<>();

	/**
	 * Reads a finished recording
	 */
	@Nonnull
	static ProfileSummary of(@Nonnull final Path recording) throws IOException {
		final ProfileSummary summary = new ProfileSummary();
		try (RecordingFile file = new RecordingFile(recording)) {
			while (file.hasMoreEvents())
				summary.add(file.readEvent());
		}
		return summary;
	}

	private void add(@Nonnull final RecordedEvent event) {
		switch (event.getEventType().getName()) {
		case "dsurround.Environment":
			for (final ValueDescriptor field : event.getFields()) {
				final String name = field.getName();
				if (!name.equals("startTime") && !name.equals("duration") && !name.equals("eventThread")
						&& !name.equals("stackTrace"))
					this.environment.put(name, event.getValue(name));
			}
			break;
		case "dsurround.Frames":
			frame(event.getInt("frames"), event.getDouble("mean"), event.getDouble("p95"), event.getDouble("max"),
					event.getDouble("gpu"), event.getDouble("cpu"));
			break;
		case "dsurround.RenderTimer":
			timer(event.getString("effect"), event.getDouble("gpuAverage"), event.getDouble("cpuAverage"),
					event.getBoolean("perTick"));
			break;
		case "dsurround.HandlerTick":
			handlerTick(event.getLong("tick"), event.getString("handler"), event.getLong("time"));
			break;
		case "jdk.GCPhasePause":
			pause(event.getDuration().toNanos() / 1_000_000D);
			break;
		default:
			break;
		}
	}

	void environment(@Nonnull final String field, @Nonnull final Object value) {
		this.environment.put(field, value);
	}

	void frame(final int count, final double mean, final double p95, final double max, final double gpu,
			final double cpu) {
		this.frames.add(new double[] { count, mean, p95, max, gpu, cpu });
	}

	/**
	 * @param perTick whether the effect is timed once a client tick rather than once a frame
	 */
	void timer(@Nonnull final String effect, final double gpu, final double cpu, final boolean perTick) {
		this.timers.computeIfAbsent(effect, k -> new ArrayList<>()).add(new double[] { gpu, cpu });
		if (perTick)
			this.perTick.add(effect);
	}

	void handlerTick(final long tick, @Nonnull final String handler, final long nanos) {
		this.ticks.merge(tick, nanos, Long::sum);
		this.handlerTotals.merge(handler, nanos, Long::sum);
	}

	void pause(final double ms) {
		this.pauses.add(ms);
	}

	/**
	 * Drawing a frame, on the GPU and CPU, ms: the seconds' means; zeros without frames
	 */
	private double[] drawing() {
		if (this.frames.isEmpty())
			return new double[] { 0D, 0D };
		double gpu = 0D, cpu = 0D;
		for (final double[] second : this.frames) {
			gpu += second[4];
			cpu += second[5];
		}
		return new double[] { gpu / this.frames.size(), cpu / this.frames.size() };
	}

	private int fps() {
		long count = 0;
		double total = 0D;
		for (final double[] second : this.frames) {
			count += (long) second[0];
			total += second[1] * second[0];
		}
		return count == 0 || total <= 0D ? 0 : (int) Math.round(1000D * count / total);
	}

	/**
	 * The effects that drew something, the costliest first: name, GPU and CPU ms. A part ("- " name, following its
	 * effect) is in its effect's time and left out, unless it took longer than the effect, which it then can't be part
	 * of, or is timed once a tick: that is named for its effect. One timed once a tick is given as its time a frame (20
	 * ticks a second spread over the frames), to compare with the rest.
	 */
	@Nonnull
	private List<Map.Entry<String, double[]>> effects() {
		final List<Map.Entry<String, double[]>> effects = new ArrayList<>();
		String effect = "";
		double[] effectMeans = { 0D, 0D };
		final int fps = fps();
		for (final Map.Entry<String, List<double[]>> entry : this.timers.entrySet()) {
			double gpu = 0D, cpu = 0D;
			for (final double[] row : entry.getValue()) {
				gpu += row[0];
				cpu += row[1];
			}
			final int n = entry.getValue().size();
			final double[] means = { gpu / n, cpu / n };
			String name = entry.getKey();
			if (this.perTick.contains(name) && fps > 0) {
				means[0] *= TICKS_PER_SECOND / fps;
				means[1] *= TICKS_PER_SECOND / fps;
			}
			if (name.startsWith("- ")) {
				if (!this.perTick.contains(entry.getKey()) && means[0] <= effectMeans[0] && means[1] <= effectMeans[1])
					continue;
				name = effect + " " + name.substring(2).toLowerCase(Locale.ROOT);
			} else {
				effect = name;
				effectMeans = means;
			}
			if (means[0] + means[1] >= TRIVIAL_MS)
				effects.add(new java.util.AbstractMap.SimpleImmutableEntry<>(name, means));
		}
		effects.sort(Comparator.comparingDouble((Map.Entry<String, double[]> e) -> e.getValue()[0] + e.getValue()[1])
				.reversed());
		return effects;
	}

	/**
	 * The frame rate and the costliest effect, as a share of drawing a frame; null without frames or effects
	 */
	@Nullable
	ProfileHeadline headline() {
		final double[] drawing = drawing();
		if (this.frames.isEmpty() || drawing[0] <= 0D && drawing[1] <= 0D)
			return null;
		ProfileHeadline best = null;
		for (final Map.Entry<String, double[]> effect : effects()) {
			final double gpuShare = share(effect.getValue()[0], drawing[0]);
			final double cpuShare = share(effect.getValue()[1], drawing[1]);
			if (best == null || Math.max(gpuShare, cpuShare) > Math.max(best.gpuShare, best.cpuShare))
				best = new ProfileHeadline(fps(), effect.getKey(), gpuShare, cpuShare);
		}
		return best;
	}

	private static double share(final double part, final double whole) {
		return whole > 0D ? part / whole : 0D;
	}

	/**
	 * The summary, at most {@link #LIMIT} characters
	 *
	 * @param scene the profile's scene, as the command names it
	 */
	@Nonnull
	String text(@Nonnull final String scene) {
		final List<String> lines = new ArrayList<>();
		final Map<String, Object> e = this.environment;
		final int seconds = this.frames.isEmpty() ? this.ticks.size() / 20 : this.frames.size();
		lines.add(f("Dynamic Surroundings profile: %s, %d s", scene, seconds));
		lines.add(f("%s, %s mods; Java %s; %s", get(e, "mod"), get(e, "mods"), get(e, "java"), get(e, "os")));
		lines.add(f("GPU: %s (%s; %s, driver %s)", get(e, "gpu"), get(e, "gpuVendor"), get(e, "backend"),
				get(e, "driver")));
		String display = f("Display: %sx%s, %s, render distance %s, vsync %s, frame limit %s", get(e, "width"),
				get(e, "height"), get(e, "graphics"), get(e, "renderDistance"),
				Boolean.TRUE.equals(e.get("vsync")) ? "on" : "off", limit(e.get("frameRateLimit")));
		if (Boolean.TRUE.equals(e.get("shaderPack")))
			display += ", shader pack";
		final Object run = e.get("devRun");
		if (run instanceof String && !((String) run).isEmpty())
			display += ", dev run " + run;
		lines.add(display);

		final double[] drawing = drawing();
		if (!this.frames.isEmpty()) {
			final double[] p95s = this.frames.stream().mapToDouble(s -> s[2]).sorted().toArray();
			final double longest = this.frames.stream().mapToDouble(s -> s[3]).max().orElse(0D);
			final double mean = fps() > 0 ? 1000D / fps() : 0D;
			lines.add(f(
					"Frames: %d fps, mean %.1f ms, p95 %.1f (worst second %.1f), longest %.1f; drawing GPU %.2f ms, CPU %.2f ms",
					fps(), mean, p95s[p95s.length / 2], p95s[p95s.length - 1], longest, drawing[0], drawing[1]));
		}

		if (!this.ticks.isEmpty()) {
			final double[] totals = this.ticks.values().stream().mapToDouble(n -> n / 1_000_000D).sorted().toArray();
			final double mean = Arrays.stream(totals).average().orElse(0D);
			final List<String> top = this.handlerTotals.entrySet().stream()
					.sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(TOP_HANDLERS)
					.map(h -> f("%s %.2f", h.getKey(), h.getValue() / 1_000_000D / totals.length))
					.collect(Collectors.toList());
			lines.add(f("Ticks: mean %.2f ms, p95 %.2f; most: %s", mean,
					totals[Math.min(totals.length - 1, (int) (totals.length * 0.95))], String.join(", ", top)));
		}

		if (!this.pauses.isEmpty()) {
			final double[] sorted = this.pauses.stream().mapToDouble(Double::doubleValue).sorted().toArray();
			lines.add(f("GC: %d pauses, median %.1f ms, longest %.1f ms", sorted.length, sorted[sorted.length / 2],
					sorted[sorted.length - 1]));
		}

		// The effects after what is above, as many as fit
		final List<String> effects = effects().stream().map(entry -> {
			final double[] ms = entry.getValue();
			return this.frames.isEmpty() ? f("%s %.2f/%.2f", entry.getKey(), ms[0], ms[1])
					: f("%s %.2f/%.2f (%.0f%%/%.0f%%)", entry.getKey(), ms[0], ms[1], 100D * share(ms[0], drawing[0]),
							100D * share(ms[1], drawing[1]));
		}).collect(Collectors.toList());
		final String effectsLine = fit(
				"Effects (ms GPU/CPU" + (this.frames.isEmpty() ? "" : ", share of drawing") + "): ", effects,
				LIMIT - length(lines) - 1);
		// After the frames, which they are shares of
		if (effectsLine != null)
			lines.add(Math.min(lines.size(), this.frames.isEmpty() ? 4 : 5), effectsLine);
		return String.join("\n", lines);
	}

	@Nonnull
	private static Object get(@Nonnull final Map<String, Object> map, @Nonnull final String key) {
		final Object value = map.get(key);
		return value == null ? "?" : value;
	}

	@Nonnull
	private static String limit(@Nullable final Object frameRateLimit) {
		// The video setting's top, 260, is no limit
		return frameRateLimit instanceof Number && ((Number) frameRateLimit).intValue() >= 260 ? "none"
				: String.valueOf(frameRateLimit);
	}

	private static int length(@Nonnull final List<String> lines) {
		int length = 0;
		for (final String line : lines)
			length += line.length() + 1;
		return Math.max(0, length - 1);
	}

	/**
	 * A line of as many of the items as fit in {@code room} characters, saying how many more there are; null if there
	 * are none, or not even the title fits
	 */
	@Nullable
	private static String fit(@Nonnull final String title, @Nonnull final List<String> items, final int room) {
		if (items.isEmpty() || room < title.length() + 12)
			return null;
		final StringBuilder line = new StringBuilder(title);
		int shown = 0;
		for (final String item : items) {
			final int left = items.size() - shown - 1;
			final int more = left > 0 ? f(", +%d more", left).length() : 0;
			if (line.length() + (shown > 0 ? 2 : 0) + item.length() + more > room)
				break;
			if (shown > 0)
				line.append(", ");
			line.append(item);
			shown++;
		}
		if (shown < items.size())
			line.append(shown > 0 ? ", " : "").append(f("+%d more", items.size() - shown));
		return line.toString();
	}

	@Nonnull
	private static String f(@Nonnull final String format, final Object... args) {
		return String.format(Locale.ROOT, format, args);
	}
}
