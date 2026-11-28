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

## Particle weather
- `rain.particleWeather` (off by default): rain and snow as single drops and flakes, ported from the 26.x line
  (`client/weather/particle`: `PrecipitationField`, `PrecipitationColumns`, `PackedDrops`, `Snowflakes`, `Wind`, with
  their tests). Run from `RenderWeather`: ticked by the first frame of each client tick, drawn every frame; the dust is
  still the sheets of `StormRenderer` (`dustOnly`). Off under an OptiFine shader pack. The drops are blown by the scene's wind (below).
- Off by default because it came over as an experiment (the 26.x line has it on): the sheets are what 1.12.2 players
  know, and it was the first port on the list. The window frost and streaks don't need it and are on.
- Landings (`Landings`, from 26.3's): where a snowflake lands it leaves a fleck (`SnowFlecks`, tested) that lies flat
  as the flake it fell as, then shrinks and fades (30 to 70 ticks; 1024 at most), not on water, snow or magma. One the
  field says was blown in below its column's top is followed on (`RayWalk`, 8 blocks, 32 a tick with the rain's) to
  what it hits: the side of a wall, leaves or a trunk, where it sticks (`rain.snowOnSides`), or the ground under
  cover. A rain drop blown into a window starts a streak (`WindowRain.blownIntoWindow`). 26.3's splashes, smoke and
  damp spots aren't here (the splashes are still `StormSplashRenderer`'s). Drawn by `DropRenderer` on the CPU each
  frame, in GPU mode too, after the drops with the snow texture. Bench: `scripts/bench/snow-flecks.json` (Ice Plains, a
  stone floor and wall, wind east: the weather pass 0.18 ms of CPU with sides, 0.13 without). Bench scene names are
  remembered per world: a new scenario needs its own, or it runs where the old one was.

## Wind
- `client/weather/wind`, ported from the 26.x line: `WindService` ticks the one `Wind` every client tick, particle
  weather or not (the storm's strength, harder in thunder and dust, its direction from the day; still air where the
  dimension has no weather), and publishes it through `SceneWind`; its line is on the diagnostics overlay.
- Particles drift with it (`effects.windBlowsParticles`) as far as they are out in the weather (`Shelter.exposure`:
  sky light, and a roof under the precipitation height; leaves, fences, walls, panes and ladders aren't roofs).
  `MixinParticle` adds the drift to a marked particle's move; `MixinParticleManager` marks vanilla smoke, large smoke,
  clouds and poofs; `IWindBlown.blow` marks ours (steam, frost breath, fire-jet flames). `ParticleDust` overrides
  `move`, so it never drifts (cave dust, mostly sheltered anyway).
- `/dsweather wind [speed 0-300 | direction <degrees|N..NW> | clear]` (`CommandDSWeather`, a client command) holds the
  wind for testing, speed as a percent of a full storm's, as 26.3's does; `/dsweather clear` or disconnecting lets it
  go. Bench scenarios can run it in a scene's `commands`.
- Wind sounds, from the 26.x line (`WindSounds`): a biome sound with `"followsWind": true` (the wind loops in
  `mcp.json`) is scaled by the wind where the player is, gusts included (0.4 in a calm, 1 at a full storm's wind, up to
  1.3 beyond; half under cover, by `Shelter.exposure`), and not played where the air is still. `weather.isStormWind`
  (on from 70% of a full storm's steady wind, off below 55%), `weather.isWindy` (50%) and `weather.windSpeed` (the
  percent) are condition variables; the storm wind rule gives biomes with no wind of their own (not mountains, hills,
  wastelands, deserts, snowy open land, the Nether or the End) the wind loop while it blows.
- Drawn by `GpuDrops` (`rain.particleWeatherOnGpu`, on): the drops uploaded to a vertex buffer once a tick, four vertices
  a drop, and `shaders/drops.vert` making each frame's quads (OpenGL 2.1: the 26.x line's texel buffers need 3.1), or
  without shaders by `DropRenderer` on the CPU, whose math the shader follows: change both. Vanilla turns particles
  with `ActiveRenderInfo`'s left (X, 0, Z) and up (YZ, XZ, XY); the other order shears the flakes.
- Bench: `scripts/bench/particle-weather.json` (sheets, CPU, GPU). At 1920x1080 the GPU drops cost 0.05 ms of CPU a
  frame, the sheets 0.13, the CPU-built drops 0.32.

## Window frost
- `client/weather/frost`, ported from the 26.x line (`rain.windowFrost`, on; needs shaders, off under an OptiFine
  shader pack): `FrostFaces` finds the outsides of glass, `FrostField` keeps them a section at a time near the player,
  `FrostConditions` says how much (cold, time of day, sun on the glass), `FrostTexture` makes the pattern at run time,
  `FrostRenderer` and `shaders/frost.*` draw it. `WindowFrost` runs it: ticked on the client tick, drawn from
  `RenderWeather` before the rain, or at `RenderWorldLastEvent` where another weather renderer is in place. No depth
  copy as in 26.3: drawn into the main framebuffer after the translucent blocks, an ordinary depth test does.
- Windows are found by class (`client/weather/Windows`: glass, stained glass and their panes, not glowstone or iron
  bars), 1.12.2 having no tags. Pane shapes are worked out from their connections (`FrostFaces.shapeOf`): a pane that
  reaches nothing is only a post in 1.12.2, too thin to frost. A one-layer snow has a collision box of no height, taken
  as open.
- `/dsweather frost 0-100 | clear` holds it for testing. Bench: `scripts/bench/window-frost.json` (a glass and pane wall
  in Ice Plains at noon: as it comes, held at 100% and at 40%).

## Window streaks
- `client/weather/streaks`, ported from the 26.x line (`rain.windowStreaks`, on): `RainOnWindows` (from 26.3's
  `Landings.seedWindows`) starts drops on the windward side of windows near the camera, `WindowStreaks` runs them down
  the glass, dripping off the bottom edge as vanilla's dripping water. It needs no falling drops, so it runs with the
  sheets as well as particle weather; with particle weather, drops blown into a window start streaks too (above). `WindowRain` ticks and draws
  them, untextured and without a shader, after the frost and before the rain.
- `RayWalk` follows a line through collision boxes and fluid surfaces over an `IBlockAccess` (1.12.2's ray trace is on
  `World` only, and tests use a fake). Bench: `scripts/bench/window-streaks.json` (a glass wall
  in a thunderstorm, `/dsweather wind speed 200` and `direction E`, with the sheets and with particle weather).

## Block tags
- `registry/tags`: the mod's own block tags, as the 26.x line has (1.12.2 has none of the game's), laid out as
  26.x's: `dsurround:effects/floor_squeaks` (`BlockTags`) is every `assets/<namespace>/dsconfigs/tags/block/effects/
  floor_squeaks.json` (`{"values": [...]}`) in mod jars and resource packs, read through the resource manager; ours
  are in `src/main/resources/assets/<namespace>/dsconfigs`. A namespace that isn't an installed mod's counts only for
  a player's resource pack. 1.12.2 can't list a folder, so the files read are those of the tags in `BlockTags.ALL` and
  of the tags they name. Entries are block names as written elsewhere in the config (`minecraft:planks`, with states
  `[variant=oak]`), `ore:<name>` (the blocks of the ore dictionary's items, which picks up mods' blocks that have an
  item) or `#<tag>`; a name without a namespace is `dsurround:`. A `tags` section in a config file
  (`"effects/floor_squeaks": [...]`) adds to them, for players' external config files. `TagResolver` does the paths,
  files and resolving with no game in it (tested); `TagRegistry` (`RegistryManager.TAGS.is(BlockTags.X, state)`)
  holds the block states; the log gives each tag's count, and the inspection overlay a block's tags.
- Floor squeaks (`sound.enableFloorSqueaks`, `FloorSqueakAccent`, from the 26.x line): one step in ten on the
  `floor_squeaks` tag (planks, wooden slabs and stairs) adds `floorsqueak`.
- Wet steps (`sound.enablePuddleSound`, `WaterySurfaceAccent`, from the 26.x line, in place of the old rain splash):
  a lily pad (`watery_step`) splashes, shallow water over the feet a thinned splash, and ground in the rain by how
  soaked it is (`client/weather/GroundWetness`: rain strength times `Shelter.exposure`, where the season says rain):
  `tracks_mud_when_wet` ground (grass, dirt) squelches (`_WETMUD`), hard ground a light slap (`_WETSLAP`: 1.12.2 has no
  filters, so `footsteps/wet_slap` is the splash's lighter takes with 26.x's high-pass baked in, shorter, level-matched), soft ground nothing. In water deeper than about 0.4 the generator plays the swim
  sound and no accents, so wading is mostly that. Accents get their own volume and pitch through `ScaledAcoustic`.
- Accent volumes keep 26.x's share of a footstep: 26.x plays them beside vanilla's step (0.15), ours beside DS's
  (about 0.95), so 26.x's factory volume x 6.33 (0.3 is `190`, 0.25 is `158`). The game plays anything over 1 at 1, as
  loud as the step and not twice as loud, and heard further (16 blocks x the volume).

## Footsteps
- `client/footsteps`: a `Generator` (`GeneratorQP` on four legs) per living entity within `effects.entityEffectRange`
  (32), run from `FxHandler`'s `LivingUpdateEvent`, so inside the entity's own update: `EntityFootprintEffect` catches
  what it throws (a mod's block under the feet that can't answer), logs it once per entity type and turns that
  entity's footsteps off, where it crashed the game ("Ticking entity").
- A delayed acoustic part (`delayed`, 30 or 50 ms in `mcp.json`) goes to `SoundEffectHandler`'s queue, counted in
  ticks (`SoundPlayer.delayTicks`: rounded up, at least the next tick): it was timed in milliseconds and dropped if
  more than 1.2 times its delay late, which any late tick did, and stalled while the entity rode or slept.
- `FacadeHelper` scans every block for facade blocks once, as the footsteps registry completes (it ran at the first
  footstep of a session, a stall then).

## Rain on surfaces
- `rain.enableRainSurfaces` (on), ported from the 26.x line: each rain sound (`StormSplashRenderer.playSplashSound`, on
  the block under the splash it picks) sounds like what it lands on. `client/weather/RainSurface` sorts the block: hot
  or water, then the `rain_on_metal`, `rain_on_glass`, `rain_on_leaves`, `rain_on_wood` and `rain_absorbing` tags, then
  the sound type (a mod's own by its step sound; leaves step as plants in 1.12.2, so they are by tag), else hard. Each
  surface has 26.x's volume and pitch for the rain without filters (1.12.2 has none); leaves, metal, glass and water
  have rain of their own (`rain.leaves` etc., 26.x's recordings), which takes 70% of the rain's place there (`RainMix`),
  played as recorded; the layer takes its share of all three rains (below).
- The rain is three, as the 26.x line's (`RainMix`, from its `RainMix` and `StormSounds`): the calm rain (`rain`), 0.5
  to 1 of vanilla's volume as the storm (`Weather.getIntensityLevel()`) strengthens, the medium rain (`rain.medium`)
  through middling storms (in from 0.2, full 0.5 to 0.7, gone at 1), and vanilla's own rain (`weather.rain`, or
  `weather.rain.above` on a roof above) from 0.5, full at 0.9; each at vanilla's volume for where it plays (0.2 in the
  open, 0.1 on a roof above, at half the pitch). Vanilla's recordings are some 19 dB louder than the calm and medium
  rain, so a drizzle patters, a steady rain hisses and a downpour roars. It was one sound at the storm's volume (0.05
  to 1, swaying with noise), which `rain.useVanillaRainSound` and dust still are.
- Where a rain sound plays (`RainSoundSpots`, `StormSplashRenderer.findSoundSpot`): not on one of the tick's splashes,
  as vanilla (8% within 2 blocks, median 6) but in a column of its own, any way round and mostly near: `12 * roll^2`
  blocks out (41% within 2, median 3). Standing on a few blocks of glass, more sounds land on it, so its own rain
  doesn't come and go (5x5: 14% to 49% of the sounds, 17 dB of swing to 3). Not in the 26.x line yet: to evaluate there
  (its glass would pulse the same way, as vanilla's rain tick picks the spot).
- The inspection overlay gives a block's surface and why, the diagnostics overlay what the last 64 rain sounds fell on
  (`RainSurfaceTally`). Wet steps use `RainSurface` too. Bench: `scripts/bench/rain-surfaces.json` (each material
  underfoot and as a roof, on and off), to listen to.

## Sounds from the 26.x line
- `scripts/sound-port-map.py [26.x repository] -o report.md` pairs 26.x's sound events with ours (prefixes such as
  `biome.` and `item.` taken off, then a table of renames) and sorts them: identical, moved, re-encoded, re-made, new,
  keep; with ffprobe on the path, each file's length and channels. Footsteps are apart: ours are 1.12.2's own acoustics.
- Brought over so far: the re-encoded and re-made non-footstep sounds, under our event names. Each kept our volume,
  matched for loudness (26.x keeps many recordings quiet and raises them in its own config, which our sounds.json can't:
  where a volume would pass 1, the missing gain is baked into the file), and 26.x's credits. `waterfall.0`
  (non-commercial) is gone as in 26.x, its falls playing `waterfall.1`; `waterfall.5` is 26.x's rebuilt one.
- Footsteps are mono, as 26.x's (they were stereo, which Minecraft plays at the ears, neither panned nor fading with
  distance, other entities' steps included): each file's channels averaged and brought back to its old loudness (the
  mean of its channels' power; up to 3.1 dB for metalbox, whose channels were two takes). `water_stereofix` was `water`
  with its channels swapped, the same sound in mono, so it is gone and `waterfine` plays `water` alone.
- 26.x's biome sounds: `ocean`, `forest.sparse`, `forest.pine`, `forest.silent`, `jungle.sparse`, `wind.arctic`,
  `underground.deep`, `eagle`, `turtledove`, `whale.right`, `whale.minke`, `seagulls.flock`, at 26.x's volumes scaled
  as their siblings' were (whales x0.75, gulls x0.87), with 26.x's rules in `mcp.json`'s biomes in Forge's types:
  forests temperate by `biome.temperature` (0.2 to 1, as 26.x; added to the biome conditions), so vanilla's taigas
  (COLD at 0.25) have the forest, sparse forests their own loop, deep forest only the spooky ones (not every dense
  one), cold woods no other forest covers the silent forest; the open sea its own waves (deep ocean them at 0.7), the
  beach the surf and gulls; arctic wind where it is snowy and not wooded (1.12.2 has no icy), in place of the hills'
  and wasteland's, and none in wooded hills (26.x has hill wind only where it is windswept); the sparse jungle loop for a jungle that isn't dense (the jungle edge: every other 1.12.2 jungle is
  DENSE); eagles beside hawks; turtledoves in woods; the deep underground below Y 16. The creature spots the 26.x line
  never had (insect buzz, bison, elephants, primates, crocodiles, rattlesnakes) and the wolf rule are this line's.
  Not brought: 26.x's fog changes and actors.
- Still to come: electric arcs, the skeleton's bow, the log and organic footsteps.

## Looping streams
- Biome loops have no repeat delay, so the game passes the loop to paulscode, whose stream thread loops the file; not
  DS's tick or vanilla's. Paulscode goes back to a stream's start while its last buffers still play only with
  `SoundSystemConfig.streamQueueFormatsMatch` set (`SoundEngine.configureSound` sets it). Unset, the default, it let the
  stream play out, noticed (20 ms polls), opened the file again and decoded before playing: a hitch at every loop,
  which DS's small stream buffers (3 x 32 KB) left uncovered.
- With the loop seamless, a file whose ends don't meet clicks: `ambient/outside/ocean.ogg` (26.x's, the same there)
  jumps from -0.023 to 0 at its seam, which a short crossfade would fix.

## Biome survey (/dsdump biomes)
- `/dsdump biomes` (`CommandDSDump`, a client command) writes every biome to
  `config/dsurround/dumps/biomes-<time>.csv`: its mod, Forge biome dictionary types and whether Forge guessed them
  (read from `logs/latest.log`, "No types have been added to Biome"), climate, top block, the decorator's counts
  (trees, grass, flowers...: -999 trees where a biome places its own, as BoP's and roofed forest), and what DS gives it
  (sounds, spot sounds, fog, dust, aurora, the config entries matched). The log gets a per-mod summary.
- `-PbiomeMods` makes dev runs load Biomes O' Plenty as well, and `-PbiomeMods=<folder>` every mod jar in that folder
  (release jars, deobfuscated), for surveying them; the dump can run unattended from a bench scenario's `commands`.
- Surveyed (vanilla, BoP, Biomes You Go, Traverse, AbyssalCraft): every biome had types from its mod, none guessed by
  Forge. The gaps are biomes typed too loosely (AbyssalCraft's swamp, lake, forest and ocean only `DEAD`), and type
  combinations the biome configs have no rule for (snowy dense forests, with no background sound).

## Biome traits
- `registry/biome/traits`, the 26.x line's biome traits with Forge's biome dictionary types for traits: `BiomeHandler`
  gives a biome its dictionary types plus those `BiomeTraits` works out, so `biome.isSwamp` in the configs and the
  runtime variables see them; the names are Forge's. No game in it (`BiomeEvidence`), tested.
- A biome whose mod gave it a kind (forest, plains, swamp... `TraitGroup.KIND`) or a dimension keeps its types: a type
  it lacks is taken as one it hasn't (Forge has no temperate), and guessing over them made vanilla's forest dense and
  its oceans wet. Only what follows is added (snowy is cold: BYG's snowy forests become cold, as vanilla's are). A
  biome with neither is worked out from its climate (Forge's thresholds; not one left at 0.5/0.5, which then says
  nothing), its id and name (`BiomeNameTraits`, whole words: BYG runs an id's words together), then its top block,
  each only filling groups nothing before it did. In the survey 15 of 250 biomes gained a type, no vanilla one.
- The dump's `typesAdded` and `typeSources` columns show what was worked out and from what.

## Effect ranges
- Two, as the 26.x line: `effects.blockEffectRange` (40) for always-on block effects (the always-on sphere scanner, and
  particle systems expire outside it) and `effects.entityEffectRange` (32) for entity effects (`FxHandler`, badges);
  16 to 64. They were one `specialEffectRange` (24): a config that has it keeps its value for both
  (`ModOptions.migrateEffectRange`, before the options are read). The client chunk cache reaches past both, 32 at
  least (`ProxyClient.chunkCacheRange`); rain splashes, which took half the old range, land within a fixed 12.

## Random block effects
- `AreaBlockEffectsHandler`: two `RandomBlockEffectScanner`s sample vanilla's random display tick spreads, 667 blocks
  a tick each within 16 and 32 (as the 26.x line's `RandomBlockSampler`, whose effects-only tail past 32 isn't here:
  no block effect range option), and `AlwaysOnBlockEffectScanner` keeps always-on effects in a sphere. A sampled
  block starts its effects by the Particles option, as 26.x: all at All, about two in three at Decreased, none at
  Minimal (`startsEffects`; their particles skipped the option); its sounds play either way.

## Waterfalls
- From the 26.x line's waterfall effect, without its mist, foam and rings: the `splash` block effect (water, Streams'
  rivers, the Betweenlands' waters in the configs) starts a `ParticleWaterSplash` where `WaterfallColumn` says falling
  water lands (falling liquid directly above, room to spread sideways, a source or solid top below: any flowing water
  above counted before, so a pool's bottom beside moving water made one). Vanilla water says it is falling (level 8
  up); a mod's fluid is taken as falling where the same fluid goes on above it. Its strength is the drop's height in
  falling blocks (it counted the landing block and the lip too, 2 more), rechecked every half second.
- Splashes: 5 plus 2.5 a block of drop, at most 20, between half and all of that each time, by the Particles option and
  fewer far from the camera (`WaterfallSplashes`), living twice a spray's life. Sounds: `WaterfallSounds` (ticked by
  `ParticleSystemHandler`) plays a loop for at most 32 waterfalls within hearing (each tier's range is 16 blocks times
  its `sounds.json` volume above 1, as the game sets it, plus 4: `waterfall.4` is 1.5 and `waterfall.5` 2, so big falls
  carry to 24 and 32 blocks, no louder close by), those with the most impact
  (loudness squared over distance squared), tiers moved down to the new strength so each sounds as it did; every splash
  had a loop of its own before. `effects.waterfallCutoff` keeps its old scale (strength + 2). The diagnostics overlay
  has the counts.

## Fireflies
- From the 26.x line (`client/fx/firefly`, tested): each flashes in one of three rhythms (`FireflyFlash`), dark most of
  its life and not drawn then, and wanders (`WanderingFlight`), steering around blocks with a collision box rather than
  bumping into them, dipping before a flash and rising while lit. `MoteFireFly` is vanilla's end rod sparkle in
  yellow-green, lit by its own flash; `ParticleCollectionFireFly` draws a halo (`textures/particles/firefly_glow.png`,
  `effects.enableFireflyGlow`) under each lit one. The spawn chance is still 1.12.2's (1 in 50 samples).
- Point lights (`client/fx/light`, `effects.enableFireflyLight`): each lit firefly records a light as it is drawn
  (`PointLightList`, relative to the camera; the nearest 64 drawn, fading from the 48th), and `PointLights` draws them at
  `RenderWorldLastEvent`: the main framebuffer's depth copied into a texture (`glCopyTexSubImage2D`; a blit into a
  texture of its own format measured no faster), then a quad a light, added (`ONE, ONE`), by `shaders/point_light.*`
  (GLSL 1.20 from 26.x's, `gl_ProjectionMatrixInverse` for the view position). Each quad sits at the depth of its
  sphere's far side, tested `GL_GREATER`, so pixels beyond the light's reach are dropped before shading: 1.12.2 doesn't
  cull particles, and in the bench field some 200 fireflies are lit at once, several close enough that their quads
  cover the screen; that test halved the lights' GPU time (0.44 to 0.22 ms). 1.12.2's translucent blocks leave no
  depth, so the light falls on what is under water or behind glass. Off without shaders or under an OptiFine shader
  pack. The old Albedo light hook (commented out) is gone.
- Each lit firefly's quad is written whole (`BufferBuilder.addVertexData`): element by element, building them was the
  fireflies' main cost on the CPU. `FireflyFlash.brightness` looks at the latest two pulses only (the same answer).
- Bench: `scripts/bench/fireflies.json` (default, no lights, no glow, off).

## Scripts (conditions)
- Conditions run through OreLib's `ExpressionCache` (`expression/ExpressionEngine`). Sounds and block effects keep
  the compiled `Script` (`SoundEffect.matches`, `BlockEffect.conditionsMet`), compiled as the config loads
  (`compileConditions`): one that doesn't compile is logged once, naming the biome rule or block it is in, with
  OreLib's reason (an unknown name gets the nearest known one suggested). Biome rule conditions (`BiomeMatcher`) are
  compiled then too ("Skipping biome entry ...").

## Known noise in a dev run's log
- FML's missing signature data and Maven library folder errors, fernflower's "Unable to simplify switch" (the mixin
  debug export). Neither mod checks a certificate fingerprint any more (dropped in DS 4.0.0 and OreLib 3.6.0.2).
