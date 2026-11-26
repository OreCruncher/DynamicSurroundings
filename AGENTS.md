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
- MCP mappings `snapshot` `20171003` (RFG adds `-1.12`): the names the code was written in. Forge is RFG's
  14.23.5.2847; `strip-latest-forge-requirements` lets dev runs load mods asking for a newer one.
- `ModInfo.VERSION` comes from `Tags.VERSION`, generated from `mod_version` in `gradle.properties` (`injectTags`).
- Optional mods are `compileOnly` (deobfuscated with `rfg.deobf`); OreLib, required, is on the runtime class path.
  CurseMaven ids are in `gradle.properties`.
- OreLib (https://github.com/OreCruncher/OreLib, the author's own library; clone in `C:\repos\OreLib`, its own
  `AGENTS.md`): `-Porelib_jar=<path to an OreLib release jar>` builds and runs against a local build instead of the
  CurseMaven one. `runClient` only: `runObfClient` doesn't copy a local jar into its mods folder.
- Quitting a client from a script: `CloseMainWindow` doesn't reach the LWJGL 2 window; stop the process you launched.

## Mixin
- Mixin is not bundled. MixinBooter (mod id `mixinbooter`, required, `[11.0,)`) provides it (CleanMix, Mixin 0.8.7)
  and reads `MixinConfigs: mixins.dsurround.json` from the jar's manifest. Bundling Mixin is what made mods clash over
  Mixin versions; there is no coremod any more (the old `TransformLoader` only started Mixin).
- The refmap (`mixins.dsurround.refmap.json`) is written by MixinBooter's annotation processor
  (`modUtils.enableMixins`). In dev runs MixinBooter finds the config without a manifest.
- `run/logs/mixinbooter.log` lists each mixin applied (`APPLY mixins.dsurround.json:...`). 13 apply by the main menu;
  `MixinGuiScreenOptionsSounds` waits for the sound options screen.

## Known noise in a dev run's log
- FML's missing signature data and Maven library folder errors, fernflower's "Unable to simplify switch" (the mixin
  debug export). Neither mod checks a certificate fingerprint any more (dropped in DS 4.0.0 and OreLib 3.6.0.2); the
  released OreLib 3.6.0.1 still logs "Invalid fingerprint detected!".
