package immersive_melodies;

import java.util.Map;

import static net.minecraft.world.entity.Mob.DEFAULT_EQUIPMENT_DROP_CHANCE;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public String README = "https://github.com/Luke100000/ImmersiveMelodies/wiki/Config";

    public int getBufferDelay = 75;
    public int maxAudibleDistance = 48;
    public float instrumentVolumeFactor = 1.0f;
    public float perceivedLoudnessAdjustmentFactor = 0.5f;

    public int humanizationTime = 5;
    public float humanizationVolume = 0.15f;
    public float humanizationPitch = 0.003f;

    public Map<String, Float> mobInstrumentFactors = Map.of(
            "minecraft:zombie", 0.01f,
            "minecraft:husk", 0.01f,
            "minecraft:skeleton", 0.01f,
            "minecraft:wither_skeleton", 0.01f,
            "minecraft:piglin_brute", 0.01f,
            "minecraft:piglin", 0.01f,
            "minecraft:zombified_piglin", 0.01f,
            "minecraft:pillager", 0.01f
    );
    public float mobInstrumentDropFactor = DEFAULT_EQUIPMENT_DROP_CHANCE;

    public boolean showOtherPlayersMelodies = true;
    public boolean forceMobsToPickUp = true;
    public boolean clickedHelp = false;
    public boolean loadInbuiltMidis = true;
    public boolean stopGameMusicForPlayers = true;
    public boolean stopGameMusicForMobs = false;
    public int uploadPermissionLevel = 0;

    public Map<Integer, Integer> scancodeToMidi = Map.ofEntries(
            Map.entry(38, 60), // A -> C4
            Map.entry(39, 62), // S -> D4
            Map.entry(40, 64), // D -> E4
            Map.entry(41, 65), // F -> F4
            Map.entry(42, 67), // G -> G4
            Map.entry(43, 69), // H -> A4
            Map.entry(44, 71), // J -> B4
            Map.entry(45, 72), // K -> C5
            Map.entry(24, 61), // Q -> C#4
            Map.entry(25, 63), // W -> D#4
            Map.entry(27, 66), // E -> F#4
            Map.entry(28, 68), // R -> G#4
            Map.entry(29, 70), // T -> A#4
            Map.entry(30, 73)  // Y -> C#5
    );

}
