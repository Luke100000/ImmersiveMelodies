package immersive_melodies;

import immersive_melodies.client.SoundManagerImpl;
import immersive_melodies.network.ClientNetworkManager;
import net.minecraft.client.MinecraftClient;

public class Client {
    public static void postLoad() {
        Common.networkManager = new ClientNetworkManager();
        Common.soundManager = new SoundManagerImpl(MinecraftClient.getInstance());
    }
}
