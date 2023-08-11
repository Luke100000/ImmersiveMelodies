package immersive_melodies;

import immersive_melodies.network.ClientNetworkManager;

public class Client {
    public static void postLoad() {
        Common.networkManager = new ClientNetworkManager();
    }
}
