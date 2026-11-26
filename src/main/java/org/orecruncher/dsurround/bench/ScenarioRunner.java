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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.orecruncher.dsurround.ModBase;
import org.orecruncher.dsurround.profiling.ProfileScene;
import org.orecruncher.dsurround.profiling.Profiler;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Runs a {@link Scenario} in a development client, then closes the game: opens (or makes) its world, and for each run
 * applies its settings, goes to its scene (found by biome, or remembered in the world), runs the scene's commands,
 * waits for the terrain to be drawn and a warm-up, takes a screenshot and records a profile (/dsprofile). Everything
 * goes in {@code bench-results/<scenario>-<time>/} in the game's folder, with a {@code manifest.json} saying what each
 * run was, and {@code bench-results/latest.txt} names that folder. Stops at the first failure, writing the manifest
 * with it, and closes the game whatever happens. The 26.x line's runner, ported; its {@code scripts/bench-summary.py}
 * reads the results.
 * <p>
 * Driven by the client tick, one step at a time; nothing here blocks the game. Commands are run as the player would
 * type them: the built-in server's (time, weather, gamerule) on the server with full permission, so the world needn't
 * allow cheats; the mod's client commands (/dsprofile) on the client.
 */
public final class ScenarioRunner {

	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	// How long each wait may take before the run is given up
	private static final int OPEN_SECONDS = 600;
	private static final int FIND_SECONDS = 300;
	private static final int ARRIVE_SECONDS = 60;
	// How far above or below the scene's place the player may be and have arrived, in blocks
	private static final double ARRIVE_DRIFT = 6D;
	private static final int DRAWN_SECONDS = 90;
	// At least this long after arriving before the terrain is taken as drawn: it starts out with nothing to draw
	private static final int SETTLE_SECONDS = 3;
	private static final int TICKS_PER_SECOND = 20;
	// How long a screen other than the title screen stays up before the world is opened over it
	private static final int OTHER_SCREEN_SECONDS = 5;
	// How long the game may take to exit once a run is over, before it is ended
	private static final int SHUTDOWN_GRACE_SECONDS = 60;
	// How far around the player a scene's commands may build or summon
	private static final int SCENE_REACH = 40;
	// The height the player is flown at: over the mountains and trees
	private static final double FLY_HEIGHT = 230D;
	// What a command says when it fails only because there was nothing to change: a scene's building commands, once
	// the scene is built
	static final Set<String> NOTHING_TO_CHANGE = ImmutableSet.of("commands.fill.failed", "commands.setblock.noChange",
			"commands.clone.failed");

	private final Scenario scenario;
	private final Path results;
	private final Deque<Step> steps = new ArrayDeque<>();
	private final JsonObject manifest = new JsonObject();
	private final JsonArray runResults = new JsonArray();
	// Found on the server's thread, read on the client's
	private final Map<String, SceneLocations.Location> sessionScenes = new ConcurrentHashMap<>();
	// Server commands that failed, from the server's thread
	private final Set<String> commandFailures = ConcurrentHashMap.newKeySet();
	private final JsonArray failedCommands = new JsonArray();

	@Nullable
	private Step current;
	private int stepTicks;
	@Nullable
	private GuiScreen waitingScreen;
	private int waitingScreenTicks;
	private boolean finished;
	@Nullable
	private SceneLocations locations;
	// Every setting any run sets, as they were when the session started
	@Nullable
	private Map<String, JsonElement> baseline;
	// How fast the player is flown east in this scene
	private double flySpeed;

	private ScenarioRunner(@Nonnull final Scenario scenario, @Nonnull final Path results) {
		this.scenario = scenario;
		this.results = results;
	}

	/**
	 * One thing to do, ticked until it is done, within {@code timeout} ticks (0 for one go)
	 */
	private static final class Step {
		final String name;
		final BooleanSupplier tick;
		final int timeout;

		Step(@Nonnull final String name, @Nonnull final BooleanSupplier tick, final int timeout) {
			this.name = name;
			this.tick = tick;
			this.timeout = timeout;
		}
	}

	/**
	 * Starts running the scenario on the client's next tick
	 */
	static void start(@Nonnull final Path file) {
		MinecraftForge.EVENT_BUS.register(new Object() {
			private boolean begun;
			@Nullable
			private ScenarioRunner runner;

			@SubscribeEvent
			public void tick(final TickEvent.ClientTickEvent event) {
				if (event.phase != TickEvent.Phase.END)
					return;
				if (!this.begun) {
					this.begun = true;
					this.runner = create(file);
				}
				if (this.runner != null)
					this.runner.tick();
			}
		});
	}

	@Nullable
	private static ScenarioRunner create(@Nonnull final Path file) {
		final Minecraft mc = Minecraft.getMinecraft();
		final Path resultsRoot = mc.mcDataDir.toPath().resolve("bench-results");
		final Scenario scenario;
		try {
			scenario = Scenario.read(file);
		} catch (final Exception e) {
			ModBase.log().error(String.format("Bench: unable to read the scenario %s", file), e);
			writeFailure(resultsRoot, file, e);
			mc.shutdown();
			return null;
		}
		final Path results = resultsRoot.resolve(scenario.name + "-" + LocalDateTime.now().format(STAMP));
		final ScenarioRunner runner = new ScenarioRunner(scenario, results);
		runner.begin(file);
		return runner;
	}

	private void begin(@Nonnull final Path file) {
		try {
			Files.createDirectories(this.results);
			Files.write(this.results.getParent().resolve("latest.txt"),
					this.results.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8));
		} catch (final IOException e) {
			ModBase.log().error(String.format("Bench: unable to make the results folder %s", this.results), e);
		}
		this.manifest.addProperty("scenario", file.toAbsolutePath().toString());
		this.manifest.addProperty("started", LocalDateTime.now().toString());
		this.manifest.addProperty("world", this.scenario.world);
		this.manifest.add("runs", this.runResults);
		this.manifest.add("failedCommands", this.failedCommands);
		this.manifest.addProperty("finished", false);
		ModBase.log().info("Bench: running %s, results in %s", file, this.results);

		step("open the world", this::openWorld, 0);
		step("wait for the world", () -> {
			final Minecraft mc = Minecraft.getMinecraft();
			return mc.player != null && mc.world != null && mc.getIntegratedServer() != null
					&& mc.currentScreen == null;
		}, OPEN_SECONDS * TICKS_PER_SECOND);
		step("set up the session", this::setUpSession, 0);
	}

	private void step(@Nonnull final String name, @Nonnull final BooleanSupplier tick, final int timeout) {
		this.steps.add(new Step(name, tick, timeout));
	}

	private void tick() {
		if (this.finished)
			return;
		try {
			final EntityPlayerSP flying = Minecraft.getMinecraft().player;
			if (this.flySpeed > 0D && flying != null) {
				flying.capabilities.isFlying = true;
				// At a height held, or it sinks into the mountains
				flying.motionX = flying.motionY = flying.motionZ = 0;
				flying.setPosition(flying.posX + this.flySpeed, FLY_HEIGHT, flying.posZ);
			}
			if (this.current == null) {
				this.current = this.steps.poll();
				this.stepTicks = 0;
				if (this.current == null) {
					finish(null);
					return;
				}
				ModBase.log().info("Bench: %s", this.current.name);
			}
			this.stepTicks++;
			if (this.current.tick.getAsBoolean()) {
				this.current = null;
			} else if (this.current.timeout > 0 && this.stepTicks > this.current.timeout) {
				throw new IllegalStateException("timed out: " + this.current.name);
			}
		} catch (final Exception e) {
			ModBase.log().error(String.format("Bench: failed at %s", this.current == null ? "?" : this.current.name), e);
			finish(e);
		}
	}

	/**
	 * Opens the scenario's world from the title screen, making it first if it isn't there; false until the title screen
	 * shows, or another screen has stayed up {@link #OTHER_SCREEN_SECONDS}
	 */
	private boolean openWorld() {
		final Minecraft mc = Minecraft.getMinecraft();
		// Pausing when the window loses focus would hold a run up
		mc.gameSettings.pauseOnLostFocus = false;
		final GuiScreen screen = mc.currentScreen;
		if (mc.world != null || screen == null)
			return false;
		if (!(screen instanceof GuiMainMenu)) {
			if (screen != this.waitingScreen) {
				this.waitingScreen = screen;
				this.waitingScreenTicks = 0;
			}
			if (++this.waitingScreenTicks < OTHER_SCREEN_SECONDS * TICKS_PER_SECOND)
				return false;
			ModBase.log().info("Bench: opening the world over %s", screen.getClass().getName());
			this.manifest.addProperty("openedOver", screen.getClass().getName());
		}
		mc.gameSettings.saveOptions();
		final String name = this.scenario.world;
		if (mc.getSaveLoader().canLoadWorld(name)) {
			mc.launchIntegratedServer(name, name, null);
		} else {
			ModBase.log().info("Bench: making the world %s", name);
			final long seed = this.scenario.seed != null ? this.scenario.seed : new java.util.Random().nextLong();
			final WorldSettings settings = new WorldSettings(seed, GameType.CREATIVE, true, false, WorldType.DEFAULT)
					.enableCommands();
			mc.launchIntegratedServer(name, name, settings);
		}
		return true;
	}

	private boolean setUpSession() {
		final Minecraft mc = Minecraft.getMinecraft();
		final WorldServer overworld = server().getWorld(0);
		this.manifest.addProperty("seed", overworld.getSeed());
		try {
			this.locations = SceneLocations.load(worldFolder());
		} catch (final IOException e) {
			throw new IllegalStateException("unable to read the world's scenes", e);
		}
		final JsonArray modChanges = new JsonArray();
		this.locations.modChanges().forEach(modChanges::add);
		this.manifest.add("modChangesSinceScenes", modChanges);

		if (this.scenario.vsync != null) {
			mc.gameSettings.enableVsync = this.scenario.vsync;
			org.lwjgl.opengl.Display.setVSyncEnabled(this.scenario.vsync);
		}
		if (this.scenario.maxFps != null)
			mc.gameSettings.limitFramerate = this.scenario.maxFps;
		if (this.scenario.width != null && this.scenario.height != null)
			setWindowSize(this.scenario.width, this.scenario.height);
		if (this.scenario.volume != null)
			mc.gameSettings.setSoundLevel(SoundCategory.MASTER, this.scenario.volume);
		if (this.scenario.musicVolume != null)
			mc.gameSettings.setSoundLevel(SoundCategory.MUSIC, this.scenario.musicVolume);
		// Its hints would sit in every screenshot
		mc.gameSettings.tutorialStep = TutorialSteps.NONE;
		mc.gameSettings.saveOptions();

		for (final String command : this.scenario.setup)
			command(command);

		// Every run's settings, checked before anything is recorded, and as they all are now, to put back before each
		final Map<String, JsonElement> all = new LinkedHashMap<>();
		for (final Scenario.Run run : this.scenario.runs)
			all.putAll(run.settings);
		final String problems = BenchSettings.check(all);
		if (problems != null)
			throw new IllegalArgumentException("settings that can't be applied: " + problems);
		this.baseline = BenchSettings.current(all.keySet());

		final List<Scenario.Run> order = this.scenario.order();
		for (int i = 0; i < order.size(); i++)
			queueRun(i, order.get(i), order.size());
		return true;
	}

	private void queueRun(final int index, @Nonnull final Scenario.Run run, final int total) {
		final Scenario.Scene scene = this.scenario.scenes.get(run.scene);
		final JsonObject result = new JsonObject();
		final String tag = String.format(Locale.ROOT, "%02d-%s", index + 1, run.label);
		result.addProperty("index", index + 1);
		result.addProperty("label", run.label);
		result.addProperty("scene", run.scene);
		final JsonObject settings = new JsonObject();
		run.settings.forEach(settings::add);
		result.add("settings", settings);
		final Arrival arrival = new Arrival();

		step(tag + ": settings", () -> {
			BenchSettings.apply(this.baseline);
			BenchSettings.apply(run.settings);
			result.addProperty("started", LocalDateTime.now().toString());
			// In the manifest from the start, so a run that fails says how far it got
			this.runResults.add(result);
			return true;
		}, 0);
		step(tag + ": find the scene", () -> {
			if (arrival.found == null)
				arrival.found = sceneLocation(run.scene, scene, result);
			if (!arrival.found.isDone())
				return false;
			arrival.location = getDone(arrival.found);
			result.add("location", locationJson(arrival.location));
			return true;
		}, FIND_SECONDS * TICKS_PER_SECOND);
		step(tag + ": go there", () -> {
			// The last scene is over: flying on would keep the player from arriving at this one
			this.flySpeed = 0D;
			final SceneLocations.Location l = arrival.location;
			final float yaw = scene.yaw != null ? scene.yaw : l.yaw;
			final float pitch = scene.pitch != null ? scene.pitch : l.pitch;
			serverCommand(String.format(Locale.ROOT, "tp @a %.3f %.3f %.3f %.2f %.2f", l.x, l.y, l.z, yaw, pitch),
					false);
			return true;
		}, 0);
		step(tag + ": arrive", () -> {
			final EntityPlayerSP player = Minecraft.getMinecraft().player;
			final SceneLocations.Location l = arrival.location;
			// Across, within two blocks; up and down, within a few, as a player put over open water sinks into it
			if (player == null || sq(player.posX - l.x) + sq(player.posZ - l.z) > 4
					|| Math.abs(player.posY - l.y) > ARRIVE_DRIFT)
				return false;
			// The scene's commands need the ground around loaded on the server: a fill or summon where it isn't fails
			if (arrival.loaded == null || arrival.loaded.isDone() && !getDone(arrival.loaded))
				arrival.loaded = server().callFromMainThread(ScenarioRunner::sceneLoaded);
			if (!arrival.loaded.isDone() || !getDone(arrival.loaded))
				return false;
			for (final String command : scene.commands)
				command(command);
			return true;
		}, ARRIVE_SECONDS * TICKS_PER_SECOND);
		step(tag + ": wait for the terrain", () -> {
			if (this.stepTicks < SETTLE_SECONDS * TICKS_PER_SECOND)
				return false;
			if (Minecraft.getMinecraft().renderGlobal.hasNoChunkUpdates())
				return true;
			if (this.stepTicks >= DRAWN_SECONDS * TICKS_PER_SECOND) {
				// Recorded anyway, and said so: some scenes keep sections changing (flowing water)
				result.addProperty("terrainNotSettled", true);
				return true;
			}
			return false;
		}, 0);
		step(tag + ": warm up", () -> {
			if (this.stepTicks == 1) {
				for (final String command : scene.settledCommands)
					command(command);
				this.flySpeed = scene.flySpeed;
				// Which run this is, for anyone watching
				final ITextComponent said = new TextComponentString(String.format(Locale.ROOT, "Bench %d/%d: %s",
						index + 1, total,
						run.caption.equals(run.label) ? run.label : run.label + " (" + run.caption + ")"));
				Minecraft.getMinecraft().player.sendMessage(said);
				Minecraft.getMinecraft().ingameGUI.setOverlayMessage(said, false);
			}
			return this.stepTicks >= this.scenario.warmup * TICKS_PER_SECOND;
		}, 0);
		step(tag + ": record", () -> {
			final Minecraft mc = Minecraft.getMinecraft();
			if (this.stepTicks == 1) {
				final List<ITextComponent> said = Profiler.start(profileScene(scene.profile), this.scenario.record,
						zip -> {
							arrival.recorded = true;
							keepRecording(zip, tag, result);
						});
				if (!Profiler.isBusy()) {
					final StringBuilder text = new StringBuilder();
					for (final ITextComponent line : said)
						text.append(text.length() > 0 ? "; " : "").append(line.getUnformattedText());
					throw new IllegalStateException("the profile didn't start: " + text);
				}
				return false;
			}
			// The screenshot as recording starts, without the HUD: hidden for a couple of ticks, so a frame is drawn
			// without it, then taken and the HUD shown again. The scene's shot commands run as the HUD is hidden, and
			// the shot waits their delay
			if (this.scenario.screenshot && !arrival.shot && Profiler.isRecording()) {
				if (arrival.shotTick == 0) {
					arrival.shotTick = this.stepTicks;
					mc.gameSettings.hideGUI = true;
					for (final String command : scene.shotCommands)
						command(command);
				} else if (this.stepTicks >= arrival.shotTick + Math.max(2, scene.shotDelay)) {
					screenshot(tag, result);
					mc.gameSettings.hideGUI = false;
					arrival.shot = true;
				}
			}
			if (!arrival.recorded)
				return false;
			result.addProperty("finished", LocalDateTime.now().toString());
			writeManifest();
			return true;
		}, (this.scenario.record + 120) * TICKS_PER_SECOND);
	}

	// How a run's arrival is going
	private static final class Arrival {
		@Nullable
		ListenableFuture<SceneLocations.Location> found;
		@Nullable
		SceneLocations.Location location;
		@Nullable
		ListenableFuture<Boolean> loaded;
		int shotTick;
		boolean shot;
		volatile boolean recorded;
	}

	private static double sq(final double v) {
		return v * v;
	}

	private static <T> T getDone(@Nonnull final ListenableFuture<T> future) {
		try {
			return future.get();
		} catch (final Exception e) {
			final Throwable cause = e.getCause() != null ? e.getCause() : e;
			throw new IllegalStateException(cause.getMessage(), cause);
		}
	}

	/**
	 * Where a scene is: remembered this session, or in the world (checked that it is still in its biome, unless
	 * marked), or found by its biome and remembered; on the server's thread
	 */
	@Nonnull
	private ListenableFuture<SceneLocations.Location> sceneLocation(@Nonnull final String name,
			@Nonnull final Scenario.Scene scene, @Nonnull final JsonObject result) {
		final MinecraftServer server = server();
		final SceneLocations.Location known = this.sessionScenes.get(name);
		final SceneLocations.Location remembered = this.locations.get(name);
		return server.callFromMainThread(() -> {
			final WorldServer world = server.getWorld(0);
			SceneLocations.Location location = known;
			if (location != null)
				result.addProperty("how", "this session");
			else {
				if (remembered != null && (remembered.marked || SceneFinder.stillThere(world, remembered))) {
					result.addProperty("how", remembered.marked ? "marked" : "remembered");
					location = remembered;
				} else {
					if (scene.biome == null)
						throw new IllegalStateException(
								"scene '" + name + "' has no biome to find it by: mark it with /dsbench mark " + name);
					result.addProperty("how", remembered == null ? "found" : "found again (its biome had moved)");
					location = SceneFinder.find(world, scene.biome, 0, 0);
					try {
						this.locations.put(name, location, world.getSeed());
					} catch (final IOException e) {
						ModBase.log().warn("Bench: unable to remember scene %s: %s", name, e);
					}
				}
				this.sessionScenes.put(name, location);
			}
			// Arriving to one side of the place, if the scene says so: its ground loaded before its commands run
			if (scene.offsetX != 0 || scene.offsetZ != 0)
				location = SceneFinder.offset(world, location, scene.offsetX, scene.offsetZ);
			return location;
		});
	}

	private void keepRecording(@Nullable final Path zip, @Nonnull final String tag, @Nonnull final JsonObject result) {
		if (zip == null) {
			result.addProperty("error", "the profile couldn't be made (see the log)");
			return;
		}
		try {
			final Path kept = this.results.resolve(tag + "-" + zip.getFileName());
			Files.move(zip, kept, StandardCopyOption.REPLACE_EXISTING);
			result.addProperty("recording", kept.getFileName().toString());
		} catch (final IOException e) {
			result.addProperty("recording", zip.toAbsolutePath().toString());
		}
	}

	private void screenshot(@Nonnull final String tag, @Nonnull final JsonObject result) {
		final Minecraft mc = Minecraft.getMinecraft();
		final String name = tag + ".png";
		// Written to <results>/screenshots
		ScreenShotHelper.saveScreenshot(this.results.toFile(), name, mc.displayWidth, mc.displayHeight,
				mc.getFramebuffer());
		result.addProperty("screenshot", "screenshots/" + name);
	}

	@Nonnull
	private static ProfileScene profileScene(@Nonnull final String name) {
		for (final ProfileScene scene : ProfileScene.values())
			if (scene.commandName().equals(name))
				return scene;
		throw new IllegalArgumentException("no /dsprofile scene " + name);
	}

	/**
	 * Runs a command as the player would type it: the server's on the server, with full permission, as the player and
	 * where they are; anything else (the mod's client commands) on the client
	 */
	private void command(@Nonnull final String command) {
		final String text = command.startsWith("/") ? command.substring(1) : command;
		final String root = text.split(" ", 2)[0];
		if (server().getCommandManager().getCommands().containsKey(root))
			serverCommand(text, true);
		else if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().player, "/" + text) == 0)
			throw new IllegalArgumentException("no such command: " + text);
	}

	/**
	 * Whether the ground within {@link #SCENE_REACH} of the player is loaded on the server, so a scene's commands can
	 * build and summon there; on the server's thread
	 */
	private static boolean sceneLoaded() {
		final List<EntityPlayerMP> players = server().getPlayerList().getPlayers();
		if (players.isEmpty())
			return false;
		final EntityPlayerMP player = players.get(0);
		final World world = player.world;
		final BlockPos center = player.getPosition();
		for (int dx = -SCENE_REACH; dx <= SCENE_REACH; dx += SCENE_REACH)
			for (int dz = -SCENE_REACH; dz <= SCENE_REACH; dz += SCENE_REACH)
				if (!world.isBlockLoaded(center.add(dx, 0, dz)))
					return false;
		return true;
	}

	/**
	 * Runs a command on the server with full permission, as the player and where they are if {@code asPlayer}. One
	 * that fails is logged and listed in the manifest, unless all it said was that it had nothing to change (a fill
	 * that builds a scene, on the scene's next run)
	 */
	private void serverCommand(@Nonnull final String command, final boolean asPlayer) {
		final MinecraftServer server = server();
		server.addScheduledTask(() -> {
			final List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
			if (asPlayer && !players.isEmpty()) {
				// As the player itself, as typing it would: some commands insist on a player (/ds). The bench world is
				// made with cheats on, so the player may use them; what a command says goes to its chat
				final int succeeded = server.getCommandManager().executeCommand(players.get(0), command);
				if (succeeded == 0 && this.commandFailures.add(command))
					ModBase.log().warn("Bench: command failed: %s", command);
				return;
			}
			final BenchCommandSender sender = new BenchCommandSender(server, null);
			final int succeeded = server.getCommandManager().executeCommand(sender, command);
			if (succeeded == 0 && !sender.nothingToChange() && this.commandFailures.add(command))
				ModBase.log().warn("Bench: command failed: %s (%s)", command, sender.said());
		});
	}

	/**
	 * Runs commands with full permission, as an entity and where it is (or the server), keeping what they say: they
	 * would otherwise fill the chat, and a failure's message says whether there was only nothing to change
	 */
	private static final class BenchCommandSender implements ICommandSender {
		private final MinecraftServer server;
		@Nullable
		private final Entity entity;
		private final List<ITextComponent> said = new ArrayList<>();

		BenchCommandSender(@Nonnull final MinecraftServer server, @Nullable final Entity entity) {
			this.server = server;
			this.entity = entity;
		}

		@Override
		@Nonnull
		public String getName() {
			return this.entity != null ? this.entity.getName() : "Bench";
		}

		@Override
		public void sendMessage(@Nonnull final ITextComponent component) {
			this.said.add(component);
		}

		@Override
		public boolean canUseCommand(final int permLevel, @Nonnull final String commandName) {
			return true;
		}

		@Override
		@Nonnull
		public BlockPos getPosition() {
			return this.entity != null ? this.entity.getPosition() : BlockPos.ORIGIN;
		}

		@Override
		@Nonnull
		public Vec3d getPositionVector() {
			return this.entity != null ? this.entity.getPositionVector() : Vec3d.ZERO;
		}

		@Override
		@Nonnull
		public World getEntityWorld() {
			return this.entity != null ? this.entity.world : this.server.getWorld(0);
		}

		@Override
		@Nullable
		public Entity getCommandSenderEntity() {
			return this.entity;
		}

		@Override
		public boolean sendCommandFeedback() {
			return false;
		}

		@Override
		@Nullable
		public MinecraftServer getServer() {
			return this.server;
		}

		// Whether every message it got was only that there was nothing to change
		boolean nothingToChange() {
			if (this.said.isEmpty())
				return false;
			for (final ITextComponent message : this.said)
				if (!(message instanceof TextComponentTranslation)
						|| !NOTHING_TO_CHANGE.contains(((TextComponentTranslation) message).getKey()))
					return false;
			return true;
		}

		@Nonnull
		String said() {
			final StringBuilder text = new StringBuilder();
			for (final ITextComponent message : this.said)
				text.append(text.length() > 0 ? "; " : "").append(message.getUnformattedText());
			return text.toString();
		}
	}

	/**
	 * Sets the window's size, out of fullscreen, and the game's framebuffers with it; the manifest says what it is
	 */
	private void setWindowSize(final int width, final int height) {
		final Minecraft mc = Minecraft.getMinecraft();
		try {
			if (mc.isFullScreen())
				mc.toggleFullscreen();
			org.lwjgl.opengl.Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(width, height));
			mc.resize(width, height);
		} catch (final org.lwjgl.LWJGLException e) {
			throw new IllegalStateException("unable to set the window to " + width + "x" + height, e);
		}
		this.manifest.addProperty("window", mc.displayWidth + "x" + mc.displayHeight);
	}

	@Nonnull
	private static MinecraftServer server() {
		final MinecraftServer server = Minecraft.getMinecraft().getIntegratedServer();
		if (server == null)
			throw new IllegalStateException("no single player world open");
		return server;
	}

	@Nonnull
	private static Path worldFolder() {
		return server().getWorld(0).getSaveHandler().getWorldDirectory().toPath();
	}

	@Nonnull
	private static JsonObject locationJson(@Nonnull final SceneLocations.Location l) {
		final JsonObject json = new JsonObject();
		json.addProperty("biome", l.biome);
		final JsonArray pos = new JsonArray();
		pos.add(l.x);
		pos.add(l.y);
		pos.add(l.z);
		json.add("pos", pos);
		json.addProperty("yaw", l.yaw);
		json.addProperty("pitch", l.pitch);
		json.addProperty("biomeShare", l.biomeShare);
		return json;
	}

	/**
	 * Puts the settings back, writes the manifest and closes the game
	 */
	private void finish(@Nullable final Exception failure) {
		if (this.finished)
			return;
		this.finished = true;
		this.flySpeed = 0D;
		Minecraft.getMinecraft().gameSettings.hideGUI = false;
		if (this.baseline != null) {
			try {
				BenchSettings.apply(this.baseline);
			} catch (final Exception e) {
				ModBase.log().warn("Bench: unable to put the settings back: %s", e);
			}
		}
		if (failure != null)
			this.manifest.addProperty("error", (this.current == null ? "" : this.current.name + ": ") + failure);
		this.manifest.addProperty("finished", failure == null);
		this.manifest.addProperty("ended", LocalDateTime.now().toString());
		writeManifest();
		ModBase.log().info("Bench: %s; results in %s", failure == null ? "done" : "failed", this.results);
		Minecraft.getMinecraft().shutdown();
		// The game closes its window, but other mods' threads can hold the JVM open after, and an unattended run would
		// never end: everything is written by now
		final Thread watchdog = new Thread(() -> {
			try {
				Thread.sleep(SHUTDOWN_GRACE_SECONDS * 1000L);
			} catch (final InterruptedException e) {
				return;
			}
			ModBase.log().warn("Bench: the game hasn't exited %d seconds after closing; ending it",
					SHUTDOWN_GRACE_SECONDS);
			// FML's way out (it reroutes Runtime.halt to this anyway)
			net.minecraftforge.fml.common.FMLCommonHandler.instance().exitJava(failure == null ? 0 : 1, true);
		}, "dsurround-bench-exit");
		watchdog.setDaemon(true);
		watchdog.start();
	}

	private void writeManifest() {
		while (this.failedCommands.size() > 0)
			this.failedCommands.remove(0);
		this.commandFailures.stream().sorted().forEach(this.failedCommands::add);
		try {
			Files.write(this.results.resolve("manifest.json"), new GsonBuilder().setPrettyPrinting().create()
					.toJson(this.manifest).getBytes(StandardCharsets.UTF_8));
		} catch (final IOException e) {
			ModBase.log().error("Bench: unable to write the manifest", e);
		}
	}

	/**
	 * A manifest saying the scenario couldn't even be read, so whoever waits for one isn't left waiting
	 */
	private static void writeFailure(@Nonnull final Path resultsRoot, @Nonnull final Path file,
			@Nonnull final Exception e) {
		try {
			final Path folder = resultsRoot.resolve("failed-" + LocalDateTime.now().format(STAMP));
			Files.createDirectories(folder);
			final JsonObject json = new JsonObject();
			json.addProperty("scenario", file.toAbsolutePath().toString());
			json.addProperty("finished", false);
			json.addProperty("error", "unable to read the scenario: " + e);
			Files.write(folder.resolve("manifest.json"),
					new GsonBuilder().setPrettyPrinting().create().toJson(json).getBytes(StandardCharsets.UTF_8));
			Files.write(resultsRoot.resolve("latest.txt"),
					folder.toAbsolutePath().toString().getBytes(StandardCharsets.UTF_8));
		} catch (final IOException ignored) {
			// Nothing more can be done; the log has it
		}
	}
}
