import org.blockartistry.mod.DynSurround.data.xface.{ BiomeConfig, SoundConfig, Biomes }

var cfg = new BiomeConfig();
cfg.biomeName = "(?i)(?!.*forest.*|.*taiga.*|.*jungle.*|.*\\+.*|.*savanna.*)(.*hills.*|.*plateau.*|.*wasteland.*|.*canyon.*|tundra|.*highland.*|.*volcanic.*)";
var sound = new SoundConfig();
sound.sound = "dsurround:wind";
sound.volume = 0.5F;
cfg.sounds.add(sound);
cfg.soundReset = true;
Biomes.register(cfg);
