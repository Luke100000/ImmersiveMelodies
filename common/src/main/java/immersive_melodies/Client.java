package immersive_melodies;

import immersive_melodies.client.sound.SoundManagerImpl;
import immersive_melodies.network.ClientNetworkManager;
import immersive_melodies.util.TimerImpl;
import net.minecraft.client.MinecraftClient;

public class Client {
    public static void postLoad() {
        MinecraftClient client = MinecraftClient.getInstance();
        Common.networkManager = new ClientNetworkManager();
        Common.soundManager = new SoundManagerImpl(client);
        Common.timer = new TimerImpl(client);
    }
}
