package immersive_melodies.network;

import immersive_melodies.client.gui.ImmersiveMelodiesScreen;
import immersive_melodies.network.s2c.MelodyListMessage;
import immersive_melodies.network.s2c.OpenGuiRequest;
import immersive_melodies.resources.ClientMelodyManager;
import net.minecraft.client.MinecraftClient;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleOpenGuiRequest(OpenGuiRequest request) {
        if (request.gui == OpenGuiRequest.Type.SELECTOR) {
            MinecraftClient.getInstance().setScreen(new ImmersiveMelodiesScreen());
        }
    }

    @Override
    public void handleMelodyListMessage(MelodyListMessage response) {
        ClientMelodyManager.getMelodiesList().clear();
        ClientMelodyManager.getMelodiesList().putAll(response.getMelodies());

        ClientMelodyManager.cleanupMelodies();

        if (MinecraftClient.getInstance().currentScreen instanceof ImmersiveMelodiesScreen screen) {
            screen.refreshPage();
        }
    }
}
