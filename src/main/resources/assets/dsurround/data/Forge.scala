// Configures Vanilla Minecraft sounds and effects.  This script should run first
// before any other scripts run.

// For logging
import org.blockartistry.mod.DynSurround.data.xface.Log

// Imports to handle biomes
import org.blockartistry.mod.DynSurround.data.xface.{ BiomeConfig, SoundConfig, Biomes }

// Imports to handle blocks
import org.blockartistry.mod.DynSurround.data.xface.{ BlockConfig, BlockEffectConfig, BlockEffectType, Blocks }

// Imports to handle footsteps
import org.blockartistry.mod.DynSurround.data.xface.{ BlockClass, Footsteps }

// Imports to handle dimensions
import org.blockartistry.mod.DynSurround.data.xface.{ DimensionConfig, Dimensions }

Log.info("PROCESSING START - Forge.scala");

/////////////////////////////
//
// BIOME CONFIGURATION
//
/////////////////////////////
var biome = new BiomeConfig();
biome.setBiomeName("(?i)(?!.*forest.*|.*taiga.*|.*jungle.*|.*\\+.*|.*savanna.*)(.*hills.*|.*plateau.*|.*wasteland.*|.*canyon.*|tundra|.*highland.*|.*volcanic.*)");
biome.setResetSound(true);
biome.addSounds(new SoundConfig().setSoundName("dsurround:wind").setVolume(0.5F));
Biomes.register(biome);

/////////////////////////////
//
// BLOCK CONFIGURATION
//
/////////////////////////////
var block = new BlockConfig();
block.addBlocks("minecraft:stone:0","minecraft:stone:1","minecraft:stone:3","minecraft:stone:5","minecraft:dirt:*","minecraft:gravel","minecraft:sand:*");
block.setResetEffects(true);
block.addEffects(new BlockEffectConfig().setEffectType(BlockEffectType.DUST).setEffectChance(500));
Blocks.register(block);

/////////////////////////////
//
// FOOTSTEP CONFIGURATION
//
/////////////////////////////
Footsteps.registerForgeEntries(BlockClass.MARBLE, "blockQuartz", "marble", "stoneMarble", "blockMarble");

Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:barrier");

/////////////////////////////
//
// DIMENSION CONFIGURATION
//
/////////////////////////////
Dimensions.register(new DimensionConfig().setDimensionId(-1).setHasWeather(true).setHasCloudHaze(false).setHasAuroras(false).setSeaLevel(0));
Dimensions.register(new DimensionConfig().setDimensionId(1).setHasWeather(false).setHasCloudHaze(false).setHasAuroras(false).setSeaLevel(0).setCloudHeight(128));
Dimensions.register(new DimensionConfig().setDimensionName("Erebus").setSeaLevel(24));

Log.info("PROCESSING END - Forge.scala");
