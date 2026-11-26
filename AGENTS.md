# Dynamic Surroundings 1.12.2 — working notes

The 1.12.2 Forge line of Dynamic Surroundings (MIT, `LICENSE.md`; assets credited in `CREDITS.md`). The 26.x line
lives in https://github.com/OreCruncher/DynamicSurroundingsFabric. Branch `OreCruncher/1.12.2-4.0.0` is 4.0.0: the
build moved off ForgeGradle 3, and Mixin comes from MixinBooter.

## Build
- RetroFuturaGradle 2.0.3 on Gradle 9.7 (from CleanroomMC's TemplateDevEnv, MIT; its notice is at the top of
  `build.gradle` and `settings.gradle`). RFG is compiled for Java 25, so **Gradle runs on JDK 25**; the mod compiles
  and runs on a Java 8 toolchain, which Gradle finds or downloads (foojay).
  Git Bash: `JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot" ./gradlew build`
- Jar: `build/libs/dynamicsurroundings-forge-1.12.2-<version>.jar` (the `-dev` jar is not for players).
- `runClient`: dev client (`run/`). `runObfClient`: the release jar from `run/obfuscated/mods`, beside MixinBooter, as a
  player installs it; use it to check anything about the jar or its manifest.
- Tests: `./gradlew test` (JUnit 5, `src/test`), plain unit tests with no game running (`ScanJobTests` checks the
  sphere scanner's jobs visit each block once). Minecraft's classes are on the class path, but nothing is bootstrapped.
- MCP mappings `snapshot` `20171003` (RFG adds `-1.12`): the names the code was written in. Forge is RFG's
  14.23.5.2847; `strip-latest-forge-requirements` lets dev runs load mods asking for a newer one.
- `ModInfo.VERSION` comes from `Tags.VERSION`, generated from `mod_version` in `gradle.properties` (`injectTags`).
- Optional mods are `compileOnly` (deobfuscated with `rfg.deobf`); OreLib, required, is on the runtime class path.
  CurseMaven ids are in `gradle.properties`.
- OreLib (https://github.com/OreCruncher/OreLib, the author's own library; clone in `C:\repos\OreLib`, its own
  `AGENTS.md`): DS 4.0.0 requires 3.6.0.2 (`orelib_version`), which isn't on CurseForge yet, so with `orelib_file`
  empty the build uses the jar built in the checkout beside this one (`../OreLib/build/libs`; build it there first)
  and fails saying so if it's missing. `-Porelib_jar=<path to an OreLib release jar>` picks another jar. Once 3.6.0.2
  is released, set `orelib_file` to its CurseForge file id. `runObfClient` gets a local jar copied into its mods
  folder (`copyOrelibToObfMods`).
- Quitting a client from a script: `CloseMainWindow` doesn't reach the LWJGL 2 window; stop the process you launched.

## Mixin
- Mixin is not bundled. MixinBooter (mod id `mixinbooter`, required, `[11.0,)`) provides it (CleanMix, Mixin 0.8.7)
  and reads `MixinConfigs: mixins.dsurround.json` from the jar's manifest. Bundling Mixin is what made mods clash over
  Mixin versions; there is no coremod any more (the old `TransformLoader` only started Mixin).
- The refmap (`mixins.dsurround.refmap.json`) is written by MixinBooter's annotation processor
  (`modUtils.enableMixins`). In dev runs MixinBooter finds the config without a manifest.
- `run/logs/mixinbooter.log` lists each mixin applied (`APPLY mixins.dsurround.json:...`). 13 apply by the main menu;
  `MixinGuiScreenOptionsSounds` waits for the sound options screen.

## Profiling (/dsprofile)
- `/dsprofile [general|aurora|weather] [10-600 seconds]` (client command, two minutes by default) records the game with
  Java Flight Recorder into `config/dsurround/profiles/dsurround-<scene>-<time>.zip` (the `.jfr` and a short summary,
  which is also copied to the clipboard and logged; the newest 10 are kept). Ported from the 26.x line, whose
  `scripts/jfr-summary.py` reads these recordings: the events keep 26.x's names and fields (`dsurround.Environment`,
  `Mod`, `HandlerTick`, `RenderTimer`, `Frames`). The aurora and weather scenes send `/time`, `/weather` and
  `/gamerule` (and `/ds setstr`) in single player.
- Needs Java 8u262 or newer (OpenJDK's `jdk.jfr`): the dev toolchain has it; the Minecraft launcher's own Java 8u51
  doesn't, and the command then says so. Nothing touches `jdk.jfr` until `JfrSupport.isAvailable()` says it is there.
- `lib/profiling` is the generic part, with nothing from the mod in it (the recorder `FlightRecording`, `Redaction`,
  `ProfileFiles`, `RenderTimer` with `ARB_timer_query` GPU timestamps); `profiling` is the mod's (events, `Profiler`,
  `ProfileSources`, `ProfileSummary`, scenes). Privacy: only `FlightRecording.ALLOWED` and the mod's events are
  recorded; Java 8 can't write a recording again without some events, so one that holds any other (another recording
  running at the same time) is deleted, not kept; then the player's folders are redacted.
- Render timers: `RenderTimers.add(new RenderTimer(name))` beside what it times, `begin()`/`end()` around the drawing
  (the aurora, weather, speech bubbles, each particle collection; rain splashes per tick, CPU only). Handler ticks come
  from `EffectManager`'s loop, frames from Forge's render tick (`FrameTimes`).

## Bench (runClientBench)
- `./gradlew runClientBench -Pscenario=scripts/bench/weather.json` runs a scenario unattended in `run-bench` (its own
  config, options and worlds) and closes the game; `scripts/bench/README.md` has the format. Results in
  `run-bench/bench-results/<scenario>-<time>/` (recordings, screenshots, `manifest.json`), read with the 26.x line's
  `scripts/bench-summary.py` and `jfr-summary.py` from the DynamicSurroundingsFabric repository. Without `-Pscenario`
  it is a plain client there, for `/dsbench mark`.
- `org.orecruncher.dsurround.bench` is development only, started by reflection from `profiling/BenchHook`. RFG's dev
  runs load the `jar` task's output (the -dev jar), so the package is left out of the jar only when no run task is
  asked for, and `checkReleaseJar` (part of `build`) fails a release jar holding it: build releases without a run task.
- Settings are `ModOptions` paths (`aurora.maxBands`), set in memory on the Forge properties behind them and synced by
  `ConfigProcessor`; nothing is saved. Commands run "as the player" go through the real player (the bench world has
  cheats on), so `/ds` works; others through a sender that keeps their messages.
- Turn the display's timeout off for a bench and back on after (it runs minutes with no input).

## Auroras
- Two styles (`aurora.auroraStyle`): classic (`client/aurora/Aurora`, `AuroraRenderer`, `shaders/aurora_classic.*`:
  curtains as quads, 0.012 ms of GPU at 1920x1080) and cinematic (`CinematicAurora*`, `AuroraNight`,
  `shaders/aurora_cinematic.*` and `aurora_composite.*`: a pass over the screen, 2.2 ms fast, 6.6 ms fancy), both ported
  from the 26.x line. Both are drawn after the world on the far plane, tested less-or-equal against the depth buffer,
  so they show only where the sky is.
- `aurora_cinematic.frag` is made from the 26.x line's shader by `scripts/port-cinematic-shader.py` (GLSL 1.20,
  uniforms for its block; the near point at z -1, as 26.x has reverse-Z): change the script, not the shader, and run it
  again when 26.x's shader changes. Bench: `scripts/bench/aurora.json` (classic), `aurora-cinematic.json`.

## Known noise in a dev run's log
- FML's missing signature data and Maven library folder errors, fernflower's "Unable to simplify switch" (the mixin
  debug export). Neither mod checks a certificate fingerprint any more (dropped in DS 4.0.0 and OreLib 3.6.0.2).
