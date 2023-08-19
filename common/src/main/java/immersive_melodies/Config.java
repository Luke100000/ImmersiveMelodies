package immersive_melodies;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public String README = "https://github.com/Luke100000/ImmersiveMelodies/wiki/Config";

    public boolean showOtherPlayersMelodies = true;
    public boolean parseAllMidiTracks = false;
    public long getBufferDelay = 75;
    public float mobInstrumentFactor = 0.5f;
    public boolean forceMobsToPickUp = true;
}
