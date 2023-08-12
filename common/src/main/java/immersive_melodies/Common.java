package immersive_melodies;

import immersive_melodies.client.SoundManager;
import immersive_melodies.network.NetworkManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Common {
    public static final String SHORT_MOD_ID = "ic_ip";
    public static final String MOD_ID = "immersive_melodies";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;
    public static SoundManager soundManager;

    public static Identifier locate(String path) {
        return new Identifier(MOD_ID, path);
    }
}
