package immersive_melodies.fabric;

import immersive_melodies.Client;
import immersive_melodies.MidiListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public final class ClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(event -> Client.postLoad());
        ClientLifecycleEvents.CLIENT_STOPPING.register(event -> MidiListener.stop());
    }
}
