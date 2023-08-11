package immersive_melodies.resources;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ClientMelodyManager {
    static final Melody DEFAULT = new Melody();
    static final Map<Identifier, Melody> melodies = new HashMap<>();

    public static Map<Identifier, Melody> getMelodies() {
        return melodies;
    }

    public static Melody getMelody(Identifier identifier) {
        return melodies.getOrDefault(identifier, DEFAULT);
    }

    public static void setMelody(Identifier identifier, Melody melody) {
        melodies.put(identifier, melody);
    }
}
