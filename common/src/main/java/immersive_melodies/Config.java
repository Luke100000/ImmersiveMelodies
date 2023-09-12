package immersive_melodies;

import java.util.Map;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public String README = "https://github.com/Luke100000/ImmersiveMelodies/wiki/Config";

    public boolean showOtherPlayersMelodies = true;
    public int getBufferDelay = 75;
    public Map<String, Float> mobInstrumentFactors = Map.of(
            "minecraft:zombie", 0.01f,
            "minecraft:skeleton", 0.01f,
            "minecraft:wither_skeleton", 0.01f,
            "minecraft:piglin_brute", 0.01f,
            "minecraft:piglin", 0.01f,
            "minecraft:zombified_piglin", 0.01f,
            "minecraft:pillager", 0.01f
    );
    public boolean forceMobsToPickUp = true;
}
