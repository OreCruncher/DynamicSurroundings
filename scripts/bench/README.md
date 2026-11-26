# Bench scenarios

A development client that runs a scenario by itself and closes: it opens (or makes) a world, and for each run applies
some settings, goes to a scene, waits for the terrain and a warm-up, takes a screenshot and records a `/dsprofile`.
The code is `org.orecruncher.dsurround.bench` (`ScenarioRunner`), started only in development runs
(`profiling/BenchHook`) and left out of the published jar. Ported from the 26.x line, whose scripts read the results.

    ./gradlew runClientBench -Pscenario=scripts/bench/weather.json

(Git Bash: `JAVA_HOME=<JDK 25> ./gradlew ...`, as the other tasks.) The runs use their own folder, `run-bench`
(config, options, worlds), so the dev settings and worlds are never touched. Results go to
`run-bench/bench-results/<scenario>-<time>/`: the recordings (`01-thunderstorm-dsurround-weather-....zip`),
`screenshots/`, and `manifest.json`, which says what each run was (settings, where the scene was and how it was found),
and `"finished": true` once everything ran (or `"error"`). `bench-results/latest.txt` names the newest folder.

Reading the results: the 26.x line's scripts, from its repository (`DynamicSurroundingsFabric`):
`python scripts/bench-summary.py <results folder>` compares the runs (each label averaged over the repeats);
`python scripts/jfr-summary.py <recording zip>` looks into one.

Without `-Pscenario`, `runClientBench` is a plain client in the bench folder, for marking scenes.

## Format

```jsonc
{
  "world": "bench",            // folder in saves; made if missing (creative, cheats on)
  "seed": 20261126,            // seed to make it with
  "setup": ["gamerule doDaylightCycle false"],   // commands once the world is open
  "warmup": 15,                // seconds after the terrain is drawn, before recording
  "record": 60,                // seconds recorded a run
  "repeats": 2,                // the runs this many times, every other time reversed (A B, B A)
  "video": {"vsync": false, "maxFps": 260},       // each optional; 260 is no limit
  "screenshots": true,         // one a run, as recording starts, without the HUD
  "volume": 0.5,               // the master volume for the session, if given (and "musicVolume")
  "scenes": {
    "plains": {
      "biome": "minecraft:plains",   // found if the world doesn't remember the scene
      "yaw": 90, "pitch": 5,         // optional; otherwise toward the most of the biome, a little down
      "commands": ["weather thunder 999999"],
      "profile": "weather"           // /dsprofile scene: general, aurora, weather
    }
  },
  "runs": [
    {"label": "no-thunder", "scene": "plains", "settings": {"rain.allowBackgroundThunder": false}}
  ]
}
```

- **Settings** are `ModOptions` paths: the nested class, then the field (`rain.allowBackgroundThunder`,
  `aurora.maxBands`, `effects.specialEffectRange`). Each is set on the Forge config property behind the field, in memory,
  and `ConfigProcessor` brings the fields in step, as the config screen does; nothing is written to the config file. A
  setting needing the game restarted (`@RestartRequired(server = true)`) is refused up front. Before each run, every
  setting any run sets is put back to what it was when the session started, and again at the end. Some settings are
  only read when something starts (an aurora picks its renderer as it appears): compare those across scenarios, not
  runs.
- **Setting sets**: `"settingSets": {"off": {...}}` names a group of settings once; a run applies it with
  `"use": "off"` (or a list, in order), then its own `settings` on top.
- **Commands** are typed as the player would: the server's (`time`, `weather`, `gamerule`, `summon`, `ds`) run on the
  built-in server with full permission, as the player and where they stand; client commands (`dsprofile`, `dscalc`) on
  the client. 1.12.2's gamerules: `doDaylightCycle`, `doWeatherCycle`, `doMobSpawning`. A server command that fails is
  logged with what it said and listed in the manifest's `failedCommands`, unless all it said was that it had nothing to
  change (a fill that builds a scene, on the scene's next run).
- **Settled commands**: a scene's `"settledCommands"` run as the warm-up starts.
- **Shot commands**: a scene's `"shotCommands"` run just before its screenshot, and `"shotDelay"` is how many ticks
  the shot then waits for what they start to show (2 at least).
- **Offset**: a scene's `"offset": [x, z]` has the player arrive that many blocks east and south of its place, on the
  ground there: a stage built up in the air stands to one side of the place, roofing no scene on the ground there.
- **Flying**: a scene's `"flySpeed"` flies the player east in blocks a tick at height 230, from the warm-up on, for
  what moving through terrain costs (the always-on block scan, chunk loading).

Not ported from the 26.x line: sounds played around the player, boats, Improved Transparency, the window's size.

## Scenes

A scene with a `biome` is found the first time: the nearest of the biome to 0,0 (rings out from it, sampled from the
biome provider: 1.12.2 has no `/locate biome`), then the biome is sampled every 64 blocks out to 2048 from there for
where the most of it is, and every 16 blocks around that for the place with the most of the ground 64 blocks around
being that biome (`biomeShare` in the manifest), on dry ground of it, looking toward the most of it. It is remembered
in the world's folder, `dsurround-bench.json`, with the world's seed and the mods; a remembered scene is checked to
still be in its biome, and found again if not (the manifest says so).

Biomes go by 1.12.2's names: `minecraft:ice_flats` (snowy tundra), `minecraft:taiga_cold`, `minecraft:mesa`,
`minecraft:roofed_forest`, `minecraft:swampland`, `minecraft:desert`...

What a scene needs that a biome doesn't have can often be built by its commands where the biome is found (summoned
mobs, a lava pool, water up in the air). Otherwise stand there in the bench world (`runClientBench` without a scenario)
and run `/dsbench mark <name>`, which remembers the position and where you look; a marked scene is used as it is, even
when the scene has a biome. `/dsbench list` lists the world's scenes.
