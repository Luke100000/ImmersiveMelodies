package immersive_melodies.network;

import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;

public interface NetworkManager {
    void handleOpenGuiRequest(OpenGuiRequest request);

    void handleMelodyListMessage(MelodyListMessage response);
}
