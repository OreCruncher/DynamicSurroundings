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

package org.orecruncher.dsurround.lib.profiling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.EventSettings;
import jdk.jfr.EventType;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordingFile;

/**
 * A Java Flight Recorder recording of the running game, made in the game itself: the JDK's "profile" settings, only for
 * the events known to hold nothing that could tell who or where the player is ({@link #ALLOWED}), plus the caller's own
 * events (those named in its namespace), each fed by a {@link Source}. The only class here besides the caller's events
 * to use {@code jdk.jfr}, so nothing loads it until a recording is made (check {@link JfrSupport} first).
 * <p>
 * Players may send recordings to the mod's authors, so what is private is kept out: every other event is turned off for
 * the recording; the file is then read back, and if any other event got in anyway (another recording running at the
 * same time turns on what it asks for, in ours too) it is deleted rather than kept, as Java 8's recorder can't write a
 * file again with only some events; and the paths of the player's folders are redacted (see {@link Redaction}). It is
 * written under a name of its own until then, so a failure leaves nothing unredacted under the recording's name
 * ({@link ProfileFiles} clears it away). Ported from the 26.x line.
 */
public final class FlightRecording {

	/**
	 * The events a recording keeps, besides the caller's own: numbers, and the names of classes, methods and threads.
	 * The machine (processor, memory), samples of what each thread was running and allocating, garbage collection,
	 * threads waiting (locks, parking, sleeping), the JIT compiler, safepoints and VM operations, and the JVM's number
	 * and switch flags (the heap's size, the collector). Any other is left out until it has been looked at for what it
	 * carries: one that can carry anything private goes in {@link #PRIVATE} instead.
	 */
	public static final Set<String> ALLOWED = ImmutableSet.of(
			// The machine
			"jdk.CPUInformation", "jdk.CPULoad", "jdk.CPUTimeStampCounter", "jdk.ThreadCPULoad",
			"jdk.ThreadContextSwitchRate", "jdk.PhysicalMemory", "jdk.SwapSpace", "jdk.ResidentSetSize",
			"jdk.NativeMemoryUsage", "jdk.NativeMemoryUsageTotal", "jdk.VirtualizationInformation",
			// What threads run and allocate (Java 8 samples allocation by TLAB)
			"jdk.ExecutionSample", "jdk.NativeMethodSample", "jdk.ObjectAllocationSample",
			"jdk.ObjectAllocationInNewTLAB", "jdk.ObjectAllocationOutsideTLAB", "jdk.ThreadAllocationStatistics",
			"jdk.JavaThreadStatistics", "jdk.ThreadStart", "jdk.ThreadEnd", "jdk.ClassLoadingStatistics",
			"jdk.ExceptionStatistics", "jdk.DirectBufferStatistics",
			// Garbage collection
			"jdk.GarbageCollection", "jdk.YoungGarbageCollection", "jdk.OldGarbageCollection",
			"jdk.G1GarbageCollection", "jdk.ParallelOldGarbageCollection", "jdk.GCPhasePause", "jdk.GCPhasePauseLevel1",
			"jdk.GCPhasePauseLevel2", "jdk.GCPhaseConcurrent", "jdk.GCPhaseParallel", "jdk.GCHeapSummary",
			"jdk.G1HeapSummary", "jdk.PSHeapSummary", "jdk.MetaspaceSummary", "jdk.GCReferenceStatistics",
			"jdk.GCConfiguration", "jdk.GCHeapConfiguration", "jdk.YoungGenerationConfiguration", "jdk.SystemGC",
			"jdk.EvacuationFailed", "jdk.PromotionFailed", "jdk.ConcurrentModeFailure", "jdk.MetaspaceOOM",
			// Threads waiting
			"jdk.JavaMonitorEnter", "jdk.JavaMonitorWait", "jdk.ThreadPark", "jdk.ThreadSleep",
			// The JIT compiler, safepoints and VM operations
			"jdk.Compilation", "jdk.CompilationFailure", "jdk.CompilerConfiguration", "jdk.CompilerStatistics",
			"jdk.CodeCacheConfiguration", "jdk.CodeCacheStatistics", "jdk.CodeCacheFull", "jdk.Deoptimization",
			"jdk.SafepointBegin", "jdk.ExecuteVMOperation",
			// The JVM's flags but its text ones (paths), and what the recorder lost
			"jdk.BooleanFlag", "jdk.IntFlag", "jdk.UnsignedIntFlag", "jdk.LongFlag", "jdk.UnsignedLongFlag",
			"jdk.DoubleFlag", "jdk.DataLoss");

	/**
	 * Events known to carry something private, kept out even if they were allowed by mistake: the command line (the
	 * game's arguments hold the player's name, id and access token), environment variables and system properties
	 * (names, folders), the machine's other processes, file paths (which name the player's account), network addresses,
	 * certificates, exception messages (which often hold paths), loaded libraries and agents (paths), the OS (its
	 * description holds the machine's name on some systems), thread dumps, the recording's own destination, the JVM's
	 * text flags and heap dumps (paths).
	 */
	public static final Set<String> PRIVATE = ImmutableSet.of("jdk.JVMInformation", "jdk.InitialEnvironmentVariable",
			"jdk.InitialSystemProperty", "jdk.InitialSecurityProperty", "jdk.SecurityPropertyModification",
			"jdk.OSInformation", "jdk.SystemProcess", "jdk.NativeLibrary", "jdk.JavaAgent", "jdk.NativeAgent",
			"jdk.NetworkUtilization", "jdk.FileRead", "jdk.FileWrite", "jdk.FileForce", "jdk.SocketRead",
			"jdk.SocketWrite", "jdk.TLSHandshake", "jdk.X509Certificate", "jdk.X509Validation",
			"jdk.JavaExceptionThrow", "jdk.JavaErrorThrow", "jdk.ThreadDump", "jdk.ActiveRecording",
			"jdk.StringFlag", "jdk.StringFlagChanged", "jdk.HeapDump");

	// Set by the first warm-up, on the profile's thread; read on the render thread
	private static volatile boolean warm;

	private FlightRecording() {
	}

	/**
	 * Whether a recording keeps events of the type: the caller's own and {@link #ALLOWED}'s, never {@link #PRIVATE}'s
	 *
	 * @param namespace the caller's events' prefix ("dsurround.")
	 */
	public static boolean isAllowed(@Nonnull final String namespace, @Nonnull final String type) {
		return (type.startsWith(namespace) || ALLOWED.contains(type)) && !PRIVATE.contains(type);
	}

	static boolean isRecorderAvailable() {
		return FlightRecorder.isAvailable();
	}

	/**
	 * One of the caller's event types and what feeds it: what writes it once a second (null for one written as things
	 * happen, or once), whether that runs once more when the recording ends, for what was counted since its last
	 * second, and the sampler started and stopped with the recording.
	 */
	public static final class Source {

		private static final Runnable NOTHING = () -> {
		};

		final Class<? extends Event> type;
		@Nullable
		final Runnable write;
		final boolean flush;
		final Runnable start;
		final Runnable stop;

		public Source(@Nonnull final Class<? extends Event> type, @Nullable final Runnable write, final boolean flush,
				@Nonnull final Runnable start, @Nonnull final Runnable stop) {
			this.type = type;
			this.write = write;
			this.flush = flush;
			this.start = start;
			this.stop = stop;
		}

		/**
		 * Written once, as the recording starts
		 */
		@Nonnull
		public static Source once(@Nonnull final Class<? extends Event> type) {
			return new Source(type, null, false, NOTHING, NOTHING);
		}

		/**
		 * Written by its sampler as things happen
		 */
		@Nonnull
		public static Source live(@Nonnull final Class<? extends Event> type, @Nonnull final Runnable start,
				@Nonnull final Runnable stop) {
			return new Source(type, null, false, start, stop);
		}

		/**
		 * Written once a second, from values read at the time: nothing counted to flush
		 */
		@Nonnull
		public static Source periodic(@Nonnull final Class<? extends Event> type, @Nonnull final Runnable write,
				@Nonnull final Runnable start, @Nonnull final Runnable stop) {
			return new Source(type, write, false, start, stop);
		}
	}

	/**
	 * Records for {@code length}, then writes the recording to {@code file}, without what is private. Blocks the thread
	 * it is called on for that long, so not the render thread.
	 *
	 * @param onStart runs once the recording has started, for events written once (what it was made on)
	 */
	public static void record(@Nonnull final Path file, @Nonnull final Duration length, @Nonnull final String namespace,
			@Nonnull final List<Source> sources, @Nonnull final Runnable onStart) throws Exception {
		final Map<String, String> settings = settings(namespace);
		register(sources);

		final Path unfinished = file.resolveSibling(file.getFileName() + ProfileFiles.UNFINISHED);
		for (final Source source : sources)
			if (source.write != null)
				FlightRecorder.addPeriodicEvent(source.type, source.write);
		try {
			try (Recording recording = new Recording(settings)) {
				recording.setName("Dynamic Surroundings");
				recording.setToDisk(true);
				enable(recording, sources);
				recording.start();
				newStringEpoch();
				for (final Source source : sources)
					source.start.run();
				onStart.run();
				Thread.sleep(length.toMillis());
				stop(sources);
				// What has been counted since the last second's write
				for (final Source source : sources)
					if (source.flush && source.write != null)
						source.write.run();
				recording.stop();
				recording.dump(unfinished);
			}
			// Nothing outside the allowed events should be there, but another recording running at the same time turns
			// on what it asks for, in this one too. Java 8 can't write the file again without them, so it isn't kept
			final Set<String> stray = strayEvents(unfinished, namespace);
			if (!stray.isEmpty())
				throw new IOException("Another recording added events that may hold private details, so this one "
						+ "wasn't kept: " + stray);
			// And the paths of the player's folders redacted: shared between events, out of their reach. Only then
			// under the recording's name
			Redaction.redact(unfinished);
			Files.move(unfinished, file, StandardCopyOption.REPLACE_EXISTING);
		} finally {
			stop(sources);
			for (final Source source : sources)
				if (source.write != null)
					FlightRecorder.removePeriodicEvent(source.write);
			Files.deleteIfExists(unfinished);
		}
	}

	/**
	 * Moves the recorder on to a new chunk, so this recording writes every string it uses. Java 8's recorder caches
	 * strings of 16 to 128 characters with a one-bit epoch that flips at each chunk: two flips between recordings (one
	 * stopping, the next starting) left the cache looking current, and the second recording referred to names it never
	 * wrote (a bench run's second recording lost a handler's and a timer's names). A snapshot of a running on-disk
	 * recording starts a new chunk; the cache is then out of date and starts over.
	 */
	private static void newStringEpoch() {
		try (Recording snapshot = FlightRecorder.getFlightRecorder().takeSnapshot()) {
			// Only for the chunk it starts
			snapshot.getSize();
		}
	}

	// The event types in a recording that it shouldn't keep
	@Nonnull
	private static Set<String> strayEvents(@Nonnull final Path file, @Nonnull final String namespace)
			throws IOException {
		final Set<String> stray = new TreeSet<>();
		try (RecordingFile recorded = new RecordingFile(file)) {
			while (recorded.hasMoreEvents()) {
				final String type = recorded.readEvent().getEventType().getName();
				if (!isAllowed(namespace, type))
					stray.add(type);
			}
		}
		return stray;
	}

	/**
	 * Whether a recording has run since the game started, so the next starts without the first's slowdown
	 */
	public static boolean isWarm() {
		return warm;
	}

	/**
	 * Runs a recording with the same settings for {@code length}, kept nowhere, the first time only, so the recording
	 * after it starts as one would later in the session: the first recording since the game started slowed the game for
	 * seconds as JFR set itself up and instrumented the event classes. Later, just waits. Not the render thread.
	 */
	public static void warmUp(@Nonnull final Duration length, @Nonnull final String namespace,
			@Nonnull final List<Source> sources) throws IOException, ParseException, InterruptedException {
		if (warm) {
			Thread.sleep(length.toMillis());
			return;
		}
		register(sources);
		try (Recording recording = new Recording(settings(namespace))) {
			recording.setName("Dynamic Surroundings warm-up");
			recording.setToDisk(false);
			enable(recording, sources);
			recording.start();
			Thread.sleep(length.toMillis());
			recording.stop();
		}
		warm = true;
	}

	// The JDK's "profile" settings for the allowed events, and every other event type known turned off: one the
	// settings don't name records as its own annotations say, and most say it's on. Ours are set by enable
	@Nonnull
	private static Map<String, String> settings(@Nonnull final String namespace) throws IOException, ParseException {
		final Map<String, String> settings = new HashMap<>();
		for (final Map.Entry<String, String> setting : Configuration.getConfiguration("profile").getSettings()
				.entrySet()) {
			final String key = setting.getKey();
			if (isAllowed(namespace, key.substring(0, Math.max(key.indexOf('#'), 0))))
				settings.put(key, setting.getValue());
		}
		for (final EventType type : FlightRecorder.getFlightRecorder().getEventTypes())
			if (!isAllowed(namespace, type.getName()))
				settings.put(type.getName() + "#enabled", "false");
		return settings;
	}

	// Registered before the recording starts, so starting it instruments them with the JDK's, in one pause rather than
	// each on its first event
	private static void register(@Nonnull final List<Source> sources) {
		for (final Source source : sources)
			FlightRecorder.register(source.type);
	}

	private static void enable(@Nonnull final Recording recording, @Nonnull final List<Source> sources) {
		for (final Source source : sources) {
			final EventSettings settings = recording.enable(source.type);
			if (source.write != null)
				settings.withPeriod(Duration.ofSeconds(1));
		}
	}

	// Each stop is safe to repeat: the recording's end, then again in its finally, for a recording that failed
	private static void stop(@Nonnull final List<Source> sources) {
		for (final Source source : sources)
			source.stop.run();
	}
}
