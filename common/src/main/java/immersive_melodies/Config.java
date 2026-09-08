package immersive_melodies;

import java.util.Map;

import static net.minecraft.world.entity.DropChances.DEFAULT_EQUIPMENT_DROP_CHANCE;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public String README = "https://github.com/Luke100000/ImmersiveMelodies/wiki/Config";

    public int bufferDelay = 75;
    public int maxAudibleDistance = 48;
    public float instrumentVolumeFactor = 1.0f;
    public float perceivedLoudnessAdjustmentFactor = 0.5f;
    public int humanizationTime = 20;
    public float humanizationVolume = 0.06f;
    public float humanizationPitch = 0.02f;
    public float humanizationLength = 0.04f;

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
    public boolean autoSynchronize = true;
    public boolean forceMobsToPickUp = true;
    public boolean clickedHelp = false;
    public boolean loadInbuiltMidis = true;
    public boolean stopGameMusicForPlayers = true;
    public boolean stopGameMusicForMobs = false;
    public int uploadPermissionLevel = 0;
    public int rightClickToDropEntityInstrumentPermissionLevel = 0;

    public Map<Integer, Integer> keycodeToMidi = Map.ofEntries(
            Map.entry(65, 60), // A -> C4
            Map.entry(83, 62), // S -> D4
            Map.entry(68, 64), // D -> E4
            Map.entry(70, 65), // F -> F4
            Map.entry(71, 67), // G -> G4
            Map.entry(72, 69), // H -> A4
            Map.entry(74, 71), // J -> B4
            Map.entry(75, 72), // K -> C5

            Map.entry(87, 61), // W -> C#4
            Map.entry(69, 63), // E -> D#4
            Map.entry(84, 66), // T -> F#4
            Map.entry(89, 68), // Y -> G#4
            Map.entry(85, 70)  // U -> A#4
    );
}
