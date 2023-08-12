package immersive_melodies.resources;

import immersive_melodies.cobalt.network.NetworkHandler;
import immersive_melodies.network.c2s.MelodyRequest;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientMelodyManager {
    static final Melody DEFAULT = new Melody();
    static final Map<Identifier, Melody> melodies = new HashMap<>();
    static final Map<Identifier, MelodyDescriptor> melodiesList = new HashMap<>();
    static final Set<Identifier> requested = new HashSet<>();

    public static Map<Identifier, MelodyDescriptor> getMelodiesList() {
        return melodiesList;
    }

    public static Melody getMelody(Identifier identifier) {
        if (!melodies.containsKey(identifier) && !requested.contains(identifier)) {
            NetworkHandler.sendToServer(new MelodyRequest(identifier));
            requested.add(identifier);
        }
        return melodies.getOrDefault(identifier, DEFAULT);
    }

    public static void setMelody(Identifier identifier, Melody melody) {
        melodies.put(identifier, melody);
    }
}
