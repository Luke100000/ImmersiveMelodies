package immersive_melodies;

import immersive_melodies.client.sound.SoundManager;
import immersive_melodies.network.NetworkManager;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Common {
    public static final String MOD_ID = "immersive_melodies";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;
    public static SoundManager soundManager;

    public static Identifier locate(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public interface RegisterHelper<T> {
        void register(Identifier name, T value);
    }
}
