// Configures Vanilla Minecraft sounds and effects.  This script should run first
// before any other scripts run.  Don't change the name from mcp!

// For logging and misc
import org.blockartistry.mod.DynSurround.data.xface.Log

// Imports for sounds
import org.blockartistry.mod.DynSurround.data.xface.{ SoundConfig, SoundType }

// Imports to handle biomes
import org.blockartistry.mod.DynSurround.data.xface.{ BiomeConfig, Biomes }

// Imports to handle blocks
import org.blockartistry.mod.DynSurround.data.xface.{ BlockConfig, EffectConfig, EffectType, Blocks }

// Imports to handle footsteps
import org.blockartistry.mod.DynSurround.data.xface.{ BlockClass, Footsteps }

// Imports to handle dimensions
import org.blockartistry.mod.DynSurround.data.xface.{ DimensionConfig, Dimensions }

Log.info("PROCESSING START - Minecraft Vanilla baseline");

/////////////////////////////
//
// BIOME CONFIGURATION
//
/////////////////////////////

// Sound shortcuts for reused sounds
var soundForest = new SoundConfig("dsurround:forest").setConditions("(?i)(?!.*#raining#.*).*#day#.*");
var soundCrickets = new SoundConfig("dsurround:crickets").setConditions("(?i)(?!.*#raining#.*).*#night#.*").setVolume(0.1F);
var soundOwl = new SoundConfig("dsurround:owl").setConditions("(?i)(?!.*#raining#.*).*#night#.*").setSoundType(SoundType.SPOT).setVolume(0.3F);
var soundInsectBuzz = new SoundConfig("dsurround:insectbuzz").setConditions("(?i)(?!.*#raining#.*)(.*)").setSoundType(SoundType.SPOT).setVolume(0.5F).setIsVariable(true);
var soundWolf = new SoundConfig("dsurround:wolf").setConditions("(?i)(?!.*#raining#.*).*#night#.*").setSoundType(SoundType.SPOT).setVolume(0.3F);

var biome = new BiomeConfig("(?i)(?!.*forest.*|.*taiga.*|.*jungle.*|.*\\+.*|.*savanna.*)(.*hills.*|.*plateau.*|.*wasteland.*|.*canyon.*|tundra|.*highland.*|.*volcanic.*)");
biome.addSound(new SoundConfig("dsurround:wind").setVolume(0.5F));
Biomes.register(biome);

biome = new BiomeConfig("(?i)(.*mountain.*|.*hills\\+.*|crag|alps)");
biome.addSound(new SoundConfig("dsurround:wind"));
biome.register();

biome = new BiomeConfig("(?i)(.*jungle.*|.*tropical.*)");
biome.setSpotSoundChance(600);
biome.setResetSound(true);
biome.addSound(new SoundConfig("dsurround:jungle").setConditions("(?i)(?!.*#raining#.*)(.*)").setVolume(0.3F).setPitch(0.8F));
biome.addSound(soundInsectBuzz);
biome.addSound(new SoundConfig("dsurround:primates").setConditions("(?i)(?!.*#raining#.*)(.*#day#.*)").setSoundType(SoundType.SPOT).setVolume(0.3F));
biome.register();

biome = new BiomeConfig("(?i)(?!.*ice.*)(.*plains.*|.*meadow.*|grassland|brushland|.*shrubland.*|prairie|.*field.*|.*chaparral.*|overgrown cliffs|steppe)");
biome.addSound(soundCrickets).addSound(soundInsectBuzz);
biome.register();

biome = new BiomeConfig("(?i)(?!.*ice.*)(.*plains.*|grassland|prairie)");
biome.addSound(new SoundConfig("dsurround:bison").setConditions("(?i)(?!.*#raining#.*)(.*#day#.*)").setSoundType(SoundType.SPOT).setVolume(0.5F));
biome.register();

biome = new BiomeConfig("(?i)(?!.*dead.*|.*flower.*|.*fungi.*|.*frost.*|.*snow.*|.*kelp.*)(.*forest.*|.*cherry.*|.*orchard.*|.*wood.*|.*wetland.*|.*grove.*|.*springs.*)");
biome.addSound(soundForest).addSound(soundCrickets).addSound(soundOwl);
biome.register();

biome = new BiomeConfig("(?i)(heathland|thicket|land of lakes|mangrove|origin island)");
biome.addSound(soundForest).addSound(soundCrickets);
biome.register();

biome = new BiomeConfig("(?i)(?!.*cold.*|.*snow.*)(.*taiga.*)");
biome.addSound(new SoundConfig("dsurround:taiga").setConditions("(?i)(?!.*#raining#.*).*#day#.*").setVolume(0.7F));
biome.register();

biome = new BiomeConfig("(?i)(.*taiga.*|.*snow.*forest.*)");
biome.addSound(soundOwl).addSound(soundWolf);
biome.register();

biome = new BiomeConfig("(?i)(?!.*frozen.*|.*cold.*)(.*beach.*|.*ocean.*|.*kelp.*|.*reef.*)");
biome.addSound(new SoundConfig("dsurround:beach"));
biome.addSound(new SoundConfig("dsurround:seagulls").setConditions("(?i).*#day#.*").setSoundType(SoundType.SPOT).setVolume(0.3F));
biome.register();

biome = new BiomeConfig("(?i)(?!.*frozen.*|.*dry.*)(.*river.*)");
biome.addSound(new SoundConfig("dsurround:river")).addSound(soundInsectBuzz);
biome.register();

biome = new BiomeConfig("Deep Ocean");
biome.setResetSound(true);
biome.addSound(new SoundConfig("dsurround:beach").setPitch(0.4F));
biome.register();

biome = new BiomeConfig("(?i)(.*swamp.*)");
biome.setHasFog(true).setFogColor(64,96,64).setFogDensity(0.02F);
biome.register();

biome = new BiomeConfig("(?i)(?!.*dead.*)(.*swamp.*)");
biome.setSpotSoundChance(600);
biome.addSound(soundCrickets).addSound(soundInsectBuzz);
biome.register();

biome = new BiomeConfig("(?i)(.*fen.*|.*bog.*|.*marsh.*|.*moor.*|.*bayou.*|quagmire|silkglades)");
biome.setSpotSoundChance(600);
biome.addSound(soundCrickets).addSound(soundInsectBuzz);
biome.setHasFog(true).setFogColor(128,128,128).setFogDensity(0.02F);
biome.register();

biome = new BiomeConfig("(?i)(?!.*dead.*)(.*swamp.*|.*marsh.*|.*bayou.*)");
biome.addSound(new SoundConfig("dsurround:crocodile").setSoundType(SoundType.SPOT).setVolume(0.7F));
biome.register();

biome = new BiomeConfig("(?i)(.*desert.*|.*sand.*|.*mesa.*|.*wasteland.*|.*sahara.*|outback|oasis)");
biome.setHasDust(true).setDustColor(204,185,102);
biome.register();

biome = new BiomeConfig("(?i)(?!.*cold.*|.*frozen.*)(.*desert.*|.*sand.*|.*mesa.*)");
biome.addSound(new SoundConfig("dsurround:rattlesnake").setConditions("(?i)(?!.*#raining#.*).*#day#.*").setSoundType(SoundType.SPOT).setVolume(1.0F));
biome.register();

biome = new BiomeConfig("(?i)(.*savanna.*)");
biome.addSound(new SoundConfig("dsurround:elephant").setConditions("(?i).*#day#.*").setSoundType(SoundType.SPOT).setVolume(0.7F));
biome.addSound(new SoundConfig("dsurround:insectbuzz").setConditions("(?i)(.*)").setSoundType(SoundType.SPOT).setVolume(0.5F).setWeight(20).setIsVariable(true));
biome.register();

biome = new BiomeConfig("(?i)(the end|sky)");
biome.setHasAurora(false);
biome.addSound(new SoundConfig("dsurround:theend").setVolume(0.2F));
biome.register();

biome = new BiomeConfig("(?i)(.*taiga.*|.*frozen.*|.*ice.*|.*tundra.*|.*polar.*|.*snow.*|.*glacier.*|.*arctic.*)");
biome.setHasAurora(true);
biome.register();

biome = new BiomeConfig("(?i)(hell|.*visceral.*|.*inferno.*|undergarden|boneyard)");
biome.setHasDust(true).setDustColor(255,0,0);
biome.register();

biome = new BiomeConfig("Underground");
biome.addSound(new SoundConfig("dsurround:underground").setVolume(0.2F));
biome.addSound(new SoundConfig("dsurround:rockfall").setSoundType(SoundType.SPOT).setVolume(0.3F).setWeight(30));
biome.addSound(new SoundConfig("dsurround:monstergrowl").setConditions("(?i).*#night#.*").setSoundType(SoundType.SPOT).setVolume(0.3F).setWeight(10));
biome.register();

biome = new BiomeConfig("UnderOCN");
biome.addSound(new SoundConfig("dsurround:underocean").setVolume(0.1F));
biome.register();

biome = new BiomeConfig("UnderDOCN");
biome.addSound(new SoundConfig("dsurround:underdeepocean").setVolume(0.1F));
biome.addSound(new SoundConfig("dsurround:whale").setSoundType(SoundType.SPOT).setVolume(0.3F));
biome.register();

biome = new BiomeConfig("UnderRVR");
biome.addSound(new SoundConfig("dsurround:underriver").setVolume(0.1F).setPitch(1.5F));
biome.register();

biome = new BiomeConfig("Player");
biome.addSound(new SoundConfig("dsurround:heartbeat").setConditions("(?i).*#hurt#.*"));
biome.addSound(new SoundConfig("dsurround:tummy").setConditions("(?i).*#hungry#.*").setSoundType(SoundType.PERIODIC).setRepeatDelay(300));
biome.register();

/////////////////////////////
//
// BLOCK CONFIGURATION
//
/////////////////////////////
var block = new BlockConfig("minecraft:stone:0","minecraft:stone:1","minecraft:stone:3","minecraft:stone:5","minecraft:dirt:*","minecraft:gravel","minecraft:sand:*");
block.addEffect(new EffectConfig(EffectType.DUST).setEffectChance(500));
block.register();

block = new BlockConfig("minecraft:lava");
block.addEffect(new EffectConfig(EffectType.FIRE).setEffectChance(1800));
block.register();

block = new BlockConfig("minecraft:water");
block.addEffect(new EffectConfig(EffectType.STEAM).setEffectChance(10));
block.addEffect(new EffectConfig(EffectType.BUBBLE).setEffectChance(1800));
block.register();

block = new BlockConfig("minecraft:ice","minecraft:packed_ice");
block.setSoundChance(10000);
block.addSound(new SoundConfig("dsurround:ice").setVolume(0.5F).setPitch(1.0F).setIsVariable(false));
block.register();

block = new BlockConfig("minecraft:waterlily");
block.setSoundChance(25);
block.addSound(new SoundConfig("dsurround:frog").setConditions("(?i)(?!.*#inside#.*)(.*)").setVolume(0.4F).setPitch(1.0F).setIsVariable(true));
block.register();

block = new BlockConfig("minecraft:soul_sand");
block.setSoundChance(8000);
block.addSound(new SoundConfig("dsurround:soulsand").setVolume(0.2F).setPitch(1.0F).setIsVariable(true));
block.register();

block = new BlockConfig("minecraft:planks","minecraft:oak_stairs","minecraft:spruce_stairs","minecraft:jungle_stairs","minecraft:birch_stairs","minecraft:acacia_stairs","minecraft:dark_oak_stairs");
block.setStepSoundChance(150);
block.addSound(new SoundConfig("dsurround:floorsqueak").setSoundType(SoundType.STEP).setVolume(0.2F).setIsVariable(true));
block.register();

block = new BlockConfig("minecraft:nether_brick","minecraft:nether_brick_fence","minecraft:nether_brick_stairs");
block.setSoundChance(8000);
block.addSound(new SoundConfig("dsurround:breathing").setConditions("(?i)(.*#nether#.*)").setVolume(0.5F).setIsVariable(true));
block.register();

block = new BlockConfig("minecraft:tallgrass");
block.setSoundChance(25000);
block.addSound(new SoundConfig("dsurround:hiss").setConditions("(?i)(?!.*raining.*|.*cold.*|.*freezing.*)(.*#day#.*)").setVolume(1.0F).setIsVariable(true));
block.register();

block = new BlockConfig("minecraft:monster_egg");
block.setSoundChance(600);
block.addSound(new SoundConfig("dsurround:hiss").setVolume(1.0F).setIsVariable(true));
block.addSound(new SoundConfig("dsurround:insectcrawl").setVolume(1.0F).setIsVariable(true));
block.register();

block = new BlockConfig("minecraft:bookshelf");
block.setSoundChance(500);
block.addSound(new SoundConfig("dsurround:bookshelf").setVolume(0.7F));
block.register();

block = new BlockConfig("minecraft:deadbush");
block.setSoundChance(200);
block.addSound(new SoundConfig("dsurround:rattlesnake").setConditions("(?i)(?!.*#raining#.*).*#day#.*#tcwarm#.*").setVolume(1.0F));
block.addSound(new SoundConfig("dsurround:hiss").setConditions("(?i)(?!.*#raining#.*).*#day#.*#tcwarm#.*").setVolume(1.0F).setIsVariable(true));
block.addSound(new SoundConfig("dsurround:insectcrawl").setConditions("(?i)(?!.*#raining#.*).*#day#.*#tcwarm#.*").setVolume(1.0F).setIsVariable(true));
block.register();

block = new BlockConfig("minecraft:red_flower:*","minecraft:yellow_flower:*","minecraft:double_plant:*");
block.addEffect(new EffectConfig(EffectType.FIREFLY).setEffectChance(50));
block.register();

/////////////////////////////
//
// FOOTSTEP CONFIGURATION
//
/////////////////////////////
Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:barrier", "minecraft:air", "minecraft:flowing_water", "minecraft:water", "minecraft:flowing_lava", "minecraft:lava");
Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:web", "minecraft:piston_extension", "minecraft:yellow_flower", "minecraft:red_flower", "minecraft:brown_mushroom");
Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:red_mushroom", "minecraft:torch", "minecraft:redstone_wire", "minecraft:standing_sign", "minecraft:wall_sign");
Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:lever", "minecraft:unlit_redstone_torch", "minecraft:redstone_torch", "minecraft:stone_button", "minecraft:portal");
Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:pumpkin_stem", "minecraft:melon_stem", "minecraft:end_portal", "minecraft:tripwire", "minecraft:tripwire_hook");
Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:wooden_button");
Footsteps.registerFootsteps(BlockClass.STONE, "minecraft:stone", "minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:stone_stairs");
Footsteps.registerFootsteps(BlockClass.STONE, "minecraft:netherrack", "minecraft:end_stone", "minecraft:cobblestone_wall+bigger", "minecraft:coal_block");
Footsteps.registerFootsteps(BlockClass.STONE, "minecraft:prismarine");
Footsteps.registerFootsteps(BlockClass.GRASS, "minecraft:grass", "minecraft:grass_path", "minecraft:cactus", "minecraft:mycelium");
Footsteps.registerFootsteps(BlockClass.DIRT, "minecraft:dirt", "minecraft:farmland", "minecraft:dirt");
Footsteps.registerFootsteps(BlockClass.WOOD, "minecraft:planks", "minecraft:log", "minecraft:log2", "minecraft:bookshelf", "minecraft:crafting_table");
Footsteps.registerFootsteps(BlockClass.WOOD, "minecraft:oak_stairs", "minecraft:double_wooden_slab", "minecraft:wooden_slab", "minecraft:spruce_stairs", "minecraft:birch_stairs");
Footsteps.registerFootsteps(BlockClass.WOOD, "minecraft:jungle_stairs", "minecraft:acacia_stairs", "minecraft:dark_oak_stairs");
Footsteps.registerFootsteps(BlockClass.WHEAT, "minecraft:wheat");
Footsteps.registerFootsteps(BlockClass.SQUEAKYWOOD, "minecraft:chest", "minecraft:trapped_chest");
Footsteps.registerFootsteps(BlockClass.SAPLINGS, "minecraft:sapling");
Footsteps.registerFootsteps(BlockClass.BEDROCK, "minecraft:bedrock");
Footsteps.registerFootsteps(BlockClass.SAND, "minecraft:sand");
Footsteps.registerFootsteps(BlockClass.GRAVEL, "minecraft:gravel");
Footsteps.registerFootsteps(BlockClass.ORE, "minecraft:gold_ore", "minecraft:iron_ore", "minecraft:coal_ore", "minecraft:lapis_ore", "minecraft:diamond_ore");
Footsteps.registerFootsteps(BlockClass.ORE, "minecraft:redstone_ore", "minecraft:lit_redstone_ore", "minecraft:emerald_ore", "minecraft:quartz_ore");
Footsteps.registerFootsteps(BlockClass.LEAVES, "minecraft:leaves", "minecraft:leaves2", "minecraft:chorus_flower", "minecraft:chorus_plant", "minecraft:hay_block");
Footsteps.registerFootsteps(BlockClass.MUD, "minecraft:sponge");
Footsteps.registerFootsteps(BlockClass.GLASS, "minecraft:glass", "minecraft:stained_glass", "minecraft:glass_pane", "minecraft:beacon", "minecraft:stained_glass_pane", "minecraft:end_rod");
Footsteps.registerFootsteps(BlockClass.COMPOSITE, "minecraft:lapis_block", "minecraft:diamond_block", "minecraft:emerald_block", "minecraft:redstone_block");
Footsteps.registerFootsteps(BlockClass.STONEMACHINE, "minecraft:dispenser", "minecraft:furnace", "minecraft:lit_furnace", "minecraft:dropper");
Footsteps.registerFootsteps(BlockClass.SANDSTONE, "minecraft:sandstone", "minecraft:red_sandstone", "minecraft:hardened_clay", "minecraft:sandstone_stairs", "minecraft:red_sandstone_stairs");
Footsteps.registerFootsteps(BlockClass.WOODUTILITY, "minecraft:noteblock", "minecraft:jukebox");
Footsteps.registerFootsteps(BlockClass.RUG, "minecraft:bed", "minecraft:wool", "minecraft:carpet", "minecraft:carpet+carpet");
Footsteps.registerFootsteps(BlockClass.HARDMETAL, "minecraft:gold_block", "minecraft:iron_block");
Footsteps.registerFootsteps(BlockClass.BRICKSTONE, "minecraft:brick_block", "minecraft:stonebrick", "minecraft:brick_stairs", "minecraft:stone_brick_stairs");
Footsteps.registerFootsteps(BlockClass.BRICKSTONE, "minecraft:flower_pot", "minecraft:stained_hardened_clay");
Footsteps.registerFootsteps(BlockClass.EQUIPMENT, "minecraft:tnt");
Footsteps.registerFootsteps(BlockClass.OBSIDIAN, "minecraft:obsidian", "minecraft:dragon_egg", "minecraft:ender_chest");
Footsteps.registerFootsteps(BlockClass.METALBAR, "minecraft:mob_spawner", "minecraft:iron_bars", "minecraft:brewing_stand", "minecraft:cauldron", "minecraft:hopper");
Footsteps.registerFootsteps(BlockClass.BLUNTWOOD, "minecraft:wooden_door", "minecraft:acacia_door", "minecraft:dark_oak_door", "minecraft:birch_door", "minecraft:jungle_door", "minecraft:spruce_door");
Footsteps.registerFootsteps(BlockClass.BLUNTWOOD, "minecraft:trapdoor");
Footsteps.registerFootsteps(BlockClass.LADDERDEFAULT, "minecraft:ladder");
Footsteps.registerFootsteps(BlockClass.METALSUBPARTS, "minecraft:iron_door", "minecraft:iron_trapdoor");
Footsteps.registerFootsteps(BlockClass.REED, "minecraft:reeds");
Footsteps.registerFootsteps(BlockClass.FENCE, "minecraft:fence", "minecraft:acacia_fence", "minecraft:dark_oak_fence", "minecraft:birch_fence", "minecraft:jungle_fence", "minecraft:spruce_fence");
Footsteps.registerFootsteps(BlockClass.FENCE, "minecraft:fence_gate", "minecraft:acacia_fence_gate", "minecraft:dark_oak_fence_gate", "minecraft:birch_fence_gate", "minecraft:jungle_fence_gate", "minecraft:spruce_fence_gate");
Footsteps.registerFootsteps(BlockClass.ORGANICSOLID, "minecraft:pumpkin", "minecraft:lit_pumpkin", "minecraft:melon_block");
Footsteps.registerFootsteps(BlockClass.ORGANIC, "minecraft:nether_wart_block", "minecraft:cake", "minecraft:brown_mushroom_block", "minecraft:red_mushroom_block");
Footsteps.registerFootsteps(BlockClass.ORGANIC, "minecraft:skull");
Footsteps.registerFootsteps(BlockClass.ORGANICDRY, "minecraft:cocoa");
Footsteps.registerFootsteps(BlockClass.QUICKSAND, "minecraft:soul_sand");
Footsteps.registerFootsteps(BlockClass.GLOWSTONE, "minecraft:glowstone");
Footsteps.registerFootsteps(BlockClass.STONEUTILITY, "minecraft:unpowered_repeater", "minecraft:powered_repeater", "minecraft:enchanting_table", "minecraft:end_portal_frame");
Footsteps.registerFootsteps(BlockClass.STONEUTILITY, "minecraft:redstone_lamp", "minecraft:lit_redstone_lamp", "minecraft:sea_lantern", "minecraft:command_block");
Footsteps.registerFootsteps(BlockClass.STONEUTILITY, "minecraft:unpowered_comparator", "minecraft:powered_comparator");
Footsteps.registerFootsteps(BlockClass.CROP, "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots");
Footsteps.registerFootsteps(BlockClass.ANVIL, "minecraft:anvil");
Footsteps.registerFootsteps(BlockClass.DAYLIGHTDETECTOR, "minecraft:daylight_detector", "minecraft:daylight_detector_inverted");
Footsteps.registerFootsteps(BlockClass.MARBLE, "minecraft:quartz_block", "minecraft:quartz_stairs");
Footsteps.registerFootsteps(BlockClass.PACKEDICE, "minecraft:packed_ice");

Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:golden_rail", "minecraft:detector_rail", "minecraft:rail", "minecraft:activator_rail");
Footsteps.registerFootsteps(BlockClass.RAILS, "minecraft:golden_rail+foliage", "minecraft:detector_rail+foliage", "minecraft:rail+foliage", "minecraft:activator_rail+foliage");

Footsteps.registerFootsteps(BlockClass.STONEMACHINE, "minecraft:sticky_piston");
Footsteps.registerFootsteps(BlockClass.WOODSTICKY, "minecraft:sticky_piston^1");

Footsteps.registerFootsteps(BlockClass.STONEMACHINE, "minecraft:piston");
Footsteps.registerFootsteps(BlockClass.WOOD, "minecraft:piston^1", "minecraft:piston_head");
Footsteps.registerFootsteps(BlockClass.WOODSTICKY, "minecraft:piston^9");

Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:tallgrass");
Footsteps.registerFootsteps(BlockClass.MESSYGROUND, "minecraft:tallgrass+messy");
Footsteps.registerFootsteps(BlockClass.STRAW, "minecraft:tallgrass^0+foliage");
Footsteps.registerFootsteps(BlockClass.BRUSH, "minecraft:tallgrass^1+foliage", "minecraft:tallgrass^2+foliage");

Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:deadbush");
Footsteps.registerFootsteps(BlockClass.MESSYGROUND, "minecraft:deadbush+messy");
Footsteps.registerFootsteps(BlockClass.STRAW, "minecraft:deadbush+foliage");

Footsteps.registerFootsteps(BlockClass.STONE, "minecraft:double_stone_slab", "minecraft:double_stone_slab^3", "minecraft:double_stone_slab2","minecraft:stone_slab", "minecraft:stone_slab^3", "minecraft:stone_slab^11", "minecraft:stone_slab2");
Footsteps.registerFootsteps(BlockClass.BRICKSTONE, "minecraft:double_stone_slab^0","minecraft:double_stone_slab^4","minecraft:double_stone_slab^5","minecraft:double_stone_slab^6","minecraft:double_stone_slab^8","minecraft:stone_slab^0");
Footsteps.registerFootsteps(BlockClass.BRICKSTONE, "minecraft:stone_slab^4","minecraft:stone_slab^5","minecraft:stone_slab^6","minecraft:stone_slab^8","minecraft:stone_slab^12","minecraft:stone_slab^13","minecraft:stone_slab^14");
Footsteps.registerFootsteps(BlockClass.SANDSTONE, "minecraft:double_stone_slab^1","minecraft:double_stone_slab^9","minecraft:double_stone_slab2^0","minecraft:double_stone_slab2^8","minecraft:stone_slab^1","minecraft:stone_slab^9","minecraft:stone_slab2^0","minecraft:stone_slab2^8");
Footsteps.registerFootsteps(BlockClass.WOOD, "minecraft:double_stone_slab^2","minecraft:stone_slab^2","minecraft:stone_slab^10");
Footsteps.registerFootsteps(BlockClass.MARBLE, "minecraft:double_stone_slab^7", "minecraft:double_stone_slab^15","minecraft:stone_slab^7","minecraft:stone_slab^15");

Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:fire");
Footsteps.registerFootsteps(BlockClass.FIRE, "minecraft:fire+foliage");

Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:nether_wart");
Footsteps.registerFootsteps(BlockClass.ORGANIC, "minecraft:nether_wart+foliage");

Footsteps.registerFootsteps(BlockClass.STONE, "minecraft:stone_pressure_plate", "minecraft:stone_pressure_plate+carpet");
Footsteps.registerFootsteps(BlockClass.WOOD, "minecraft:wooden_pressure_plate", "minecraft:wooden_pressure_plate+carpet");
Footsteps.registerFootsteps(BlockClass.HARDMETAL, "minecraft:light_weighted_pressure_plate", "minecraft:light_weighted_pressure_plate+carpet");
Footsteps.registerFootsteps(BlockClass.HARDMETAL, "minecraft:heavy_weighted_pressure_plate", "minecraft:heavy_weighted_pressure_plate+carpet");

Footsteps.registerFootsteps(BlockClass.SNOW, "minecraft:snow_layer", "minecraft:snow_layer+carpet");
Footsteps.registerFootsteps(BlockClass.LEAVES, "minecraft:vine", "minecraft:vine+foliage");
Footsteps.registerFootsteps(BlockClass.WATERFINE, "minecraft:waterlily", "minecraft:waterlily+carpet");
Footsteps.registerFootsteps(BlockClass.BRICKSTONE, "minecraft:nether_brick", "minecraft:nether_brick_fence+bigger", "minecraft:nether_brick_stairs");

Footsteps.registerFootsteps(BlockClass.STONE, "minecraft:monster_egg^0", "minecraft:monster_egg^1");
Footsteps.registerFootsteps(BlockClass.BRICKSTONE, "minecraft:monster_egg^2");

Footsteps.registerFootsteps(BlockClass.NOT_EMITTER, "minecraft:double_plant");
Footsteps.registerFootsteps(BlockClass.MESSYGROUND, "minecraft:double_plant+messy");
Footsteps.registerFootsteps(BlockClass.BRUSH, "minecraft:double_plant^0+foliage", "minecraft:double_plant^2+foliage", "minecraft:double_plant^8+foliage", "minecraft:double_plant^9+foliage", "minecraft:double_plant^10+foliage");
Footsteps.registerFootsteps(BlockClass.BRUSH, "minecraft:double_plant^11+foliage", "minecraft:double_plant^12+foliage", "minecraft:double_plant^13+foliage", "minecraft:double_plant^14+foliage", "minecraft:double_plant^15+foliage");
Footsteps.registerFootsteps(BlockClass.BRUSHSTRAWTRANSITION, "minecraft:double_plant^1+foliage", "minecraft:double_plant^3+foliage", "minecraft:double_plant^4+foliage","minecraft:double_plant^5+foliage"); 

Footsteps.registerFootsteps(BlockClass.STANDINGBANNER, "minecraft:standing_banner", "minecraft:standing_banner+foliage");
Footsteps.registerFootsteps(BlockClass.WALLBANNER, "minecraft:wall_banner");

/////////////////////////////
//
// DIMENSION CONFIGURATION
//
/////////////////////////////
Dimensions.register(new DimensionConfig().setDimensionId(-1).setHasWeather(true).setHasCloudHaze(false).setHasAuroras(false).setSeaLevel(0));
Dimensions.register(new DimensionConfig().setDimensionId(1).setHasWeather(false).setHasCloudHaze(false).setHasAuroras(false).setSeaLevel(0).setCloudHeight(128));
Dimensions.register(new DimensionConfig().setDimensionName("Erebus").setSeaLevel(24));

Log.info("PROCESSING END - Minecraft Vanilla baseline");
